package com.teamproj.backend.service.stat;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.dict.DictLikeRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.rank.RankDictAllTimeResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictLike;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.security.jwt.JwtTokenUtils;
import com.teamproj.backend.service.RankService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class RankServiceTest {
    @Autowired
    RankService rankService;

    @Autowired
    DictRepository dictRepository;
    @Autowired
    DictLikeRepository dictLikeRepository;
    @Autowired
    UserRepository userRepository;

    User user;
    UserDetailsImpl userDetails;
    List<User> dummyUserList;
    List<Dict> dummyList;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("testuser12345")
                .nickname("1135tester")
                .password("a1234567")
                .build();

        user = userRepository.save(user);
        userDetails = UserDetailsImpl.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .build();

        dummyList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            dummyList.add(Dict.builder()
                    .firstAuthor(user)
                    .recentModifier(user)
                    .dictName("test" + i)
                    .summary("test")
                    .content("test")
                    .build());
        }

        dummyList = dictRepository.saveAll(dummyList);

        // test0 : 3등, test1 : 2등, test2 : 1등
        dummyUserList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            dummyUserList.add(User.builder()
                    .username("dummy" + i)
                    .nickname("dummy" + i)
                    .password("a1234567")
                    .build());
        }
        dummyUserList = userRepository.saveAll(dummyUserList);
        for (int i = 0; i < 3; i++) {
            for (int k = 0; k <= i; k++) {
                dictLikeRepository.save(DictLike.builder()
                        .dict(dummyList.get(i))
                        .user(dummyUserList.get(k))
                        .build());
            }
        }
    }

    @Nested
    @DisplayName("사전 전체 랭킹")
    class DictRank {
        @Nested
        @DisplayName("회원의 조회")
        class TestUser {
            @Test
            @DisplayName("좋아요 이력이 있는 사용자의 조회")
            void userHaveLike() {
                // given
                userDetails = UserDetailsImpl.builder()
                        .username(dummyUserList.get(0).getUsername())
                        .password(dummyUserList.get(0).getPassword())
                        .build();
                String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

                // when
                List<RankDictAllTimeResponseDto> result = rankService.getAllTimeDictRank(token);

                // then
                assertTrue(result.size() > 0);
            }

            @Test
            @DisplayName("좋아요 이력이 없는 사용자의 조회")
            void userNotHaveLike() {
                // given
                String token = "BEARER " + JwtTokenUtils.generateJwtToken(userDetails);

                // when
                List<RankDictAllTimeResponseDto> result = rankService.getAllTimeDictRank(token);

                // then
                assertTrue(result.size() > 0);
            }
        }



        @Test
        @DisplayName("비회원의 조회")
        void notUser() {
            // given
            String token = "";

            // when
            List<RankDictAllTimeResponseDto> result = rankService.getAllTimeDictRank(token);

            // then
            assertTrue(result.size() > 0);
        }
    }
}
