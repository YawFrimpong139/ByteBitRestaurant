package org.codewithzea.orderservice.service;


import org.codewithzea.orderservice.exceptions.PaymentFailedException;
import org.codewithzea.orderservice.exceptions.RefundFailedException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class PaymentService {

    // In-memory store for demo purposes - replace with actual payment gateway integration
    private final ConcurrentMap<String, PaymentRecord> paymentRecords = new ConcurrentHashMap<>();

    /**
     * Processes a payment transaction
     * @param amount The amount to charge
     * @param customerId The customer ID
     * @return Payment transaction ID
     * @throws PaymentFailedException If payment processing fails
     */
    @Retryable(retryFor = PaymentFailedException.class,
            backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional
    public String processPayment(BigDecimal amount, UUID customerId) {
        // In production, integrate with real payment gateway (Stripe, PayPal, etc.)
        // This is a mock implementation with simulated failures

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentFailedException("Invalid payment amount: " + amount);
        }

        // Simulate 10% failure rate for demo purposes
        if (Math.random() > 0.9) {
            throw new PaymentFailedException("Payment processing failed due to gateway error");
        }

        String paymentId = "pay_" + UUID.randomUUID();
        paymentRecords.put(paymentId, new PaymentRecord(paymentId, amount, customerId, "COMPLETED"));

        return paymentId;
    }

    /**
     * Initiates a refund for a payment
     *
     * @param paymentId The original payment ID
     * @throws RefundFailedException If refund processing fails
     */
    @Retryable(retryFor = RefundFailedException.class,
            backoff = @Backoff(delay = 100, multiplier = 2))
    @Transactional
    public void initiateRefund(String paymentId) {
        PaymentRecord payment = paymentRecords.get(paymentId);

        if (payment == null) {
            throw new RefundFailedException("Original payment not found: " + paymentId);
        }

        if (!"COMPLETED".equals(payment.status())) {
            throw new RefundFailedException("Cannot refund payment with status: " + payment.status());
        }

        // Simulate 10% failure rate for demo purposes
        if (Math.random() > 0.9) {
            throw new RefundFailedException("Refund processing failed due to gateway error");
        }

        String refundId = "ref_" + UUID.randomUUID();
        paymentRecords.put(paymentId, payment.withStatus("REFUNDED"));

    }

    /**
     * Checks payment status
     * @param paymentId The payment ID to check
     * @return Current payment status
     */
    public String checkPaymentStatus(String paymentId) {
        PaymentRecord payment = paymentRecords.get(paymentId);
        return payment != null ? payment.status() : "NOT_FOUND";
    }

    // Internal record to track payment state
    private record PaymentRecord(
            String paymentId,
            BigDecimal amount,
            UUID customerId,
            String status
    ) {
        public PaymentRecord withStatus(String newStatus) {
            return new PaymentRecord(paymentId, amount, customerId, newStatus);
        }
    }
}