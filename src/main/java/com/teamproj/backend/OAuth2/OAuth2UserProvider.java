package com.teamproj.backend.OAuth2;

import com.teamproj.backend.model.User;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class OAuth2UserProvider implements OAuth2User {
    private static final String NAME_ATTRIBUTE_KEY = "name";
    public static final String SOCIAL_PROVIDER_KEY = "socialProviderKey";

    protected Long id;
    protected String name;
    protected String email;
    protected String socialProviderKey;
    protected String profileImg;
    protected String OAuthProvider;
    protected Collection<? extends GrantedAuthority> authorities = new ArrayList<>();
    protected Map<String, Object> attributes = new HashMap<>();

    public OAuth2UserProvider(User user) {
        this.id = user.getId();
        this.name = user.getNickname();
        this.email = user.getUsername();
        this.profileImg = user.getProfileImage();
    }

    public User toUser() {
        return User.builder()
                .id(id)
                .nickname(name)
                .username(email)
                .profileImage(profileImg)
                .build();
    }


    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public <A> A getAttribute(String name) {
        return (A) attributes.get(name);
    }
}