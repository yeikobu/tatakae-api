package fit.tatakae.infrastructure.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fit.tatakae.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Tests that JwtAuthenticationEntryPoint correctly serializes ErrorResponse with Instant timestamp.
 * Verifies the fix for prod bug where bare ObjectMapper couldn't serialize Instant, causing 500.
 */
@ExtendWith(MockitoExtension.class)
public class JwtAuthenticationEntryPointSerializationTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private AuthenticationException authException;

    @Test
    public void shouldSerializeErrorResponseWithInstantTimestampUsingSpringObjectMapper() throws Exception {
        // Arrange: Spring-managed ObjectMapper with JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(objectMapper);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/api/v1/auth/me");
        when(authException.getMessage()).thenReturn("Full authentication is required");
        when(response.getWriter()).thenReturn(writer);

        // Act: serialize ErrorResponse with Instant timestamp
        entryPoint.commence(request, response, authException);
        writer.flush();

        String jsonResponse = stringWriter.toString();

        // Assert: JSON is non-empty and contains expected fields
        assertFalse(jsonResponse.isEmpty(), "Response body should not be empty");
        assertTrue(jsonResponse.contains("\"message\""), "JSON should contain message field");
        assertTrue(jsonResponse.contains("\"code\":\"UNAUTHORIZED\""), "JSON should contain code field");
        assertTrue(jsonResponse.contains("\"status\":401"), "JSON should contain status field");
        assertTrue(jsonResponse.contains("\"path\":\"/api/v1/auth/me\""), "JSON should contain path field");
        assertTrue(jsonResponse.contains("\"timestamp\""), "JSON should contain timestamp field (Instant serialized)");
    }

    @Test
    public void shouldFallbackToMinimalJsonWhenSerializationFails() throws Exception {
        // Arrange: bare ObjectMapper without JavaTimeModule (simulates the bug scenario)
        ObjectMapper bareObjectMapper = new ObjectMapper();
        // DO NOT register JavaTimeModule - this will cause Instant serialization to fail

        JwtAuthenticationEntryPoint entryPoint = new JwtAuthenticationEntryPoint(bareObjectMapper);

        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);

        when(request.getRequestURI()).thenReturn("/api/v1/users");
        when(authException.getMessage()).thenReturn("Unauthorized");
        when(response.getWriter()).thenReturn(writer);

        // Act: attempt to serialize ErrorResponse with Instant (will fail, trigger fallback)
        entryPoint.commence(request, response, authException);
        writer.flush();

        String jsonResponse = stringWriter.toString();

        // Assert: fallback minimal JSON is returned (no 500, no empty body)
        assertFalse(jsonResponse.isEmpty(), "Fallback response body should not be empty");
        assertTrue(jsonResponse.contains("\"message\":\"Unauthorized\""), "Fallback JSON should contain message");
        assertTrue(jsonResponse.contains("\"code\":\"UNAUTHORIZED\""), "Fallback JSON should contain code");
        assertTrue(jsonResponse.contains("\"status\":401"), "Fallback JSON should contain status");
        assertTrue(jsonResponse.contains("\"path\":\"/api/v1/users\""), "Fallback JSON should contain path");
        // Fallback does NOT include timestamp
    }
}
