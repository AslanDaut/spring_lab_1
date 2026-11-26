# Laboratory Work 12 Verification Evidence

## Repository adaptation

The available `lab12` checkout contains `AccountService` and no `BookService` or catalogue controller. The tests therefore use the account transfer domain and an account REST endpoint. Variant 1's archive behavior does not exist in this codebase; the service tests cover its existing simulated-failure branch, and the JaCoCo exercise covers the new blocked-source-account rule.

## Clean verification

Command: `./mvnw clean verify`

```text
[INFO] Tests run: 8, Failures: 0, Errors: 0, Skipped: 0 -- AccountServiceTest
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0 -- AccountControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0 -- AccountRepositoryTest
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0 -- AccountApiIntegrationTest
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

The aggregate includes the existing application-context and transaction tests. `AccountApiIntegrationTest` uses `@SpringBootTest(webEnvironment = RANDOM_PORT)`, `@AutoConfigureRestTestClient`, and `RestTestClient` to send actual HTTP requests to the embedded server. Tomcat bound to random port 52743 in the recorded run. The test logs into the application, obtains and sends CSRF tokens, posts a transfer, and reads back the persisted balance.

The test starts `postgres:17-alpine` through `@Container` and `@ServiceConnection`. Flyway validates and applies all three migrations to the PostgreSQL 17.12 container. The transfer of 125.00 leaves Alice's account with 875.00, which is checked through the HTTP API.

`AccountRepositoryTest` is a separate `@DataJpaTest` slice using the embedded test database. It uses `TestEntityManager.flush()` and `clear()` before checking both positive and negative repository results.

## JaCoCo coverage in `kz.iitu.spring_lab_1.service`

| Measurement | Line coverage | Branch coverage |
|---|---:|---:|
| Before `rejectsTransfersFromBlockedAccounts` | 20/21 (95%) | 3/4 (75%) |
| After `rejectsTransfersFromBlockedAccounts` | 21/21 (100%) | 4/4 (100%) |

### Yellow line changed to green

JaCoCo class view: `AccountService.java`, line 30.

```text
if ("BLOCKED".equals(from.getStatus())) {
Before: yellow — 1 of 2 branches covered; 1 missed.
Test added: rejectsTransfersFromBlockedAccounts()
After: green — 2 of 2 branches covered; 0 missed.
```

The added unit test is `AccountServiceTest.rejectsTransfersFromBlockedAccounts()`. The report includes this source-line excerpt and the before/after values, as permitted by the handout in place of a JaCoCo screenshot.

## Remaining uncovered branch

`Account.debit`, line 50, still has an uncovered path for an amount less than or equal to zero. It should receive a test because rejecting non-positive transfers protects account balances from invalid operations.
