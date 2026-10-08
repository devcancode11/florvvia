package com.florvvia.controller;

import com.florvvia.model.*;
import com.florvvia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {
  @Autowired private OrderRepository orders;
  @Autowired private UserRepository users;
  @Autowired private ProductRepository products;
  @Autowired private ReviewRepository reviews;
  @Autowired private AddressRepository addresses;

  private boolean isAdmin(User u) { return u != null && "ADMIN".equalsIgnoreCase(u.getRole()); }

  @GetMapping("/stats")
  public ResponseEntity<?> stats(@AuthenticationPrincipal User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    double revenue = orders.findAll().stream()
      .filter(o -> !"CANCELLED".equals(o.getStatus()))
      .mapToDouble(ShopOrder::getTotal).sum();
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("products", products.count());
    m.put("users", users.count());
    m.put("orders", orders.count());
    m.put("revenue", revenue);
    return ResponseEntity.ok(m);
  }

  @GetMapping("/orders")
  public ResponseEntity<?> allOrders(@AuthenticationPrincipal User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    return ResponseEntity.ok(orders.findAllByOrderByCreatedAtDesc());
  }

  @PostMapping("/orders/{id}/status")
  public ResponseEntity<?> setStatus(@AuthenticationPrincipal User u, @PathVariable Long id, @RequestBody Map<String, String> b) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    var opt = orders.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    ShopOrder o = opt.get();
    o.setStatus(b.getOrDefault("status", o.getStatus()));
    if (b.containsKey("paymentStatus")) o.setPaymentStatus(b.get("paymentStatus"));
    orders.save(o);
    return ResponseEntity.ok(o);
  }

  @GetMapping("/users")
  public ResponseEntity<?> allUsers(@AuthenticationPrincipal User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    List<Map<String, Object>> list = new ArrayList<>();
    for (User x : users.findAll()) list.add(AuthController.pub(x));
    return ResponseEntity.ok(list);
  }

  // All saved customer delivery addresses (address book) — admin only.
  @GetMapping("/addresses")
  public ResponseEntity<?> allAddresses(@AuthenticationPrincipal User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    List<Map<String, Object>> out = new ArrayList<>();
    for (Address a : addresses.findAll()) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", a.getId());
      m.put("label", a.getLabel());
      m.put("fullName", a.getFullName());
      m.put("phone", a.getPhone());
      m.put("line1", a.getLine1());
      m.put("line2", a.getLine2());
      m.put("city", a.getCity());
      m.put("state", a.getState());
      m.put("pincode", a.getPincode());
      m.put("isDefault", a.isDefault());
      try {
        User owner = a.getUser();
        if (owner != null && owner.getId() != null) {
          m.put("userId", owner.getId());
          var fresh = users.findById(owner.getId());
          if (fresh.isPresent()) {
            m.put("userEmail", fresh.get().getEmail());
            m.put("userName", fresh.get().getName());
          }
        }
      } catch (Exception ignored) {}
      out.add(m);
    }
    return ResponseEntity.ok(out);
  }

  @DeleteMapping("/reviews/{id}")
  public ResponseEntity<?> delReview(@AuthenticationPrincipal User u, @PathVariable Long id) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    reviews.deleteById(id);
    return ResponseEntity.ok(Map.of("ok", true));
  }

  @GetMapping("/reviews")
  public ResponseEntity<?> allReviews(@AuthenticationPrincipal User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    return ResponseEntity.ok(reviews.findAll());
  }
}
