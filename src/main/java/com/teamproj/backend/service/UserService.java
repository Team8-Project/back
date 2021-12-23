package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.dto.user.signUp.SignUpCheckResponseDto;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.dto.user.signUp.SignUpResponseDto;
import com.teamproj.backend.dto.user.userInfo.UserInfoResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto) {
        // 아이디는 모두 소문자로 저장.
        signUpRequestDto.setUsername(signUpRequestDto.getUsername().toLowerCase());
        signUpValidCheck(signUpRequestDto);
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());

        userRepository.save(new User(signUpRequestDto, encodedPassword));

        return SignUpResponseDto.builder()
                .username(signUpRequestDto.getUsername())
                .nickname(signUpRequestDto.getNickname())
                .build();
    }

    private void signUpValidCheck(SignUpRequestDto signUpRequestDto) {
        String username = signUpRequestDto.getUsername();
        String nickname = signUpRequestDto.getNickname();
        String password = signUpRequestDto.getPassword();

        /*
            아이디 : 3자 이상, 16자 이하, 대소문자, 숫자
            닉네임 : 2자 이상, 16자 이하, 대소문자 숫자 한글
            비밀번호 : 6자 이상 16자 이하, 대소문자 숫자 조합
         */
        // username
        if (username.length() < 3 || username.length() > 16) {
            throw new IllegalArgumentException("아이디는 3자 이상 16자 이하만 입력 가능합니다.");
        } else if (!username.matches("^(?=.*[a-z0-9])[a-z0-9]{3,16}$")) {
            throw new IllegalArgumentException("아이디는 영문자 및 숫자만 입력 가능합니다.");
        }
        // nickname
        if (nickname.length() < 2 || nickname.length() > 16) {
            throw new IllegalArgumentException("닉네임은 2자 이상 16자 이하만 입력 가능합니다.");
        } else if (!nickname.matches("^(?=.*[a-z0-9가-힣])[a-z0-9가-힣]{2,16}$")) {
            throw new IllegalArgumentException("닉네임은 영어 대소문자 및 숫자, 한글만 입력 가능합니다.");
        }
        // password
        if (password.length() < 6 || password.length() > 16) {
            throw new IllegalArgumentException("비밀번호는 6자 이상 16자 이하만 입력 가능합니다.");
        } else if (!password.matches("^(?=.*[0-9])(?=.*[a-zA-Z])[a-zA-Z0-9!@#$%^&*()._-]{6,16}$")) {
            throw new IllegalArgumentException("비밀번호는 영문자 및 숫자의 조합으로 입력해야 합니다.");
        }

        // 중복검사
        Optional<User> found = userRepository.findByUsername(username);
        if (found.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 ID 입니다.");
        }
        found = userRepository.findByNickname(signUpRequestDto.getNickname());
        if (found.isPresent()) {
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        if (!signUpRequestDto.getPassword().equals(signUpRequestDto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
    }

    public UserInfoResponseDto getUserInfo(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            throw new NullPointerException("로그인하지 않은 사용자입니다.");
        }

        return UserInfoResponseDto.builder()
                .username(userDetails.getUsername())
                .nickname(userDetails.getUser().getNickname())
                .build();
    }

    public SignUpCheckResponseDto usernameValidCheck(String username) {
        Optional<User> found = userRepository.findByUsername(username);
        System.out.println("아이디 중복검사 결과 : " + !found.isPresent());
        return SignUpCheckResponseDto.builder()
                .result(!found.isPresent())
                .build();
    }

    public SignUpCheckResponseDto nicknameValidCheck(String nickname) {
        Optional<User> found = userRepository.findByNickname(nickname);
        System.out.println("닉네임 중복검사 결과 : " + !found.isPresent());
        return SignUpCheckResponseDto.builder()
                .result(!found.isPresent())
                .build();
    }
}
