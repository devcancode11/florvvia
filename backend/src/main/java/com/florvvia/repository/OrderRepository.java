package com.florvvia.repository;

import com.florvvia.model.ShopOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<ShopOrder, Long> {
  List<ShopOrder> findByUserIdOrderByCreatedAtDesc(Long userId);
  List<ShopOrder> findAllByOrderByCreatedAtDesc();
}
