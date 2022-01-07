package com.teamproj.backend.controller;


import com.teamproj.backend.dto.mypage.MyPageResponseDto;
import com.teamproj.backend.dto.ResponseDto;
import com.teamproj.backend.dto.mypage.MyPageProfileImageModifyResponseDto;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class MyPageController {
        private final MyPageService myPageService;

    @GetMapping("/api/mypage")
    public ResponseDto<MyPageResponseDto> myPage(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseDto.<MyPageResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .message("마이페이지 정보 요청")
                .data(myPageService.myPage(userDetails))
                .build();
    }


    @PostMapping("/api/user/profileImage")
    public ResponseDto<MyPageProfileImageModifyResponseDto> profileImageModify(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                               @RequestPart(value = "images") MultipartFile file) throws IOException {
        return ResponseDto.<MyPageProfileImageModifyResponseDto>builder()
                .status(HttpStatus.OK.toString())
                .data(myPageService.profileImageModify(userDetails, file))
                .build();
    }
}
