package fit.tatakae.infrastructure.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.dto.ErrorResponse;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtValidator;
import fit.tatakae.infrastructure.web.security.jwt.InvalidAppleJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AppleJwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AppleJwtAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_QUERY_PARAM = "access_token";
    private static final String EVENTS_PATH_PREFIX = "/api/v1/events";

    private final AppleJwtValidator appleJwtValidator;
    private final FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase;
    private final ObjectMapper objectMapper;

    public AppleJwtAuthenticationFilter(AppleJwtValidator appleJwtValidator,
                                        FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase,
                                        ObjectMapper objectMapper) {
        this.appleJwtValidator = appleJwtValidator;
        this.findOrCreateUserByAppleSubUseCase = findOrCreateUserByAppleSubUseCase;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            try {
                AppleJwtClaims claims = appleJwtValidator.validate(token);
                User user = findOrCreateUserByAppleSubUseCase.execute(claims.sub());

                AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getUserId(), claims.sub());
                SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

                logger.debug("Authenticated user: {} (Apple sub: {})", user.getUserId(), claims.sub());

            } catch (InvalidAppleJwtException e) {
                logger.warn("Invalid Apple JWT: {}", e.getMessage());
                sendUnauthorizedResponse(response, request.getRequestURI(),
                    "Invalid or expired authentication token: " + e.getMessage());
                return;
            } catch (Exception e) {
                logger.error("Error during authentication", e);
                sendUnauthorizedResponse(response, request.getRequestURI(),
                    "Authentication failed");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Prefer Authorization: Bearer. For EventSource clients that cannot set headers,
     * allow ?access_token= on the SSE endpoint only (avoids leaking tokens on other URLs via logs/referrers).
     */
    String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        if (isEventsPath(request)) {
            String accessToken = request.getParameter(ACCESS_TOKEN_QUERY_PARAM);
            if (accessToken != null && !accessToken.isBlank()) {
                return accessToken;
            }
        }
        return null;
    }

    private boolean isEventsPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        if (path == null) {
            return false;
        }
        return path.equals(EVENTS_PATH_PREFIX) || path.startsWith(EVENTS_PATH_PREFIX + "/");
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String path, String message)
            throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.of(
                message,
                "UNAUTHORIZED",
                HttpStatus.UNAUTHORIZED.value(),
                path
        );

        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            logger.error("Failed to serialize ErrorResponse, falling back to minimal JSON", e);
            String fallbackJson = String.format(
                "{\"message\":\"%s\",\"code\":\"UNAUTHORIZED\",\"status\":401,\"path\":\"%s\"}",
                escapeJson(message),
                escapeJson(path)
            );
            response.getWriter().write(fallbackJson);
        }
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
}
