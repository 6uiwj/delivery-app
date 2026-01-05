package com.sparta.foodorder.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통 에러
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "잘못된 입력값입니다"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 타입입니다"),
    MISSING_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C003", "필수 입력값이 누락되었습니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C004", "서버 오류가 발생했습니다"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C005", "허용되지 않은 HTTP 메서드입니다"),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "C006", "접근이 거부되었습니다"),

    // 사용자 관련 에러
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "이미 존재하는 이메일입니다"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "비밀번호가 일치하지 않습니다"),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "U004", "인증되지 않은 사용자입니다"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U005", "이미 존재하는 닉네임입니다"),
    INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST, "U006", "잘못된 이메일 형식입니다"),
    INVALID_PASSWORD_FORMAT(HttpStatus.BAD_REQUEST, "U007", "비밀번호는 최소 8자 이상이며, 영문, 숫자, 특수문자를 포함해야 합니다"),
    SAME_PASSWORD(HttpStatus.BAD_REQUEST, "U008", "현재 비밀번호와 새 비밀번호가 동일합니다"),
    INVALID_NAME_FORMAT(HttpStatus.BAD_REQUEST, "U009", "이름은 한글 또는 영문만 입력 가능합니다"),
    USER_DEACTIVATED(HttpStatus.FORBIDDEN, "U010", "탈퇴한 사용자입니다"),
    USER_BANNED(HttpStatus.FORBIDDEN, "U011", "제재된 사용자입니다"),
    USER_WITHDRAWN(HttpStatus.FORBIDDEN, "U012", "탈퇴 처리된 사용자입니다"),
    INVALID_USER_STATUS(HttpStatus.BAD_REQUEST, "U013", "잘못된 사용자 상태입니다"),
    DUPLICATE_BUSINESS_NUMBER(HttpStatus.CONFLICT, "U014", "이미 등록된 사업자번호입니다"),
    USER_PENDING_APPROVAL(HttpStatus.FORBIDDEN, "U016", "승인 대기 중인 사용자입니다"),
    USER_NOT_PENDING(HttpStatus.BAD_REQUEST, "U017", "승인 대기 상태가 아닙니다"),

    // 메뉴 관련 에러
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "존재하지 않는 메뉴입니다."),
    MENU_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "M002", "존재하지 않는 옵션입니다."),
    MENU_OPTION_VALUE_NOT_FOUND(HttpStatus.NOT_FOUND, "M003", "존재하지 않는 옵션 value 값입니다."),

    // 옵션 관련 에러
    OPTION_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "MO001", "해당 옵션에 대한 접근권한이 없습니다."),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "MO002", "존재하지 않는 옵션입니다."),
    OPTION_VALUE_NOT_FOUND(HttpStatus.NOT_FOUND, "MO003", "존재하지 않는 옵션값입니다."),

    // 주문 관련 에러
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "O001", "주문을 찾을 수 없습니다"),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "O002", "잘못된 주문 상태입니다"),
    ORDER_CANT_ACCESS(HttpStatus.FORBIDDEN, "O003", "주문에 접근할 수 없습니다.(주문의 작성자, 가게의 주인만 접근 가능)"),
    ORDER_CANCEL_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "O004", "created, pending 상태일 때만 주문 취소 가능합니다."),
    ORDER_ACCEPT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "O005", "pending 상태일 때만 주문 수락 가능합니다."),
    ORDER_REJECT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "O006", "pending 상태일 때만 주문 거절 가능합니다."),
    ORDER_READY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "O007", "accept 상태일 때만 주문 취소 가능합니다."),
    ORDER_DELIVER_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "O008", "ready 상태일 때만 주문 취소 가능합니다."),
    ORDER_COMPLETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "O009", "delivering 상태일 때만 주문 취소 가능합니다."),
    ORDER_MEMO_LENGTH(HttpStatus.BAD_REQUEST, "O010", "주문의 메모는 200자 이하입니다."),

    // 주소 관련 에러
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "A001", "주소를 찾을 수 없습니다"),
    ADDRESS_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "A002", "해당 주소에 대한 권한이 없습니다"),
    DEFAULT_ADDRESS_EXISTS(HttpStatus.BAD_REQUEST, "A003", "이미 기본 배송지가 존재합니다"),
    INVALID_POSTAL_CODE(HttpStatus.BAD_REQUEST, "A004", "우편번호는 5자리 숫자여야 합니다"),
    ADDRESS_DELETED(HttpStatus.GONE, "A005", "삭제된 주소입니다"),
    ADDRESS_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "A006", "주소는 최대 10개까지 등록할 수 있습니다"),
    DEFAULT_ADDRESS_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "A007", "기본 배송지는 삭제할 수 없습니다"),

    // 인증 관련 에러
    TOO_MANY_LOGIN_ATTEMPTS(HttpStatus.TOO_MANY_REQUESTS, "AUTH001", "로그인 시도 횟수를 초과했습니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH002", "유효하지 않은 토큰입니다"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "AUTH003", "만료된 토큰입니다"),
    TOKEN_TAMPERED(HttpStatus.UNAUTHORIZED, "AUTH004", "변조된 토큰입니다"),
    REFRESH_TOKEN_REUSED(HttpStatus.UNAUTHORIZED, "AUTH005", "재사용된 리프레시 토큰입니다"),

    // 결제 관련 에러
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY001", "결제를 찾을 수 없습니다"),
    PAYMENT_ALREADY_EXISTS(HttpStatus.CONFLICT, "PAY002", "이미 결제가 존재하는 주문입니다"),
    PAYMENT_NOT_REFUNDABLE(HttpStatus.BAD_REQUEST, "PAY003", "환불이 불가능한 결제 상태입니다"),
    PAYMENT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PAY004", "결제에 대한 접근 권한이 없습니다"),
    PAYMENT_REFUND_TIME_EXPIRED(HttpStatus.BAD_REQUEST, "PAY005", "환불 가능 시간이 초과되었습니다"),
    PAYMENT_ALREADY_REFUNDED(HttpStatus.BAD_REQUEST, "PAY006", "이미 환불된 결제입니다"),
    PAYMENT_USER_MISMATCH(HttpStatus.FORBIDDEN, "PAY007", "본인의 결제만 처리할 수 있습니다"),
    PAYMENT_OWNER_MISMATCH(HttpStatus.FORBIDDEN, "PAY008", "본인 가게의 주문만 처리할 수 있습니다"),

    // 리뷰 관련 에러
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "리뷰를 찾을 수 없습니다"),
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "R002", "이미 해당 주문에 대한 리뷰가 존재합니다"),
    ORDER_NOT_COMPLETED(HttpStatus.BAD_REQUEST, "R003", "배송 완료된 주문만 리뷰를 작성할 수 있습니다"),
    REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "R004", "리뷰에 대한 접근 권한이 없습니다"),

    // AI 관련 에러
    AI_PROVIDER_NOT_FOUND(HttpStatus.BAD_REQUEST, "AI001", "지원하지 않는 AI Provider입니다"),
    AI_PROMPT_REQUIRED(HttpStatus.BAD_REQUEST, "AI002", "프롬프트는 필수입니다"),
    AI_MODEL_NOT_FOUND(HttpStatus.BAD_REQUEST, "AI004", "지원하지 않는 AI 모델입니다"),
    AI_EMPTY_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "A201", "AI가 빈 응답을 반환했습니다"),
    AI_RESPONSE_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A202", "AI 응답 파싱에 실패했습니다"),
    AI_GEMINI_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "A301", "Gemini API 오류가 발생했습니다"),

    // 가게 관련 에러
    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "가게를 찾을 수 없습니다."),
    STORE_ALREADY_EXIST(HttpStatus.CONFLICT, "S002", "이미 존재하는 가게입니다."),
    OWNER_ALREADY_HAS_STORE(HttpStatus.CONFLICT, "S003", "한 명의 사장님은 하나의 가게만 등록 가능합니다."),
    PHONE_ALREADY_EXIST(HttpStatus.CONFLICT, "S004", "이미 존재하는 전화번호입니다."),
    STORE_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "S005", "가게에 대한 접근권한이 없습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "S006", "카테고리를 찾을 수 없습니다."),
    CATEGORY_PERMISSION_DENIED(HttpStatus.FORBIDDEN, "S007", "카테고리에 대한 접근권한이 없습니다."),
    CATEGORY_ALREADY_EXIST(HttpStatus.CONFLICT, "S008", "이미 존재하는 카테고리입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
