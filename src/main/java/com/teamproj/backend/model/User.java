package com.teamproj.backend.model;

import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.util.Timestamped;
import lombok.Builder;
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

    @Column(unique = true)
    private Long kakaoId;

    public void setKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public User(SignUpRequestDto signUpRequestDto, String encodedPassword) {
        this.username = signUpRequestDto.getUsername();
        this.nickname = signUpRequestDto.getNickname();
        this.password = encodedPassword;
    }


//    @Builder
//    public User(String username, String password, String nickname){
//        this.username = username;
//        this.password = password;
//        this.nickname = nickname;
//        this.kakaoId = null;
//    }

    @Builder
    public User(String username, String password, String nickname, Long kakaoId) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.kakaoId = kakaoId;
    }
}
