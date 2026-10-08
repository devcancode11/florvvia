package com.florvvia.repository;

import com.florvvia.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface CartRepository extends JpaRepository<CartItem, Long> {
  List<CartItem> findByUserId(Long userId);
  Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
  void deleteByUserId(Long userId);
}
