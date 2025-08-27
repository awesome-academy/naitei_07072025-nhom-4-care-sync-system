package com.example.backend.controller;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.constant.ApiConstants;
import com.example.backend.dto.ApiResponse;
import com.example.backend.dto.InvoiceResponse;
import com.example.backend.service.InvoiceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping(ApiConstants.INVOICES_ENDPOINT)
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Invoice", description = "Invoice management APIs")
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final MessageSource messageSource;

    @GetMapping("/appointment/{appointmentId}")
    @Operation(summary = "Get invoice by appointment ID", description = "Retrieve invoice information for a specific appointment")
    @PreAuthorize("hasRole('PATIENT') or hasRole('DOCTOR') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InvoiceResponse>> getInvoiceByAppointmentId(
            @PathVariable Long appointmentId) {
        log.info("Getting invoice for appointment: {}", appointmentId);

        InvoiceResponse invoice = invoiceService.getInvoiceByAppointmentId(appointmentId);
        String message = messageSource.getMessage("success.invoice.retrieved", null,
                LocaleContextHolder.getLocale());

        return ResponseEntity.ok(ApiResponse.success(invoice, message));
    }
} 
