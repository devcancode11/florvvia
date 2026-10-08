package com.florvvia.repository;

import com.florvvia.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByUserId(Long userId);
}
