package com.booking.unitmanager.mapper;

import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.model.dto.BookingCreateDTO;
import com.booking.unitmanager.model.dto.BookingReadDTO;
import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.model.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {UnitMapper.class})
public interface BookingMapper {

    @Mapping(source = "unit.id", target = "unitId")
    @Mapping(source = "user.id", target = "userId")
    BookingReadDTO toReadDTO(BookingEntity booking);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "paymentDeadline", ignore = true)
    @Mapping(target = "unit", source = "unitEntity")
    @Mapping(target = "user", source = "userEntity")
    BookingEntity toEntity(BookingCreateDTO bookingDTO, UnitEntity unitEntity, UserEntity userEntity);
}
