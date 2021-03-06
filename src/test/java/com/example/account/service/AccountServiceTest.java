package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.dto.AccountDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountUserRepository accountUserRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .build()));
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000013")
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(100L, 1000L);
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000013", accountDto.getAccountNumber());
    }

    @Test
    void createFirstAccount() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();//?????????
        user.setId(15L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000000")
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.createAccount(100L, 1000L);
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(15L, accountDto.getUserId());
        assertEquals("1000000000", accountDto.getAccountNumber());
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ?????? ?????? ?????? ")
    void createdUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("????????? ?????? ??????????????? 10???")
    void createdUserOver10() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();//?????????
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));
        //then
        assertEquals(ErrorCode.USER_MAX_DETECTED, accountException.getErrorCode());
    }

    @Test
    void deleteAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .build()));


        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        //when
        AccountDto accountDto = accountService.deleteAccount(100L, "1234567890");
        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ?????? ?????? ?????? ")
    void deletedUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("?????? ?????? ?????? - ?????? ?????? ?????? ")
    void deletedAccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());


        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("?????? ????????? ??????")
    void deleteAccountFailed_userUnMatched() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);
        AccountUser user1 = AccountUser.builder()
                .name("carti")
                .build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user1)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.USER_NOT_MATCHED, accountException.getErrorCode());
    }

    @Test
    @DisplayName("????????? ??????")
    void deleteAccountFailed_balanceExist() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);


        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .balance(100L)
                        .build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());
    }

    @Test
    @DisplayName("?????? ????????? ?????????")
    void deleteAccountFailed_alreadyUnRegistered() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .build()));

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }

    @Test
    void successGetAccountsByUserId() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountUser(user)
                        .accountNumber("1111111111")
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("2222222222")
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountUser(user)
                        .accountNumber("3333333333")
                        .balance(3000L)
                        .build()
        );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);
        //when
        List<AccountDto> accountDtos = accountService.getAccountByUserId(1L);
        //then
        assertEquals(3, accountDtos.size());
        assertEquals("1111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(1000L, accountDtos.get(0).getBalance());
        assertEquals("2222222222", accountDtos.get(1).getAccountNumber());
        assertEquals(2000L, accountDtos.get(1).getBalance());
        assertEquals("3333333333", accountDtos.get(2).getAccountNumber());
        assertEquals(3000L, accountDtos.get(2).getBalance());
    }

    @Test
    void failedGetAccounts() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.getAccountByUserId(1L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }
}