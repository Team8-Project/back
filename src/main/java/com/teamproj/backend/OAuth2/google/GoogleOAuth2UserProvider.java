package com.teamproj.backend.OAuth2.google;

import com.teamproj.backend.OAuth2.OAuth2UserProvider;

import java.util.Map;

public class GoogleOAuth2UserProvider extends OAuth2UserProvider {

    public static final String ATTRIBUTES_NAME = "name";
    public static final String ATTRIBUTES_EMAIL = "email";
    public static final String ATTRIBUTES_KEY = "sub";
    public static final String ATTRIBUTES_PICTURE = "picture";

    public GoogleOAuth2UserProvider(String OAuthProvider, Map<String, Object> providedAttributes) {
        try{
            super.attributes = providedAttributes;

//            if (!attributes.keySet().containsAll(List.of(ATTRIBUTES_NAME, ATTRIBUTES_EMAIL, ATTRIBUTES_KEY, ATTRIBUTES_PICTURE))) {
//                throw new IllegalArgumentException("수정 예정");
//            }

            super.name = String.valueOf(attributes.get(ATTRIBUTES_NAME));
            super.email = String.valueOf(attributes.get(ATTRIBUTES_EMAIL));
            super.socialProviderKey = String.valueOf(attributes.get(ATTRIBUTES_KEY));
            super.profileImg = String.valueOf(attributes.get(ATTRIBUTES_PICTURE));
            super.OAuthProvider = OAuthProvider;
        }
        catch (NullPointerException nullPointerException){
            throw new NullPointerException("수정 예정");
        }
    }
}