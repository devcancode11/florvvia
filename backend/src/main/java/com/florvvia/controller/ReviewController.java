package com.florvvia.controller;

import com.florvvia.model.*;
import com.florvvia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {
  @Autowired private ReviewRepository reviews;
  @Autowired private ProductRepository products;

  @GetMapping("/product/{productId}")
  public List<Review> byProduct(@PathVariable Long productId) {
    return reviews.findByProductIdOrderByCreatedAtDesc(productId);
  }

  @PostMapping
  public ResponseEntity<?> add(@AuthenticationPrincipal User u, @RequestBody Map<String, Object> b) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login to review"));
    Long pid = Long.valueOf(String.valueOf(b.get("productId")));
    var p = products.findById(pid);
    if (p.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
    int rating = b.get("rating") == null ? 5 : Integer.parseInt(String.valueOf(b.get("rating")));
    rating = Math.max(1, Math.min(5, rating));
    String comment = b.get("comment") == null ? "" : String.valueOf(b.get("comment"));
    Review r = new Review();
    r.setProduct(p.get());
    r.setUser(u);
    r.setUserName(u.getName());
    r.setRating(rating);
    r.setComment(comment);
    reviews.save(r);
    // update product avg
    var all = reviews.findByProductIdOrderByCreatedAtDesc(pid);
    double avg = all.stream().mapToInt(Review::getRating).average().orElse(rating);
    Product pr = p.get();
    pr.setRatingAvg(Math.round(avg * 10) / 10.0);
    pr.setRatingCount(all.size());
    products.save(pr);
    return ResponseEntity.ok(r);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@AuthenticationPrincipal User u, @PathVariable Long id) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    var opt = reviews.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    Review r = opt.get();
    boolean admin = "ADMIN".equalsIgnoreCase(u.getRole());
    boolean owner = r.getUser() != null && r.getUser().getId().equals(u.getId());
    if (!admin && !owner) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
    reviews.delete(r);
    return ResponseEntity.ok(Map.of("ok", true));
  }
}
