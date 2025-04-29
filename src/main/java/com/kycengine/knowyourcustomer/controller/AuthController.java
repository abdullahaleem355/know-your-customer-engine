package com.kycengine.knowyourcustomer.controller;

import com.kycengine.knowyourcustomer.security.JwtTokenProvider;
import com.kycengine.knowyourcustomer.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
  private final UserService userService;

  @PostMapping("/register")
  public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
    String username = request.get("username");
    String password = request.get("password");

    // Call the service layer to register the user
    String result = userService.registerUser(username, password);

    if (result.equals("User registered successfully.")) {
      return ResponseEntity.ok(result);
    } else {
      return ResponseEntity.badRequest().body(result);
    }
  }

  @PostMapping("/login")
  public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
    try {
      String username = request.get("username");
      String password = request.get("password");

      if (username == null || password == null) {
        return ResponseEntity
                .badRequest()
                .body(Map.of("error", "Username and password must be provided."));
      }

      Authentication authentication = authenticationManager.authenticate(
              new UsernamePasswordAuthenticationToken(username, password)
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);
      UserDetails userDetails = (UserDetails) authentication.getPrincipal();

      String accessToken = jwtTokenProvider.createAccessToken(userDetails.getUsername());
      String refreshToken = jwtTokenProvider.createRefreshToken(userDetails.getUsername());
      long accessTokenExpiry = jwtTokenProvider.getAccessTokenExpiry();
      long refreshTokenExpiry = jwtTokenProvider.getRefreshTokenExpiry();

      return ResponseEntity.ok(Map.of(
              "accessToken", accessToken,
              "refreshToken", refreshToken,
              "accessTokenExpiryMs", accessTokenExpiry,
              "refreshTokenExpiryMs", refreshTokenExpiry
      ));

    } catch (AuthenticationException e) {
      return ResponseEntity
              .status(HttpStatus.UNAUTHORIZED)
              .body(Map.of("error", "Invalid username or password."));
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