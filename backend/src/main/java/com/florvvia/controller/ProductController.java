package com.florvvia.controller;

import com.florvvia.dto.ProductDto;
import com.florvvia.model.Product;
import com.florvvia.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {
  @Autowired private ProductRepository products;

  @GetMapping
  public List<Product> list(@RequestParam(required = false) String category,
                            @RequestParam(required = false) String search,
                            @RequestParam(required = false) String tag) {
    List<Product> all = products.findByActiveTrue();
    return all.stream().filter(p -> {
      if (category != null && !category.isBlank() && !category.equalsIgnoreCase("all")
          && !p.getCategory().equalsIgnoreCase(category)) return false;
      if (tag != null && !tag.isBlank() && (p.getTag() == null || !p.getTag().equalsIgnoreCase(tag))) return false;
      if (search != null && !search.isBlank()) {
        String s = search.toLowerCase();
        if (!p.getName().toLowerCase().contains(s)
            && (p.getDescription() == null || !p.getDescription().toLowerCase().contains(s))
            && (p.getCode() == null || !p.getCode().toLowerCase().contains(s))) return false;
      }
      return true;
    }).sorted(Comparator.comparing(Product::getCreatedAt).reversed()).collect(Collectors.toList());
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> one(@PathVariable Long id) {
    return products.findById(id)
      .map(p -> ResponseEntity.ok((Object) p))
      .orElse(ResponseEntity.status(404).body(Map.of("error", "Product not found")));
  }

  // ---- admin create/update/delete (also exposed via /api/admin, but kept here with role check in SecurityConfig? we check manually) ----
  @PostMapping
  public ResponseEntity<?> create(@RequestBody ProductDto d,
      @org.springframework.security.core.annotation.AuthenticationPrincipal com.florvvia.model.User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    Product p = new Product();
    apply(p, d);
    products.save(p);
    return ResponseEntity.ok(p);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ProductDto d,
      @org.springframework.security.core.annotation.AuthenticationPrincipal com.florvvia.model.User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    var opt = products.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    Product p = opt.get();
    apply(p, d);
    p.setUpdatedAt(LocalDateTime.now());
    products.save(p);
    return ResponseEntity.ok(p);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(@PathVariable Long id,
      @org.springframework.security.core.annotation.AuthenticationPrincipal com.florvvia.model.User u) {
    if (!isAdmin(u)) return ResponseEntity.status(403).body(Map.of("error", "Admin only"));
    var opt = products.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    Product p = opt.get();
    p.setActive(false); // soft delete so old orders stay intact
    products.save(p);
    return ResponseEntity.ok(Map.of("ok", true));
  }

  private boolean isAdmin(com.florvvia.model.User u) {
    return u != null && "ADMIN".equalsIgnoreCase(u.getRole());
  }

  private void apply(Product p, ProductDto d) {
    if (d.name != null) p.setName(d.name);
    if (d.category != null) p.setCategory(d.category.toLowerCase());
    if (d.description != null) p.setDescription(d.description);
    p.setPrice(d.price);
    if (d.mrp != null) p.setMrp(d.mrp);
    if (d.tag != null) p.setTag(d.tag);
    if (d.code != null) p.setCode(d.code);
    if (d.images != null) p.setImages(d.images);
    if (d.stock != null) p.setStock(d.stock);
    if (d.active != null) p.setActive(d.active);
  }
}
