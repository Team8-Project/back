package com.teamproj.backend.service;

import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.dict.question.mypage.DictQuestionMyPageResponseDto;
import com.teamproj.backend.dto.mypage.MyPageDictResponseDto;
import com.teamproj.backend.dto.mypage.MyPagePostBoardResponseDto;
import com.teamproj.backend.dto.mypage.MyPageProfileImageModifyResponseDto;
import com.teamproj.backend.dto.mypage.MyPageResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.model.dict.question.DictQuestion;
import com.teamproj.backend.model.dict.question.QDictCuriousToo;
import com.teamproj.backend.model.dict.question.QQuestionSelect;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.MemegleServiceStaticMethods;
import com.teamproj.backend.util.S3Uploader;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_USER;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final DictRepository dictRepository;
    private final UserRepository userRepository;

    private final CommentService commentService;

    private final S3Uploader s3Uploader;
    private final JPAQueryFactory queryFactory;

    //region 마이페이지 정보 불러오기
    public MyPageResponseDto myPage(UserDetailsImpl userDetails) {
        // 1. 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 2. 로그인한 유저인지 체크 후 해당 유저 조회
        User user = getSafeUser(userDetails.getUsername());
        // 3. 유저가 작성한 게시글 조회(삭제 된 것 포함)
        List<Board> userBoard = user.getBoardList();
        // 4. 유저가 작성한 사전 조회
        List<Dict> userDict = dictRepository.findByFirstAuthor(user);
        // 5. 유저가 작성한 용어사전 질문 조회
        List<DictQuestion> dictQuestionList = user.getDictQuestionList();
        // 6. Dto에 담을 정보 변수 초기화 및 할당
        Long userId = user.getId();                         // 유저아이디
        String nickname = user.getNickname();               // 유저 닉네임
        String profileImageUrl = user.getProfileImage();    // 유저 프로필 이미지
        int dictCount = userDict.size();                    // 유저가 작성한 사전 갯수

        // 7. 로그인한 유저가 작성한 게시글 데이터를 MyPagePostBoardResponseDto에 저장
        List<MyPagePostBoardResponseDto> postBoards = getMyPagePostBoardResponseDtoList(userBoard);
        // 8. 로그인한 유저가 작성한 사전 데이터를 MyPageDictResponseDto에 저장
        List<MyPageDictResponseDto> userDictResponseList = getMyPageDictResponseDtoList(userDict, dictCount);
        
        // 나도 궁금해요 맵
        HashMap<String, Boolean> curiousTooMap = getCuriousTooMap(dictQuestionList);
        // 채택 여부 맵
        HashMap<Long, Long> completeMap = getIsComplete(dictQuestionList);
        // 로그인한 유저가 작성한 사전질문 데이터를 dictQuestionMyPageResponseDto에 저장
        List<DictQuestionMyPageResponseDto> dictQuestionMyPageResponseDtoArrayList = getDictQuestionMyPageResponseDtoList(user, dictQuestionList, curiousTooMap, completeMap);


        // 10. 마이페이지 정보 Response
        return MyPageResponseDto.builder()
                .userId(userId)                     // 유저아이디
                .nickname(nickname)                 // 유저닉네임
                .profileImageUrl(profileImageUrl)   // 유저 프로파일 이미지URL
                .postCount(postBoards.size())               // 유저가 작성한 게시글 갯수
                .dictCount(dictCount)               // 유저가 작성한 사전 정보
                .postBoards(postBoards)             // 유저가 작성한 게시글 정보
                .dict(userDictResponseList)         // 유저가 작성한 사전 정보
                .questionCount(dictQuestionMyPageResponseDtoArrayList.size())
                .question(dictQuestionMyPageResponseDtoArrayList)
                .build();
    }
    
    // 밈짤 Response Dto 리스트
    private List<MyPagePostBoardResponseDto> getMyPagePostBoardResponseDtoList(List<Board> userBoard) {
        List<MyPagePostBoardResponseDto> postBoards = new ArrayList<>();
        if (!userBoard.isEmpty()) {
            for (Board board : userBoard) {
                if (board.isEnabled() == true) {
                    postBoards.add(
                            MyPagePostBoardResponseDto.builder()
                                .boardId(board.getBoardId())
                                .title(board.getTitle())
                                .username(board.getUser().getUsername())
                                .profileImageUrl(board.getUser().getProfileImage())
                                .thumbNail(board.getThumbNail())
                                .category(board.getBoardCategory().getCategoryName())
                                .writer(board.getUser().getNickname())
                                .content(board.getContent())
                                .createdAt(board.getCreatedAt())
                                .views(board.getViews())
                                .likeCnt(board.getLikes().size())
                                .commentCnt(commentService.getCommentList(board).size())
                                .build()
                    );
                }
            }
        }
        return postBoards;
    }
    
    // 사전 Response Dto 리스트
    private List<MyPageDictResponseDto> getMyPageDictResponseDtoList(List<Dict> userDict, int dictCount) {
        List<MyPageDictResponseDto> userDictResponseList = new ArrayList<>();
        if (dictCount > 0) {
            for (Dict dict : userDict) {
                userDictResponseList.add(
                        MyPageDictResponseDto.builder()
                            .dictId(dict.getDictId())
                            .title(dict.getDictName())
                            .summary(dict.getSummary())
                            .meaning(dict.getContent())
                            .likeCount(dict.getDictLikeList().size())
                            .firstWriter(dict.getFirstAuthor().getNickname())
                            .recentWriter(dict.getRecentModifier().getNickname())
                            .createdAt(dict.getCreatedAt())
                            .modifiedAt(dict.getModifiedAt())
                            .build()
                );
            }
        }
        return userDictResponseList;
    }

    // 사전질문 Response Dto 리스트
    private List<DictQuestionMyPageResponseDto> getDictQuestionMyPageResponseDtoList(User user, List<DictQuestion> dictQuestionList, HashMap<String, Boolean> curiousTooMap, HashMap<Long, Long> completeMap) {
        List<DictQuestionMyPageResponseDto> dictQuestionMyPageResponseDtoArrayList = new ArrayList<>();
        if(!dictQuestionList.isEmpty()) {
            for (DictQuestion dictQuestion : dictQuestionList) {
                Long questionId = dictQuestion.getQuestionId();
                // completeMap 에 값이 없을 경우 채택되지 않음 = false.
                boolean isComplete = completeMap.get(questionId) != null;
                if (dictQuestion.isEnabled() == true) {
                    dictQuestionMyPageResponseDtoArrayList.add(
                            DictQuestionMyPageResponseDto.builder()
                                    .questionId(questionId)
                                    .thumbNail(dictQuestion.getThumbNail())
                                    .title(dictQuestion.getQuestionName())
                                    .content(dictQuestion.getContent())
                                    .username(dictQuestion.getUser().getUsername())
                                    .profileImageUrl(dictQuestion.getUser().getProfileImage())
                                    .writer(dictQuestion.getUser().getNickname())
                                    .createdAt(dictQuestion.getCreatedAt())
                                    .views(dictQuestion.getViews())
                                    .curiousTooCnt(dictQuestion.getDictCuriousTooList().size())
                                    .commentCnt(dictQuestion.getQuestionCommentList().size())
                                    .isCuriousToo(user != null && curiousTooMap.get(questionId + ":" + user.getId()) != null)
                                    .isComplete(isComplete)
                                    .build()
                    );
                }
            }
        }
        return dictQuestionMyPageResponseDtoArrayList;
    }


    // 나도 궁금해요 체크 여부 받아오기 기능
    private HashMap<String, Boolean> getCuriousTooMap(List<DictQuestion> questionList) {
        QDictCuriousToo qDictCuriousToo = QDictCuriousToo.dictCuriousToo;
        List<Tuple> curiousTooTuple = queryFactory.select(qDictCuriousToo.dictQuestion.questionId, qDictCuriousToo.user.id)
                .from(qDictCuriousToo)
                .where(qDictCuriousToo.dictQuestion.in(questionList))
                .fetch();

        return MemegleServiceStaticMethods.getLikeMap(curiousTooTuple);
    }

    // 채택 여부 받아오기 기능
    private HashMap<Long, Long> getIsComplete(List<DictQuestion> questionList) {
        QQuestionSelect qQuestionSelect = QQuestionSelect.questionSelect;
        List<Tuple> selectTuple = queryFactory.select(qQuestionSelect.dictQuestion.questionId, qQuestionSelect.questionComment.questionCommentId)
                .from(qQuestionSelect)
                .where(qQuestionSelect.dictQuestion.in(questionList))
                .fetch();

        return MemegleServiceStaticMethods.getLongLongMap(selectTuple);
    }
    //endregion


    // 프로필 이미지 수정
    @Transactional
    public MyPageProfileImageModifyResponseDto profileImageModify(UserDetailsImpl userDetails,
                                                                  MultipartFile file) throws IOException {
        // 1. 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 2. 유저 정보 가져오기
        User user = getSafeUser(userDetails.getUsername());
        // 3. 유저 프로필 이미지 저장 및 수정
        String profileImageUrl = s3Uploader.upload(file, "profileImages");
        user.setProfileImage(profileImageUrl);
        // 4. 해당 프로필 이미지 Response
        return MyPageProfileImageModifyResponseDto.builder()
                .profileImageUrl(profileImageUrl)
                .build();
    }

    // get SafeEntity
    // User
    // 유저 아이디로 해당 유저 정보 조회
    private User getSafeUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            throw new NullPointerException(NOT_EXIST_USER);
        }
        return user.get();
    }
    // endregion
}
