package fit.tatakae.infrastructure.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityContextHelper {

    private SecurityContextHelper() {
    }

    public static String getAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.getUserId();
        }
        throw new IllegalStateException("No authenticated user found in security context");
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication instanceof AuthenticatedUser && authentication.isAuthenticated();
    }
}
