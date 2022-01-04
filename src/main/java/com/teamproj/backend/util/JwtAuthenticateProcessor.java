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

    // 비회원과 회원 모두 접속 가능한 기능에서, 회원만 사용 가능한 기능과 그 회원만 볼 수 있는 상태(좋아요 여부 등)를 구분하기 위해 생성한 함수.
    public UserDetailsImpl forceLogin(String token){
        /*
            token이 존재함 : Authorization 헤더에 값이 있음
            token이 존재하는데 그 정보가 유효하지 않음 : 비회원으로 처리.
         */
        if(token.equals("")){
            return null;
        }
        token = token.split("BEARER ")[1];

        /*
            굳이 try ~ catch문을 씌워준 이유!
            양식이 잘못된 토큰 또는 만료된 토큰을 들고 있더라도 일단 비회원으로써 볼 수 있는 기능은 전부 열람 가능하게 해야하기 때문!
            상기 문제를 가진 토큰이 jwtDecoder.decodeUser()의 파라미터로 들어갈 경우 IllegalArgumentException 발생함.
         */
        try{
            HashMap<String, String> userInfo = jwtDecoder.decodeUser(token);
            UserDetailsImpl userDetails = UserDetailsImpl.initUserDetails(userInfo);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return userDetails;
        }catch(Exception e){
            return null;
        }
    }

    // jwt 토큰 정보를 발급받을 때마다 무조건 사용자 정보를 받아야 하는 상황에서 벗어나기 위해 getUser 기능을 userDetailsImpl에서 외부로 빼낸 것.
    // 더 좋은 방법이 있을 수 있음. 아니 반드시 있음. 모르고 있을 뿐.
    // 현재는 이 구조로 프로그램이 많이 진행 된 상태지만, 다음 프로젝트에서도 같은 방식을 적용하지 않기 위해선 반드시 더 좋은 방법을 찾아봐야 함.
    public User getUser(UserDetailsImpl userDetails){
        Optional<User> user = userRepository.findByUsername(userDetails.getUsername());
        return user.orElseThrow(() -> new NullPointerException(ExceptionMessages.NOT_EXIST_USER));
    }
}
