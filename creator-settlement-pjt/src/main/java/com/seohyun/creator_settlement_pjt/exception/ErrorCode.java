package com.seohyun.creator_settlement_pjt.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 400 BAD REQUEST
    INVALID_REQUEST(400, HttpStatus.BAD_REQUEST, "유효하지 않은 요청입니다."),
    INVALID_ROLE(400, HttpStatus.BAD_REQUEST, "수강생만 강의를 구매할 수 있습니다."),
    INVALID_PAYMENT_AMOUNT(400, HttpStatus.BAD_REQUEST, "결제 금액이 강의 정가와 일치하지 않습니다."),
    CANCEL_AMOUNT_EXCEEDED(400, HttpStatus.BAD_REQUEST, "환불 금액이 원 결제 금액을 초과할 수 없습니다."),
    INVALID_SETTLEMENT_STATUS(400, HttpStatus.BAD_REQUEST, "유효하지 않은 정산 상태입니다."),

    // 404 NOT FOUND
    COURSE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 강의입니다."),
    USER_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 사용자입니다."),
    SALE_RECORD_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 판매 내역입니다."),
    FEE_RATE_NOT_FOUND(404, HttpStatus.NOT_FOUND, "현재 유효한 수수료율이 없습니다."),

    // 409 CONFLICT
    ALREADY_ENROLLED(409, HttpStatus.CONFLICT, "이미 수강 중인 강의입니다."),
    ALREADY_CANCELLED(409, HttpStatus.CONFLICT, "이미 취소된 판매 내역입니다."),
    SETTLEMENT_ALREADY_EXISTS(409, HttpStatus.CONFLICT, "해당 월의 정산이 이미 존재합니다."),

    // 404 NOT FOUND (settlement)
    SETTLEMENT_NOT_FOUND(404, HttpStatus.NOT_FOUND, "존재하지 않는 정산입니다."),

    // 500 INTERNAL SERVER ERROR
    INTERNAL_SERVER_ERROR(500, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류입니다.");

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}
