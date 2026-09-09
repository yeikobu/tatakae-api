package fit.tatakae.infrastructure.web.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.tatakae.infrastructure.web.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper;

    public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.of(
                authException.getMessage() != null ? authException.getMessage() : "Authentication required",
                "UNAUTHORIZED",
                HttpStatus.UNAUTHORIZED.value(),
                request.getRequestURI()
        );

        try {
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        } catch (Exception e) {
            logger.error("Failed to serialize ErrorResponse, falling back to minimal JSON", e);
            // Fallback: write minimal JSON manually if serialization fails
            String fallbackJson = String.format(
                "{\"message\":\"%s\",\"code\":\"UNAUTHORIZED\",\"status\":401,\"path\":\"%s\"}",
                escapeJson(errorResponse.message()),
                escapeJson(errorResponse.path())
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
