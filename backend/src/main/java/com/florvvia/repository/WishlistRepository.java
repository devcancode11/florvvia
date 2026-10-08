package com.florvvia.repository;

import com.florvvia.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
  List<WishlistItem> findByUserId(Long userId);
  Optional<WishlistItem> findByUserIdAndProductId(Long userId, Long productId);
}
