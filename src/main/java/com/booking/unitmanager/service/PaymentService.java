package com.booking.unitmanager.service;

import com.booking.unitmanager.model.dto.PaymentCreateDTO;
import com.booking.unitmanager.model.dto.PaymentReadDTO;

public interface PaymentService {

    PaymentReadDTO createPayment(PaymentCreateDTO paymentCreateDTO);
}
