package com.teamproj.backend.controller;

import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.InitService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InitController {
    private final InitService initService;

    @GetMapping("/init/quiz")
    public String initQuiz(){
        initService.initQuiz();
        return "OK!";
    }

    @GetMapping("/init/dict")
    public String initDict(@AuthenticationPrincipal UserDetailsImpl userDetails){
        initService.initDict(userDetails);
        return "OK!";
    }
}
