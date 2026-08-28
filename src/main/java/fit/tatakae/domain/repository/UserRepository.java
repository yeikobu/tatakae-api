package fit.tatakae.domain.repository;

import fit.tatakae.domain.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String userId);
    Optional<User> findByUsername(String username);
    List<User> findAll();
    boolean existsById(String userId);
    User save(User user);
    void delete(String userId);
}
