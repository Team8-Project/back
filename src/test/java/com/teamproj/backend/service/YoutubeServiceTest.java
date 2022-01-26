package com.teamproj.backend.service;

import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictYoutubeUrl;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
@Transactional
@Rollback
public class YoutubeServiceTest {
    /*
        해당 테스트 구간은 Youtube Data Api v3 할당량 문제로 인해
        마구 사용하면 문제가 없어도 테스트 Fail이 발생하기 때문에
        의미 없이 작동시키지 마세요!
     */
    @Autowired
    YoutubeService youtubeService;

    @Nested
    @DisplayName("성공")
    class Success {
        @Test
        @DisplayName("2자 이상의 검색")
        void successLongerEqualThan2Word() {
            // given
            Dict dict = Dict.builder()
                    .dictName("절레절레전래동화")
                    .build();
            String query = dict.getDictName();

            // when
            List<DictYoutubeUrl> dictYoutubeUrlList = youtubeService.getYoutubeSearchResult(dict, query);

            // then
            for (DictYoutubeUrl result : dictYoutubeUrlList) {
                assertEquals(dict.getDictName(), result.getDict().getDictName());
            }
        }

        @Test
        @DisplayName("2자 미만의 검색")
        void successLesserThan2Word() {
            // given
            Dict dict = Dict.builder()
                    .dictName("i")
                    .build();
            String query = dict.getDictName();

            // when
            List<DictYoutubeUrl> dictYoutubeUrlList = youtubeService.getYoutubeSearchResult(dict, query);

            // then
            assertEquals(0, dictYoutubeUrlList.size());
        }
    }
}
