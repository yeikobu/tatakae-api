package fit.tatakae.domain.repository;

public interface AvatarStorage {
    String store(String userId, byte[] content, String contentType);

    void deleteForUser(String userId);
}
