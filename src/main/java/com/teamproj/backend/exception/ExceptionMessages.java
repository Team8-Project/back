package com.teamproj.backend.exception;

public final class ExceptionMessages {

    // User
    public static final String ILLEGAL_USERNAME_LENGTH = "아이디는 3자 이상 16자 이하만 입력 가능합니다.";
    public static final String ILLEGAL_USERNAME_FORMAT = "아이디는 영문자 및 숫자만 입력 가능합니다.";

    public static final String ILLEGAL_NICKNAME_LENGTH = "닉네임은 2자 이상 16자 이하만 입력 가능합니다.";
    public static final String ILLEGAL_NICKNAME_FORMAT = "닉네임은 영어 대소문자 및 숫자, 한글만 입력 가능합니다.";

    public static final String ILLEGAL_PASSWORD_LENGTH = "비밀번호는 6자 이상 16자 이하만 입력 가능합니다.";
    public static final String ILLEGAL_PASSWORD_FORMAT = "비밀번호는 영문자 및 숫자의 조합으로 입력해야 합니다.";

    public static final String EXIST_USERNAME = "이미 존재하는 ID 입니다.";
    public static final String EXIST_NICKNAME = "이미 존재하는 닉네임입니다.";
    public static final String ILLEGAL_MATCHING_PASSWORD_PASSWORD_CHECK = "비밀번호 확인이 일치하지 않습니다.";

    public static final String NOT_LOGIN_USER = "로그인하지 않은 사용자입니다.";
    public static final String NOT_EXIST_USER = "유효하지 않은 사용자입니다.";

    // Board
    public static final String NOT_EXIST_BOARD = "유효하지 않은 게시글입니다.";
    public static final String BOARD_IS_EMPTY = "조회 할 게시글이 없습니다.";
    public static final String NOT_EXIST_CATEGORY = "유효한 카테고리가 아닙니다.";
    public static final String NOT_MY_BOARD = "권한이 없습니다.";

    public static final String TITLE_IS_EMPTY = "제목은 필수 입력 값입니다.";
    public static final String CONTENT_IS_EMPTY = "내용은 필수 입력 값입니다.";

    public static final String SEARCH_MIN_SIZE_IS_TWO = "검색어는 최소 2자 이상이어야 합니다.";
    public static final String SEARCH_IS_EMPTY = "검색어는 필수 입력 값입니다.";

    // Comment
    public static final String NOT_MY_COMMENT = "권한이 없습니다.";
    public static final String NOT_EXIST_COMMENT = "존재하지 않는 댓글입니다.";

    // Dict
    public static final String EXIST_DICT = "이미 존재하는 사전입니다.";
    public static final String NOT_EXIST_DICT = "존재하지 않는 사전입니다.";
    public static final String NOT_EXIST_DICT_LIKE = "존재하지 않는 좋아요...?";
    public static final String SUMMARY_IS_TOO_BIG = "한줄요약은 최대 30자까지 쓸 수 있습니다.";

    // DictHistory
    public static final String NOT_EXIST_DICT_HISTORY = "유효하지 않은 역사입니다.";

    // DictCurious
    public static final String NOT_EXIST_DICT_CURIOUS = "유효하지 않은 요청입니다.";
    public static final String NOT_YOUR_CURIOUS = "당신의 요청이 아닙니다.";
    public static final String NOT_DUPLICATION_YOUR_REQUEST = "같은 사람이 중복해서 요청할 수 없습니다.";

    // DictQuestion
    public static final String DICT_QUESTION_IS_EMPTY = "조회 할 질문이 없습니다.";
    public static final String NOT_EXIST_QUESTION = "유효하지 않거나 삭제된 질문입니다.";
    public static final String NOT_MY_QUESTION = "당신이 작성한 질문이 아닙니다.";
    public static final String CAN_NOT_MODIFY_SELECT_QUESTION = "채택이 완료된 질문은 변경 할 수 없습니다.";
    public static final String ALREADY_SELECT = "이미 채택을 완료한 글입니다.";
    public static final String CAN_NOT_SELECT_MINE = "자기 자신을 채택할 수 없습니다.";

    // Alarm
    public static final String NOT_EXIST_ALARM = "유효하지 않은 알람입니다.";
    public static final String NOT_YOUR_ALARM = "당신의 알람이 아닙니다.";

    // OAuth2 Login
    public static final String NOT_VALID_CODE = "유효하지 않거나 잘못된 코드입니다.";
}
