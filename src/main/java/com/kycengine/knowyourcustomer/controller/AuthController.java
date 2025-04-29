package com.kycengine.knowyourcustomer.controller;

import com.kycengine.knowyourcustomer.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthenticationManager authenticationManager;
  private final JwtTokenProvider jwtTokenProvider;


  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
    try {
      String username = request.get("username");
      String password = request.get("password");

      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(username, password)
      );

      String accessToken = jwtTokenProvider.createAccessToken(username);
      String refreshToken = jwtTokenProvider.createRefreshToken(username);
      long accessTokenExpiry = jwtTokenProvider.getAccessTokenExpiry();
      long refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiry();

      return ResponseEntity.ok(Map.of(
              "accessToken", accessToken,
              "refreshToken", refreshToken,
              "accessTokenExpiryMs", accessTokenExpiry,
              "refreshTokenExpiryMs", refreshTokenExpiry
      ));

    } catch (AuthenticationException e) {
      throw new RuntimeException("Invalid username or password");
    }
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
    String refreshToken = request.get("refreshToken");

    if (!jwtTokenProvider.isRefreshTokenValid(refreshToken)) {
      return ResponseEntity.status(401).body(Map.of("message", "Invalid or expired refresh token"));
    }

    // Validate the refresh token and generate a new access token
    String userName = jwtTokenProvider.getUsernameFromToken(refreshToken);
    String newAccessToken = jwtTokenProvider.createAccessToken(userName);
    String newRefreshToken = jwtTokenProvider.createRefreshToken(userName);
    long accessTokenExpiry = jwtTokenProvider.getAccessTokenExpiry();
    long refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiry();

    return ResponseEntity.ok(Map.of(
            "accessToken", newAccessToken,
            "refreshToken", newRefreshToken,
            "accessTokenExpiryMs", accessTokenExpiry,
            "refreshTokenExpiryMs", refreshTokenExpiry
    ));
  }
}