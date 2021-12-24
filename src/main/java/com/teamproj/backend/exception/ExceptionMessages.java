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

    // Comment
    public static final String NOT_MY_COMMENT = "권한이 없습니다.";
    public static final String NOT_EXIST_COMMENT = "존재하지 않는 댓글입니다.";

    // Dict
    public static final String EXIST_DICT = "이미 존재하는 사전입니다.";
    public static final String NOT_EXIST_DICT = "존재하지 않는 사전입니다.";
    public static final String NOT_EXIST_DICT_LIKE = "존재하지 않는 좋아요...?";

    // DictHistory
    public static final String NOT_EXIST_DICT_HISTORY = "유효하지 않은 역사입니다.";
}