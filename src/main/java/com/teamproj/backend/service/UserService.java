package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.dto.user.signUp.SignUpRequestDto;
import com.teamproj.backend.dto.user.signUp.SignUpResponseDto;
import com.teamproj.backend.model.User;
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
            throw new IllegalArgumentException("이미 존재하는 ID입니다.");
        }
        found = userRepository.findByNickname(signUpRequestDto.getNickname());
        if(found.isPresent()){
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다.");
        }
        if(!signUpRequestDto.getPassword().equals(signUpRequestDto.getPassword_check())){
            throw new IllegalArgumentException("비밀번호 확인이 일치하지 않습니다.");
        }
    }
}
