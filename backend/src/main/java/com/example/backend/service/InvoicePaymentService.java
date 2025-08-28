package com.example.backend.service;

import com.example.backend.entity.Payment;

/**
 * Service to handle invoice-payment relationship logic
 */
public interface InvoicePaymentService {

    /**
     * Update invoice status to PAID and cancel other pending payments when a payment is successful
     * 
     * @param successfulPayment The payment that was just marked as successful
     */
    void handleSuccessfulPayment(Payment successfulPayment);

    /**
     * Handle invoice cancellation when appointment is rejected
     * 
     * @param appointmentId The appointment ID that was rejected
     */
    void handleAppointmentRejection(Long appointmentId);

    /**
     * Check if invoice is eligible for payment creation
     * 
     * @param invoiceId The invoice ID to check
     * @return true if invoice can have new payments created
     */
    boolean isInvoiceEligibleForPayment(Long invoiceId);
} 
