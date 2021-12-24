package com.teamproj.backend.util;

import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.security.UserDetailsImpl;

public class ValidChecker {
    public static void loginCheck(UserDetailsImpl userDetails){
        if (userDetails == null) {
            throw new NullPointerException(ExceptionMessages.NOT_LOGIN_USER);
        }
    }
}
