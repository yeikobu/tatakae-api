package fit.tatakae.infrastructure.persistence.entity;

import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.valueobject.Username;
import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;

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

    // Default lets ddl-auto:update add the column over athletes registered before gender existed.
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    @ColumnDefault("'MALE'")
    private Gender gender;

    protected UserEntity() {
    }

    public UserEntity(String id, String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        this.id = id;
        this.username = username;
        this.country = country;
        this.privacyLevel = privacyLevel;
        this.gender = gender;
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

    public Gender getGender() {
        return gender;
    }
}
