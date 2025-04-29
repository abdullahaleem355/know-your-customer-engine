package com.kycengine.knowyourcustomer.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService
{

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
  {
    // TODO: Replace with real user loading logic (e.g., from DB)
    // For now, dummy user
    if ("admin".equals(username)) {
      return org.springframework.security.core.userdetails.User
              .withUsername("admin")
              .password("$2a$10$DowJonesIndexPasswordHashHere")
              .roles("ADMIN")
              .build();
    }

    throw new UsernameNotFoundException("User not found: " + username);
  }
}