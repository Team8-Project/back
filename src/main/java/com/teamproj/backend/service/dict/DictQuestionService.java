package com.teamproj.backend.service.dict;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.teamproj.backend.Repository.ViewersRepository;
import com.teamproj.backend.Repository.dict.DictCuriousTooRepository;
import com.teamproj.backend.Repository.dict.DictQuestionCommentRepository;
import com.teamproj.backend.Repository.dict.DictQuestionRepository;
import com.teamproj.backend.Repository.dict.QuestionSelectRepository;
import com.teamproj.backend.Repository.image.ImageRepository;
import com.teamproj.backend.dto.dict.question.DictQuestionResponseDto;
import com.teamproj.backend.dto.dict.question.DictQuestionUploadRequestDto;
import com.teamproj.backend.dto.dict.question.DictQuestionUploadResponseDto;
import com.teamproj.backend.dto.dict.question.comment.DictQuestionCommentResponseDto;
import com.teamproj.backend.dto.dict.question.detail.DictQuestionDetailResponseDto;
import com.teamproj.backend.dto.dict.question.search.DictQuestionSearchResponseDto;
import com.teamproj.backend.dto.dict.question.update.DictQuestionUpdateRequestDto;
import com.teamproj.backend.model.User;
import com.teamproj.backend.model.dict.question.*;
import com.teamproj.backend.model.image.Image;
import com.teamproj.backend.model.image.ImageTypeEnum;
import com.teamproj.backend.model.viewers.ViewTypeEnum;
import com.teamproj.backend.model.viewers.Viewers;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.RedisService;
import com.teamproj.backend.service.StatService;
import com.teamproj.backend.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;

@Service
@RequiredArgsConstructor
public class DictQuestionService {
    private final DictQuestionRepository dictQuestionRepository;
    private final DictCuriousTooRepository dictCuriousTooRepository;
    private final DictQuestionCommentRepository dictQuestionCommentRepository;
    private final QuestionSelectRepository questionSelectRepository;

    private final ImageRepository imageRepository;
    private final ViewersRepository viewersRepository;

    private final RedisService redisService;
    private final StatService statService;
    private final DictQuestionCommentService commentService;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final S3Uploader s3Uploader;

    private final JPAQueryFactory queryFactory;

    private final String S3dirName = "dictQuestionImages";

    //region 질문 전체조회
    public List<DictQuestionResponseDto> getQuestion(int page, int size, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 받아온 회원 정보로 User 정보 받아오기
        User user = getSafeUserByUserDetails(userDetails);
        // 4. 카테고리와 enabled(삭제 안된) 데이터를 페이지네이션 조건에 맞게 리스트형식으로 가져오기
        List<DictQuestion> questionList = getSafeQuestionList(true, page, size);

        return getDictQuestionResponseDtoList(user, questionList);
    }

    private User getSafeUserByUserDetails(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return null;
        }
        return jwtAuthenticateProcessor.getUser(userDetails);
    }

    private List<DictQuestion> getSafeQuestionList(boolean enabled, int page, int size) {
        Optional<Page<DictQuestion>> dictQuestionPage = dictQuestionRepository.findAllByEnabledOrderByCreatedAtDesc(enabled, PageRequest.of(page, size));
        return dictQuestionPage.orElseThrow(() -> new NullPointerException(DICT_QUESTION_IS_EMPTY)).toList();
    }

    private List<DictQuestionResponseDto> getDictQuestionResponseDtoList(User user, List<DictQuestion> questionList) {
        // 5. DB에서 받아온 게시글 List 데이터를 담을 Response Dto 생성
        List<DictQuestionResponseDto> dictQuestionResponseDtoList = new ArrayList<>();

        // 작성자 맵
        HashMap<String, String> userInfoMap = getUserInfoMap(questionList);
        // 나도 궁금해요 맵
        HashMap<String, Boolean> curiousTooMap = getCuriousTooMap(questionList);
        // 좋아요 개수 맵
        HashMap<Long, Long> curiousTooCountMap = getCuriousTooCountMap(questionList);
        // 댓글 개수 맵
        HashMap<Long, Long> commentCountMap = getDictQuestionCommentCountMap(questionList);
        // 채택 여부 맵
        HashMap<Long, Long> completeMap = getIsComplete(questionList);

        for (DictQuestion dictQuestion : questionList) {
            // Map 에 사용 될 id 키값
            Long questionId = dictQuestion.getQuestionId();

            // likeCountMap 에 값이 없을경우 좋아요가 없음 = 0개.
            Long curiousTooCountLong = curiousTooCountMap.get(questionId);
            int curiousTooCount = curiousTooCountLong == null ? 0 : curiousTooCountLong.intValue();

            // CommentCountMap 에 값이 없을 경우 댓글이 없음 = 0개.
            Long commentCountLong = commentCountMap.get(questionId);
            int commentCount = commentCountLong == null ? 0 : commentCountLong.intValue();

            // completeMap 에 값이 없을 경우 채택되지 않음 = false.
            boolean isComplete = completeMap.get(questionId) != null;

            // 7. 질문 List 데이터를 DtoList 에 담아서 리턴
            dictQuestionResponseDtoList.add(DictQuestionResponseDto.builder()
                    .questionId(questionId)
                    .title(dictQuestion.getQuestionName())
                    .thumbNail(dictQuestion.getThumbNail())
                    .content(dictQuestion.getContent())
                    .username(userInfoMap.get(questionId + ":username"))
                    .profileImageUrl(userInfoMap.get(questionId + ":profileImage"))
                    .writer(userInfoMap.get(questionId + ":nickname"))
                    .createdAt(dictQuestion.getCreatedAt())
                    .views(dictQuestion.getViews())
                    .curiousTooCnt(curiousTooCount)
                    .commentCnt(commentCount)
                    .isCuriousToo(user != null && curiousTooMap.get(questionId + ":" + user.getId()) != null)
                    .isComplete(isComplete)
                    .build());
        }

        return dictQuestionResponseDtoList;
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

    // 댓글 개수 받아오기 기능
    private HashMap<Long, Long> getDictQuestionCommentCountMap(List<DictQuestion> questionList) {
        QDictQuestionComment qComment = QDictQuestionComment.dictQuestionComment;
        NumberPath<Long> count = Expressions.numberPath(Long.class, "c");
        List<Tuple> commentCountListTuple = queryFactory
                .select(qComment.dictQuestion.questionId, qComment.count().as(count))
                .from(qComment)
                .where(qComment.dictQuestion.in(questionList)
                        .and(qComment.enabled.eq(true)))
                .groupBy(qComment.dictQuestion.questionId)
                .fetch();

        return MemegleServiceStaticMethods.getLongLongMap(commentCountListTuple);
    }

    // 나도 궁금해요 개수 받아오기 기능
    private HashMap<Long, Long> getCuriousTooCountMap(List<DictQuestion> questionList) {
        QDictCuriousToo qDictCuriousToo = QDictCuriousToo.dictCuriousToo;
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;

        List<Tuple> curiousTooCountListTuple = queryFactory
                .select(qDictCuriousToo.dictQuestion.questionId, qDictCuriousToo.count())
                .from(qDictCuriousToo)
                .where(qDictCuriousToo.dictQuestion.in(questionList))
                .groupBy(qDictQuestion)
                .fetch();

        return MemegleServiceStaticMethods.getLongLongMap(curiousTooCountListTuple);
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

    // 질문 작성자 정보 받아오기 기능
    private HashMap<String, String> getUserInfoMap(List<DictQuestion> questionList) {
        // 얻어오는 정보 : 사용자 아이디, 사용자 닉네임, 사용자 프로필이미지
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;
        List<Tuple> userInfoTuple = queryFactory.select(qDictQuestion.questionId, qDictQuestion.user.username, qDictQuestion.user.nickname, qDictQuestion.user.profileImage)
                .from(qDictQuestion)
                .where(qDictQuestion.in(questionList))
                .fetch();

        return MemegleServiceStaticMethods.getUserInfoMap(userInfoTuple);
    }
    //endregion

    //region 질문 작성
    @Transactional
    public DictQuestionUploadResponseDto uploadQuestion(UserDetailsImpl userDetails,
                                                        DictQuestionUploadRequestDto dictQuestionUploadRequestDto,
                                                        MultipartFile multipartFile) {
        // 글 작성할려는 유저 로그인 여부 확인
        ValidChecker.loginCheck(userDetails);
        // 1. Request로 넘어온 데이터 유효성 검사(게시글 제목, 게시글 내용)
        String questionName = dictQuestionUploadRequestDto.getTitle();
        String content = dictQuestionUploadRequestDto.getContent();
        if (questionName.isEmpty()) {
            throw new IllegalArgumentException(TITLE_IS_EMPTY);
        }
        if (content.isEmpty()) {
            throw new IllegalArgumentException(CONTENT_IS_EMPTY);
        }

        // 2. multipartFile로 넘어온 이미지 데이터 null 체크 => null이 아니면 S3 버킷에 저장
        String imageUrl = "";
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
        }

        // 3. 게시글 데이터 DB에 저장
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        DictQuestion dictQuestion = DictQuestion.builder()
                .user(user)                         // 작성자
                .questionName(questionName)         // 제목
                .content(content)                   // 내용
                .thumbNail(imageUrl)                // 이미지
                .enabled(true)                      // 활성화 여부
                .build();
        dictQuestion = dictQuestionRepository.save(dictQuestion);

        // 4. 작성한 게시글에 맞는 이미지 저장
        Image image = Image.builder()
                .imageTypeEnum(ImageTypeEnum.DICT_QUESTION)
                .targetId(dictQuestion.getQuestionId())
                .imageUrl(imageUrl)
                .build();

        imageRepository.save(image);

        // 5. 저장한 데이터 기반으로 Response(질문번호, 제목, 내용, 이미지주소, 생성일)
        return DictQuestionUploadResponseDto.builder()
                .questionId(dictQuestion.getQuestionId())
                .title(dictQuestion.getQuestionName())
                .content(dictQuestion.getContent())
                .thumbNail(dictQuestion.getThumbNail())
                .createdAt(dictQuestion.getCreatedAt())
                .build();
    }
    //endregion

    //region 질문 상세 조회
    public DictQuestionDetailResponseDto getQuestionDetail(Long questionId, String token) {
        // 1. 회원 정보가 존재할 시 로그인 처리
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. 질문 조회
        DictQuestion dictQuestion = getSafeQuestionById(questionId);
        // 3. 질문 좋아요 여부 조회(로그인한 유저만)
        User user = getSafeUserByUserDetails(userDetails);

        // 4. 게시글 조회수 관련 처리 로직
        // - 조회하는 유저 IP를 통해 조회수 새로고침과 같은 중복 처리 방지
        String userIp = StatisticsUtils.getClientIp();
        if (isView(dictQuestion, userIp)) {
            viewersRepository.save(Viewers.builder()
                    .viewTypeEnum(ViewTypeEnum.DICT_QUESTION)
                    .targetId(questionId)
                    .viewerIp(userIp)
                    .build());
            dictQuestionRepository.updateView(questionId);
        }

        // 5. 게시글 좋아요 리스트 조회
//        List<BoardLike> boardLikeList = boardLikeRepository.findAllByBoard(board);
//
//        List<CommentResponseDto> commentList = commentService.getCommentList(board);
        // 채택 여부
        QuestionSelect questionSelect = dictQuestion.getQuestionSelect();
        Long selectedCommentId = questionSelect == null ? 0L : questionSelect.getQuestionComment().getQuestionCommentId();
        // 좋아요 여부
        boolean isCuriousToo = getSafeCurious(user, dictQuestion);

        User writer = dictQuestion.getUser();
        List<DictQuestionCommentResponseDto> commentList = commentService.getCommentList(dictQuestion, user);
        return DictQuestionDetailResponseDto.builder()
                .questionId(questionId)
                .username(writer.getUsername())
                .writer(writer.getNickname())
                .profileImageUrl(writer.getProfileImage())
                .title(dictQuestion.getQuestionName())
                .content(dictQuestion.getContent())
                .thumbNail(dictQuestion.getThumbNail())
                .createdAt(dictQuestion.getCreatedAt())
                .views(dictQuestion.getViews())
                .curiousTooCnt(dictQuestion.getDictCuriousTooList().size())
                .isCuriousToo(isCuriousToo)
                .commentList(commentList)
                .commentCnt(commentList.size())
                .selectedComment(selectedCommentId)
                .build();
    }

    private boolean getSafeCurious(User user, DictQuestion dictQuestion) {
        if (user == null) {
            return false;
        }
        return dictCuriousTooRepository.existsByUserAndDictQuestion(user, dictQuestion);
    }

    private DictQuestion getSafeQuestionById(Long questionId) {
        Optional<DictQuestion> dictQuestion = dictQuestionRepository.findById(questionId);
        return dictQuestion.orElseThrow(() -> new NullPointerException(NOT_EXIST_QUESTION));
    }

    private boolean isView(DictQuestion dictQuestion, String ip) {
        ViewTypeEnum type = ViewTypeEnum.DICT_QUESTION;
        Long dictId = dictQuestion.getQuestionId();
        Optional<Viewers> viewers = viewersRepository.findByViewerIpAndViewTypeEnumAndTargetId(ip, type, dictId);
        return !viewers.isPresent();
    }
    //endregion

    //region 질문 업데이트(수정)
    @Transactional
    public String updateQuestion(Long questionId,
                                 UserDetailsImpl userDetails,
                                 DictQuestionUpdateRequestDto dictQuestionUpdateRequestDto,
                                 MultipartFile multipartFile) {
        // 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 1. 업데이트할 질문 조회
        DictQuestion dictQuestion = getSafeQuestionById(questionId);
        // 채택한 질문은 수정할 수 없음
        checkSelectQuestion(dictQuestion);
        // 2. 게시글 수정 권한 체크
        checkPermissionToQuestion(userDetails, dictQuestion);

        // 3. multipartFile로 넘어온 이미지 파일 저장
        // - 기존에 S3에 저장되어 있는 이미지 삭제 후
        String imageUrl = "";
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
            deleteImg(dictQuestion);
        }

        // 수정
        deleteImg(dictQuestion);
        dictQuestion.update(dictQuestionUpdateRequestDto, imageUrl);
        // 수정내역 통계에 저장(수정 내용은 보관되지 않음)
        statService.statQuestionModify(dictQuestion);

        // 7. 저장 및 Response 전송
        return "수정 완료";
    }

    private void deleteImg(DictQuestion dictQuestion) {
        try {
            String oldImageUrl = URLDecoder.decode(
                    dictQuestion.getThumbNail().replace(
                            "https://memeglememegle-bucket.s3.ap-northeast-2.amazonaws.com/", ""
                    ),
                    "UTF-8"
            );
            s3Uploader.deleteFromS3(oldImageUrl);
        } catch (Exception e) {
        }
    }
    //endregion

    //region 질문 삭제
    @Transactional
    public String deleteQuestion(UserDetailsImpl userDetails, Long questionId) {
        // 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 1. 삭제할 질문 조회
        DictQuestion dictQuestion = getSafeQuestion(questionId);
        // 이미 채택한 질문은 삭제할 수 없음
        checkSelectQuestion(dictQuestion);
        // 2. 질문 삭제 권한 체크
        checkPermissionToQuestion(userDetails, dictQuestion);
        // 3. 질문 삭제 => enabled = false
        dictQuestion.setEnabled(false);

        return "삭제 완료";
    }

    private void checkSelectQuestion(DictQuestion dictQuestion) {
        if (dictQuestion.getQuestionSelect() != null) {
            throw new IllegalArgumentException(CAN_NOT_MODIFY_SELECT_QUESTION);
        }
    }

    private DictQuestion getSafeQuestion(Long questionId) {
        Optional<DictQuestion> dictQuestion = dictQuestionRepository.findById(questionId);
        return dictQuestion.orElseThrow(() -> new NullPointerException(NOT_EXIST_QUESTION));
    }
    //endregion

    //region 질문 나도 궁금해요
    public boolean curiousTooQuestion(UserDetailsImpl userDetails, Long questionId) {
        // 로그인한 유저인지 체크
        ValidChecker.loginCheck(userDetails);
        // 1. 좋아요할 게시글 조회
        DictQuestion dictQuestion = getSafeQuestion(questionId);
        // 2. 해당 게시글 좋아요 조회
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        Optional<DictCuriousToo> dictCuriosToo = dictCuriousTooRepository.findByDictQuestionAndUser(dictQuestion, user);

        // 3. 나도 궁금해요 여부 확인
        // - 체크 되어있다면 취소 후 false 반환
        // - 체크 안 되어있다면 추가 후 true 반환
        boolean isLike = false;
        if (dictCuriosToo.isPresent()) {
            dictCuriousTooRepository.deleteByDictQuestionAndUser(dictQuestion, user);
        } else {
            dictCuriousTooRepository.save(DictCuriousToo.builder()
                    .dictQuestion(dictQuestion)
                    .user(user)
                    .build());
            isLike = true;
        }

        return isLike;
    }
    //endregion

    //region 질문 검색
    public List<DictQuestionSearchResponseDto> questionSearch(User user, String q, int page, int size) {
        // 1. 검색어 입력값이 2자 이하일 경우 검색 불가
        if (q.length() < 2) {
            return new ArrayList<>();
        }
//        RecentSearch recentSearch = RecentSearch.builder()
//                .viewerIp(StatisticsUtils.getClientIp())
//                .query(q)
//                .type(QueryTypeEnum.BOARD)
//                .build();
//        recentSearchRepository.save(recentSearch);

        // 2. 제목에 검색어가 포함되어 있는 질문 리스트 조회
        List<DictQuestion> questionList = getSafeSearchResult(q, page * size, size);

        // 작성자 맵
        HashMap<String, String> userInfoMap = getUserInfoMap(questionList);
        // 나도 궁금해요 맵
        HashMap<String, Boolean> curiousTooMap = getCuriousTooMap(questionList);
        // 좋아요 개수 맵
        HashMap<Long, Long> curiousTooCountMap = getCuriousTooCountMap(questionList);
        // 댓글 개수 맵
        HashMap<Long, Long> commentCountMap = getDictQuestionCommentCountMap(questionList);
        // 채택 여부 맵
        HashMap<Long, Long> completeMap = getIsComplete(questionList);


        // 3. 검색 결과가 있으면 해당 게시글들 Response
        List<DictQuestionSearchResponseDto> dictQuestionSearchResponseDtoList = new ArrayList<>();
        for (DictQuestion dictQuestion : questionList) {
            // Map 에 사용 될 id 키값
            Long questionId = dictQuestion.getQuestionId();

            // likeCountMap 에 값이 없을경우 좋아요가 없음 = 0개.
            Long curiousTooCountLong = curiousTooCountMap.get(questionId);
            int curiousTooCount = curiousTooCountLong == null ? 0 : curiousTooCountLong.intValue();

            // CommentCountMap 에 값이 없을 경우 댓글이 없음 = 0개.
            Long commentCountLong = commentCountMap.get(questionId);
            int commentCount = commentCountLong == null ? 0 : commentCountLong.intValue();

            // completeMap 에 값이 없을 경우 채택되지 않음 = false.
            boolean isComplete = completeMap.get(questionId) != null;

            dictQuestionSearchResponseDtoList.add(DictQuestionSearchResponseDto.builder()
                    .questionId(questionId)
                    .title(dictQuestion.getQuestionName())
                    .thumbNail(dictQuestion.getThumbNail())
                    .content(dictQuestion.getContent())
                    .username(userInfoMap.get(questionId + ":username"))
                    .profileImageUrl(userInfoMap.get(questionId + ":profileImage"))
                    .writer(userInfoMap.get(questionId + ":nickname"))
                    .createdAt(dictQuestion.getCreatedAt())
                    .views(dictQuestion.getViews())
                    .curiousTooCnt(curiousTooCount)
                    .commentCnt(commentCount)
                    .isCuriousToo(user != null && curiousTooMap.get(questionId + ":" + user.getId()) != null)
                    .isComplete(isComplete)
                    .build());
        }

        return dictQuestionSearchResponseDtoList;
    }

    private List<DictQuestion> getSafeSearchResult(String q, int page, int size) {
        // 전문검색 쿼리 뒤의 글자도 검색 되도록.
        String newQ = q + "*";
        Optional<List<DictQuestion>> result = dictQuestionRepository.findAllByTitleAndContentByFullText(newQ, true, page, size);

        // 검색결과가 존재하지 않을 시 빈 리스트 return.
        return result.orElseGet(ArrayList::new);
    }
    //endregion

    //region 질문 채택
    public String selectAnswer(UserDetailsImpl userDetails, Long commentId) {
        ValidChecker.loginCheck(userDetails);
        DictQuestionComment comment = getSafeDictQuestionComment(commentId);

        // 내가 쓴 질문만 채택할 수 있음
        DictQuestion dictQuestion = comment.getDictQuestion();
        checkPermissionToQuestion(userDetails, dictQuestion);

        // 이미 채택된 질문인지 확인함
        checkSelected(dictQuestion);

        // 내가 작성한 댓글은 채택할 수 없음
        checkSelectMine(userDetails, comment);

        questionSelectRepository.save(QuestionSelect.builder()
                .dictQuestion(dictQuestion)
                .questionComment(comment)
                .build());

        return "채택 완료";
    }

    // 내 댓글인지 확인(내 댓글은 채택 불가)
    private void checkSelectMine(UserDetailsImpl userDetails, DictQuestionComment comment) {
        if(userDetails.getUsername().equals(comment.getUser().getUsername())){
            throw new IllegalArgumentException(CAN_NOT_SELECT_MINE);
        }
    }
    // 이미 채택이 완료된 글인지 확인
    private void checkSelected(DictQuestion dictQuestion) {
        if (questionSelectRepository.existsByDictQuestion(dictQuestion)) {
            throw new IllegalArgumentException(ALREADY_SELECT);
        }
    }

    private DictQuestionComment getSafeDictQuestionComment(Long commentId) {
        Optional<DictQuestionComment> comment = dictQuestionCommentRepository.findById(commentId);
        return comment.orElseThrow(() -> new NullPointerException(NOT_EXIST_COMMENT));
    }
    //endregion

    //region 카테고리별 게시글 총 개수
    public Long getTotalQuestionCount() {
        return dictQuestionRepository.countByEnabled(true);
    }
    //endregion

    //region 중복코드 정리
    // 질문 변경 권한 확인
    private void checkPermissionToQuestion(UserDetailsImpl userDetails, DictQuestion dictQuestion) {
        if (!userDetails.getUsername().equals(dictQuestion.getUser().getUsername())) {
            throw new IllegalArgumentException(NOT_MY_QUESTION);
        }
    }
    //endregion
}
