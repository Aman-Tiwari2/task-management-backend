//package com.example.taskmanager.controller;
//
//import com.example.taskmanager.dto.*;
//import com.example.taskmanager.service.AuthService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/auth")
//public class AuthController {
//
//    private final AuthService authService;
//    public AuthController(AuthService authService) { this.authService = authService; }
//
//    @PostMapping("/register")
//    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest req) {
//        return ResponseEntity.ok(authService.register(req));
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest req) {
//        return ResponseEntity.ok(authService.login(req));
//    }
//}
package com.example.taskmanager.controller;

import com.example.taskmanager.dto.LoginRequest;
import com.example.taskmanager.dto.RegisterRequest;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.example.taskmanager.security.JwtService;
import com.example.taskmanager.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authManager;
    private final UserService userService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService,
                          AuthenticationManager authManager,
                          UserService userService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authManager = authManager;
        this.userService = userService;
    }

    // ✅ Register a new user (defaults to USER role)
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest body) {
        String email = body.email();
        String password = body.password();
        String roleStr = (body.role() != null && !body.role().isBlank()) ? body.role() : "USER";

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User already exists"));
        }

        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (Exception e) {
            role = Role.USER;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        userRepository.save(user);

        return ResponseEntity.ok(Map.of(
                "message", "User registered successfully",
                "role", role.name()
        ));
    }

    // ✅ Login and get JWT token
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest body) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(body.email(), body.password())
        );

        String username = auth.getName();
        String role = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USER");

        // Generate JWT token using username and role
        String token = jwtService.generateToken(username, role);

        return ResponseEntity.ok(Map.of(
                "token", token,
                "email", username,
                "role", role
        ));
    }

    // ✅ Promote a user to ADMIN (Admin-only endpoint)
    @PutMapping("/make-admin/{id}")
    public ResponseEntity<?> makeAdmin(Authentication auth, @PathVariable Long id) {
        User updated = userService.makeAdmin(auth, id);
        return ResponseEntity.ok(Map.of(
                "message", updated.getEmail() + " is now an ADMIN"
        ));
    }
}
