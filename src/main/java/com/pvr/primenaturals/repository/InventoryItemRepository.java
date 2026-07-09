package com.pvr.primenaturals.repository;

import com.pvr.primenaturals.entity.InventoryItem;
import com.pvr.primenaturals.entity.Product;
import com.pvr.primenaturals.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, Long> {
    List<InventoryItem> findByProduct(Product product);
    List<InventoryItem> findByWarehouse(Warehouse warehouse);
    Optional<InventoryItem> findByProductAndWarehouse(Product product, Warehouse warehouse);
}
