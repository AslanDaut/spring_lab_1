package kz.iitu.spring_lab_1.service;

import kz.iitu.spring_lab_1.domain.Account;
import kz.iitu.spring_lab_1.domain.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final AuditService auditService;

    public AccountService(AccountRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount, boolean simulateError) {
        Account from = repository.findById(fromId).orElseThrow();
        Account to = repository.findById(toId).orElseThrow();

        from.debit(amount);
        to.credit(amount);

        repository.save(from);
        repository.save(to);

        // Запись аудита в отдельной транзакции (REQUIRES_NEW)
        auditService.logAuditRecord("Transfer initiated from " + fromId + " to " + toId);

        if (simulateError) {
            // Unchecked исключение приведет к откату (rollback) перевода,
            // но аудит останется сохраненным.
            throw new RuntimeException("Simulated error to trigger rollback!");
        }
    }
}