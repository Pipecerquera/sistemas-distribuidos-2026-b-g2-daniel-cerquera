package co.edu.corhuila.barbersaas.identityauth.app;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.corhuila.barbersaas.identityauth.adapter.out.security.Rs256TokenIssuer;
import co.edu.corhuila.barbersaas.identityauth.domain.model.Role;
import co.edu.corhuila.barbersaas.identityauth.domain.model.User;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Negative test of barber-saas-docs #156 (STRIDE S, TB1): an access token signed by the
 * service's own key, but issued 16 minutes ago with a 15-minute lifetime, must not open
 * GET /api/v1/auth/me. A copied token stops working once its lifetime ends.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ExpiredAccessTokenHttpTest {

    private static final Duration ACCESS_TOKEN_LIFETIME = Duration.ofMinutes(15);

    @Autowired
    private MockMvc http;

    @Autowired
    private Rs256TokenIssuer issuer;

    @DynamicPropertySource
    static void privateKey(DynamicPropertyRegistry registry) {
        InternalHttpTest.privateKey(registry);
    }

    @Test
    void a_token_issued_16_minutes_ago_with_a_15_minute_lifetime_gets_401_on_me() throws Exception {
        User client = new User(UUID.randomUUID(), null, "Camila", "camila@example.com", "hash", null, null,
                Role.CLIENT, true, Instant.now());
        Instant issued = Instant.now().minus(Duration.ofMinutes(16));
        String expired = issuer.issueBound(client, null, issued, ACCESS_TOKEN_LIFETIME).token();

        http.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + expired))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHORIZED"));
    }

    @Test
    void the_same_token_inside_its_lifetime_is_not_refused_as_expired() throws Exception {
        User client = new User(UUID.randomUUID(), null, "Camila", "camila@example.com", "hash", null, null,
                Role.CLIENT, true, Instant.now());
        String fresh = issuer.issueBound(client, null, Instant.now(), ACCESS_TOKEN_LIFETIME).token();

        // The user does not exist in the database, so the service may answer 404, never 401.
        http.perform(get("/api/v1/auth/me").header("Authorization", "Bearer " + fresh))
                .andExpect(result -> {
                    if (result.getResponse().getStatus() == 401) {
                        throw new AssertionError("a valid token was refused as unauthorized");
                    }
                });
    }
}
