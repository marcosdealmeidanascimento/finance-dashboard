package com.finances.dashboard;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * End-to-end tests over HTTP against a real Postgres (Testcontainers). Every test creates its own users,
 * so tests are independent and can run in any order.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestcontainersConfiguration.class)
class ApiIntegrationTest {

    private static final String PASSWORD = "123456";
    private static final AtomicInteger SEQ = new AtomicInteger();

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private ObjectMapper mapper;

    // ---------------------------------------------------------------- helpers

    record Res(int status, JsonNode body) {
        String message() {
            return body == null ? null : body.path("message").asText(null);
        }
    }

    record Session(long id, String email, String accessToken, String refreshToken) {
    }

    /** Map that allows null values (Map.of doesn't), for JSON bodies. */
    private static Map<String, Object> json(Object... keyValues) {
        Map<String, Object> map = new LinkedHashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private Res call(HttpMethod method, String path, String token, Object body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (token != null) {
            headers.setBearerAuth(token);
        }
        // A String body is sent as-is (used to post malformed JSON).
        Object payload = body instanceof String raw ? raw : body == null ? null : toJson(body);
        ResponseEntity<String> response = rest.exchange("/api" + path, method, new HttpEntity<>(payload, headers),
                String.class);
        return new Res(response.getStatusCode().value(), parse(response.getBody()));
    }

    private Res get(String path, String token) {
        return call(HttpMethod.GET, path, token, null);
    }

    private Res post(String path, String token, Object body) {
        return call(HttpMethod.POST, path, token, body);
    }

    private Res put(String path, String token, Object body) {
        return call(HttpMethod.PUT, path, token, body);
    }

    private Res delete(String path, String token) {
        return call(HttpMethod.DELETE, path, token, null);
    }

    private String toJson(Object body) {
        try {
            return mapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    private JsonNode parse(String body) {
        try {
            return body == null || body.isBlank() ? null : mapper.readTree(body);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private static String newEmail() {
        return "user" + SEQ.incrementAndGet() + "-" + System.nanoTime() + "@example.com";
    }

    private Res signUp(String email, String password) {
        return post("/users", null, json("name", "Test User", "email", email, "password", password));
    }

    private Res login(String email, String password) {
        return post("/auth/login", null, json("email", email, "password", password));
    }

    private Session newSession() {
        String email = newEmail();
        assertThat(signUp(email, PASSWORD).status()).isEqualTo(200);
        Res login = login(email, PASSWORD);
        assertThat(login.status()).isEqualTo(200);
        String access = login.body().get("accessToken").asText();
        long id = get("/users/me", access).body().get("id").asLong();
        return new Session(id, email, access, login.body().get("refreshToken").asText());
    }

    private static Map<String, Object> payment(String description, double amount, String dueDate, boolean recurring) {
        return json("description", description, "amount", amount, "dueDate", dueDate, "recurring", recurring,
                "method", "PIX");
    }

    private long createPayment(Session session, Map<String, Object> body) {
        Res res = post("/payment", session.accessToken(), body);
        assertThat(res.status()).isEqualTo(200);
        return res.body().get("id").asLong();
    }

    private long createIncome(Session session) {
        Res res = post("/incomes", session.accessToken(),
                json("description", "Salário", "amount", 5000, "receivedDate", "2030-01-05"));
        assertThat(res.status()).isEqualTo(200);
        return res.body().get("id").asLong();
    }

    // ---------------------------------------------------------------- tests

    @Nested
    class Authentication {

        @Test
        void emailIsCaseInsensitiveAndUniqueAmongActiveUsers() {
            String email = "Mixed.Case" + SEQ.incrementAndGet() + "@Example.com";
            assertThat(signUp(email, PASSWORD).status()).isEqualTo(200);

            assertThat(login(email.toLowerCase(), PASSWORD).status()).isEqualTo(200);
            Res duplicate = signUp(email.toUpperCase(), PASSWORD);
            assertThat(duplicate.status()).isEqualTo(409);
            assertThat(duplicate.message()).isEqualTo("Este e-mail já está em uso.");
        }

        @Test
        void wrongPasswordAndUnknownEmailLookTheSame() {
            Session session = newSession();
            Res wrongPassword = login(session.email(), "wrong-password");
            Res unknownEmail = login(newEmail(), PASSWORD);

            assertThat(wrongPassword.status()).isEqualTo(401);
            assertThat(unknownEmail.status()).isEqualTo(401);
            assertThat(wrongPassword.message()).isEqualTo(unknownEmail.message()).isEqualTo("E-mail ou senha inválidos.");
        }

        @Test
        void refreshTokenIsRotatedOnEveryUse() {
            Session session = newSession();

            Res first = post("/auth/refresh", null, json("refreshToken", session.refreshToken()));
            assertThat(first.status()).isEqualTo(200);
            String rotated = first.body().get("refreshToken").asText();
            assertThat(rotated).isNotEqualTo(session.refreshToken());
            assertThat(get("/users/me", first.body().get("accessToken").asText()).status()).isEqualTo(200);

            // The old value stops working; the new one works.
            assertThat(post("/auth/refresh", null, json("refreshToken", session.refreshToken())).status()).isEqualTo(401);
            assertThat(post("/auth/refresh", null, json("refreshToken", rotated)).status()).isEqualTo(200);
        }

        @Test
        void logoutRevokesTheRefreshToken() {
            Session session = newSession();
            assertThat(post("/auth/logout", session.accessToken(), null).status()).isEqualTo(204);
            assertThat(post("/auth/refresh", null, json("refreshToken", session.refreshToken())).status()).isEqualTo(401);
        }

        @Test
        void deletedAccountLosesAllAccessAndFreesItsEmail() {
            Session session = newSession();
            assertThat(delete("/users/" + session.id(), session.accessToken()).status()).isEqualTo(204);

            assertThat(get("/users/me", session.accessToken()).status()).isEqualTo(401);
            assertThat(login(session.email(), PASSWORD).status()).isEqualTo(401);
            assertThat(post("/auth/refresh", null, json("refreshToken", session.refreshToken())).status()).isEqualTo(401);
            assertThat(signUp(session.email(), "another-password").status()).isEqualTo(200);
        }

        @Test
        void repeatedFailedLoginsAreRateLimited() {
            Session session = newSession();
            for (int i = 0; i < 5; i++) {
                assertThat(login(session.email(), "wrong-password").status()).isEqualTo(401);
            }

            // Even the right password is refused until the window passes.
            Res blocked = login(session.email(), PASSWORD);
            assertThat(blocked.status()).isEqualTo(429);
            assertThat(blocked.message()).startsWith("Muitas tentativas de login.");
        }

        @Test
        void successfulLoginResetsTheFailureCount() {
            Session session = newSession();
            for (int i = 0; i < 4; i++) {
                login(session.email(), "wrong-password");
            }
            assertThat(login(session.email(), PASSWORD).status()).isEqualTo(200);
            for (int i = 0; i < 4; i++) {
                login(session.email(), "wrong-password");
            }
            assertThat(login(session.email(), PASSWORD).status()).isEqualTo(200);
        }

        @Test
        void protectedRoutesRequireAToken() {
            assertThat(get("/users/me", null).status()).isEqualTo(401);
            assertThat(get("/users/me", "not-a-jwt").status()).isEqualTo(401);
        }
    }

    @Nested
    class Validation {

        @Test
        void invalidPaymentIsRejectedWithEveryReason() {
            Session session = newSession();
            Res res = post("/payment", session.accessToken(), json("description", " ", "amount", -5));

            assertThat(res.status()).isEqualTo(400);
            assertThat(res.message()).contains("Informe a descrição.", "O valor deve ser maior que zero.",
                    "Informe a data de vencimento.", "Informe a forma de pagamento.");
        }

        @Test
        void invalidSignUpIsRejected() {
            Res res = signUp("not-an-email", "123");
            assertThat(res.status()).isEqualTo(400);
            assertThat(res.message()).contains("E-mail inválido.", "A senha deve ter entre 6 e 72 caracteres.");
        }

        @Test
        void incomeAmountMustBePositive() {
            Session session = newSession();
            Res res = post("/incomes", session.accessToken(),
                    json("description", "x", "amount", 0, "receivedDate", "2030-01-01"));
            assertThat(res.status()).isEqualTo(400);
        }

        @Test
        void dateRangesAreRequiredAndOrdered() {
            Session session = newSession();
            assertThat(get("/summary", session.accessToken()).status()).isEqualTo(400);
            assertThat(get("/incomes?startDate=2030-01-31&endDate=2030-01-01", session.accessToken()).status())
                    .isEqualTo(400);
            assertThat(get("/summary?startDate=2030-01-01&endDate=2030-01-31", session.accessToken()).status())
                    .isEqualTo(200);
        }

        @Test
        void malformedRequestsGetProperClientErrors() {
            Session session = newSession();
            assertThat(post("/auth/login", null, "{not json").status()).isEqualTo(400);
            assertThat(get("/does-not-exist", session.accessToken()).status()).isEqualTo(404);
            assertThat(get("/payment/abc", session.accessToken()).status()).isEqualTo(400);
            assertThat(get("/users", session.accessToken()).status()).isEqualTo(405);
        }

        @Test
        void profilePictureMustBeAnImage() {
            Session session = newSession();
            HttpHeaders partHeaders = new HttpHeaders();
            partHeaders.setContentType(MediaType.TEXT_PLAIN);
            ByteArrayResource file = new ByteArrayResource("not an image".getBytes()) {
                @Override
                public String getFilename() {
                    return "notes.txt";
                }
            };
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("file", new HttpEntity<>(file, partHeaders));

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(session.accessToken());
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            ResponseEntity<String> response = rest.exchange("/api/users/profile-picture/" + session.id(),
                    HttpMethod.PUT, new HttpEntity<>(parts, headers), String.class);

            assertThat(response.getStatusCode().value()).isEqualTo(400);
            assertThat(parse(response.getBody()).path("message").asText()).isEqualTo("O arquivo precisa ser uma imagem.");
        }
    }

    @Nested
    class Ownership {

        @Test
        void anotherUsersPaymentIsInvisibleAndUntouchable() {
            Session owner = newSession();
            Session intruder = newSession();
            long id = createPayment(owner, payment("Aluguel", 1000, "2030-01-10", false));

            assertThat(get("/payment/" + id, intruder.accessToken()).status()).isEqualTo(404);
            assertThat(put("/payment/" + id, intruder.accessToken(), payment("hack", 1, "2030-01-10", false)).status())
                    .isEqualTo(404);
            assertThat(post("/payment/mark-paid/" + id, intruder.accessToken(), null).status()).isEqualTo(404);
            assertThat(post("/payment/mark-cancelled/" + id, intruder.accessToken(), null).status()).isEqualTo(404);
            assertThat(delete("/payment/" + id, intruder.accessToken()).status()).isEqualTo(404);

            Res mine = get("/payment/" + id, owner.accessToken());
            assertThat(mine.status()).isEqualTo(200);
            assertThat(mine.body().get("description").asText()).isEqualTo("Aluguel");
            assertThat(mine.body().get("status").asText()).isEqualTo("PENDING");
        }

        @Test
        void anotherUsersIncomeIsInvisibleAndUntouchable() {
            Session owner = newSession();
            Session intruder = newSession();
            long id = createIncome(owner);

            assertThat(get("/incomes/" + id, intruder.accessToken()).status()).isEqualTo(404);
            assertThat(put("/incomes/" + id, intruder.accessToken(),
                    json("description", "hack", "amount", 1, "receivedDate", "2030-01-05")).status()).isEqualTo(404);
            assertThat(delete("/incomes/" + id, intruder.accessToken()).status()).isEqualTo(404);
            assertThat(get("/incomes/" + id, owner.accessToken()).body().get("description").asText()).isEqualTo("Salário");
        }

        @Test
        void listsOnlyContainTheCallersRecords() {
            Session owner = newSession();
            Session other = newSession();
            createPayment(owner, payment("Só do dono", 10, "2030-01-10", false));

            assertThat(get("/payment", other.accessToken()).body().get("totalElements").asInt()).isZero();
            assertThat(get("/payment", owner.accessToken()).body().get("totalElements").asInt()).isEqualTo(1);
        }

        @Test
        void usersCanOnlyChangeTheirOwnAccount() {
            Session alice = newSession();
            Session bob = newSession();
            Map<String, Object> update = json("name", "hack", "email", newEmail(), "password", null);

            assertThat(put("/users/" + alice.id(), bob.accessToken(), update).status()).isEqualTo(403);
            assertThat(delete("/users/" + alice.id(), bob.accessToken()).status()).isEqualTo(403);
            assertThat(put("/users/" + bob.id(), bob.accessToken(),
                    json("name", "Bob", "email", alice.email(), "password", null)).status()).isEqualTo(409);
        }
    }

    @Nested
    class PaymentStatusRules {

        @Test
        void paidPaymentIsFinalAndRecurringOnesRollOver() {
            Session session = newSession();
            String token = session.accessToken();
            long id = createPayment(session, payment("Internet", 100, "2030-01-15", true));

            assertThat(post("/payment/mark-paid/" + id, token, null).status()).isEqualTo(200);
            JsonNode pending = get("/payment?status=PENDING&size=50", token).body().get("content");
            assertThat(pending).hasSize(1);
            assertThat(pending.get(0).get("dueDate").asText()).isEqualTo("2030-02-15");

            assertThat(post("/payment/mark-paid/" + id, token, null).status()).isEqualTo(409);
            assertThat(put("/payment/" + id, token, payment("x", 1, "2030-01-15", false)).status()).isEqualTo(409);
            assertThat(post("/payment/mark-cancelled/" + id, token, null).status()).isEqualTo(409);
            Res deletePaid = delete("/payment/" + id, token);
            assertThat(deletePaid.status()).isEqualTo(409);
            assertThat(deletePaid.message()).isEqualTo("Pagamentos já pagos não podem ser excluídos.");
        }

        @Test
        void cancelledPaymentCannotBePaidButCanBeDeleted() {
            Session session = newSession();
            String token = session.accessToken();
            long id = createPayment(session, payment("Luz", 200, "2030-01-20", false));

            assertThat(post("/payment/mark-cancelled/" + id, token, null).status()).isEqualTo(200);
            assertThat(post("/payment/mark-paid/" + id, token, null).status()).isEqualTo(409);
            assertThat(delete("/payment/" + id, token).status()).isEqualTo(204);
            assertThat(get("/payment/" + id, token).status()).isEqualTo(404);
        }

        @Test
        void pendingPaymentCanBeEdited() {
            Session session = newSession();
            long id = createPayment(session, payment("Água", 80, "2030-01-25", false));

            Res edited = put("/payment/" + id, session.accessToken(), payment("Água e esgoto", 95.5, "2030-01-26", false));
            assertThat(edited.status()).isEqualTo(200);
            assertThat(edited.body().get("description").asText()).isEqualTo("Água e esgoto");
            assertThat(edited.body().get("amount").decimalValue()).isEqualByComparingTo("95.5");
        }

        @Test
        void summaryAddsUpTheMonth() {
            Session session = newSession();
            createIncome(session); // 5000 on 2030-01-05
            createPayment(session, payment("Aluguel", 1200, "2030-01-10", false));
            createPayment(session, payment("Fora do mês", 999, "2030-02-10", false));

            JsonNode summary = get("/summary?startDate=2030-01-01&endDate=2030-01-31", session.accessToken()).body();
            assertThat(summary.get("totalIncome").decimalValue()).isEqualByComparingTo("5000");
            assertThat(summary.get("totalPayments").decimalValue()).isEqualByComparingTo("1200");
            assertThat(summary.get("balance").decimalValue()).isEqualByComparingTo("3800");
        }
    }

    @Nested
    class Cors {

        private ResponseEntity<String> preflight(String origin) {
            HttpHeaders headers = new HttpHeaders();
            headers.setOrigin(origin);
            headers.setAccessControlRequestMethod(HttpMethod.GET);
            return rest.exchange("/api/summary", HttpMethod.OPTIONS, new HttpEntity<>(headers), String.class);
        }

        @Test
        void onlyConfiguredOriginsAreAllowed() {
            assertThat(preflight("http://localhost:8081").getHeaders().getAccessControlAllowOrigin())
                    .isEqualTo("http://localhost:8081");
            assertThat(preflight("http://evil.example").getStatusCode().value()).isEqualTo(403);
        }
    }
}
