package kz.iitu.spring_lab_1;

import kz.iitu.spring_lab_1.domain.Account;
import kz.iitu.spring_lab_1.domain.AccountRepository;
import kz.iitu.spring_lab_1.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransactionLabTests {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void taskA_and_B_testTransferAndRollback() {
        // Task A: Успешный перевод (Alice -> Bob: 100)
        accountService.transfer(1L, 2L, new BigDecimal("100.00"), false);
        Account alice = accountRepository.findById(1L).orElseThrow();
        assertEquals(new BigDecimal("900.00"), alice.getBalance());

        // Task A & B: Ошибка и откат (Rollback). Alice попытается отправить 50, но выпадет ошибка
        assertThrows(RuntimeException.class, () -> {
            accountService.transfer(1L, 2L, new BigDecimal("50.00"), true);
        });

        // Баланс Алисы не изменился из-за отката транзакции @Transactional
        Account aliceAfterRollback = accountRepository.findById(1L).orElseThrow();
        assertEquals(new BigDecimal("900.00"), aliceAfterRollback.getBalance());
        System.out.println("Rollback successful: Balance remained " + aliceAfterRollback.getBalance());
    }

    @Test
    void taskD_testOptimisticLocking() {
        // Загружаем один и тот же аккаунт в двух разных переменных (имитация конкурентности)
        Account copy1 = accountRepository.findById(1L).orElseThrow();
        Account copy2 = accountRepository.findById(1L).orElseThrow();

        // Первая "транзакция" успешно сохраняет изменения
        copy1.setBalance(new BigDecimal("800.00"));
        accountRepository.save(copy1);

        // Вторая "транзакция" пытается сохранить устаревшие данные
        copy2.setBalance(new BigDecimal("999.00"));

        // Ожидаем исключение оптимистичной блокировки из-за несовпадения поля @Version
        assertThrows(ObjectOptimisticLockingFailureException.class, () -> {
            accountRepository.save(copy2);
        });
        System.out.println("Optimistic locking effectively prevented data overwrite!");
    }
}