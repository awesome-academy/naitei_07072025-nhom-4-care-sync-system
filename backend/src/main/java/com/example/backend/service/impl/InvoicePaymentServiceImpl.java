package com.example.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.backend.constant.enums.InvoiceStatus;
import com.example.backend.constant.enums.PaymentStatus;
import com.example.backend.entity.Invoice;
import com.example.backend.entity.Payment;
import com.example.backend.exception.BusinessException;
import com.example.backend.repository.InvoiceRepository;
import com.example.backend.repository.PaymentRepository;
import com.example.backend.service.InvoicePaymentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InvoicePaymentServiceImpl implements InvoicePaymentService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    @Transactional
    public void handleSuccessfulPayment(Payment successfulPayment) {
        Invoice invoice = successfulPayment.getInvoice();

        if (invoice.getStatus() == InvoiceStatus.PENDING) {
            updateInvoiceStatusToPaid(invoice);
        }

        cancelOtherPendingPayments(invoice.getId(), successfulPayment.getId(), invoice.getInvoiceCode());
    }

    private void updateInvoiceStatusToPaid(Invoice invoice) {
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
        log.info("Updated invoice {} status to PAID", invoice.getInvoiceCode());
    }

    private void cancelOtherPendingPayments(Long invoiceId, Long successfulPaymentId, String invoiceCode) {
        List<Payment> pendingPayments = paymentRepository.findByInvoiceIdAndStatus(
                invoiceId, PaymentStatus.PENDING);

        List<Payment> paymentsToCancel = pendingPayments.stream()
                .filter(payment -> !payment.getId().equals(successfulPaymentId))
                .peek(payment -> {
                    payment.setStatus(PaymentStatus.CANCELLED);
                    log.info("Cancelled pending payment {} for invoice {}", 
                            payment.getTransactionCode(), invoiceCode);
                })
                .toList();

        if (!paymentsToCancel.isEmpty()) {
            paymentRepository.saveAll(paymentsToCancel);
        }
    }

    @Override
    @Transactional
    public void handleAppointmentRejection(Long appointmentId) {
        var invoiceOpt = invoiceRepository.findByAppointmentId(appointmentId);

        if (invoiceOpt.isEmpty()) {
            log.info("No invoice found for rejected appointment: {}", appointmentId);
            return;
        }

        Invoice invoice = invoiceOpt.get();
        InvoiceStatus status = invoice.getStatus();

        switch (status) {
            case PENDING:
                cancelPendingInvoiceAndPayments(invoice);
                break;
            case PAID:
                handlePaidInvoiceRefund(invoice);
                break;
            case CANCELLED:
                log.info("Invoice {} is already CANCELLED for appointment {}", 
                        invoice.getInvoiceCode(), appointmentId);
                break;
            default:
                log.warn("Unknown invoice status {} for appointment {}", status, appointmentId);
        }
    }

    private void cancelPendingInvoiceAndPayments(Invoice invoice) {
        // Cancel PENDING invoice
        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoiceRepository.save(invoice);
        log.info("Cancelled PENDING invoice {} due to appointment rejection", invoice.getInvoiceCode());
        
        // Cancel all pending payments for this invoice
        List<Payment> pendingPayments = paymentRepository.findByInvoiceIdAndStatus(
                invoice.getId(), PaymentStatus.PENDING);
        
        List<Payment> paymentsToCancel = pendingPayments.stream()
                .peek(payment -> {
                    payment.setStatus(PaymentStatus.CANCELLED);
                    log.info("Cancelled pending payment {} due to appointment rejection", 
                            payment.getTransactionCode());
                })
                .toList();

        if (!paymentsToCancel.isEmpty()) {
            paymentRepository.saveAll(paymentsToCancel);
        }
    }

    private void handlePaidInvoiceRefund(Invoice invoice) {
        log.info("Handling refund for PAID invoice {} due to appointment rejection", invoice.getInvoiceCode());
        
        // Find the successful payment
        List<Payment> successfulPayments = paymentRepository.findByInvoiceIdAndStatus(
                invoice.getId(), PaymentStatus.SUCCESSFUL);
        
        if (!successfulPayments.isEmpty()) {
            Payment successfulPayment = successfulPayments.get(0);
            
            // Mark payment as REFUNDED
            successfulPayment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(successfulPayment);
            
            // Mark invoice as CANCELLED (refunded)
            invoice.setStatus(InvoiceStatus.CANCELLED);
            invoiceRepository.save(invoice);
            
            log.info("Marked payment {} as REFUNDED and invoice {} as CANCELLED due to appointment rejection", 
                    successfulPayment.getTransactionCode(), invoice.getInvoiceCode());
            
            // TODO: Integrate with payment gateway refund API
            // For now, we just mark as REFUNDED in our system
            // In production, you would call MoMo/VNPay refund API here
            
        } else {
            log.warn("No successful payment found for PAID invoice {}", invoice.getInvoiceCode());
        }
    }

    @Override
    public boolean isInvoiceEligibleForPayment(Long invoiceId) {
        try {
            Invoice invoice = invoiceRepository.findById(invoiceId)
                    .orElseThrow(() -> new BusinessException("error.invoice.not.found"));
            
            return invoice.getStatus() == InvoiceStatus.PENDING;
        } catch (Exception e) {
            log.warn("Error checking invoice eligibility for payment: {}", e.getMessage());
            return false;
        }
    }
} 
