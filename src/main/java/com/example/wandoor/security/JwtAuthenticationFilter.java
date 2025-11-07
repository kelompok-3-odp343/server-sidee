package com.example.wandoor.security;

import com.example.wandoor.config.RequestContext;
import com.example.wandoor.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.MDC;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Log4j2
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final RequestContext requestContext;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String path = req.getRequestURI();

        // Skip auth endpoints
        if (path.startsWith("/api/auth/")) {
            chain.doFilter(req, res);
            return;
        }

        String authHeader = req.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            unauthorized(res, "Unauthorized: Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Check Redis blacklist (logout)
            String blacklistKey = "jwt_blacklist:" + token;
            if (stringRedisTemplate.hasKey(blacklistKey)) {
                unauthorized(res, "Unauthorized: Token has been logged out");
                return;
            }

            var jwt = jwtUtils.validateToken(token);
            String userId = jwt.getSubject();
            String role = jwt.getClaim("role").asString();

            // Nasabah MUST send Customer-Id header
            String cifHeader = req.getHeader("Customer-Id");
            if (!"ADMIN".equalsIgnoreCase(role) && cifHeader == null) {
                unauthorized(res, "Unauthorized: Missing Customer-Id for NASABAH");
                return;
            }

            // Put into Spring Security Context
            var authToken = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(() -> "ROLE_" + role)
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(authToken);

            // Store in RequestContext (to be used in services)
            RequestContext ctx = RequestContext.get();
            ctx.setUserId(userId);
            ctx.setCif(cifHeader); // null allowed for admin

            // Logging context
            MDC.put("userId", userId);
            MDC.put("cif", cifHeader);

            chain.doFilter(req, res);

        } catch (Exception e) {
            log.error("❌ JWT Filtering Error: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            unauthorized(res, "Unauthorized: Invalid JWT token");
        }
    }


    private void unauthorized(HttpServletResponse res, String message) throws IOException {
        if (res.isCommitted()) return;
        res.resetBuffer();
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setContentType("application/json");
        res.getWriter().write("{\"status\":false,\"message\":\"" + message + "\"}");
        res.flushBuffer();
    }
}
