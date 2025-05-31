package org.example.repository;

import org.example.model.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    List<Inventory> findByProductId(String productId);
    List<Inventory> findByProductIdOrderByAvailableQuantityDesc(String productId);
    List<Inventory> findByWarehouseId(String warehouseId);
    Optional<Inventory> findByProductIdAndWarehouseId(String productId, String warehouseId);
}
