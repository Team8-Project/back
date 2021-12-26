package com.teamproj.backend.util;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtAuthenticateProcessor {
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    public UserDetailsImpl forceLogin(String token){
        if(token.equals("")){
            return null;
        }
        token = token.split("BEARER ")[1];

        HashMap<String, String> userInfo = jwtDecoder.decodeUser(token);
        UserDetailsImpl userDetails = UserDetailsImpl.initUserDetails(userInfo);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return userDetails;
    }

    public User getUser(UserDetailsImpl userDetails){
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        if(!user.isPresent()){
            throw new NullPointerException(ExceptionMessages.NOT_EXIST_USER);
        }
        return user.get();
    }
}
