package com.example.wandoor.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

import java.util.Date;
import java.util.Map;

@Component
@Log4j2
public class JwtUtils {
    @Value("${JWT_SECRET}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:3600000}")
    private long jwtExperiatonMs;

    public String generateToken(Map<String, Object> claims, String userId) {
        var algorithm = Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
        var builder = JWT.create().withClaim("userId", userId);
        claims.forEach((k, v) -> builder.withClaim(k, v.toString()));
        builder.withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExperiatonMs));
        return builder.sign(algorithm);
    }

    public DecodedJWT validateToken(String token){
        try {
        var algorithm =  Algorithm.HMAC256(jwtSecret.getBytes(StandardCharsets.UTF_8));
        var jwtVerifier = JWT.require(algorithm).build();
        var decoded = jwtVerifier.verify(token);
        log.info("✅ Token valid untuk subject={} role={}", jwtVerifier.verify(token).getSubject(), jwtVerifier.verify(token).getClaim("role").asString());
        return decoded;
        } catch (Exception e) {
            log.error("❌ JWT invalid: {} | Secret used: {}", e.getMessage(), jwtSecret);
            throw e;
        }
    }

    public String getUserId(String token){
        return validateToken(token).getSubject();
    }

    public String getRole(String token){
        return validateToken(token).getClaim("role").asString();
    }

    public long getRemainingValidity(String token) {
        Date expiration = extractExpiration(token);
        return (expiration.getTime() - System.currentTimeMillis()) / 1000;
    }

    public Date extractExpiration(String token) {
        return validateToken(token).getExpiresAt();
    }


}

