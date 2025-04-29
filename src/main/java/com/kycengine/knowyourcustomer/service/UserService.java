package com.kycengine.knowyourcustomer.service;

import com.kycengine.knowyourcustomer.entity.User;
import com.kycengine.knowyourcustomer.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  @Autowired
  private UserRepository userRepository;

  public String registerUser(String username, String password) {
    // Check if username already exists
    if (userRepository.findByUsername(username).isPresent()) {
      return "Username is already taken.";
    }
    User user = new User();
    user.setUsername(username);

    // Hash the password before saving
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    String hashedPassword = encoder.encode(password);
    user.setPassword(hashedPassword);

    userRepository.save(user);
    return "User registered successfully.";
  }
}