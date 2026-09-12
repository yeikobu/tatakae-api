package fit.tatakae.infrastructure.web.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import fit.tatakae.TestUsers;
import fit.tatakae.application.usecase.FindOrCreateUserByAppleSubUseCase;
import fit.tatakae.domain.entity.Gender;
import fit.tatakae.domain.entity.PrivacyLevel;
import fit.tatakae.domain.entity.User;
import fit.tatakae.infrastructure.web.security.jwt.AppleJwtValidator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private AppleJwtValidator appleJwtValidator;

    @Mock
    private FindOrCreateUserByAppleSubUseCase findOrCreateUserByAppleSubUseCase;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private AppleJwtAuthenticationFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new AppleJwtAuthenticationFilter(appleJwtValidator, findOrCreateUserByAppleSubUseCase, objectMapper);
    }

    @Test
    public void shouldReturn401WhenBearerTokenIsInvalid() throws Exception {
        // Arrange
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(request.getRequestURI()).thenReturn("/api/v1/users/test");

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        when(appleJwtValidator.validate(invalidToken)).thenThrow(
                new fit.tatakae.infrastructure.web.security.jwt.InvalidAppleJwtException("Invalid JWT format"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);
        
        String responseBody = writer.toString();
        assertTrue(responseBody.contains("UNAUTHORIZED"));
        assertTrue(responseBody.contains("Invalid or expired authentication token"));
    }

    @Test
    public void shouldReturn401WhenBearerTokenIsExpired() throws Exception {
        // Arrange
        String expiredToken = "expired.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(request.getRequestURI()).thenReturn("/api/v1/users/test");

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        when(appleJwtValidator.validate(expiredToken)).thenThrow(
                new fit.tatakae.infrastructure.web.security.jwt.InvalidAppleJwtException("Token has expired"));

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        
        String responseBody = writer.toString();
        assertTrue(responseBody.contains("UNAUTHORIZED"));
        assertTrue(responseBody.contains("expired"));
    }

    @Test
    public void shouldContinueFilterChainWhenNoBearerToken() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/users/test");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void shouldAuthenticateViaAccessTokenQueryOnEventsEndpoint() throws Exception {
        // Arrange
        String token = "query.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getParameter("access_token")).thenReturn(token);

        fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims claims =
                new fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims("apple-sub-1", "a@b.c");
        when(appleJwtValidator.validate(token)).thenReturn(claims);

        User user = TestUsers.user("jacob", "cl", PrivacyLevel.PUBLIC, Gender.MALE);
        when(findOrCreateUserByAppleSubUseCase.execute("apple-sub-1")).thenReturn(user);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        assertTrue(org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() instanceof AuthenticatedUser);
        AuthenticatedUser authenticated = (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        assertTrue(authenticated.getUserId().equals(user.getUserId()));
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldIgnoreAccessTokenQueryOnNonEventsEndpoints() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/users/me");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(appleJwtValidator, never()).validate(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void shouldPreferBearerHeaderOverAccessTokenQuery() {
        // Arrange — when Bearer is present, path/query must not matter
        when(request.getHeader("Authorization")).thenReturn("Bearer bearer.jwt.token");

        // Act / Assert
        assertTrue("bearer.jwt.token".equals(filter.extractToken(request)));
    }

    @Test
    public void shouldAuthenticateViaAccessTokenOnEventsSubpath() throws Exception {
        // Arrange
        String token = "subpath.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/events/");
        when(request.getParameter("access_token")).thenReturn(token);

        fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims claims =
                new fit.tatakae.infrastructure.web.security.jwt.AppleJwtClaims("apple-sub-3", null);
        when(appleJwtValidator.validate(token)).thenReturn(claims);
        User user = TestUsers.user("cara", "cl", PrivacyLevel.PUBLIC, Gender.FEMALE);
        when(findOrCreateUserByAppleSubUseCase.execute("apple-sub-3")).thenReturn(user);

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldIgnoreBlankAccessTokenOnEventsEndpoint() throws Exception {
        // Arrange
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getParameter("access_token")).thenReturn("   ");

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(appleJwtValidator, never()).validate(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void extractTokenReturnsNullWhenEventsPathUriIsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn(null);
        assertTrue(filter.extractToken(request) == null);
    }
}
