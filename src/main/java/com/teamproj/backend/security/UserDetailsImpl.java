package com.teamproj.backend.security;

import com.teamproj.backend.security.jwt.JwtTokenUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@Builder
@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {
    private String username;
    private String password;

    public static UserDetailsImpl initUserDetails(HashMap<String, String> userInfo){
        return UserDetailsImpl.builder()
                .username(userInfo.get(JwtTokenUtils.CLAIM_USER_NAME))
                .password(userInfo.get(JwtTokenUtils.CLAIM_USER_PASSWORD))
                .build();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority simpleAuthority = new SimpleGrantedAuthority("ROLE_USER");
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(simpleAuthority);

        return authorities;
    }
}