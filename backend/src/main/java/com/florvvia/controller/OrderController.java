package com.florvvia.controller;

import com.florvvia.dto.OrderRequest;
import com.florvvia.model.*;
import com.florvvia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
  @Autowired private OrderRepository orders;
  @Autowired private ProductRepository products;
  @Autowired private CartRepository cart;
  @Autowired private AddressRepository addresses;
  @Autowired private UserRepository users;

  @Value("${app.whatsapp-number:917417566249}")
  private String whatsappNumber;

  @GetMapping("/mine")
  public ResponseEntity<?> mine(@AuthenticationPrincipal User u) {
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    return ResponseEntity.ok(orders.findByUserIdOrderByCreatedAtDesc(u.getId()));
  }

  @PostMapping
  @Transactional
  public ResponseEntity<?> place(@AuthenticationPrincipal User u, @RequestBody OrderRequest r) {
    User owner = null;
    String email = null, name = null, phone = null;
    if (u != null) {
      owner = users.findById(u.getId()).orElse(u);
      email = owner.getEmail(); name = owner.getName(); phone = owner.getPhone();
    }

    // Build items
    List<OrderItem> items = new ArrayList<>();
    double subtotal = 0;

    if (r.items != null && !r.items.isEmpty()) {
      for (OrderRequest.Item it : r.items) {
        var p = products.findById(it.productId);
        if (p.isEmpty() || !p.get().isActive()) continue;
        int qty = it.qty == null ? 1 : Math.max(1, Math.min(99, it.qty));
        OrderItem oi = new OrderItem();
        oi.setProductId(p.get().getId());
        oi.setProductName(p.get().getName());
        oi.setPrice(p.get().getPrice());
        oi.setQty(qty);
        oi.setImage(p.get().getImages().isEmpty() ? "" : p.get().getImages().get(0));
        items.add(oi);
        subtotal += p.get().getPrice() * qty;
      }
    } else if (owner != null) {
      var cartItems = cart.findByUserId(owner.getId());
      for (CartItem ci : cartItems) {
        OrderItem oi = new OrderItem();
        oi.setProductId(ci.getProduct().getId());
        oi.setProductName(ci.getProduct().getName());
        oi.setPrice(ci.getProduct().getPrice());
        oi.setQty(ci.getQty());
        oi.setImage(ci.getProduct().getImages().isEmpty() ? "" : ci.getProduct().getImages().get(0));
        items.add(oi);
        subtotal += ci.getProduct().getPrice() * ci.getQty();
      }
    }
    if (items.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));

    // Address text
    String addressText;
    if (r.addressId != null && owner != null) {
      var a = addresses.findById(r.addressId);
      if (a.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "Invalid address"));
      Address ad = a.get();
      name = ad.getFullName(); phone = ad.getPhone();
      addressText = ad.getFullName() + ", " + ad.getLine1()
        + (ad.getLine2() != null && !ad.getLine2().isBlank() ? ", " + ad.getLine2() : "")
        + ", " + nz(ad.getCity()) + " " + nz(ad.getPincode()) + ", " + nz(ad.getState())
        + " (" + ad.getPhone() + ")";
    } else {
      if (r.fullName != null) name = r.fullName;
      if (r.phone != null) phone = r.phone;
      addressText = nz(r.fullName) + ", " + nz(r.line1) + ", " + nz(r.city) + " " + nz(r.pincode) + ", " + nz(r.state) + " (" + nz(r.phone) + ")";
      if (owner != null && (r.line1 != null && !r.line1.isBlank())) {
        // auto-save address to address book
        Address ad = new Address();
        ad.setUser(owner);
        ad.setFullName(nz(r.fullName, owner.getName()));
        ad.setPhone(nz(r.phone, owner.getPhone()));
        ad.setLine1(r.line1); ad.setCity(r.city); ad.setState(r.state); ad.setPincode(r.pincode);
        ad.setLabel("Home");
        ad.setDefault(addresses.findByUserId(owner.getId()).isEmpty());
        addresses.save(ad);
      }
    }

    String mode = r.paymentMode == null ? "COD" : r.paymentMode.toUpperCase();
    double delivery = subtotal >= 999 ? 0 : 49;
    double total = subtotal + delivery;

    ShopOrder o = new ShopOrder();
    o.setUser(owner);
    o.setCustomerName(name);
    o.setCustomerEmail(email);
    o.setCustomerPhone(phone);
    o.setAddressText(addressText);
    o.setSubtotal(subtotal);
    o.setDeliveryFee(delivery);
    o.setTotal(total);
    o.setPaymentMode(mode);
    o.setPaymentStatus(mode.equals("COD") || mode.equals("WHATSAPP") ? "PENDING" : "PENDING");
    o.setStatus("PLACED");
    for (OrderItem oi : items) oi.setOrder(o);
    o.setItems(items);
    orders.save(o);
    o.setOrderNo("FL-" + (1000 + o.getId()));
    // WhatsApp confirmation link
    StringBuilder msg = new StringBuilder("Hi Florvvia 🌸\nNew order " + o.getOrderNo() + "\n");
    for (OrderItem oi : items) msg.append("• ").append(oi.getProductName()).append(" x").append(oi.getQty()).append(" - ₹").append(oi.getPrice() * oi.getQty()).append("\n");
    msg.append("Total: ₹").append(total).append(" (").append(mode).append(")\n");
    msg.append("Name: ").append(nz(name)).append("\nAddress: ").append(addressText);
    String wa = "https://wa.me/" + whatsappNumber + "?text=" + URLEncoder.encode(msg.toString(), StandardCharsets.UTF_8);
    o.setWhatsappLink(wa);
    orders.save(o);

    if (owner != null && (r.items == null || r.items.isEmpty())) {
      cart.deleteByUserId(owner.getId()); // clear cart after order from cart
    }
    return ResponseEntity.ok(o);
  }

  @PostMapping("/{id}/cancel")
  public ResponseEntity<?> cancel(@AuthenticationPrincipal User u, @PathVariable Long id) {
    // Customer cancellation is disabled — only admins can cancel (via admin panel).
    if (u == null) return ResponseEntity.status(401).body(Map.of("error", "Login required"));
    if (!"ADMIN".equalsIgnoreCase(u.getRole()))
      return ResponseEntity.status(403).body(Map.of("error", "Cancellation is disabled. Contact us on Instagram @florvvia for help."));
    var opt = orders.findById(id);
    if (opt.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Not found"));
    ShopOrder o = opt.get();
    if (!o.getStatus().equals("PLACED") && !o.getStatus().equals("CONFIRMED"))
      return ResponseEntity.badRequest().body(Map.of("error", "Cannot cancel now"));
    o.setStatus("CANCELLED");
    orders.save(o);
    return ResponseEntity.ok(o);
  }

  private String nz(String s) { return s == null ? "" : s; }
  private String nz(String s, String fb) { return (s == null || s.isBlank()) ? (fb == null ? "" : fb) : s; }
}
