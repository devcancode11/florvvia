package com.florvvia.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "addresses")
public class Address {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @JsonIgnore
  private User user;

  private String label = "Home";
  @Column(nullable = false)
  private String fullName;
  private String phone;
  @Column(nullable = false, length = 1000)
  private String line1;
  private String line2;
  private String city;
  private String state;
  private String pincode;
  private boolean isDefault = false;

  public Long getId() { return id; }
  public void setId(Long id) { this.id = id; }
  public User getUser() { return user; }
  public void setUser(User user) { this.user = user; }
  public String getLabel() { return label; }
  public void setLabel(String label) { this.label = label; }
  public String getFullName() { return fullName; }
  public void setFullName(String fullName) { this.fullName = fullName; }
  public String getPhone() { return phone; }
  public void setPhone(String phone) { this.phone = phone; }
  public String getLine1() { return line1; }
  public void setLine1(String line1) { this.line1 = line1; }
  public String getLine2() { return line2; }
  public void setLine2(String line2) { this.line2 = line2; }
  public String getCity() { return city; }
  public void setCity(String city) { this.city = city; }
  public String getState() { return state; }
  public void setState(String state) { this.state = state; }
  public String getPincode() { return pincode; }
  public void setPincode(String pincode) { this.pincode = pincode; }
  public boolean isDefault() { return isDefault; }
  public void setDefault(boolean aDefault) { isDefault = aDefault; }
}
