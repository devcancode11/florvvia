package com.florvvia.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class ShopOrder {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String orderNo; // e.g. FL-1001

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  private String customerName;
  private String customerEmail;
  private String customerPhone;

  @Column(length = 2000)
  private String addressText;

  private double subtotal;
  private double deliveryFee;
  private double total;
  private String paymentMode = "COD"; // COD, UPI, RAZORPAY, WHATSAPP
  private String paymentStatus = "PENDING"; // PENDING, PAID, FAILED
  private String status = "PLACED"; // PLACED, CONFIRMED, SHIPPED, DELIVERED, CANCELLED

  @Column(length = 2000)
  private String whatsappLink;

  private LocalDateTime createdAt = LocalDateTime.now();

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
  private List<OrderItem> items = new ArrayList<>();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getOrderNo() { return orderNo; }
  public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public String getCustomerName() { return customerName; }
  public void setCustomerName(String customerName) { this.customerName = customerName; }
  public String getCustomerEmail() { return customerEmail; }
  public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
  public String getCustomerPhone() { return customerPhone; }
  public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
  public String getAddressText() { return addressText; }
  public void setAddressText(String addressText) { this.addressText = addressText; }
  public double getSubtotal() { return subtotal; }
  public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
  public double getDeliveryFee() { return deliveryFee; }
  public void setDeliveryFee(double deliveryFee) { this.deliveryFee = deliveryFee; }
  public double getTotal() { return total; }
  public void setTotal(double total) { this.total = total; }
  public String getPaymentMode() { return paymentMode; }
  public void setPaymentMode(String paymentMode) { this.paymentMode = paymentMode; }
  public String getPaymentStatus() { return paymentStatus; }
  public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
  public String getStatus() { return status; }
  public void setStatus(String status) { this.status = status; }
  public String getWhatsappLink() { return whatsappLink; }
  public void setWhatsappLink(String whatsappLink) { this.whatsappLink = whatsappLink; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public List<OrderItem> getItems() { return items; }
  public void setItems(List<OrderItem> items) { this.items = items; }
}
