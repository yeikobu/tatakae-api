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

    private final AppleJwtValidator appleJwtValidator;
    private final FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AppleJwtAuthenticationFilter(AppleJwtValidator appleJwtValidator,
                                        FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase) {
        this.appleJwtValidator = appleJwtValidator;
        this.findOrCreateUserByAppleSubUseCase = findOrCreateUserByAppleSubUseCase;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String token = authorizationHeader.substring(BEARER_PREFIX.length());

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

        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
