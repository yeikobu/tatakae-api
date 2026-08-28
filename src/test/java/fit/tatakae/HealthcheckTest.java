package fit.tatakae;

import fit.tatakae.infrastructure.persistence.adapter.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// The probe has to reach the database, otherwise it is a hardcoded "UP" that lies the moment
// PostgreSQL goes away.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class HealthcheckTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldReportTheServiceAsUp() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void shouldIncludeTheDatabaseAsACheckedComponent() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/healthcheck"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.db.status").value("UP"))
                .andExpect(jsonPath("$.components.db.details.database").value("PostgreSQL"));
    }

    // Actuator is mapped at /healthcheck on purpose: no other management endpoint is exposed.
    @Test
    public void shouldNotExposeAnyOtherManagementEndpoint() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/env")).andExpect(status().isNotFound());
        mockMvc.perform(get("/beans")).andExpect(status().isNotFound());
        mockMvc.perform(get("/actuator/health")).andExpect(status().isNotFound());
    }
}
