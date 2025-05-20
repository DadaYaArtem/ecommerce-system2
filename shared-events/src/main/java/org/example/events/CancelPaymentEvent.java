package org.example.events;

/**
 * Event that signals a payment request should be canceled.
 * This prevents the payment service from processing requests for orders
 * that have already timed out or been canceled.
 */
public class CancelPaymentEvent {
    
    private String orderId;
    private String customerId;
    
    public CancelPaymentEvent() {
    }
    
    public CancelPaymentEvent(String orderId, String customerId) {
        this.orderId = orderId;
        this.customerId = customerId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getCustomerId() {
        return customerId;
    }
    
    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }
    
    @Override
    public String toString() {
        return "CancelPaymentEvent{" +
                "orderId='" + orderId + '\'' +
                ", customerId='" + customerId + '\'' +
                '}';
    }
}