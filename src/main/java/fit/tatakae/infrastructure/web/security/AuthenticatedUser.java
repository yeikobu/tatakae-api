package fit.tatakae.infrastructure.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class AuthenticatedUser implements Authentication {

    private final String userId;
    private final String appleSub;
    private boolean authenticated = true;

    public AuthenticatedUser(String userId, String appleSub) {
        this.userId = userId;
        this.appleSub = appleSub;
    }

    public String getUserId() {
        return userId;
    }

    public String getAppleSub() {
        return appleSub;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return userId;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return userId;
    }
}
