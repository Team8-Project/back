package com.teamproj.backend.OAuth2;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;
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

        saveOrUpdate(refactoredOAuth2User);

        return refactoredOAuth2User;
    }

    private void saveOrUpdate(OAuth2UserProvider refactoredOAuth2User) {
        String email = refactoredOAuth2User.email;

        // To Do: findByGoogleId 로 변경 되어야 함
        Optional<User> findUser = userRepository.findByUsername(email);
        if(!findUser.isPresent()) {
            String nickname = refactoredOAuth2User.name;
            String profileImg = refactoredOAuth2User.profileImg;
            String password = UUID.randomUUID() + refactoredOAuth2User.socialProviderKey;

            User user = User.builder()
                    .username(email)
                    .profileImage(profileImg)
                    .nickname(nickname)
                    .password(password)
                    .build();

            userRepository.save(user);
        }
    }
}