package com.florvvia.controller;

import com.florvvia.model.*;
import com.florvvia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "*")
public class CartController {
  @Autowired private CartRepository cart;
  @Autowired private ProductRepository products;

  @GetMapping
  public ResponseEntity<?> list(@AuthenticationPrincipal User u) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    return ResponseEntity.ok(cart.findByUserId(u.getId()));
  }

  @PostMapping("/add")
  public ResponseEntity<?> add(@AuthenticationPrincipal User u, @RequestBody Map<String, Object> b) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    Long pid = Long.valueOf(String.valueOf(b.get("productId")));
    int qty = b.get("qty") == null ? 1 : Integer.parseInt(String.valueOf(b.get("qty")));
    var p = products.findById(pid);
    if (p.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Product not found"));
    var opt = cart.findByUserIdAndProductId(u.getId(), pid);
    CartItem ci = opt.orElseGet(() -> {
      CartItem n = new CartItem();
      n.setUser(u);
      n.setProduct(p.get());
      n.setQty(0);
      return n;
    });
    int base = opt.isPresent() ? opt.get().getQty() : 0;
    ci.setQty(Math.max(1, Math.min(99, base + qty)));
    cart.save(ci);
    return ResponseEntity.ok(cart.findByUserId(u.getId()));
  }

  @PostMapping("/update")
  public ResponseEntity<?> update(@AuthenticationPrincipal User u, @RequestBody Map<String, Object> b) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    Long pid = Long.valueOf(String.valueOf(b.get("productId")));
    int qty = Integer.parseInt(String.valueOf(b.get("qty")));
    var opt = cart.findByUserIdAndProductId(u.getId(), pid);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not in cart"));
    if (qty <= 0) cart.delete(opt.get());
    else { opt.get().setQty(Math.min(99, qty)); cart.save(opt.get()); }
    return ResponseEntity.ok(cart.findByUserId(u.getId()));
  }

  @PostMapping("/remove")
  public ResponseEntity<?> remove(@AuthenticationPrincipal User u, @RequestBody Map<String, Object> b) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    Long pid = Long.valueOf(String.valueOf(b.get("productId")));
    cart.findByUserIdAndProductId(u.getId(), pid).ifPresent(cart::delete);
    return ResponseEntity.ok(cart.findByUserId(u.getId()));
  }

  @PostMapping("/clear")
  public ResponseEntity<?> clear(@AuthenticationPrincipal User u) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    cart.deleteByUserId(u.getId());
    return ResponseEntity.ok(Map.of("ok", true));
  }
}
