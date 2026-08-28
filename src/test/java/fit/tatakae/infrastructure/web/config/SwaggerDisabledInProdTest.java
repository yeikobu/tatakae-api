package fit.tatakae.infrastructure.web.config;

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

// Ciberseguridad: outside development the contract must not be reachable at all.
@SpringBootTest(properties = "spring.jpa.hibernate.ddl-auto=update")
@AutoConfigureMockMvc
@ActiveProfiles("prod")
public class SwaggerDisabledInProdTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldHideTheOpenApiSpecificationInProduction() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api-docs")).andExpect(status().isNotFound());
    }

    @Test
    public void shouldHideTheSwaggerConsoleInProduction() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isNotFound());
        mockMvc.perform(get("/swagger-ui.html")).andExpect(status().isNotFound());
    }

    @Test
    public void shouldAnswerNotFoundOnAnUnmappedRouteInsteadOfAServerError() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api/v1/nothing-here"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ENDPOINT_NOT_FOUND"));
    }
}
