package com.teamproj.backend.service;

import com.teamproj.backend.Repository.board.BoardCategoryRepository;
import com.teamproj.backend.Repository.board.BoardRepository;
import com.teamproj.backend.dto.board.BoardUploadRequestDto;
import com.teamproj.backend.dto.board.BoardUploadResponseDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.security.UserDetailsImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardCategoryRepository boardCategoryRepository;

//    @Mock
//    private BoardSubjectRepository boardSubjectRepository;
//
//    @Mock
//    private BoardLikeRepository boardLikeRepository;


    @Test
//    @WithMockUser(username = "테스트계정", password = "custom_password")
    @DisplayName("게시물 저장 / 정상")
    void uploadBoard_sucess() {
        // givien
        User user = User.builder()
                .username("테스트으")
                .nickname("테스트")
                .password("테스트으")
                .build();

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        BoardUploadRequestDto boardUploadRequestDto = BoardUploadRequestDto.builder()
                        .title("타이틀")
                        .content("내용")
                        .subject(null)
                        .category(null)
                        .build();

        BoardUploadResponseDto boardUploadResponseDto = boardService.uploadBoard(userDetails, boardUploadRequestDto, "카테고리");
    }
}