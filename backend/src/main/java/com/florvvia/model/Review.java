package com.florvvia.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @JsonIgnore
  private User user;

  private String userName;
  private int rating = 5;
  @Column(length = 2000)
  private String comment;
  private LocalDateTime createdAt = LocalDateTime.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public Product getProduct() { return product; }
  public void setProduct(Product product) { this.product = product; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public String getUserName() { return userName; }
  public void setUserName(String userName) { this.userName = userName; }
  public int getRating() { return rating; }
  public void setRating(int rating) { this.rating = rating; }
  public String getComment() { return comment; }
  public void setComment(String comment) { this.comment = comment; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
