package fit.tatakae.infrastructure.web;

import fit.tatakae.application.port.UserEventPublisher;
import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.infrastructure.sse.SseHub;
import fit.tatakae.infrastructure.storage.AvatarFileStorage;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtValidator;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.nio.file.Path;

/**
 * Beans that {@code @WebMvcTest} slices need after avatar + SSE were added.
 * Stubbing happens at {@code @Bean} creation time (before MVC resource handlers init).
 */
@TestConfiguration
public class WebMvcSliceTestConfiguration {

    @Bean
    @Primary
    AvatarFileStorage avatarFileStorage() {
        AvatarFileStorage storage = Mockito.mock(AvatarFileStorage.class);
        Mockito.when(storage.storageDirectory())
                .thenReturn(Path.of(System.getProperty("java.io.tmpdir"), "tatakae-avatars-webmvc-test"));
        return storage;
    }

    @Bean
    @Primary
    UserEventPublisher userEventPublisher() {
        return Mockito.mock(UserEventPublisher.class);
    }

    @Bean
    @Primary
    SseHub sseHub() {
        return Mockito.mock(SseHub.class);
    }

    @Bean
    @Primary
    AppleJwtValidator appleJwtValidator() {
        return Mockito.mock(AppleJwtValidator.class);
    }

    @Bean
    @Primary
    FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase() {
        return Mockito.mock(FindOrCreateUserByAppleSubUseCase.class);
    }
}
