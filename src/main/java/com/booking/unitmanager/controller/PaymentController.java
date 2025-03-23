package com.booking.unitmanager.controller;

import com.booking.unitmanager.model.dto.PaymentCreateDTO;
import com.booking.unitmanager.model.dto.PaymentReadDTO;
import com.booking.unitmanager.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentReadDTO> createPayment(@Valid @RequestBody PaymentCreateDTO createDTO) {
        PaymentReadDTO payment = paymentService.createPayment(createDTO);
        return ResponseEntity.ok(payment);
    }
}
