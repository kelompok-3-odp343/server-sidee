package com.example.wandoor.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtils {
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.expiration-ms:3600000}")
    private long jwtExperiatonMs;

    public String generateToken(String userId, String role){
        var algorithm = Algorithm.HMAC256(jwtSecret);
        return JWT.create()
                .withSubject(userId)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtExperiatonMs))
                .sign(algorithm);
    }

    public DecodedJWT validateToken(String token){
        var algorithm = Algorithm.HMAC256(jwtSecret);
        var jwtVerifier = JWT.require(algorithm).build();
        return jwtVerifier.verify(token);
    }

    public String getUserId(String token){
        return validateToken(token).getSubject();
    }

    public String getRole(String token){
        return validateToken(token).getClaim("role").asString();
    }
}
