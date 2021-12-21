package com.teamproj.backend.model;

import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.util.Timestamped;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class User extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false)
    private String password;

    public User(SignUpRequestDto signUpRequestDto, String encodedPassword) {
        this.username = signUpRequestDto.getUsername();
        this.nickname = signUpRequestDto.getNickname();
        this.password = encodedPassword;
    }
}
