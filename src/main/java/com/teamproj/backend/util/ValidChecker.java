package com.teamproj.backend.util;

import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.security.UserDetailsImpl;

public class ValidChecker {
    public static void loginCheck(UserDetailsImpl userDetails){
        if (userDetails == null) {
            throw new NullPointerException(ExceptionMessages.NOT_LOGIN_USER);
        }
    }

    // 토큰이 유효하지 않을 경우 빈문자열로 반환.
    public static String tokenCheck(String token){
        if(token == null || !token.contains("BEARER ")){
            return "";
        }
        return token;
    }
}
