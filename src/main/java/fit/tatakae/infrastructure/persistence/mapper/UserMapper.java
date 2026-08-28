package fit.tatakae.infrastructure.persistence.mapper;

import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.persistence.entity.UserEntity;

public final class UserMapper {

    private UserMapper() {
    }

    public static User toDomain(UserEntity entity) {
        return new User(entity.getId(), entity.getUsername(), entity.getCountry(), entity.getPrivacyLevel());
    }

    public static UserEntity toEntity(User user) {
        return new UserEntity(user.getUserId(), user.getUsername(), user.getCountry(), user.getPrivacyLevel());
    }
}
