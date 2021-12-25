package com.teamproj.backend.model;

import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.dict.DictLike;
import com.teamproj.backend.util.Timestamped;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

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

    @Column
    private String profileImage;

    @Column(unique = true)
    private Long kakaoId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private final List<Board> boardList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private final List<DictLike> dictLikeList = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private final List<RecentSearch> recentSearchList = new ArrayList<>();

    public void setKakaoId(Long kakaoId) {
        this.kakaoId = kakaoId;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
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
