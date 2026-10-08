package com.florvvia.config;

import com.florvvia.model.*;
import com.florvvia.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {
  @Autowired private UserRepository users;
  @Autowired private ProductRepository products;
  @Autowired private PasswordEncoder enc;

  @Override
  public void run(String... args) {
    if (users.findByEmail("admin@florvvia.in").isEmpty()) {
      User a = new User();
      a.setName("Florvvia Admin");
      a.setEmail("admin@florvvia.in");
      a.setPhone("7417566249");
      a.setPasswordHash(enc.encode("admin123"));
      a.setRole("ADMIN");
      users.save(a);
      System.out.println("Seeded admin: admin@florvvia.in / admin123");
    }
    if (products.count() == 0) {
      seed("Pipe-cleaner Purple Lily", "flowers", 99.0, "New", "FL-LILY-PURPLE-01",
        "Elegant handmade pipe-cleaner purple lily that stays beautiful forever. Perfect for gifting and decor.",
        List.of("images/flowers/lily-purple1.jpg", "images/flowers/lily-purple2.jpg", "images/flowers/lily-purple3.jpg"));
      seed("Pipe-cleaner Cherry Lily", "flowers", 119.0, "Bestseller", "FL-LILY-CHERRY-01",
        "Beautiful handmade cherry-colored pipe-cleaner lily that never fades. Perfect for romantic gifts and decor.",
        List.of("images/flowers/lily-cherry1.jpg", "images/flowers/lily-cherry2.jpg", "images/flowers/lily-cherry3.jpg"));
      seed("Pipe-cleaner Pink Lily", "flowers", 119.0, "New", "FL-LILY-PINK-01",
        "Beautiful handmade pink pipe-cleaner lily. Soft, aesthetic and everlasting.",
        List.of("images/flowers/lily-pink1.jpg", "images/flowers/lily-pink2.jpg", "images/flowers/lily-pink3.jpg"));
      seed("Pipe-cleaner Sakura Flower", "flowers", 99.0, "New", "FL-SAKURA-01",
        "Delicate handmade sakura-inspired flower. Soft pink tones symbolizing love and new beginnings.",
        List.of("images/flowers/sakura1.jpg", "images/flowers/sakura2.jpg", "images/flowers/sakura3.jpg"));
      seed("Pipe-cleaner Sunflower", "flowers", 149.0, "New", "FL-SUN-01",
        "Bright and cheerful handmade sunflower. Symbol of happiness — single flower or bouquet.",
        List.of("images/flowers/sunflower1.jpg"));
      seed("Sunflower Pot", "flowers", 249.0, "Bestseller", "FL-POT-SUN-01",
        "Handmade sunflower pot. Cute desk decor piece that brings warmth and happiness. Custom pot colors available.",
        List.of("images/flowers/sunflower-pot1.jpg", "images/flowers/sunflower-pot2.jpg", "images/flowers/sunflower-pot3.jpg"));
      seed("Purple Lily & Tulip Bouquet", "bouquets", 799.0, "New", "FL-BOUQUET-01",
        "Elegant handmade bouquet featuring purple lilies and pastel tulips. Custom color combinations available.",
        List.of("images/bouquets/purple-lily-tulip1.jpg", "images/bouquets/purple-lily-tulip2.jpg", "images/bouquets/purple-lily-tulip3.jpg"));
      seed("Red Rose Bouquet", "bouquets", 799.0, "Bestseller", "FL-BOUQUET-02",
        "Handmade bouquet with premium pipe-cleaner roses. Perfect for anniversaries, birthdays and proposals.",
        List.of("images/bouquets/bouquet1.jpg"));
      seed("Heart Keychain", "keychains", 99.0, "New", "FL-KEY-HEART-01",
        "Soft handmade crochet heart keychain. Lightweight, durable and perfect for gifting.",
        List.of("images/keychains/heart1.jpg"));
      seed("Star Keychain", "keychains", 99.0, "New", "FL-KEY-STAR-01",
        "Mini crochet star keychain. Cute keepsake for bags and keys.",
        List.of("images/keychains/star1.jpg"));
      seed("Bow Keychain", "keychains", 99.0, "New", "FL-KEY-BOW-01",
        "Mini crochet bow keychain. Aesthetic handmade gift.",
        List.of("images/keychains/k1.jpg"));
      System.out.println("Seeded products: " + products.count());
    }
  }

  private void seed(String name, String cat, double price, String tag, String code, String desc, List<String> imgs) {
    Product p = new Product();
    p.setName(name);
    p.setCategory(cat);
    p.setPrice(price);
    p.setTag(tag);
    p.setCode(code);
    p.setDescription(desc);
    p.setImages(imgs);
    p.setStock(100);
    p.setActive(true);
    products.save(p);
  }
}
