package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    INTERNAL_SERVER_ERROR("내부 서버오류가 발생했습니다."),
    USER_NOT_FOUND("사용자가 없습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    AMOUNT_EXCEED_BALANCE("거래금액이 잔액보다 큼니다."),
    TRANSACTION_NOT_FOUND("해당 거래가 없음"),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    ACCOUNT_TRANSACTION_LOCK("해당계좌는 사용중입니다."),
    USER_NOT_MATCHED("사용자와 계좌정보가 맞지않습니다."),
    CANCEL_MUST_FULLY("부분 취소는 불가합니다."),
    TOO_OLD_TO_CANCEL("1년이 지난 거래는 취소불가합니다."),
    TRANSACTION_NOT_MATCHED("해당 거래는 이 계좌에서 발생한 거래가 아닙니다."),
    ACCOUNT_ALREADY_UNREGISTERED("이미 해지되었습니다."),
    BALANCE_NOT_EMPTY("잔액이 존재하여 해지할수없습니다."),
    USER_MAX_DETECTED("사용자당 가질수있는 최대계좌갯수는 10개입니다.");

    private final String description;
}
