package com.teamproj.backend.service;

import com.teamproj.backend.Repository.CommentRepository;
import com.teamproj.backend.Repository.RecentSearchRepository;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardHashTagRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.Repository.quiz.QuizRepository;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.board.BoardCategory;
import com.teamproj.backend.model.board.BoardHashTag;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.JwtAuthenticateProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_CATEGORY;

@Service
@RequiredArgsConstructor
public class StressTestService {
    // board
    private final BoardRepository boardRepository;
    private final BoardCategoryRepository boardCategoryRepository;
    private final BoardHashTagRepository boardHashTagRepository;

    private final BoardService boardService;

    // comment
    private final CommentRepository commentRepository;

    // common
    private final RecentSearchRepository recentSearchRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    // dict
    private final DictRepository dictRepository;

    // Util
    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;

    public String boardWrite10000(UserDetailsImpl userDetails) {
        boardHashTagRepository.deleteAll();
        boardRepository.deleteAll();
        BoardCategory boardCategory = boardCategoryRepository.findById("FREEBOARD").orElse(null);

        StringBuilder str = new StringBuilder();

        for(int i = 0; i < 100; i++){
            str.append("테스트에요");
        }

        List<Board> boardList = new ArrayList<>();

        String hashTag = "test";

        for(int i = 0; i < 10000; i++){
            boardList.add(Board.builder()
                    .title("테스트입니다" + i)
                    .content(str.toString())
                    .boardCategory(boardCategory)
                    .user(jwtAuthenticateProcessor.getUser(userDetails))
                    .thumbNail("")
                    .enabled(true)
                    .build());
        }

        List<Board> resultList = boardRepository.saveAll(boardList);

        List<BoardHashTag> hashTagList = new ArrayList<>();
        for(Board b : resultList){
            hashTagList.add(BoardHashTag.builder()
                    .hashTagName(hashTag)
                    .board(b)
                    .build());
        }
        boardHashTagRepository.saveAll(hashTagList);

        return "success write to " + resultList.size() + "records";
    }

    public String dictWrite500(UserDetailsImpl userDetails) {
        User user = jwtAuthenticateProcessor.getUser(userDetails);

        StringBuilder str = new StringBuilder();

        for(int i = 0; i < 20; i++){
            str.append("테스트에요");
        }

        List<Dict> dictList = new ArrayList<>();
        for(int i = 0; i < 500; i++){
            dictList.add(Dict.builder()
                    .firstAuthor(user)
                    .recentModifier(user)
                    .content(str.toString())
                    .dictName("테스트....입니다" + i)
                    .summary("테스트용입니다이에요테스트용입니다이에요")
                    .build());
        }

        List<Dict> resultList = dictRepository.saveAll(dictList);

        return "success write to " + resultList.size() + " records";
    }

    private BoardCategory getSafeBoardCategory(String categoryName) {
        return boardCategoryRepository.findById(categoryName.toUpperCase())
                .orElseThrow(()->new NullPointerException(NOT_EXIST_CATEGORY));
    }
}
