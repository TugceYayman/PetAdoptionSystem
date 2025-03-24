package com.petadoption.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import com.petadoption.exception.InvalidJwtException;
import com.petadoption.exception.TokenExpiredException;
import javax.crypto.SecretKey;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {

    private final SecretKey jwtSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final long EXPIRATION_MS = 86400000; // 24 hours

    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", List.of(role)) 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(jwtSecretKey)
                .compact();
    }


    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(jwtSecretKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return extractAllClaims(token).getSubject();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = extractAllClaims(token);
        Object rolesObject = claims.get("role");

        if (rolesObject instanceof List<?>) {
            return ((List<?>) rolesObject).stream()
            		.map(Object::toString)
            		.toList();
        } else if (rolesObject instanceof String) {
            return List.of(rolesObject.toString()); 
        } else {
            return new ArrayList<>();
        }
    }



    public Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new TokenExpiredException("Token has expired.", e);
        } catch (JwtException e) {
            throw new InvalidJwtException("Invalid JWT token.", e);
        }
    }


}
