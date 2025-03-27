package com.petadoption.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.petadoption.exception.InvalidJwtException;
import com.petadoption.exception.TokenExpiredException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.lang.reflect.Field;
import java.util.Date;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private String testToken;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        testToken = jwtUtil.generateToken("user@petadoption.com", "USER");
    }

    @Test
    void generateToken_Valid() {
        assertNotNull(testToken);
    }

    @Test
    void validateToken_Valid() {
        assertTrue(jwtUtil.validateToken(testToken));
    }

    @Test
    void validateToken_Invalid() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }

    @Test
    void getUsernameFromToken() {
        String username = jwtUtil.getUsernameFromToken(testToken);
        assertEquals("user@petadoption.com", username);
    }

    @Test
    void getRolesFromToken() {
        List<String> roles = jwtUtil.getRolesFromToken(testToken);
        assertTrue(roles.contains("USER"));
    }
    

@Test
void extractAllClaims_shouldThrowTokenExpiredException() throws Exception {
    // Reflectively access the secret key to generate an expired token
    Field keyField = JwtUtil.class.getDeclaredField("jwtSecretKey");
    keyField.setAccessible(true);
    var secretKey = (javax.crypto.SecretKey) keyField.get(jwtUtil);

    // Create an expired token (set expiration to the past)
    String expiredToken = Jwts.builder()
            .setSubject("user@petadoption.com")
            .claim("role", List.of("USER"))
            .setIssuedAt(new Date(System.currentTimeMillis() - 100000)) // issued in past
            .setExpiration(new Date(System.currentTimeMillis() - 5000)) // already expired
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();

    TokenExpiredException exception = assertThrows(
            TokenExpiredException.class,
            () -> jwtUtil.extractAllClaims(expiredToken)
    );

    assertEquals("Token has expired.", exception.getMessage());
}
    
    @Test
    void extractAllClaims_shouldThrowInvalidJwtException() {

        InvalidJwtException exception = assertThrows(
                InvalidJwtException.class,
                () -> jwtUtil.extractAllClaims("invalid.token")
        );

        assertEquals("Invalid JWT token.", exception.getMessage());
    }

    
    @Test
    void getRolesFromToken_shouldHandleStringRole() throws Exception {
        Field keyField = JwtUtil.class.getDeclaredField("jwtSecretKey");
        keyField.setAccessible(true);
        var secretKey = (javax.crypto.SecretKey) keyField.get(jwtUtil);

        String tokenWithStringRole = Jwts.builder()
                .setSubject("user@petadoption.com")
                .claim("role", "USER")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        List<String> roles = jwtUtil.getRolesFromToken(tokenWithStringRole);
        assertEquals(1, roles.size());
        assertEquals("USER", roles.get(0));
    }

    @Test
    void getRolesFromToken_shouldHandleUnknownRoleType() throws Exception {
        Field keyField = JwtUtil.class.getDeclaredField("jwtSecretKey");
        keyField.setAccessible(true);
        var secretKey = (javax.crypto.SecretKey) keyField.get(jwtUtil);

        String tokenWithInvalidRole = Jwts.builder()
                .setSubject("user@petadoption.com")
                .claim("role", 12345)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();

        List<String> roles = jwtUtil.getRolesFromToken(tokenWithInvalidRole);
        assertTrue(roles.isEmpty());
    }

    

}
