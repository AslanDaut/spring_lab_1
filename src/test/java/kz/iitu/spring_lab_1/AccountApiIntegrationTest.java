package kz.iitu.spring_lab_1;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.EntityExchangeResult;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestTestClient
@Testcontainers
class AccountApiIntegrationTest {
    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    RestTestClient client;

    @Test
    void transfersThenReadsThePersistedBalanceFromPostgres() {
        EntityExchangeResult<String> anonymousLoginPage = client.get()
                .uri("/login")
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult();
        String anonymousSession = sessionCookie(anonymousLoginPage.getResponseHeaders().get(HttpHeaders.SET_COOKIE));
        String loginCsrf = csrfToken(anonymousLoginPage.getResponseBody());

        EntityExchangeResult<Void> loginResponse = client.post()
                .uri("/login")
                .header(HttpHeaders.COOKIE, anonymousSession)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("username=admin&password=admin123&_csrf=" + loginCsrf)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectBody()
                .isEmpty();
        String authenticatedSession = sessionCookie(loginResponse.getResponseHeaders().get(HttpHeaders.SET_COOKIE));

        EntityExchangeResult<String> authenticatedLoginPage = client.get()
                .uri("/login")
                .header(HttpHeaders.COOKIE, authenticatedSession)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult();
        String transferCsrf = csrfToken(authenticatedLoginPage.getResponseBody());

        client.post()
                .uri("/api/accounts/transfers")
                .header(HttpHeaders.COOKIE, authenticatedSession)
                .header("X-CSRF-TOKEN", transferCsrf)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"fromId\":1,\"toId\":2,\"amount\":125.00}")
                .exchange()
                .expectStatus().isNoContent();

        client.get()
                .uri("/api/accounts/1")
                .header(HttpHeaders.COOKIE, authenticatedSession)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.ownerName").isEqualTo("Alice")
                .jsonPath("$.balance").isEqualTo(875.0);
    }

    private static String sessionCookie(List<String> setCookies) {
        return setCookies.stream()
                .filter(cookie -> cookie.startsWith("JSESSIONID="))
                .map(cookie -> cookie.split(";", 2)[0])
                .findFirst()
                .orElseThrow(() -> new AssertionError("No JSESSIONID cookie was returned"));
    }

    private static String csrfToken(String html) {
        Matcher matcher = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"").matcher(html);
        assertThat(matcher.find()).as("login form should contain a CSRF token").isTrue();
        return matcher.group(1);
    }
}
