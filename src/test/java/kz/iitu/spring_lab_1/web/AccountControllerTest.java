package kz.iitu.spring_lab_1.web;

import java.math.BigDecimal;
import kz.iitu.spring_lab_1.domain.Account;
import kz.iitu.spring_lab_1.domain.AppUserRepository;
import kz.iitu.spring_lab_1.exception.AccountNotFoundException;
import kz.iitu.spring_lab_1.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfiguration.class, AccountExceptionHandler.class})
class AccountControllerTest {
    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AccountService accountService;

    @MockitoBean
    AppUserRepository userRepository;

    @Test
    @WithMockUser(roles = "USER")
    void existingAccountReturnsItsJsonOwner() throws Exception {
        given(accountService.byId(1L))
                .willReturn(new Account("Alice", new BigDecimal("1000.00"), "ACTIVE"));

        mockMvc.perform(get("/api/accounts/1").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName").value("Alice"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void missingAccountReturnsProblemDetail() throws Exception {
        given(accountService.byId(7L)).willThrow(new AccountNotFoundException(7L));

        mockMvc.perform(get("/api/accounts/7").with(user("user").roles("USER")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Account 7 not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void invalidTransferBodyReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/accounts/transfers")
                        .with(user("admin").roles("ADMIN"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
