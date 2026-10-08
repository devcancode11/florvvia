package com.florvvia.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "wishlist_items", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id","product_id"}))
public class WishlistItem {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }
}
