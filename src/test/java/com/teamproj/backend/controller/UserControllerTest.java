//package com.teamproj.backend.controller;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//public class UserControllerTest {
//    @Autowired
//    UserController userController;
//
//    private MockMvc mock;
//
//    @BeforeEach
//
//    public void setup() {
//        mock = MockMvcBuilders.standaloneSetup(userController).build();
//    }
//
//    private String username;
//    private String nickname;
//    private String password;
//    private String passwordCheck;
//
//    @Test
//    public void welcomeViewTest() throws Exception {
//        mock.perform(get("/api/signup")
//                        .param("username", "tester12345")
//                        .param("nickname", "tester12345")
//                        .param("password", "a1234567")
//                        .param("passwordCheck", "a1234567"))
//                .andExpect(status().isOk());
//    }
//}
