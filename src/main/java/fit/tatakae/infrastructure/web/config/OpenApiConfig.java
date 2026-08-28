package fit.tatakae.infrastructure.web.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

// Documentation only exists in development: the production profile never builds this bean.
@Configuration
@Profile("dev")
public class OpenApiConfig {

    @Bean
    public OpenAPI tatakaeOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Tatakae API")
                        .version("1.0.0")
                        .description("Friendships, training sessions and global, local and friends leaderboards "
                                + "for Tatakae, the calisthenics app that counts repetitions with on device AI.")
                        .contact(new Contact().name("Jacob Aguilar").url("https://tatakae.fit"))
                        .license(new License().name("Academic project, Desafio Latam")));
    }
}
