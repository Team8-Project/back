package com.teamproj.backend.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;  // S3 버킷 이름

    // 서비스단에서 넘어온 MultipartFile을 convert 메서드를 사용해 File 형식으로 변환
    // - 변환 후에 private upload 메서드 실행
    public String upload(MultipartFile multipartFile, String dirName) throws IOException {
        // 1. convert 메서드를 통해 multipartFile을 File 객체로 변환 후 로컬에 저장
        File uploadFile = convert(multipartFile)  // 파일 변환할 수 없으면 에러
                .orElseThrow(() -> new IllegalArgumentException("error: MultipartFile -> File convert fail"));
        // 2. private upload 함수에 File 형식 데이터와 디렉토리네임 전송
        return upload(uploadFile, dirName);
    }
    
    // 업로드된 S3 파일 삭제
    public void deleteFromS3(String source) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, source));
    }

    // S3로 파일 업로드하기
    private String upload(File uploadFile, String dirName) {
        // 1. S3에 저장될 파일 이름 [파일이름 = 디렉토리 네임 + 랜덤한 고유 식별자 + 업로드파일 이름]
        String fileName = dirName + "/" + UUID.randomUUID() + uploadFile.getName();
        // 2. putS3 함수를 통해 S3에 업로드 후 업로드된 이미지URL 가져오기
        String uploadImageUrl = putS3(uploadFile, fileName);
        // 3. 로컬에 있는 이미지 파일 삭제
        removeNewFile(uploadFile);
        // 4. S3에 업로드된 이미지 URL 리턴
        return uploadImageUrl;
    }

    // S3로 업로드
    private String putS3(File uploadFile, String fileName) {
        // 1. S3에 파일 업로드 진행(버킷, 파일이름, 업로드파일)
        amazonS3Client.putObject(new PutObjectRequest(bucket, fileName, uploadFile).withCannedAcl(CannedAccessControlList.PublicRead));
        // 2. 아마존S3로부터 업로드된 파일 URL String 형식으로 변환 후 리턴
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }

    // 로컬에 저장된 이미지 지우기
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("File delete success");
            return;
        }
        log.info("File delete fail");
    }

    // 로컬에 파일 업로드 하기
    private Optional<File> convert(MultipartFile file) throws IOException {
        // 1. 프로젝트 경로로 설정된 File 객체 생성
        File convertFile = new File(System.getProperty("user.dir") + "/" + file.getOriginalFilename());
        // 2. 바로 위에서 지정한 경로에 File이 생성됨 (경로가 잘못되었다면 생성 불가능)
        if (convertFile.createNewFile()) {
            // 3. FileOutputStream 데이터를 파일에 바이트 스트림으로 저장
            try (FileOutputStream fos = new FileOutputStream(convertFile)) {
                fos.write(file.getBytes());
            }
            // 4. 로컬에 제대로 업로드 되었다면 convertFile 리턴
            return Optional.of(convertFile);
        }
        return Optional.empty();
    }
}
