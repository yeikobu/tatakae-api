package fit.tatakae.application.usecase;

import fit.tatakae.domain.entity.User;
import fit.tatakae.domain.exception.ResourceNotFoundException;
import fit.tatakae.domain.repository.AvatarStorage;
import fit.tatakae.domain.repository.UserRepository;
import fit.tatakae.domain.valueobject.UserId;

public class DeleteAvatarUseCase {
    private final UserRepository userRepository;
    private final AvatarStorage avatarStorage;

    public DeleteAvatarUseCase(UserRepository userRepository, AvatarStorage avatarStorage) {
        this.userRepository = userRepository;
        this.avatarStorage = avatarStorage;
    }

    public User execute(String userId) {
        String identity = UserId.of(userId).asString();
        User stored = userRepository.findById(identity)
                .orElseThrow(() -> new ResourceNotFoundException("User " + identity + " was not found"));
        avatarStorage.deleteForUser(identity);
        return userRepository.save(stored.withoutAvatar());
    }
}
