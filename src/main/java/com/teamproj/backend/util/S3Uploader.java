package com.teamproj.backend.util;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    public String bucket;  // S3 버킷 이름
    
    // 이미지 업로드
    public String upload(MultipartFile file, String dirName) {
        String fileName = createFileName(file.getOriginalFilename(), dirName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            s3UploadImg(inputStream, objectMetadata, fileName);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "파일 변환 중 에러가 발생하였습니다. (%s)", file.getOriginalFilename()
                    )
            );
        }
        return getFileUrl(fileName);
    }


    // S3에 업로드된 이미지 파일 URL 가져오기
    private String getFileUrl(String fileName) {
        return amazonS3Client.getUrl(bucket, fileName).toString();
    }


    // S3에 업로드될 이미지 파일이름 생성
    private String createFileName(String originalFilename, String dirName) {
        return dirName + "/" + UUID.randomUUID() + originalFilename;
    }


    // S3에 이미지 업로드
    private void s3UploadImg(InputStream inputStream, ObjectMetadata objectMetadata, String fileName) {
        amazonS3Client.putObject(
                new PutObjectRequest(bucket, fileName, inputStream, objectMetadata)
                                    .withCannedAcl(CannedAccessControlList.PublicRead)
        );
    }


    // 업로드된 S3 파일 삭제
    public void deleteFromS3(String source) {
        amazonS3Client.deleteObject(new DeleteObjectRequest(bucket, source));
    }
}
