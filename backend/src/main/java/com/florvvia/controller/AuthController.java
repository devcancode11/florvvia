package com.florvvia.controller;

import com.florvvia.dto.*;
import com.florvvia.model.User;
import com.florvvia.repository.UserRepository;
import com.florvvia.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
  @Autowired private UserRepository users;
  @Autowired private PasswordEncoder enc;
  @Autowired private JwtUtil jwt;

  @PostMapping("/register")
  public ResponseEntity<?> register(@RequestBody RegisterRequest r) {
    if (r.email == null || r.password == null || r.name == null)
      return ResponseEntity.badRequest().body(Map.of("error", "name, email, password required"));
    String email = r.email.trim().toLowerCase();
    if (users.findByEmail(email).isPresent())
      return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
    User u = new User();
    u.setName(r.name.trim());
    u.setEmail(email);
    u.setPhone(r.phone);
    u.setPasswordHash(enc.encode(r.password));
    u.setRole("USER");
    users.save(u);
    String token = jwt.generate(u.getId(), u.getEmail(), u.getRole());
    return ResponseEntity.ok(Map.of("token", token, "user", pub(u)));
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@RequestBody LoginRequest r) {
    if (r.email == null || r.password == null)
      return ResponseEntity.badRequest().body(Map.of("error", "email & password required"));
    var opt = users.findByEmail(r.email.trim().toLowerCase());
    if (opt.isEmpty() || !enc.matches(r.password, opt.get().getPasswordHash()))
      return ResponseEntity.status(401).body(Map.of("error", "Invalid email or password"));
    User u = opt.get();
    String token = jwt.generate(u.getId(), u.getEmail(), u.getRole());
    return ResponseEntity.ok(Map.of("token", token, "user", pub(u)));
  }

  @GetMapping("/me")
  public ResponseEntity<?> me(@AuthenticationPrincipal User u) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Not logged in"));
    User fresh = users.findById(u.getId()).orElse(u);
    return ResponseEntity.ok(pub(fresh));
  }

  public static Map<String, Object> pub(User u) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("id", u.getId());
    m.put("name", u.getName());
    m.put("email", u.getEmail());
    m.put("phone", u.getPhone());
    m.put("role", u.getRole());
    return m;
  }
}
