package com.teamproj.backend.OAuth2.google;

import com.teamproj.backend.model.User;
import lombok.Getter;

import java.io.Serializable;

@Getter
public class SessionUser implements Serializable {
    private String name;
    private String email;
    private String picture;

    public SessionUser(User user) {
        this.name = user.getNickname();
        this.email = user.getUsername();
        this.picture = user.getProfileImage();
    }
}