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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
public class SwaggerEnabledInDevTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void shouldServeTheOpenApiSpecificationInDevelopment() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("Tatakae API"))
                .andExpect(jsonPath("$.paths['/api/v1/friendships']").exists());
    }

    @Test
    public void shouldServeTheSwaggerConsoleInDevelopment() throws Exception {
        // Act and Assert
        mockMvc.perform(get("/swagger-ui/index.html")).andExpect(status().isOk());
    }
}
