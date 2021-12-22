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
    
    public SignUpResponseDto signUp(SignUpRequestDto signUpRequestDto){
        signUpValidCheck(signUpRequestDto);
        String encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());

        userRepository.save(new User(signUpRequestDto, encodedPassword));

        return SignUpResponseDto.builder()
                .username(signUpRequestDto.getUsername())
                .nickname(signUpRequestDto.getNickname())
                .build();
    }

    private void signUpValidCheck(SignUpRequestDto signUpRequestDto) {
        Optional<User> found = userRepository.findByUsername(signUpRequestDto.getUsername());
        if(found.isPresent()){
            throw new IllegalArgumentException("이미 존재하는 ID 입니다.");
        }
        found = userRepository.findByNickname(signUpRequestDto.getNickname());
        if(found.isPresent()){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        if(!signUpRequestDto.getPassword().equals(signUpRequestDto.getPasswordCheck())){
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
    }

    public UserInfoResponseDto getUserInfo(UserDetailsImpl userDetails) {
        if(userDetails == null){
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
