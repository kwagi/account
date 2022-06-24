package com.example.account.controller;

import com.example.account.dto.useBalance;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@Slf4j
@RequiredArgsConstructor
public class TransactionController {
    @PostMapping("/transaction/use")
    public useBalance.Response useBalance(
            @Valid @RequestBody useBalance.Request request) {
    }
}
