package com.teamproj.backend.util;

import com.teamproj.backend.security.UserDetailsImpl;

public class ValidChecker {
    public static void loginCheck(UserDetailsImpl userDetails){
        if (userDetails == null) {
            throw new NullPointerException("로그인하지 않은 사용자입니다.");
        }
    }
}
