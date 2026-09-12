package fit.tatakae.infrastructure.web.config;

import fit.tatakae.infrastructure.storage.AvatarFileStorage;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AvatarWebConfig implements WebMvcConfigurer {

    private final AvatarFileStorage avatarFileStorage;

    public AvatarWebConfig(AvatarFileStorage avatarFileStorage) {
        this.avatarFileStorage = avatarFileStorage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = avatarFileStorage.storageDirectory().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/avatars/**")
                .addResourceLocations(location);
    }
}
