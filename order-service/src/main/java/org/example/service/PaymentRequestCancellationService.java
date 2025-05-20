package org.example.service;

import org.example.events.CancelPaymentEvent;
import org.example.messaging.CancelPaymentEventProducer;
import org.example.model.Order;
import org.springframework.stereotype.Service;

/**
 * Service to handle payment request cancellations.
 * This ensures that when timeouts occur, we properly cancel the payment request
 * and don't process any late responses.
 */
@Service
public class PaymentRequestCancellationService {
    
    private final CancelPaymentEventProducer cancelPaymentProducer;
    
    public PaymentRequestCancellationService(CancelPaymentEventProducer cancelPaymentProducer) {
        this.cancelPaymentProducer = cancelPaymentProducer;
    }
    
    /**
     * Cancels a payment request for an order by sending a cancellation event
     * to the payment service.
     * 
     * @param order The order for which to cancel the payment
     */
    public void cancelPaymentRequest(Order order) {
        CancelPaymentEvent cancelEvent = new CancelPaymentEvent(
                order.getId(),
                order.getCustomerId()
        );
        
        cancelPaymentProducer.sendCancelPayment(cancelEvent);
    }
}