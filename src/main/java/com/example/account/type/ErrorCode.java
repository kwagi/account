package com.example.account.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    USER_NOT_FOUND("사용자가 없음");

    private final String description;
}
