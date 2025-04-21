package org.example.service;

import org.example.model.Inventory;
import org.example.model.InventoryReservation;
import org.example.repository.InventoryRepository;
import org.example.repository.InventoryReservationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryReservationRepository reservationRepository;

    public InventoryService(InventoryRepository inventoryRepository,
                            InventoryReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }

    public boolean tryReserve(String orderId, String productId, int quantity) {
        // ✅ Сначала проверим, есть ли смысл резервировать
        if (!canReserve(productId, quantity)) {
            return false;
        }

        List<Inventory> inventories = inventoryRepository.findByProductIdOrderByAvailableQuantityDesc(productId);

        int remaining = quantity;
        List<InventoryReservation> toReserve = new ArrayList<>();

        for (Inventory inv : inventories) {
            if (remaining <= 0) break;

            int canReserve = Math.min(remaining, inv.getAvailableQuantity());
            if (canReserve <= 0) continue;

            System.out.println("⬅️ До резерву: " + inv.getWarehouseId() + " → " + inv.getAvailableQuantity());

            inv.setAvailableQuantity(inv.getAvailableQuantity() - canReserve);
            inventoryRepository.save(inv);

            System.out.println("✅ Після резерву: " + inv.getWarehouseId() + " → " + inv.getAvailableQuantity());

            InventoryReservation reservation = new InventoryReservation();
            reservation.setOrderId(orderId);
            reservation.setProductId(productId);
            reservation.setWarehouseId(inv.getWarehouseId());
            reservation.setQuantity(canReserve);
            reservation.setStatus("RESERVED");

            toReserve.add(reservation);
            remaining -= canReserve;
        }

        reservationRepository.saveAll(toReserve);
        return true;
    }

    public void releaseReservation(String orderId) {
        List<InventoryReservation> reservations = reservationRepository.findByOrderIdAndStatus(orderId, "RESERVED");

        for (InventoryReservation r : reservations) {
            Inventory inv = inventoryRepository.findByProductIdOrderByAvailableQuantityDesc(r.getProductId()).stream()
                    .filter(i -> i.getWarehouseId().equals(r.getWarehouseId()))
                    .findFirst().orElseThrow();

            System.out.println("🔁 До зняття резерву: " + inv.getWarehouseId() + " → " + inv.getAvailableQuantity());
            inv.setAvailableQuantity(inv.getAvailableQuantity() + r.getQuantity());
            inventoryRepository.save(inv);
            System.out.println("♻️ Після зняття резерву: " + inv.getWarehouseId() + " → " + inv.getAvailableQuantity());

            r.setStatus("RELEASED");
            reservationRepository.save(r);
        }
    }

    public boolean canReserve(String productId, int quantity) {
        int totalAvailable = inventoryRepository.findByProductIdOrderByAvailableQuantityDesc(productId).stream()
                .mapToInt(Inventory::getAvailableQuantity)
                .sum();

        return totalAvailable >= quantity;
    }
}
