package com.teamproj.backend.util;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtDecoder;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ManuallyJwtLoginProcessor {
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;

    public UserDetailsImpl forceLogin(String token){
        if(token.equals("")){
            return null;
        }
        token = token.split("BEARER ")[1];
        String username = jwtDecoder.decodeUsername(token);
        Optional<User> user = userRepository.findByUsername(username);
        if(!user.isPresent()){
            throw new NullPointerException("유효하지 않은 사용자입니다.");
        }
        UserDetailsImpl userDetails = new UserDetailsImpl(user.get());
        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return userDetails;
    }
}
