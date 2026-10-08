package com.florvvia.dto;

import java.util.List;

public class OrderRequest {
  public Long addressId;
  // guest / direct fields (optional if addressId given)
  public String fullName;
  public String phone;
  public String line1;
  public String city;
  public String state;
  public String pincode;
  public String paymentMode; // COD, UPI, RAZORPAY, WHATSAPP
  public List<Item> items; // for guest checkout; logged-in users may omit (uses cart)

  public static class Item {
    public Long productId;
    public Integer qty;
  }
}
