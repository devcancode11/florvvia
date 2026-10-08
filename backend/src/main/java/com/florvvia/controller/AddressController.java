package com.florvvia.controller;

import com.florvvia.model.*;
import com.florvvia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/addresses")
@CrossOrigin(origins = "*")
public class AddressController {
  @Autowired private AddressRepository addresses;
  @Autowired private UserRepository users;

  @GetMapping
  public ResponseEntity<?> list(@AuthenticationPrincipal User u) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    return ResponseEntity.ok(addresses.findByUserId(u.getId()));
  }

  @PostMapping
  public ResponseEntity<?> create(@AuthenticationPrincipal User u, @RequestBody Address a) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    if (a.getFullName() == null || a.getLine1() == null)
      return ResponseEntity.badRequest().body(Map.of("error", "fullName and line1 required"));
    User owner = users.findById(u.getId()).orElse(u);
    a.setId(null);
    a.setUser(owner);
    List<Address> existing = addresses.findByUserId(u.getId());
    if (existing.isEmpty() || a.isDefault()) {
      if (a.isDefault() || existing.isEmpty()) {
        for (Address e : existing) { if (e.isDefault()) { e.setDefault(false); addresses.save(e); } }
        a.setDefault(true);
      }
    }
    addresses.save(a);
    return ResponseEntity.ok(addresses.findByUserId(u.getId()));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@AuthenticationPrincipal User u, @PathVariable Long id, @RequestBody Address in) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    var opt = addresses.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    Address a = opt.get();
    if (!a.getUser().getId().equals(u.getId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
    a.setLabel(in.getLabel());
    a.setFullName(in.getFullName());
    a.setPhone(in.getPhone());
    a.setLine1(in.getLine1());
    a.setLine2(in.getLine2());
    a.setCity(in.getCity());
    a.setState(in.getState());
    a.setPincode(in.getPincode());
    if (in.isDefault()) {
      for (Address e : addresses.findByUserId(u.getId())) {
        if (!e.getId().equals(id) && e.isDefault()) { e.setDefault(false); addresses.save(e); }
      }
      a.setDefault(true);
    } else a.setDefault(in.isDefault());
    addresses.save(a);
    return ResponseEntity.ok(addresses.findByUserId(u.getId()));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@AuthenticationPrincipal User u, @PathVariable Long id) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    var opt = addresses.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    if (!opt.get().getUser().getId().equals(u.getId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
    addresses.delete(opt.get());
    return ResponseEntity.ok(addresses.findByUserId(u.getId()));
  }

  @PostMapping("/{id}/default")
  public ResponseEntity<?> setDefault(@AuthenticationPrincipal User u, @PathVariable Long id) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    var opt = addresses.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    if (!opt.get().getUser().getId().equals(u.getId())) return ResponseEntity.status(403).body(Map.of("error", "Forbidden"));
    for (Address e : addresses.findByUserId(u.getId())) {
      boolean def = e.getId().equals(id);
      if (e.isDefault() != def) { e.setDefault(def); addresses.save(e); }
    }
    return ResponseEntity.ok(addresses.findByUserId(u.getId()));
  }
}
