package org.example.events;

public class InventoryErrorEvent {
    private String orderId;
    private String errorMessage;

    public InventoryErrorEvent(String orderId, String errorMessage) {
        this.orderId = orderId;
        this.errorMessage = errorMessage;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "InventoryErrorEvent{" +
                "orderId='" + orderId + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
