package fit.tatakae.infrastructure.push;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.apns")
public class ApnsProperties {

    private String teamId = "";
    private String keyId = "";
    private String privateKey = "";
    private String privateKeyPath = "";
    private String sandboxKeyId = "";
    private String sandboxPrivateKey = "";
    private String sandboxPrivateKeyPath = "";
    private String bundleId = "com.aguilarjacob.tatakae";
    private String sandboxBundleId = "com.aguilarjacob.tatakae.dev";

    public boolean isConfigured() {
        return isProductionConfigured() || isSandboxConfigured();
    }

    public boolean isProductionConfigured() {
        return hasKey(keyId, privateKey, privateKeyPath);
    }

    public boolean isSandboxConfigured() {
        return hasKey(sandboxKeyId, sandboxPrivateKey, sandboxPrivateKeyPath);
    }

    private boolean hasKey(String keyId, String privateKey, String privateKeyPath) {
        return !teamId.isBlank() && !keyId.isBlank() && (!privateKey.isBlank() || !privateKeyPath.isBlank());
    }

    public String bundleIdFor(boolean sandbox) {
        return sandbox ? sandboxBundleId : bundleId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getPrivateKeyPath() {
        return privateKeyPath;
    }

    public void setPrivateKeyPath(String privateKeyPath) {
        this.privateKeyPath = privateKeyPath;
    }

    public String getSandboxKeyId() {
        return sandboxKeyId;
    }

    public void setSandboxKeyId(String sandboxKeyId) {
        this.sandboxKeyId = sandboxKeyId;
    }

    public String getSandboxPrivateKey() {
        return sandboxPrivateKey;
    }

    public void setSandboxPrivateKey(String sandboxPrivateKey) {
        this.sandboxPrivateKey = sandboxPrivateKey;
    }

    public String getSandboxPrivateKeyPath() {
        return sandboxPrivateKeyPath;
    }

    public void setSandboxPrivateKeyPath(String sandboxPrivateKeyPath) {
        this.sandboxPrivateKeyPath = sandboxPrivateKeyPath;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getSandboxBundleId() {
        return sandboxBundleId;
    }

    public void setSandboxBundleId(String sandboxBundleId) {
        this.sandboxBundleId = sandboxBundleId;
    }
}
