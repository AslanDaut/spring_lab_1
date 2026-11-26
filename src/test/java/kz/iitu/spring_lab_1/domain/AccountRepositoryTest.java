package kz.iitu.spring_lab_1.domain;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {
    @Autowired
    AccountRepository repository;

    @Autowired
    TestEntityManager em;

    @Test
    void findsAccountsWithTheRequestedStatus() {
        em.persist(new Account("AslanActive", new BigDecimal("1000.00"), "ACTIVE"));
        em.persist(new Account("FrozenUser", new BigDecimal("500.00"), "BLOCKED"));
        em.flush();
        em.clear();

        var found = repository.findByStatus("ACTIVE");

        assertThat(found).extracting(Account::getOwnerName)
                .contains("AslanActive")
                .doesNotContain("FrozenUser");
    }

    @Test
    void excludesActiveAccountsFromTheBlockedQuery() {
        em.persist(new Account("BlockedOnlyUser", new BigDecimal("300.00"), "BLOCKED"));
        em.persist(new Account("ActiveOnlyUser", new BigDecimal("200.00"), "ACTIVE"));
        em.flush();
        em.clear();

        var found = repository.findByStatus("BLOCKED");

        assertThat(found).extracting(Account::getOwnerName)
                .containsExactly("BlockedOnlyUser")
                .doesNotContain("ActiveOnlyUser");
    }
}
