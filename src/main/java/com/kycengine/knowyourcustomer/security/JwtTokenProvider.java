package com.kycengine.knowyourcustomer.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKeyString;

  @Value("${jwt.expiration.millis}")
  private long jwtExpirationMillis;

  @Value("${refresh.expiration.millis}")
  private long refreshExpirationMillis;

  private Key secretKey;

  @PostConstruct
  public void init() {
    byte[] decodedKey = Base64.getDecoder().decode(Base64.getEncoder().encodeToString(secretKeyString.getBytes()));
    this.secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "HmacSHA256");
  }

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();
  }

  private boolean isTokenExpired(String token) {
    return extractExpiration(token).before(new Date());
  }

  public String createAccessToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("token_type", "access");
    return createToken(claims, username, jwtExpirationMillis);
  }

  public String createRefreshToken(String username) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("token_type", "refresh");
    return createToken(claims, username, refreshExpirationMillis);
  }

  public long getAccessTokenExpiry() {
     return jwtExpirationMillis;
  }

  public long getRefreshTokenExpiry() {
    return refreshExpirationMillis;
  }

  private String createToken(Map<String, Object> claims, String subject, long validity) {
    return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + validity))
            .signWith(secretKey)
            .compact();
  }

  public String getUsernameFromToken(String token) {
    return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
  }

  public boolean isTokenValid(String token, UserDetails userDetails) {
    final String username = extractUsername(token);
    return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
  }

  public boolean isRefreshTokenValid(String token) {
    try {
      Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
      String tokenType = claims.get("token_type", String.class);
      if (tokenType == null || !tokenType.equals("refresh")) {
        return false;
      }
      return !isTokenExpired(token);
    } catch (JwtException e) {
      return false;
    }
  }
}