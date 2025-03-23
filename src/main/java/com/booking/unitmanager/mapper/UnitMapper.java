package com.booking.unitmanager.mapper;

import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.model.dto.UnitCreateDTO;
import com.booking.unitmanager.model.dto.UnitReadDTO;
import com.booking.unitmanager.model.dto.UnitUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UnitMapper {

    UnitReadDTO toReadDTO(UnitEntity unit);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    UnitEntity toEntity(UnitCreateDTO unitDTO);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "bookings", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "totalCost", ignore = true)
    void updateEntityFromDTO(UnitUpdateDTO unitDTO, @MappingTarget UnitEntity unit);
}
