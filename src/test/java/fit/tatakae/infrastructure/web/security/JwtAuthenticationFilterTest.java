package fit.tatakae.infrastructure.web.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
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

    private AppleJwtAuthenticationFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new AppleJwtAuthenticationFilter(appleJwtValidator, findOrCreateUserByAppleSubUseCase);
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

        // Act
        filter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
