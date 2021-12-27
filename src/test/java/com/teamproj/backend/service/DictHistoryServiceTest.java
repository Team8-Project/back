package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.dto.dict.DictPostRequestDto;
import com.teamproj.backend.dto.dict.DictPutRequestDto;
import com.teamproj.backend.dto.dict.DictResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryDetailResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryRecentResponseDto;
import com.teamproj.backend.dto.dictHistory.DictHistoryResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
class DictHistoryServiceTest {
    @Autowired
    private JwtAuthenticateProcessor jwtAuthenticateProcessor;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private DictService dictService;
    @Autowired
    private DictHistoryService dictHistoryService;

    UserDetailsImpl userDetails;

    String dictName;
    String content;
    User user;

    DictPostRequestDto dictPostRequestDto;
    Long dictId;

    @BeforeEach
    void setup() {
        dictName = "타이틀";
        content = "내용";

        user = User.builder()
                .username("유저네임")
                .nickname("닉네임")
                .password("Q1234567")
                .build();

        userRepository.save(user);
        userDetails = UserDetailsImpl.builder()
                .username("유저네임")
                .password("Q1234567")
                .build();

        // 게시글 작성
        dictPostRequestDto = DictPostRequestDto.builder()
                .title(UUID.randomUUID().toString())
                .content(UUID.randomUUID().toString())
                .build();
        dictService.postDict(userDetails, dictPostRequestDto);

        // 게시글 수정
        List<DictResponseDto> response = dictService.getDictList(0, 5, "");
        for (DictResponseDto dictResponseDto : response) {
            if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                dictId = dictResponseDto.getDictId();
                break;
            }
        }

        DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                .content("변경")
                .build();
        dictService.putDict(userDetails, dictId, dictPutRequestDto);
    }

    @Nested
    @DisplayName("용어사전 수정 내역")
    class getDictHistory {
        @Test
        @DisplayName("성공")
        void getDictHistory_success_login() {
            // given

            // when
            DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);

            // then
            assertEquals(dictId, dictHistoryResponseDto.getDictId());
            assertEquals(dictPostRequestDto.getTitle(), dictHistoryResponseDto.getTitle());
            assertEquals(jwtAuthenticateProcessor.getUser(userDetails).getNickname(), dictHistoryResponseDto.getFirstWriter());
            assertEquals(1, dictHistoryResponseDto.getHistory().size());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 사전의 수정 내역 열람 시도")
        void getDictHistory_fail_non_exist_dict() {
            // given

            // when
            Exception exception = assertThrows(NullPointerException.class,
                    () -> dictHistoryService.getDictHistory(0L)
            );

            // then
            assertEquals(NOT_EXIST_DICT, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("용어사전 수정 내역 상세")
    class getDictHistoryDetail {
        @Test
        @DisplayName("성공")
        void getDictHistoryDetail_success_login() {
            // given
            String content = UUID.randomUUID().toString();
            dictPostRequestDto = DictPostRequestDto.builder()
                    .title(UUID.randomUUID().toString())
                    .content(content)
                    .build();
            dictService.postDict(userDetails, dictPostRequestDto);

            List<DictResponseDto> response = dictService.getDictList(0, 5, "");

            // id 찾기
            Long dictId = null;
            for (DictResponseDto dictResponseDto : response) {
                if (dictResponseDto.getTitle().equals(dictPostRequestDto.getTitle())) {
                    dictId = dictResponseDto.getDictId();
                    break;
                }
            }

            DictPutRequestDto dictPutRequestDto = DictPutRequestDto.builder()
                    .content("변경")
                    .build();

            dictService.putDict(userDetails, dictId, dictPutRequestDto);

            DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);
            DictHistoryRecentResponseDto dictHistoryRecentResponseDto = dictHistoryResponseDto.getHistory().get(0);

            // when
            DictHistoryDetailResponseDto dictHistoryDetailResponseDto = dictHistoryService.getDictHistoryDetail(dictHistoryRecentResponseDto.getHistoryId());

            // then
            assertEquals(dictPostRequestDto.getTitle(), dictHistoryDetailResponseDto.getTitle());
            assertEquals(jwtAuthenticateProcessor.getUser(userDetails).getNickname(), dictHistoryDetailResponseDto.getModifier());
            assertEquals(content, dictHistoryDetailResponseDto.getContent());
        }

        @Test
        @DisplayName("실패 - 존재하지 않는 수정 내역 열람 시도")
        void getDictHistory_fail_non_exist_dict() {
            // given

            // when
            Exception exception = assertThrows(NullPointerException.class,
                    () -> dictHistoryService.getDictHistoryDetail(0L)
            );

            // then
            assertEquals(NOT_EXIST_DICT_HISTORY, exception.getMessage());
        }
    }

    @Nested
    @DisplayName("용어사전 롤백")
    class RevertHistory {
        @Test
        @DisplayName("성공")
        void revertHistory_success() {
            // given
            DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);
            Long historyId = dictHistoryResponseDto.getHistory().get(0).getHistoryId();

            // when
            dictHistoryService.revertDict(historyId, userDetails);
            dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);

            // then
            assertEquals(2, dictHistoryResponseDto.getHistory().size());
        }

        @Nested
        @DisplayName("실패")
        class RevertHistory_fail {
            @Test
            @DisplayName("비회원의 롤백 시도")
            void revertHistory_fail_non_login_user() {
                // given
                DictHistoryResponseDto dictHistoryResponseDto = dictHistoryService.getDictHistory(dictId);
                Long historyId = dictHistoryResponseDto.getHistory().get(0).getHistoryId();

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictHistoryService.revertDict(historyId, null)
                );

                // then
                assertEquals(NOT_LOGIN_USER, exception.getMessage());
            }

            @Test
            @DisplayName("존재하지 않는 히스토리로 롤백 시도")
            void revertHistory_fail_non_exist_history() {
                // given

                // when
                Exception exception = assertThrows(NullPointerException.class,
                        () -> dictHistoryService.revertDict(0L, userDetails)
                );

                // then
                assertEquals(NOT_EXIST_DICT_HISTORY, exception.getMessage());
            }
        }
    }
}