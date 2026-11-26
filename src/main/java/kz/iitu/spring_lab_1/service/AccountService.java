package kz.iitu.spring_lab_1.service;

import java.math.BigDecimal;
import kz.iitu.spring_lab_1.domain.Account;
import kz.iitu.spring_lab_1.domain.AccountRepository;
import kz.iitu.spring_lab_1.exception.AccountNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {
    private final AccountRepository repository;
    private final AuditService auditService;

    public AccountService(AccountRepository repository, AuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public Account byId(Long id) {
        return repository.findById(id).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount, boolean simulateError) {
        Account from = byId(fromId);
        Account to = byId(toId);

        if ("BLOCKED".equals(from.getStatus())) {
            throw new IllegalStateException("Blocked source account cannot make transfers");
        }

        from.debit(amount);
        to.credit(amount);

        repository.save(from);
        repository.save(to);

        // Запись аудита в отдельной транзакции (REQUIRES_NEW)
        auditService.logAuditRecord("Transfer initiated from " + fromId + " to " + toId);

        if (simulateError) {
            // Unchecked исключение приведет к откату перевода, аудит при этом сохранится.
            throw new RuntimeException("Simulated error to trigger rollback!");
        }
    }
}
