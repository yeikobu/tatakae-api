package fit.tatakae.domain.entity;

import fit.tatakae.domain.valueobject.UserId;
import fit.tatakae.domain.valueobject.Username;

public class User {
    private final UserId userId;
    private final Username username;
    private final String country;
    private final PrivacyLevel privacyLevel;
    private final Gender gender;
    private final String appleSub;
    private final String avatarUrl;

    // Reconstitution constructor: used when the athlete identity already exists (e.g. loaded from a repository).
    public User(String userId, String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        this(UserId.of(userId), new Username(username), country, privacyLevel, gender, null, null);
    }

    public User(String userId, String username, String country, PrivacyLevel privacyLevel, Gender gender, String appleSub) {
        this(UserId.of(userId), new Username(username), country, privacyLevel, gender, appleSub, null);
    }

    public User(String userId, String username, String country, PrivacyLevel privacyLevel, Gender gender,
                String appleSub, String avatarUrl) {
        this(UserId.of(userId), new Username(username), country, privacyLevel, gender, appleSub, avatarUrl);
    }

    private User(UserId userId, Username username, String country, PrivacyLevel privacyLevel, Gender gender,
                 String appleSub, String avatarUrl) {
        this.userId = userId;
        this.username = username;
        this.country = country;
        this.privacyLevel = privacyLevel;
        this.gender = gender;
        this.appleSub = appleSub;
        this.avatarUrl = avatarUrl;
    }

    // A brand new athlete: the identity is minted here, never accepted from the outside.
    public static User register(String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        return new User(UserId.generate(), new Username(username), country, privacyLevel, gender, null, null);
    }

    // Register an athlete via Apple Sign In: the identity is minted here, and linked to the Apple sub.
    public static User registerWithApple(String appleSub, String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        return new User(UserId.generate(), new Username(username), country, privacyLevel, gender, appleSub, null);
    }

    // Profile changes, handle included, never touch the identity.
    public User updatedTo(String username, String country, PrivacyLevel privacyLevel, Gender gender) {
        return new User(this.userId, new Username(username), country, privacyLevel, gender, this.appleSub, this.avatarUrl);
    }

    public User withAvatarUrl(String avatarUrl) {
        return new User(this.userId, this.username, this.country, this.privacyLevel, this.gender, this.appleSub, avatarUrl);
    }

    public User withoutAvatar() {
        return withAvatarUrl(null);
    }

    public String getUserId() {
        return this.userId.asString();
    }

    public String getUsername() {
        return this.username.value();
    }

    public String getCountry() {
        return this.country;
    }

    public PrivacyLevel getPrivacyLevel() {
        return this.privacyLevel;
    }

    public Gender getGender() {
        return this.gender;
    }

    public boolean isPublic() {
        return this.privacyLevel == PrivacyLevel.PUBLIC;
    }

    public boolean isFromCountry(String otherCountry) {
        return this.country.equals(otherCountry);
    }

    public boolean hasGender(Gender otherGender) {
        return this.gender == otherGender;
    }

    public boolean hasUsername(String otherUsername) {
        return this.username.equals(new Username(otherUsername));
    }

    public String getAppleSub() {
        return this.appleSub;
    }

    public String getAvatarUrl() {
        return this.avatarUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof User other)) {
            return false;
        }
        return this.userId.equals(other.userId);
    }

    @Override
    public int hashCode() {
        return this.userId.hashCode();
    }
}
