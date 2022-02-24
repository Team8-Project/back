package com.teamproj.backend.service.admin;

import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    public void banUser(UserDetailsImpl userDetails){
    }
    public void deleteDict(){

    }
    public void deletePost(){

    }

    private void adminCheck(UserDetailsImpl userDetails){
        // 1. 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);
        // 2. 계정 권한 확인

    }
}
