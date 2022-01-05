package com.teamproj.backend.OAuth2.google;

import com.teamproj.backend.util.Social;

import java.util.Map;

import static org.springframework.security.config.oauth2.client.CommonOAuth2Provider.GOOGLE;

public class OAuth2UserProviderFactory {

    public static OAuth2UserProvider of (String OAuthProvider, Map<String, Object> attributes ) {
        switch ( Social.valueOf(OAuthProvider.toUpperCase()) ) {
//            case NAVER:
//                return new NaverOAuth2UserProvider(OAuthProvider, attributes);
//            case KAKAO:
//                return new KakaoOAuth2UserProvider(OAuthProvider, attributes);
            case GOOGLE:
                return new GoogleOAuth2UserProvider(OAuthProvider, attributes);
            default:
                throw new IllegalArgumentException("수정 예정");
        }
    }
}