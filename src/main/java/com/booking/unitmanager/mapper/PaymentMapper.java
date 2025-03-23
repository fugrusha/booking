package com.booking.unitmanager.mapper;

import com.booking.unitmanager.model.entity.PaymentEntity;
import com.booking.unitmanager.model.dto.PaymentReadDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = BookingMapper.class)
public interface PaymentMapper {

    PaymentReadDTO toReadDTO(PaymentEntity paymentEntity);
}
