package kz.iitu.spring_lab_1.service;

import java.math.BigDecimal;
import java.util.Optional;
import kz.iitu.spring_lab_1.domain.Account;
import kz.iitu.spring_lab_1.domain.AccountRepository;
import kz.iitu.spring_lab_1.exception.AccountNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    AccountRepository repository;

    @Mock
    AuditService auditService;

    @InjectMocks
    AccountService service;

    private Account accountWithBalance(String balance) {
        return new Account("Test account", new BigDecimal(balance), "ACTIVE");
    }

    @Test
    void transfersMoneyBetweenTwoExistingAccounts() {
        Account from = accountWithBalance("500.00");
        Account to = accountWithBalance("100.00");
        given(repository.findById(1L)).willReturn(Optional.of(from));
        given(repository.findById(2L)).willReturn(Optional.of(to));

        service.transfer(1L, 2L, new BigDecimal("150.00"), false);

        assertThat(from.getBalance()).isEqualByComparingTo("350.00");
        assertThat(to.getBalance()).isEqualByComparingTo("250.00");
        verify(auditService).logAuditRecord("Transfer initiated from 1 to 2");
    }

    @Test
    void throwsAccountNotFoundWhenSourceAccountDoesNotExist() {
        given(repository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.transfer(99L, 2L, new BigDecimal("50.00"), false))
                .isInstanceOf(AccountNotFoundException.class)
                .hasMessage("Account 99 not found");

        verify(repository, never()).save(any());
        verify(auditService, never()).logAuditRecord(any());
    }

    @Test
    void savesBothAccountsWithUpdatedBalances() {
        Account from = accountWithBalance("1000.00");
        Account to = accountWithBalance("200.00");
        given(repository.findById(1L)).willReturn(Optional.of(from));
        given(repository.findById(2L)).willReturn(Optional.of(to));

        service.transfer(1L, 2L, new BigDecimal("300.00"), false);

        var savedCaptor = ArgumentCaptor.forClass(Account.class);
        verify(repository, times(2)).save(savedCaptor.capture());
        assertThat(savedCaptor.getAllValues())
                .extracting(Account::getBalance)
                .containsExactly(new BigDecimal("700.00"), new BigDecimal("500.00"));
    }

    @Test
    void throwsRuntimeExceptionWhenFailureSimulationIsRequested() {
        given(repository.findById(1L)).willReturn(Optional.of(accountWithBalance("500.00")));
        given(repository.findById(2L)).willReturn(Optional.of(accountWithBalance("100.00")));

        assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("100.00"), true))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Simulated error to trigger rollback!");

        verify(auditService).logAuditRecord("Transfer initiated from 1 to 2");
    }

    @Test
    void rejectsTransfersFromBlockedAccounts() {
        Account blocked = new Account("Frozen", new BigDecimal("500.00"), "BLOCKED");
        given(repository.findById(1L)).willReturn(Optional.of(blocked));
        given(repository.findById(2L)).willReturn(Optional.of(accountWithBalance("100.00")));

        assertThatThrownBy(() -> service.transfer(1L, 2L, new BigDecimal("50.00"), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Blocked source account cannot make transfers");

        verify(repository, never()).save(any());
        verify(auditService, never()).logAuditRecord(any());
    }

    @ParameterizedTest
    @CsvSource({
            "500.00, 100.00, 50.00, 450.00, 150.00",
            "200.00, 0.00, 200.00, 0.00, 200.00",
            "1000.50, 49.50, 0.50, 1000.00, 50.00"
    })
    void transfersVariousValidAmountsCorrectly(
            String fromStart, String toStart, String amount, String expectedFrom, String expectedTo) {
        Account from = accountWithBalance(fromStart);
        Account to = accountWithBalance(toStart);
        given(repository.findById(1L)).willReturn(Optional.of(from));
        given(repository.findById(2L)).willReturn(Optional.of(to));

        service.transfer(1L, 2L, new BigDecimal(amount), false);

        assertThat(from.getBalance()).isEqualByComparingTo(expectedFrom);
        assertThat(to.getBalance()).isEqualByComparingTo(expectedTo);
    }
}
