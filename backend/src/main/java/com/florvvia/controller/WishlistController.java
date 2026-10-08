package com.florvvia.controller;

import com.florvvia.model.*;
import com.florvvia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/wishlist")
@CrossOrigin(origins = "*")
public class WishlistController {
  @Autowired private WishlistRepository wish;
  @Autowired private ProductRepository products;

  @GetMapping
  public ResponseEntity<?> list(@AuthenticationPrincipal User u) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    return ResponseEntity.ok(wish.findByUserId(u.getId()));
  }

  @PostMapping("/toggle")
  public ResponseEntity<?> toggle(@AuthenticationPrincipal User u, @RequestBody Map<String, Object> b) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    Long pid = Long.valueOf(String.valueOf(b.get("productId")));
    var p = products.findById(pid);
    if (p.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
    var opt = wish.findByUserIdAndProductId(u.getId(), pid);
    boolean added;
    if (opt.isPresent()) { wish.delete(opt.get()); added = false; }
    else {
      WishlistItem w = new WishlistItem();
      w.setUser(u); w.setProduct(p.get());
      wish.save(w); added = true;
    }
    return ResponseEntity.ok(Map.of("added", added, "items", wish.findByUserId(u.getId())));
  }
}
