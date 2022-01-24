package com.teamproj.backend.service.test;

import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.DictHistory;
import com.teamproj.backend.model.dict.DictYoutubeUrl;
import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestService {
    private final DictQuestionRepository dictQuestionRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardRepository boardRepository;
    private final DictRepository dictRepository;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    public void del(UserDetailsImpl userDetails){
        boardRepository.deleteAll();
        dictQuestionRepository.deleteAll();
    }
    @Transactional
    public void testDataInput(UserDetailsImpl userDetails) {
        User user = jwtAuthenticateProcessor.getUser(userDetails);


        List<DictQuestion> dictQuestionList = new ArrayList<>();
        List<Board> boardList = new ArrayList<>();

        BoardCategory boardCategory = BoardCategory.builder()
                .categoryName("TEST")
                .build();

        boardCategory = boardCategoryRepository.save(boardCategory);


        for (int i = 0; i < 10000; i++) {
            StringBuilder sb = genSb();
            dictQuestionList.add(DictQuestion.builder()
                    .user(user)
                    .questionName("question" + i)
                    .content(sb.toString())
                    .thumbNail("")
                    .enabled(true)
                    .build());

            boardList.add(Board.builder()
                    .title("board" + i)
                    .boardCategory(boardCategory)
                    .content(sb.toString())
                    .user(user)
                    .thumbNail("")
                    .enabled(true)
                    .build());
        }

        dictQuestionRepository.saveAll(dictQuestionList);
        boardRepository.saveAll(boardList);
    }

    StringBuilder genSb(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            sb.append(UUID.randomUUID());
            if(sb.length() > 500){
                break;
            }
        }

        return sb;
    }
    @Transactional
    public void testDataInput2(UserDetailsImpl userDetails) {
        User user = jwtAuthenticateProcessor.getUser(userDetails);

        List<Dict> dictList = new ArrayList<>();
        List<DictHistory> dictHistoryList = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            StringBuilder sb = genSb();

            Dict dict = Dict.builder()
                    .firstAuthor(user)
                    .recentModifier(user)
                    .content(sb.toString())
                    .dictName("dict" + i)
                    .summary(UUID.randomUUID().toString().substring(0, 10))
                    .build();

            dict.addHistory(DictHistory.builder()
                    .prevSummary(dict.getSummary())
                    .prevContent(dict.getContent())
                    .user(user)
                    .dict(dict)
                    .build());


            dictList.add(dict);
        }

        // 사전 저장
        dictRepository.saveAll(dictList);
    }
}
