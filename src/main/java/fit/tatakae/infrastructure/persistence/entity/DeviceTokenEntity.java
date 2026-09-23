package fit.tatakae.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "device_tokens")
public class DeviceTokenEntity {

    @Id
    @Column(name = "token", nullable = false, length = 64)
    private String token;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "sandbox", nullable = false)
    private boolean sandbox;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DeviceTokenEntity() {
    }

    public DeviceTokenEntity(String token, String userId, boolean sandbox, Instant updatedAt) {
        this.token = token;
        this.userId = userId;
        this.sandbox = sandbox;
        this.updatedAt = updatedAt;
    }

    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean isSandbox() {
        return sandbox;
    }

    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
