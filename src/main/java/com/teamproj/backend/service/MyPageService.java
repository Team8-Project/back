package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.mypage.MyPageDictResponseDto;
import com.teamproj.backend.dto.mypage.MyPagePostBoardResponseDto;
import com.teamproj.backend.dto.mypage.MyPageResponseDto;
import com.teamproj.backend.dto.mypage.MyPageProfileImageModifyResponseDto;
import com.teamproj.backend.exception.ExceptionMessages;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.board.Board;
import com.teamproj.backend.model.dict.Dict;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.util.S3Uploader;
import com.teamproj.backend.util.ValidChecker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.teamproj.backend.exception.ExceptionMessages.NOT_EXIST_USER;

@Service
@RequiredArgsConstructor
public class MyPageService {

    private final BoardRepository boardRepository;
    private final DictRepository dictRepository;
    private final UserRepository userRepository;
    private final CommentService commentService;

    private final S3Uploader s3Uploader;

    public MyPageResponseDto myPage(UserDetailsImpl userDetails) {
        // 1. 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 2. 로그인한 유저인지 체크 후 해당 유저 조회
        User user = getSafeUser(userDetails.getUsername());
        // 3. 유저가 작성한 게시글 중에 삭제 안된것 조회
        List<Board> userBoard = boardRepository.findByUserAndEnabled(user, true);
        // 4. 유저가 작성한 사전 조회
        List<Dict> userDict = dictRepository.findByFirstAuthor(user);
        // 5. , 닉네임, 프로필이미지, 유저가 작성한
        Long userId = user.getId();                         // 유저아이디
        String nickname = user.getNickname();               // 유저 닉네임
        String profileImageUrl = user.getProfileImage();    // 유저 프로필 이미지
        int postCount = userBoard.size();                   // 유저가 작성한 게시글 갯수
        int dictCount = userDict.size();                    // 유저가 작성한 사전 갯수

        // 6. 로그인한 유저가 작성한 게시글 데이터를 MyPagePostBoardResponseDto에 저장
        List<MyPagePostBoardResponseDto> postBoards = new ArrayList<>();
        if (postCount > 0) {
            for (Board board : userBoard) {
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
                            .hashTags(board.getBoardHashTagList().stream().map(
                                    e -> e.getHashTagName()).collect(Collectors.toCollection(ArrayList::new)))
                            .build()
                );
            }
        }

        // 7. 로그인한 유저가 작성한 사전 데이터를 MyPageDictResponseDto에 저장
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

        // 8. 마이페이지 정보 Response
        return MyPageResponseDto.builder()
                .userId(userId)                     // 유저아이디
                .nickname(nickname)                 // 유저닉네임
                .profileImageUrl(profileImageUrl)   // 유저 프로파일 이미지URL
                .postCount(postCount)               // 유저가 작성한 게시글 갯수
                .dictCount(dictCount)               // 유저가 작성한 사전 정보
                .postBoards(postBoards)             // 유저가 작성한 게시글 정보
                .dict(userDictResponseList)         // 유저가 작성한 사전 정보
                .build();
    }

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
