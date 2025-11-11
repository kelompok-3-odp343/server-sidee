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
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = req.getRequestURI();

        // Lewati JWT Filter untuk endpoint auth
        if (path.startsWith("/api/auth/")) {
            filterChain.doFilter(req, response);
            return;
        }

        var header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")){
            unauthorized(response, "Unauthorized - Token JWT tidak valid");
            return;
        }

        var token = header.substring(7);

        try {
            // Check token blacklist
            var blacklistKey = "jwt_blacklist:" + token;
            if (stringRedisTemplate.hasKey(blacklistKey)) {
                unauthorized(response, "Unauthorized - Token sudah logout");
                return;
            }

            // Validate JWT
            var jwt = jwtUtils.validateToken(token);
            var userId = jwt.getClaim("userId").asString();
            var role = jwt.getClaim("role").asString();
            var cif = jwt.getClaim("cif").asString();
            //TODO Add NPP for Admin

            if (!"NASABAH".equalsIgnoreCase(role)) cif = null;

            MDC.put("userId", userId);
            if (cif != null) MDC.put("cif", cif);

            if (SecurityContextHolder.getContext().getAuthentication() == null){
                var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of(() -> "ROLE_" + role));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
                SecurityContextHolder.getContext().setAuthentication(auth);
            }

            // ✅ Set RequestContext (SUPAYA SERVICE DAPAT ADMIN USER ID)
            RequestContext ctx = RequestContext.get();
            ctx.setUserId(userId);
            ctx.setCif(cif);

            log.info("🔐 Authenticated user={} role={} cif={}", userId, role, cif != null ? cif : "-");

            filterChain.doFilter(req, response);

        } catch (Exception e) {
            log.error("❌ Error di JwtAuthenticationFilter: {}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
            unauthorized(response, "Unauthorized - Token JWT tidak valid");
        } finally {
            MDC.remove("userId");
            MDC.remove("cif");
        }
    }

    private void unauthorized(HttpServletResponse res, String msg) throws IOException {
        if (res.isCommitted()) return;
        res.resetBuffer();
        res.setStatus(HttpStatus.UNAUTHORIZED.value());
        res.setCharacterEncoding("UTF-8");
        res.setContentType("application/json");
        res.getWriter().write("{\"status\":false,\"message\":\"" + msg + "\"}");
    }
}
