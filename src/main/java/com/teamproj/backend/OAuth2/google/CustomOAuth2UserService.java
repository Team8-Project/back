package com.teamproj.backend.OAuth2.google;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;

    // 구글로 부터 받은 userRequest 데이터에 대한 후처리되는 함수
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2UserService<OAuth2UserRequest, OAuth2User> defaultOAuth2UserService = new DefaultOAuth2UserService();
        String OAuthProvider = userRequest.getClientRegistration().getRegistrationId();
        OAuth2User defaultOAuth2User = defaultOAuth2UserService.loadUser(userRequest);
        OAuth2UserProvider refactoredOAuth2User = OAuth2UserProviderFactory
                .of(OAuthProvider, defaultOAuth2User.getAttributes());

        saveOrUpdate(refactoredOAuth2User.email);

        return refactoredOAuth2User;
    }

    private void saveOrUpdate(String userEmail) {
//        User user = userRepository.findBySocialProviderKey(OAuth2UserProvider.getSocialProviderKey())
//                .map(savedUser -> savedUser.update(OAuth2UserProvider.getName(), OAuth2UserProvider.getEmail(), OAuth2UserProvider.getProfileImg()))
//                .orElse(OAuth2UserProvider.toUser());

        // To Do: findByGoogleId 로 변경 되어야 함
        User user = userRepository.findByUsername("dlawjsgurk@gmail.com")
                .orElseThrow(
                        () -> new NullPointerException("구글 계정이 없습니다.")
                );



//        userRepository.save(user);
    }
}
