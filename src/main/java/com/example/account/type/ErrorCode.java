package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없습니다."),
    ACCOUNT_NOT_FOUND("계좌가 없습니다."),
    USER_NOT_MATCHED("사용자와 계좌정보가 맞지않습니다."),
    ACCOUNT_ALREADY_UNREGISTERED("이미 해지되었습니다."),
    BALANCE_NOT_EMPTY("잔액이 존재하여 해지할수없습니다."),
    USER_MAX_DETECTED("사용자당 가질수있는 최대계좌갯수는 10개입니다.");

    private final String description;
}
