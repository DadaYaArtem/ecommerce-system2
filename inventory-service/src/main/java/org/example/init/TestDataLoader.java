package org.example.init;

import jakarta.annotation.PostConstruct;
import org.example.model.Inventory;
import org.example.repository.InventoryRepository;
import org.springframework.stereotype.Component;

@Component
public class TestDataLoader {

    private final InventoryRepository inventoryRepository;

    public TestDataLoader(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @PostConstruct
    public void init() {
        if (inventoryRepository.count() == 0) {
            inventoryRepository.save(new Inventory("p123", "w1", 3333));
//            inventoryRepository.save(new Inventory("p123", "w1", 2));
            inventoryRepository.save(new Inventory("p789", "w2", 2222));
            inventoryRepository.save(new Inventory("p456", "w3", 5555));
            System.out.println("✅ [TestDataLoader] Стартові залишки успішно додано в inventory");
        }
    }
}
