package com.florvvia.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String category = "flowers"; // flowers, bouquets, keychains, others

  @Column(length = 2000)
  private String description;

  @Column(nullable = false)
  private double price = 0;

  private Double mrp; // optional strikethrough
  private String tag; // New, Bestseller, etc.
  private String code; // FL-XXX-01

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
  @Column(name = "image_url", length = 1000)
  private List<String> images = new ArrayList<>();

  private int stock = 100;
  private boolean active = true;
  private double ratingAvg = 0;
  private int ratingCount = 0;

  private LocalDateTime createdAt = LocalDateTime.now();
  private LocalDateTime updatedAt = LocalDateTime.now();

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }
  public String getCategory() { return category; }
  public void setCategory(String category) { this.category = category; }
  public String getDescription() { return description; }
  public void setDescription(String description) { this.description = description; }
  public double getPrice() { return price; }
  public void setPrice(double price) { this.price = price; }
  public Double getMrp() { return mrp; }
  public void setMrp(Double mrp) { this.mrp = mrp; }
  public String getTag() { return tag; }
  public void setTag(String tag) { this.tag = tag; }
  public String getCode() { return code; }
  public void setCode(String code) { this.code = code; }
  public List<String> getImages() { return images; }
  public void setImages(List<String> images) { this.images = images; }
  public int getStock() { return stock; }
  public void setStock(int stock) { this.stock = stock; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public double getRatingAvg() { return ratingAvg; }
  public void setRatingAvg(double ratingAvg) { this.ratingAvg = ratingAvg; }
  public int getRatingCount() { return ratingCount; }
  public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
  public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
