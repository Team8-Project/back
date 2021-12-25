package com.teamproj.backend.security.provider;

import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtDecoder;
import com.teamproj.backend.security.jwt.JwtPreProcessingToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
@RequiredArgsConstructor
public class JWTAuthProvider implements AuthenticationProvider {
    private final JwtDecoder jwtDecoder;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        String token = (String) authentication.getPrincipal();
        HashMap<String, String> userInfo = jwtDecoder.decodeUser(token);
        UserDetailsImpl userDetails = UserDetailsImpl.initUserDetails(userInfo);

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return JwtPreProcessingToken.class.isAssignableFrom(authentication);
    }
}
