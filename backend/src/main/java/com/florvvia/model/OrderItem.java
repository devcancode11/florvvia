package com.florvvia.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "order_items")
public class OrderItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id")
  @JsonIgnore
  private ShopOrder order;

  private Long productId;
  private String productName;
  private double price;
  private int qty;
  private String image;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public ShopOrder getOrder() { return order; }
  public void setOrder(ShopOrder order) { this.order = order; }
  public Long getProductId() { return productId; }
  public void setProductId(Long productId) { this.productId = productId; }
  public String getProductName() { return productName; }
  public void setProductName(String productName) { this.productName = productName; }
  public double getPrice() { return price; }
  public void setPrice(double price) { this.price = price; }
  public int getQty() { return qty; }
  public void setQty(int qty) { this.qty = qty; }
  public String getImage() { return image; }
  public void setImage(String image) { this.image = image; }
}
