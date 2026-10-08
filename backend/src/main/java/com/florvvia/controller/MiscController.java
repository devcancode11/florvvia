package com.florvvia.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.*;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MiscController {

  @Value("${app.whatsapp-number:917417566249}")
  private String whatsappNumber;

  @Value("${app.upload-dir:./uploads}")
  private String uploadDir;

  @GetMapping("/config")
  public Map<String, Object> config() {
    return Map.of(
      "whatsappNumber", whatsappNumber,
      "upiId", "florvvia@upi",
      "freeDeliveryAbove", 999,
      "deliveryFee", 49
    );
  }

  @PostMapping("/upload")
  public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
      @org.springframework.security.core.annotation.AuthenticationPrincipal com.florvvia.model.User u) {
    if (u == null || !"ADMIN".equalsIgnoreCase(u.getRole()))
      return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    try {
      Path dir = Paths.get(uploadDir);
      Files.createDirectories(dir);
      String ext = "";
      String orig = file.getOriginalFilename() == null ? "img" : file.getOriginalFilename();
      int dot = orig.lastIndexOf('.');
      if (dot >= 0) ext = orig.substring(dot);
      String name = "p-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8) + ext;
      Files.copy(file.getInputStream(), dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
      return ResponseEntity.ok(Map.of("url", "/uploads/" + name));
    } catch (Exception e) {
      return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
    }
  }
}
