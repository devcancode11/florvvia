package com.florvvia.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
public class JwtUtil {
  @Value("${app.jwt.secret}")
  private String secret;
  @Value("${app.jwt.expiration-ms}")
  private long expirationMs;

  private SecretKey key() {
    byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
    if (bytes.length < 32) {
      byte[] padded = new byte[32];
      System.arraycopy(bytes, 0, padded, 0, bytes.length);
      bytes = padded;
    }
    return Keys.hmacShaKeyFor(bytes);
  }

  public String generate(Long userId, String email, String role) {
    return Jwts.builder()
      .subject(String.valueOf(userId))
      .claim("email", email)
      .claim("role", role)
      .issuedAt(new Date())
      .expiration(new Date(System.currentTimeMillis() + expirationMs))
      .signWith(key())
      .compact();
  }

  public Claims parse(String token) {
    return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token).getPayload();
  }
}
