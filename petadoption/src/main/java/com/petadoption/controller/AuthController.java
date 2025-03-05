package com.petadoption.controller;

import com.petadoption.model.User;
import com.petadoption.repository.UserRepository;
import com.petadoption.security.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // <- Use interface, not the specific BCrypt class
    private final JwtUtil jwtUtil;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Email already exists"));
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of("message", "User registered successfully"));
    }



    @PostMapping("/login")
    public Map<String, String> login(@RequestBody Map<String, String> credentials) {
        Optional<User> optionalUser = userRepository.findByEmail(credentials.get("email"));
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (passwordEncoder.matches(credentials.get("password"), user.getPassword())) {
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
                return Map.of("token", token, "role", user.getRole());
            }
        }
        throw new RuntimeException("Invalid credentials");
    }
}
