package fit.tatakae.infrastructure.persistence.entity;

import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.valueobject.Username;
import jakarta.persistence.*;

@Entity
@Table(
        name = "users",
        uniqueConstraints = @UniqueConstraint(name = "uk_users_username", columnNames = "username")
)
public class UserEntity {

    // Opaque and immutable: the handle can change, this cannot.
    @Id
    @Column(name = "id", nullable = false, updatable = false, length = 36)
    private String id;

    @Column(name = "username", nullable = false, length = Username.MAX_LENGTH)
    private String username;

    @Column(name = "country")
    private String country;

    @Enumerated(EnumType.STRING)
    @Column(name = "privacy_level", nullable = false)
    private PrivacyLevel privacyLevel;

    protected UserEntity() {
    }

    public UserEntity(String id, String username, String country, PrivacyLevel privacyLevel) {
        this.id = id;
        this.username = username;
        this.country = country;
        this.privacyLevel = privacyLevel;
    }

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getCountry() {
        return country;
    }

    public PrivacyLevel getPrivacyLevel() {
        return privacyLevel;
    }
}
