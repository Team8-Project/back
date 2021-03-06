package com.teamproj.backend.service.dict;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import com.teamproj.backend.model.viewers.QViewers;
import com.teamproj.backend.model.viewers.ViewTypeEnum;
import com.teamproj.backend.model.viewers.Viewers;
import com.teamproj.backend.security.UserDetailsImpl;
import com.teamproj.backend.service.AlarmService;
import com.teamproj.backend.service.RedisService;
import com.teamproj.backend.service.StatService;
import com.teamproj.backend.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.net.URLDecoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.teamproj.backend.exception.ExceptionMessages.*;
import static com.teamproj.backend.model.alarm.AlarmTypeEnum.SELECT_USER;

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
    private final AlarmService alarmService;

    private final JwtAuthenticateProcessor jwtAuthenticateProcessor;
    private final S3Uploader s3Uploader;

    private final JPAQueryFactory queryFactory;

    private final String S3dirName = "dictQuestionImages";

    //region ?????? ????????????
    public List<DictQuestionResponseDto> getQuestion(int page, int size, String token) {
        // 1. ?????? ????????? ????????? ??? ????????? ??????
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. ????????? ?????? ????????? User ?????? ????????????
        User user = getSafeUserByUserDetails(userDetails);
        // 3. ??????????????? enabled(?????? ??????) ???????????? ?????????????????? ????????? ?????? ????????????????????? ????????????
//        List<Tuple> tupleList = getQuestionProc(user, true, page, size);
        List<DictQuestion> tupleList = getQuestionProc(true, page, size);
        // 4. ???????????? ????????? DTO ???????????? ???????????? return.
//        return getDictQuestionResponseDtoList(tupleList);
        return getDictQuestionResponseDtoList(user, tupleList);
    }

    private User getSafeUserByUserDetails(UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return null;
        }
        return jwtAuthenticateProcessor.getUser(userDetails);
    }

//    // ????????? ???????????? ????????? ??? ?????? ??????????????? ??? ????????? ?????? ????????? ???????????? ???????????? ??????.....
//    private List<Tuple> getQuestionProc(User user, boolean enabled, int page, int size) {
//        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;
//        QDictQuestionComment qDictQuestionComment = QDictQuestionComment.dictQuestionComment;
//        QDictCuriousToo qDictCuriousToo = QDictCuriousToo.dictCuriousToo;
//        QQuestionSelect qQuestionSelect = QQuestionSelect.questionSelect;
//
//        /*
//            ?????? ????????? ??????
//            ***** dictQuestion *****
//            0 : ???????????? - Long questionId
//            1 : ?????? - String questionName
//            2 : ????????? - String thumbNail
//            3 : ?????? - String content
//            8 : ????????? - LocalDateTime createdAt
//            9 : ????????? - Integer views
//            ***** user *****
//            4 : ??????????????? - Long userId
//            5 : ????????? ????????? - String username
//            6 : ????????? ??????????????? - String userProfileImage
//            7 : ????????? ????????? - String userNickname
//            ***** dictCuriousToo *****
//            10 : ?????????????????? ?????? - Integer dictCuriousTooCount
//            12 : ?????????????????? ?????? - Long isDictCuriousToo // 0??? ?????? false, 0 ????????? ?????? true
//            ***** comment *****
//            11 : ?????? ?????? - Long commentSize
//            ***** questionSelect *****
//            13 : ????????? ?????? - Long selectedCommentId
//         */
//        int offset = page * size;
//        return queryFactory
//                .select(qDictQuestion.questionId,
//                        qDictQuestion.questionName,
//                        qDictQuestion.thumbNail,
//                        qDictQuestion.content,
//                        qDictQuestion.user.id,
//                        qDictQuestion.user.username,
//                        qDictQuestion.user.profileImage,
//                        qDictQuestion.user.nickname,
//                        qDictQuestion.createdAt,
//                        qDictQuestion.views,
//                        qDictQuestion.dictCuriousTooList.size(),
//                        queryFactory
//                                .select(qDictQuestionComment.count())
//                                .from(qDictQuestionComment)
//                                .where(qDictQuestionComment.dictQuestion.eq(qDictQuestion),
//                                        qDictQuestionComment.enabled.eq(true)),
//                        queryFactory
//                                .select(qDictCuriousToo.count())
//                                .from(qDictCuriousToo)
//                                .where(qDictCuriousToo.dictQuestion.eq(qDictQuestion),
//                                        eqUser(user)),
//                        queryFactory
//                                .select(qQuestionSelect.questionComment.questionCommentId)
//                                .from(qQuestionSelect)
//                                .where(qQuestionSelect.dictQuestion.eq(qDictQuestion))
//                )
//                .from(qDictQuestion)
//                .where(qDictQuestion.enabled.eq(enabled))
//                .orderBy(qDictQuestion.questionId.desc())
//                .offset(offset)
//                .limit(size)
//                .fetch();
//    }

    private List<DictQuestion> getQuestionProc(boolean enabled, int page, int size) {
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;

        int offset = page * size;
        return queryFactory
                .selectFrom(qDictQuestion)
                .where(qDictQuestion.enabled.eq(enabled))
                .orderBy(qDictQuestion.questionId.desc())
                .offset(offset)
                .limit(size)
                .fetch();
    }

//    private BooleanExpression eqUser(User user) {
//        return user == null ?
//                QDictCuriousToo.dictCuriousToo.curiousTooId.eq(0L) :
//                QDictCuriousToo.dictCuriousToo.user.eq(user);
//    }

//    private List<DictQuestionResponseDto> getDictQuestionResponseDtoList(List<Tuple> tupleList) {
//        // DB ?????? ????????? ????????? List ???????????? ?????? Response Dto ??????
//        List<DictQuestionResponseDto> dictQuestionResponseDtoList = new ArrayList<>();
//        for (Tuple tuple : tupleList) {
//            Long questionId = tuple.get(0, Long.class);
//            String title = tuple.get(1, String.class);
//            String thumbNail = tuple.get(2, String.class);
//            String content = tuple.get(3, String.class);
////            Long writerId = tuple.get(4, Long.class);
//            String username = tuple.get(5, String.class);
//            String profileImage = tuple.get(6, String.class);
//            String writer = tuple.get(7, String.class);
//            LocalDateTime createdAt = tuple.get(8, LocalDateTime.class);
//            Integer views = tuple.get(9, Integer.class);
//            Integer curiousTooCnt = tuple.get(10, Integer.class);
//            Long commentCnt = tuple.get(11, Long.class);
//            Long isCuriousTooLong = tuple.get(12, Long.class);
//            Boolean isCuriousToo = isCuriousTooLong != null && isCuriousTooLong > 0;
//            Long isCompleteLong = tuple.get(13, Long.class);
//            Boolean isComplete = isCompleteLong != null && isCompleteLong > 0;
//
//            dictQuestionResponseDtoList.add(DictQuestionResponseDto.builder()
//                    .questionId(questionId)
//                    .title(title)
//                    .thumbNail(thumbNail)
//                    .content(content)
//                    .username(username)
//                    .profileImageUrl(profileImage)
//                    .writer(writer)
//                    .createdAt(createdAt)
//                    .views(views == null ? 0 : views)
//                    .curiousTooCnt(curiousTooCnt == null ? 0 : curiousTooCnt)
//                    .commentCnt(commentCnt == null ? 0 : commentCnt.intValue())
//                    .isCuriousToo(isCuriousToo)
//                    .isComplete(isComplete)
//                    .build()
//            );
//        }
//
//        return dictQuestionResponseDtoList;
//    }

    private List<DictQuestionResponseDto> getDictQuestionResponseDtoList(User user, List<DictQuestion> questionList) {
        List<Long> questionIdList = new ArrayList<>();
        for (DictQuestion dictQuestion : questionList) {
            questionIdList.add(dictQuestion.getQuestionId());
        }

        // ????????? ???
        HashMap<String, String> userInfoMap = getUserInfoMap(questionList);
        // ?????? ???????????? ???
        HashMap<String, Boolean> curiousTooMap = getCuriousTooMap(questionIdList);
        // ????????? ?????? ???
        HashMap<Long, Long> curiousTooCountMap = getCuriousTooCountMap(questionList);
        // ?????? ?????? ???
        HashMap<Long, Long> commentCountMap = getDictQuestionCommentCountMap(questionList);
        // ?????? ?????? ???
        HashMap<Long, Long> completeMap = getIsComplete(questionIdList);

        Long userId = user == null ? null : user.getId();
        // DB ?????? ????????? ????????? List ???????????? ?????? Response Dto ??????
        List<DictQuestionResponseDto> dictQuestionResponseDtoList = new ArrayList<>();
        for (DictQuestion d : questionList) {
            Long questionId = d.getQuestionId();

            Long curiousTooCntLong = curiousTooCountMap.get(questionId);
            int curiousTooCnt;
            if(curiousTooCntLong == null){
                curiousTooCnt = 0;
            }else{
                curiousTooCnt = curiousTooCntLong.intValue();
            }
            Long commentCntLong = commentCountMap.get(questionId);
            int commentCnt;
            if(commentCntLong == null){
                commentCnt = 0;
            }else{
                commentCnt = commentCntLong.intValue();
            }
            dictQuestionResponseDtoList.add(DictQuestionResponseDto.builder()
                    .questionId(questionId)
                    .title(d.getQuestionName())
                    .thumbNail(d.getThumbNail())
                    .content(d.getContent())
                    .username(userInfoMap.get(questionId+":username"))
                    .profileImageUrl(userInfoMap.get(questionId+":profileImage"))
                    .writer(userInfoMap.get(questionId+":nickname"))
                    .createdAt(d.getCreatedAt())
                    .views(d.getViews())
                    .curiousTooCnt(curiousTooCnt)
                    .commentCnt(commentCnt)
                    .isCuriousToo(user != null && curiousTooMap.get(questionId + ":" + userId) != null)
                    .isComplete(completeMap.get(questionId) != null)
                    .build()
            );
        }

        return dictQuestionResponseDtoList;
    }

    // ?????? ?????? ???????????? ??????
    private HashMap<Long, Long> getIsComplete(List<Long> questionIdList) {
        QQuestionSelect qQuestionSelect = QQuestionSelect.questionSelect;
        List<Tuple> selectTuple = queryFactory.select(qQuestionSelect.dictQuestion.questionId, qQuestionSelect.questionComment.questionCommentId)
                .from(qQuestionSelect)
                .where(qQuestionSelect.dictQuestion.questionId.in(questionIdList))
                .fetch();

        return MemegleServiceStaticMethods.getLongLongMap(selectTuple);
    }

    // ?????? ?????? ???????????? ??????
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

    // ?????? ???????????? ?????? ???????????? ??????
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

    // ?????? ???????????? ?????? ?????? ???????????? ??????
    private HashMap<String, Boolean> getCuriousTooMap(List<Long> questionIdList) {
        QDictCuriousToo qDictCuriousToo = QDictCuriousToo.dictCuriousToo;
        List<Tuple> curiousTooTuple = queryFactory.select(qDictCuriousToo.dictQuestion.questionId, qDictCuriousToo.user.id)
                .from(qDictCuriousToo)
                .where(qDictCuriousToo.dictQuestion.questionId.in(questionIdList))
                .fetch();

        return MemegleServiceStaticMethods.getLikeMap(curiousTooTuple);
    }

    // ?????? ????????? ?????? ???????????? ??????
    private HashMap<String, String> getUserInfoMap(List<DictQuestion> questionList) {
        // ???????????? ?????? : ????????? ?????????, ????????? ?????????, ????????? ??????????????????
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;
        List<Tuple> userInfoTuple = queryFactory.select(qDictQuestion.questionId, qDictQuestion.user.username, qDictQuestion.user.nickname, qDictQuestion.user.profileImage)
                .from(qDictQuestion)
                .where(qDictQuestion.in(questionList))
                .fetch();

        return MemegleServiceStaticMethods.getUserInfoMap(userInfoTuple);
    }
    //endregion

    //region ?????? ??????
    @Transactional
    public DictQuestionUploadResponseDto uploadQuestion(UserDetailsImpl userDetails,
                                                        DictQuestionUploadRequestDto dictQuestionUploadRequestDto,
                                                        MultipartFile multipartFile) {
        // ??? ??????????????? ?????? ????????? ?????? ??????
        ValidChecker.loginCheck(userDetails);
        // 1. Request??? ????????? ????????? ????????? ??????(????????? ??????, ????????? ??????)
        String questionName = dictQuestionUploadRequestDto.getTitle();
        String content = dictQuestionUploadRequestDto.getContent();
        if (questionName.isEmpty()) {
            throw new IllegalArgumentException(TITLE_IS_EMPTY);
        }
        if (content.isEmpty()) {
            throw new IllegalArgumentException(CONTENT_IS_EMPTY);
        }

        // 2. multipartFile??? ????????? ????????? ????????? null ?????? => null??? ????????? S3 ????????? ??????
        String imageUrl = "";
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
        }

        // 3. ????????? ????????? DB??? ??????
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        DictQuestion dictQuestion = DictQuestion.builder()
                .user(user)                         // ?????????
                .questionName(questionName)         // ??????
                .content(content)                   // ??????
                .thumbNail(imageUrl)                // ?????????
                .enabled(true)                      // ????????? ??????
                .build();
        dictQuestion = dictQuestionRepository.save(dictQuestion);

        // 4. ????????? ???????????? ?????? ????????? ??????
        Image image = Image.builder()
                .imageTypeEnum(ImageTypeEnum.DICT_QUESTION)
                .targetId(dictQuestion.getQuestionId())
                .imageUrl(imageUrl)
                .build();

        imageRepository.save(image);

        // 5. ????????? ????????? ???????????? Response(????????????, ??????, ??????, ???????????????, ?????????)
        return DictQuestionUploadResponseDto.builder()
                .questionId(dictQuestion.getQuestionId())
                .title(dictQuestion.getQuestionName())
                .content(dictQuestion.getContent())
                .thumbNail(dictQuestion.getThumbNail())
                .createdAt(dictQuestion.getCreatedAt())
                .build();
    }
    //endregion

    //region ?????? ?????? ??????
    public DictQuestionDetailResponseDto getQuestionDetail(Long questionId, String token) {
        // 1. ?????? ????????? ????????? ??? ????????? ??????
        UserDetailsImpl userDetails = jwtAuthenticateProcessor.forceLogin(token);
        // 2. ?????? ????????? ?????? ??????(???????????? ?????????)
        User user = getSafeUserByUserDetails(userDetails);
        // 3. ?????? ??????
        Tuple dictQuestionTuple = getSafeQuestionTupleById(questionId, user);
        // 4. ????????? DTO ???????????? ???????????? return.
        return dictQuestionTupleToDictQuestionDetailResponseDto(questionId, dictQuestionTuple, user);
    }

    private DictQuestionDetailResponseDto dictQuestionTupleToDictQuestionDetailResponseDto(Long questionId,
                                                                                           Tuple dictQuestionTuple,
                                                                                           User user) {
        String username = dictQuestionTuple.get(0, String.class);
        String writer = dictQuestionTuple.get(1, String.class);
        String profileImageUrl = dictQuestionTuple.get(2, String.class);
        String title = dictQuestionTuple.get(3, String.class);
        String content = dictQuestionTuple.get(4, String.class);
        String thumbNail = dictQuestionTuple.get(5, String.class);
        LocalDateTime createdAt = dictQuestionTuple.get(6, LocalDateTime.class);
        Integer viewsInteger = dictQuestionTuple.get(7, Integer.class);
        int views = viewsInteger == null ? 0 : viewsInteger;
        Integer curiousTooCntInteger = dictQuestionTuple.get(8, Integer.class);
        int curiousTooCnt = curiousTooCntInteger == null ? 0 : curiousTooCntInteger;
        Long isCuriousTooLong = dictQuestionTuple.get(9, Long.class);
        Boolean isCuriousToo = isCuriousTooLong != null && isCuriousTooLong > 0;
        Long selectedComment = dictQuestionTuple.get(10, Long.class);
        selectedComment = selectedComment == null ? 0L : selectedComment;
        Long viewerIpLong = dictQuestionTuple.get(11, Long.class);
        boolean isView = viewerIpLong != null && viewerIpLong > 0;

        // 4. ????????? ????????? ?????? ?????? ??????
        // - ???????????? ?????? IP??? ?????? ????????? ??????????????? ?????? ?????? ?????? ??????
        if (!isView) {
            viewersRepository.save(Viewers.builder()
                    .viewTypeEnum(ViewTypeEnum.DICT_QUESTION)
                    .targetId(questionId)
                    .viewerIp(StatisticsUtils.getClientIp())
                    .build());
            dictQuestionRepository.updateView(questionId);
        }

        List<DictQuestionCommentResponseDto> commentList = commentService.getCommentList(questionId, user, selectedComment);
        return DictQuestionDetailResponseDto.builder()
                .questionId(questionId)
                .username(username)
                .writer(writer)
                .profileImageUrl(profileImageUrl)
                .title(title)
                .content(content)
                .thumbNail(thumbNail)
                .createdAt(createdAt)
                .views(views)
                .curiousTooCnt(curiousTooCnt)
                .isCuriousToo(isCuriousToo)
                .commentList(commentList)
                .commentCnt(commentList.size())
                .selectedComment(selectedComment)
                .build();
    }

    private Tuple getSafeQuestionTupleById(Long questionId, User user) {
        QDictQuestion qDictQuestion = QDictQuestion.dictQuestion;
        QDictCuriousToo qDictCuriousToo = QDictCuriousToo.dictCuriousToo;
        QViewers qViewers = QViewers.viewers;
        QQuestionSelect qQuestionSelect = QQuestionSelect.questionSelect;

        String userIp = StatisticsUtils.getClientIp();

        Tuple result = queryFactory
                .select(qDictQuestion.user.username,
                        qDictQuestion.user.nickname,
                        qDictQuestion.user.profileImage,
                        qDictQuestion.questionName,
                        qDictQuestion.content,
                        qDictQuestion.thumbNail,
                        qDictQuestion.createdAt,
                        qDictQuestion.views,
                        qDictQuestion.dictCuriousTooList.size(),
                        queryFactory
                                .select(qDictCuriousToo.count())
                                .from(qDictCuriousToo)
                                .where(qDictCuriousToo.dictQuestion.eq(qDictQuestion),
                                        isCuriousToo(user)),
                        queryFactory
                                .select(qQuestionSelect.questionComment.questionCommentId.max())
                                .from(qQuestionSelect)
                                .where(qQuestionSelect.dictQuestion.eq(qDictQuestion)),
                        queryFactory
                                .select(qViewers.count())
                                .from(qViewers)
                                .where(qViewers.targetId.eq(questionId), qViewers.viewerIp.eq(userIp), qViewers.viewTypeEnum.eq(ViewTypeEnum.DICT_QUESTION))
                )
                .from(qDictQuestion)
                .where(qDictQuestion.questionId.eq(questionId),
                        qDictQuestion.enabled.eq(true))
                .fetchFirst();

        if(result == null){
            throw new NullPointerException(NOT_EXIST_QUESTION);
        }
        return result;
    }

    private BooleanExpression isCuriousToo(User user){
        return user == null ?
                QDictCuriousToo.dictCuriousToo.curiousTooId.eq(0L) :
                QDictCuriousToo.dictCuriousToo.user.eq(user);
    }

    private DictQuestion getSafeQuestionById(Long questionId) {
        Optional<DictQuestion> dictQuestion = dictQuestionRepository.findById(questionId);
        return dictQuestion.orElseThrow(() -> new NullPointerException(NOT_EXIST_QUESTION));
    }
    //endregion

    //region ?????? ????????????(??????)
    @Transactional
    public String updateQuestion(Long questionId,
                                 UserDetailsImpl userDetails,
                                 DictQuestionUpdateRequestDto dictQuestionUpdateRequestDto,
                                 MultipartFile multipartFile) {
        // ???????????? ???????????? ??????
        ValidChecker.loginCheck(userDetails);
        // 1. ??????????????? ?????? ??????
        DictQuestion dictQuestion = getSafeQuestionById(questionId);
        // ????????? ????????? ????????? ??? ??????
        checkSelectQuestion(dictQuestion);
        // 2. ????????? ?????? ?????? ??????
        checkPermissionToQuestion(userDetails, dictQuestion);

        // 3. multipartFile??? ????????? ????????? ?????? ??????
        // - ????????? S3??? ???????????? ?????? ????????? ?????? ???
        String imageUrl = "";
        if (multipartFile != null && !multipartFile.isEmpty()) {
            imageUrl = s3Uploader.upload(multipartFile, S3dirName);
            deleteImg(dictQuestion);
        }

        // ??????
        dictQuestion.update(dictQuestionUpdateRequestDto, imageUrl);
        // ???????????? ????????? ??????(?????? ????????? ???????????? ??????)
        statService.statQuestionModify(dictQuestion);

        // 7. ?????? ??? Response ??????
        return "?????? ??????";
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

    //region ?????? ??????
    @Transactional
    public String deleteQuestion(UserDetailsImpl userDetails, Long questionId) {
        // ???????????? ???????????? ??????
        ValidChecker.loginCheck(userDetails);
        // 1. ????????? ?????? ??????
        DictQuestion dictQuestion = getSafeQuestion(questionId);
        // ?????? ????????? ????????? ????????? ??? ??????
        checkSelectQuestion(dictQuestion);
        // 2. ?????? ?????? ?????? ??????
        checkPermissionToQuestion(userDetails, dictQuestion);
        // 3. ?????? ?????? => enabled = false
        dictQuestion.setEnabled(false);

        return "?????? ??????";
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

    //region ?????? ?????? ????????????
    public boolean curiousTooQuestion(UserDetailsImpl userDetails, Long questionId) {
        // ???????????? ???????????? ??????
        ValidChecker.loginCheck(userDetails);
        // 1. ???????????? ????????? ??????
        DictQuestion dictQuestion = getSafeQuestion(questionId);
        // 2. ?????? ????????? ????????? ??????
        User user = jwtAuthenticateProcessor.getUser(userDetails);
        Optional<DictCuriousToo> dictCuriousToo = dictCuriousTooRepository.findByDictQuestionAndUser(dictQuestion, user);

        // 3. ?????? ???????????? ?????? ??????
        // - ?????? ??????????????? ?????? ??? false ??????
        // - ?????? ??? ??????????????? ?????? ??? true ??????
        boolean isLike = false;
        if (dictCuriousToo.isPresent()) {
            dictCuriousTooRepository.deleteById(dictCuriousToo.get().getCuriousTooId());
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

    //region ?????? ??????
    public List<DictQuestionSearchResponseDto> questionSearch(User user, String q, int page, int size) {
        // 1. ????????? ???????????? 2??? ????????? ?????? ?????? ??????
        if (q.length() < 2) {
            return new ArrayList<>();
        }
//        RecentSearch recentSearch = RecentSearch.builder()
//                .viewerIp(StatisticsUtils.getClientIp())
//                .query(q)
//                .type(QueryTypeEnum.BOARD)
//                .build();
//        recentSearchRepository.save(recentSearch);

        // 2. ????????? ???????????? ???????????? ?????? ?????? ????????? ??????
        List<DictQuestion> questionList = getSafeSearchResult(q, page * size, size);
        List<Long> questionIdList = new ArrayList<>();
        for (DictQuestion dictQuestion : questionList) {
            questionIdList.add(dictQuestion.getQuestionId());
        }
        // ????????? ???
        HashMap<String, String> userInfoMap = getUserInfoMap(questionList);
        // ?????? ???????????? ???
        HashMap<String, Boolean> curiousTooMap = getCuriousTooMap(questionIdList);
        // ????????? ?????? ???
        HashMap<Long, Long> curiousTooCountMap = getCuriousTooCountMap(questionList);
        // ?????? ?????? ???
        HashMap<Long, Long> commentCountMap = getDictQuestionCommentCountMap(questionList);
        // ?????? ?????? ???
        HashMap<Long, Long> completeMap = getIsComplete(questionIdList);


        // 3. ?????? ????????? ????????? ?????? ???????????? Response
        List<DictQuestionSearchResponseDto> dictQuestionSearchResponseDtoList = new ArrayList<>();
        for (DictQuestion dictQuestion : questionList) {
            // Map ??? ?????? ??? id ??????
            Long questionId = dictQuestion.getQuestionId();

            // likeCountMap ??? ?????? ???????????? ???????????? ?????? = 0???.
            Long curiousTooCountLong = curiousTooCountMap.get(questionId);
            int curiousTooCount = curiousTooCountLong == null ? 0 : curiousTooCountLong.intValue();

            // CommentCountMap ??? ?????? ?????? ?????? ????????? ?????? = 0???.
            Long commentCountLong = commentCountMap.get(questionId);
            int commentCount = commentCountLong == null ? 0 : commentCountLong.intValue();

            // completeMap ??? ?????? ?????? ?????? ???????????? ?????? = false.
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
        // ???????????? ?????? ?????? ????????? ?????? ?????????.
        String newQ = q + "*";
        Optional<List<DictQuestion>> result = dictQuestionRepository.findAllByTitleAndContentByFullText(newQ, true, page, size);

        // ??????????????? ???????????? ?????? ??? ??? ????????? return.
        return result.orElseGet(ArrayList::new);
    }
    //endregion

    //region ?????? ??????
    @Transactional
    public String selectAnswer(UserDetailsImpl userDetails, Long commentId) {
        ValidChecker.loginCheck(userDetails);
        DictQuestionComment comment = getSafeDictQuestionComment(commentId);

        // ?????? ??? ????????? ????????? ??? ??????
        DictQuestion dictQuestion = comment.getDictQuestion();
        checkPermissionToQuestion(userDetails, dictQuestion);

        // ?????? ????????? ???????????? ?????????
        checkSelected(dictQuestion);

        // ?????? ????????? ????????? ????????? ??? ??????
        checkSelectMine(userDetails, comment);

        QuestionSelect questionSelect = QuestionSelect.builder()
                .dictQuestion(dictQuestion)
                .questionComment(comment)
                .build();

        dictQuestion.questionSelect(questionSelect);

        // ?????? ?????? ??? ?????? ??????????????? ??????
        alarmService.sendAlarm(SELECT_USER, dictQuestion.getQuestionId(), comment.getUser());
        return "?????? ??????";
    }

    // ??? ???????????? ??????(??? ????????? ?????? ??????)
    private void checkSelectMine(UserDetailsImpl userDetails, DictQuestionComment comment) {
        if (userDetails.getUsername().equals(comment.getUser().getUsername())) {
            throw new IllegalArgumentException(CAN_NOT_SELECT_MINE);
        }
    }

    // ?????? ????????? ????????? ????????? ??????
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

    //region ??????????????? ????????? ??? ??????
    public Long getTotalQuestionCount() {
        return dictQuestionRepository.countByEnabled(true);
    }
    //endregion

    //region ???????????? ??????
    // ?????? ?????? ?????? ??????
    private void checkPermissionToQuestion(UserDetailsImpl userDetails, DictQuestion dictQuestion) {
        if (!userDetails.getUsername().equals(dictQuestion.getUser().getUsername())) {
            throw new IllegalArgumentException(NOT_MY_QUESTION);
        }
    }
    //endregion
}
