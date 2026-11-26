package kz.iitu.spring_lab_1.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import kz.iitu.spring_lab_1.domain.Account;
import kz.iitu.spring_lab_1.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public Account byId(@PathVariable Long id) {
        return accountService.byId(id);
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest request) {
        accountService.transfer(request.fromId(), request.toId(), request.amount(), false);
        return ResponseEntity.noContent().build();
    }

    public record TransferRequest(
            @NotNull @Positive Long fromId,
            @NotNull @Positive Long toId,
            @NotNull @Positive BigDecimal amount) {
    }
}
