package com.example.account.service;

import com.example.account.domain.Account;
import com.example.account.domain.AccountUser;
import com.example.account.domain.Transaction;
import com.example.account.dto.TransactionDto;
import com.example.account.exception.AccountException;
import com.example.account.repository.AccountRepository;
import com.example.account.repository.AccountUserRepository;
import com.example.account.repository.TransactionRepository;
import com.example.account.type.AccountStatus;
import com.example.account.type.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.example.account.type.AccountStatus.IN_USE;
import static com.example.account.type.TransactionResultType.S;
import static com.example.account.type.TransactionType.CANCEL;
import static com.example.account.type.TransactionType.USE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void successUseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.useBalance(1L,
                "1000000000",
                1000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(9000L, captor.getValue().getBalanceSnapshot());
        assertEquals(9000L, transactionDto.getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());
    }

    @Test
    @DisplayName("해당 유저 없음 - 잔액 사용 실패 ")
    void useBalance_createdUserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 1000L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 실패 ")
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
                () -> transactionService.useBalance(1L, "1000000000", 1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름")
    void deleteAccountFailed_userUnMatched() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);
        AccountUser user1 = AccountUser.builder()
                .name("carti")
                .build();
        user.setId(13L);

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
                () -> transactionService.useBalance(1L, "1000000000", 1000L));
        //then
        assertEquals(ErrorCode.USER_NOT_MATCHED, accountException.getErrorCode());
    }


    @Test
    @DisplayName("이미 해지된 계좌임")
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
                () -> transactionService.useBalance(1L, "1000000000", 1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }

    @Test
    @DisplayName("거래금액이 잔액보다 큰 경우")
    void exceedAmount_UseBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(100L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1000000000", 1000L));
        //then
        assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE, accountException.getErrorCode());
        verify(transactionRepository, times(0)).save(any());
    }

    @Test
    void successCancelBalance() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(Transaction.builder()
                        .account(account)
                        .transactionType(CANCEL)
                        .transactionResultType(S)
                        .transactionId("transactionIdForCancel")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(10000L)
                        .build());

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.cancelBalance("transactionId",
                "1000000000",
                1000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(1000L, captor.getValue().getAmount());
        assertEquals(11000L, captor.getValue().getBalanceSnapshot());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(CANCEL, transactionDto.getTransactionType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals(10000L, transactionDto.getBalanceSnapshot());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용취소 실패 ")
    void cancelTransaction_AccountNotFound() {
        //given

        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(Transaction.builder()
                        .transactionType(USE)
                        .transactionResultType(S)
                        .transactionId("transactionId")
                        .transactedAt(LocalDateTime.now())
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .build())
                );

        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());


        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId",
                        "1000000000",
                        1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 거래 없음 - 잔액 사용취소 실패 ")
    void cancelTransaction_TransactionNotFound() {
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId",
                        "1000000000",
                        1000L));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    void successQueryTransaction() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Tory")
                .build();
        user.setId(12L);

        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(IN_USE)
                .accountNumber("1000000012")
                .balance(10000L)
                .build();

        Transaction transaction = Transaction.builder()
                .account(account)
                .transactionType(USE)
                .transactionResultType(S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .balanceSnapshot(9000L)
                .build();
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        //when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");
        //then
        assertEquals(USE, transactionDto.getTransactionType());
        assertEquals(S, transactionDto.getTransactionResultType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }

    @Test
    @DisplayName("원거래 없음 - 거래 조회 실패 ")
    void queryTransaction_notFound() {
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction("transactionId"));
        //then
        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, accountException.getErrorCode());
    }
}