package com.example.wandoor.security;

import com.example.wandoor.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                              HttpServletResponse response,
                              FilterChain filterChain)
        throws ServletException, IOException {

        String path = req.getRequestURI();

        if (path.startsWith("/api/auth/")) {
            System.out.println("JWT Filter skipped for: " + path);
            filterChain.doFilter(req, response);
            return;
        }

        var header = req.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")){
            filterChain.doFilter(req, response);
            return;
        }

        var token = header.substring(7);
        try {
            var jwt = jwtUtils.validateToken(token);
            var userId = jwt.getSubject();
            var role = jwt.getClaim("role").asString();

            var auth = new UsernamePasswordAuthenticationToken(userId, null, List.of(() -> "ROLE_" + role));
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(req));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(req, response);
    }
}
