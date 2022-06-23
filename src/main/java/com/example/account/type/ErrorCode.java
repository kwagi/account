package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없음"),
    USER_MAX_DETECTED("계좌 최대갯수 도달");

    private final String description;
}
