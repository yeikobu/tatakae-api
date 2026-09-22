package fit.tatakae.infrastructure.web.security;

import fit.tatakae.application.port.SessionTokenIssuer;
import fit.tatakae.infrastructure.web.security.jwt.InvalidSessionJwtException;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationFilterTest {

    @Mock
    private SessionTokenIssuer sessionTokenIssuer;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private SessionJwtAuthenticationFilter filter;

    @BeforeEach
    public void setUp() {
        filter = new SessionJwtAuthenticationFilter(sessionTokenIssuer, objectMapper);
    }

    @Test
    public void shouldReturn401WhenBearerTokenIsInvalid() throws Exception {
        String invalidToken = "invalid.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + invalidToken);
        when(request.getRequestURI()).thenReturn("/api/v1/users/test");

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        when(sessionTokenIssuer.validateAccessToken(invalidToken)).thenThrow(
                new InvalidSessionJwtException("Invalid JWT format"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        verify(filterChain, never()).doFilter(request, response);

        String responseBody = writer.toString();
        assertTrue(responseBody.contains("UNAUTHORIZED"));
        assertTrue(responseBody.contains("Invalid or expired authentication token"));
    }

    @Test
    public void shouldReturn401WhenBearerTokenIsExpired() throws Exception {
        String expiredToken = "expired.jwt.token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(request.getRequestURI()).thenReturn("/api/v1/users/test");

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        when(sessionTokenIssuer.validateAccessToken(expiredToken)).thenThrow(
                new InvalidSessionJwtException("Token has expired"));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);

        String responseBody = writer.toString();
        assertTrue(responseBody.contains("UNAUTHORIZED"));
        assertTrue(responseBody.contains("expired"));
    }

    @Test
    public void shouldContinueFilterChainWhenNoBearerToken() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/users/test");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    public void shouldAuthenticateViaAccessTokenQueryOnEventsEndpoint() throws Exception {
        String token = "query.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getParameter("access_token")).thenReturn(token);

        when(sessionTokenIssuer.validateAccessToken(token)).thenReturn("user-123");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
        assertTrue(org.springframework.security.core.context.SecurityContextHolder.getContext()
                .getAuthentication() instanceof AuthenticatedUser);
        AuthenticatedUser authenticated = (AuthenticatedUser) org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        assertTrue(authenticated.getUserId().equals("user-123"));
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldIgnoreAccessTokenQueryOnNonEventsEndpoints() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/users/me");

        filter.doFilterInternal(request, response, filterChain);

        verify(sessionTokenIssuer, never()).validateAccessToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void shouldPreferBearerHeaderOverAccessTokenQuery() {
        when(request.getHeader("Authorization")).thenReturn("Bearer bearer.jwt.token");

        assertTrue("bearer.jwt.token".equals(filter.extractToken(request)));
    }

    @Test
    public void shouldAuthenticateViaAccessTokenOnEventsSubpath() throws Exception {
        String token = "subpath.jwt.token";
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/events/");
        when(request.getParameter("access_token")).thenReturn(token);
        when(sessionTokenIssuer.validateAccessToken(token)).thenReturn("user-cara");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Test
    public void shouldIgnoreBlankAccessTokenOnEventsEndpoint() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getParameter("access_token")).thenReturn("   ");

        filter.doFilterInternal(request, response, filterChain);

        verify(sessionTokenIssuer, never()).validateAccessToken(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    public void extractTokenReturnsNullWhenEventsPathUriIsNull() {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn(null);
        assertTrue(filter.extractToken(request) == null);
    }

    @Test
    public void shouldReturn401WhenAuthenticationThrowsUnexpectedException() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer weird.token");
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(sessionTokenIssuer.validateAccessToken("weird.token")).thenThrow(new RuntimeException("boom"));

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(401);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(writer.toString().contains("Authentication failed") || writer.toString().contains("UNAUTHORIZED"));
    }

    @Test
    public void shouldIgnoreAuthorizationHeaderThatIsNotBearer() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic abc");
        when(request.getRequestURI()).thenReturn("/api/v1/users");

        assertTrue(filter.extractToken(request) == null);
        filter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(sessionTokenIssuer, never()).validateAccessToken(anyString());
    }

    @Test
    public void shouldIgnoreNullAccessTokenOnEventsEndpoint() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/events");
        when(request.getParameter("access_token")).thenReturn(null);

        assertTrue(filter.extractToken(request) == null);
    }

    @Test
    public void shouldWriteUnauthorizedJsonWhenObjectMapperSucceeds() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(request.getRequestURI()).thenReturn("/api/v1/users/test");
        when(sessionTokenIssuer.validateAccessToken("bad")).thenThrow(
                new InvalidSessionJwtException("nope"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"code\":\"UNAUTHORIZED\"}");

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(writer.toString().contains("UNAUTHORIZED"));
        verify(objectMapper).writeValueAsString(any());
    }

    @Test
    public void escapeJsonHandlesNullViaFallbackPath() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad");
        when(request.getRequestURI()).thenReturn(null);
        when(sessionTokenIssuer.validateAccessToken("bad")).thenThrow(
                new InvalidSessionJwtException("nope"));
        when(objectMapper.writeValueAsString(any())).thenThrow(new RuntimeException("ser fail"));

        StringWriter writer = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(writer));

        filter.doFilterInternal(request, response, filterChain);

        assertTrue(writer.toString().contains("UNAUTHORIZED"));
        assertTrue(writer.toString().contains("\"path\":\"\""));
    }

    @Test
    public void shouldNotFilterAuthAppleAndRefreshPaths() {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/apple");
        assertTrue(filter.shouldNotFilter(request));
        when(request.getRequestURI()).thenReturn("/api/v1/auth/refresh");
        assertTrue(filter.shouldNotFilter(request));
        when(request.getRequestURI()).thenReturn("/api/v1/auth/me");
        assertTrue(!filter.shouldNotFilter(request));
    }
}
