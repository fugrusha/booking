package com.booking.unitmanager.service.impl;

import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.exception.IllegalStateEntityException;
import com.booking.unitmanager.mapper.PaymentMapper;
import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.dao.BookingRepository;
import com.booking.unitmanager.model.enums.PaymentStatus;
import com.booking.unitmanager.model.entity.PaymentEntity;
import com.booking.unitmanager.dao.PaymentRepository;
import com.booking.unitmanager.model.dto.PaymentCreateDTO;
import com.booking.unitmanager.model.dto.PaymentReadDTO;
import com.booking.unitmanager.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;

    @Transactional
    @Override
    public PaymentReadDTO createPayment(PaymentCreateDTO paymentCreateDTO) {
        BookingEntity booking = getBookingById(paymentCreateDTO.getBookingId());

        if (!booking.isAvailableStatusToPay()) {
            throw new IllegalStateEntityException("Booking cannot be paid in its current state");
        }
        if (booking.isPaymentDeadlinePassed()) {
            throw new IllegalStateEntityException("Payment deadline has passed");
        }

        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setAmount(paymentCreateDTO.getAmount());
        paymentEntity.setBooking(booking);
        paymentEntity.setStatus(PaymentStatus.COMPLETED);
        PaymentEntity createdPayment = paymentRepository.save(paymentEntity);

        booking.markAsPaid();
        bookingRepository.save(booking);
        return paymentMapper.toReadDTO(createdPayment);
    }

    private BookingEntity getBookingById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking not found with id: " + id));
    }
}
