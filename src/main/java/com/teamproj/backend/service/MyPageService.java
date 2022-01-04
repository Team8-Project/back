package com.teamproj.backend.service;

import com.teamproj.backend.Repository.UserRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.Repository.dict.DictRepository;
import com.teamproj.backend.dto.MyPage.MyPageDictResponseDto;
import com.teamproj.backend.dto.MyPage.MyPagePostBoard;
import com.teamproj.backend.dto.MyPage.MyPageResponseDto;
import com.teamproj.backend.dto.MyPage.MyPageProfileImageModifyResponseDto;
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

        ValidChecker.loginCheck(userDetails);

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(
                        () -> new IllegalArgumentException("로그인이 필요합니다.")
                );

        List<Board> userBoard = boardRepository.findByUserAndEnabled(user, true);
        List<Dict> userDict = dictRepository.findByFirstAuthor(user);

        Long userId = user.getId();
        String nickname = user.getNickname();
        String profileImageUrl = user.getProfileImage();
        int postCount = userBoard.size();
        int dictCount = userDict.size();


        List<MyPagePostBoard> postBoards = new ArrayList<>();
        if (postCount > 0) {
            for (Board board : userBoard) {
                postBoards.add(
                        MyPagePostBoard.builder()
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

        return MyPageResponseDto.builder()
                .userId(userId)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .postCount(postCount)
                .dictCount(dictCount)
                .postBoards(postBoards)
                .dict(userDictResponseList)
                .build();
    }


    @Transactional
    public MyPageProfileImageModifyResponseDto profileImageModify(UserDetailsImpl userDetails,
                                                                  MultipartFile file) throws IOException {
        ValidChecker.loginCheck(userDetails);

        User user = getSafeUser(userDetails.getUsername());

        String profileImageUrl = s3Uploader.upload(file, "profileImages");
        user.setProfileImage(profileImageUrl);

        return MyPageProfileImageModifyResponseDto.builder()
                .profileImageUrl(profileImageUrl)
                .build();
    }

    // get SafeEntity
    // User
    private User getSafeUser(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (!user.isPresent()) {
            throw new NullPointerException(NOT_EXIST_USER);
        }
        return user.get();
    }
    // endregion
}
