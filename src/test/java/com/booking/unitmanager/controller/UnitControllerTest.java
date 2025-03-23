package com.booking.unitmanager.controller;

import com.booking.unitmanager.exception.EntityNotFoundException;
import com.booking.unitmanager.model.dto.UnitCreateDTO;
import com.booking.unitmanager.model.dto.UnitFilter;
import com.booking.unitmanager.model.dto.UnitReadDTO;
import com.booking.unitmanager.model.dto.UnitUpdateDTO;
import com.booking.unitmanager.model.enums.AccommodationType;
import com.booking.unitmanager.service.UnitService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UnitController.class)
class UnitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UnitService unitService;

    @Nested
    class CreateUnitTests {
        @Test
        void createUnit_WithValidData_ShouldReturnCreated() throws Exception {
            UnitReadDTO unitReadDTO = getUnitReadDTO();
            UnitCreateDTO unitCreateDTO = getUnitCreateDTO();
            when(unitService.createUnit(any(UnitCreateDTO.class))).thenReturn(unitReadDTO);

            mockMvc.perform(post("/api/v1/units")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(unitCreateDTO)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(unitReadDTO.getId()))
                    .andExpect(jsonPath("$.numberOfRooms").value(unitReadDTO.getNumberOfRooms()))
                    .andExpect(jsonPath("$.accommodationType").value(unitReadDTO.getAccommodationType().toString()))
                    .andExpect(jsonPath("$.floor").value(unitReadDTO.getFloor()))
                    .andExpect(jsonPath("$.baseCost").value(unitReadDTO.getBaseCost().doubleValue()))
                    .andExpect(jsonPath("$.totalCost").value(unitReadDTO.getTotalCost().doubleValue()))
                    .andExpect(jsonPath("$.description").value(unitReadDTO.getDescription()));

            verify(unitService).createUnit(any(UnitCreateDTO.class));
        }

        @Test
        void createUnit_WithInvalidData_ShouldReturnBadRequest() throws Exception {
            UnitCreateDTO invalidDTO = new UnitCreateDTO();

            mockMvc.perform(post("/api/v1/units")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void createUnit_WithInvalidNumberOfRooms_ShouldReturnBadRequest() throws Exception {
            UnitCreateDTO invalidDTO = new UnitCreateDTO();
            invalidDTO.setNumberOfRooms(0); // Invalid: must be at least 1
            invalidDTO.setAccommodationType(AccommodationType.APARTMENTS);
            invalidDTO.setFloor(3);
            invalidDTO.setBaseCost(new BigDecimal("150.00"));

            mockMvc.perform(post("/api/v1/units")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidDTO)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetUnitByIdTests {

        @Test
        void getUnitById_WithExistingId_ShouldReturnUnit() throws Exception {
            UnitReadDTO unitReadDTO = getUnitReadDTO();
            when(unitService.getUnit(1L)).thenReturn(unitReadDTO);

            mockMvc.perform(get("/api/v1/units/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(unitReadDTO.getId()))
                    .andExpect(jsonPath("$.numberOfRooms").value(unitReadDTO.getNumberOfRooms()))
                    .andExpect(jsonPath("$.accommodationType").value(unitReadDTO.getAccommodationType().toString()));

            verify(unitService).getUnit(1L);
        }

        @Test
        void getUnitById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
            when(unitService.getUnit(999L)).thenThrow(new EntityNotFoundException("Unit not found with id: 999"));

            mockMvc.perform(get("/api/v1/units/999"))
                    .andExpect(status().isNotFound());

            verify(unitService).getUnit(999L);
        }
    }

    @Nested
    class UpdateUnitTests {
        @Test
        void updateUnit_WithValidData_ShouldReturnUpdatedUnit() throws Exception {
            UnitUpdateDTO unitUpdateDTO = getUnitUpdateDTO();
            UnitReadDTO expectedResult = getUnitReadDTO();

            when(unitService.updateUnit(eq(1L), any(UnitUpdateDTO.class))).thenReturn(expectedResult);

            mockMvc.perform(put("/api/v1/units/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(unitUpdateDTO)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(expectedResult.getId()))
                    .andExpect(jsonPath("$.baseCost").value(expectedResult.getBaseCost().doubleValue()))
                    .andExpect(jsonPath("$.description").value(expectedResult.getDescription()));

            verify(unitService).updateUnit(eq(1L), any(UnitUpdateDTO.class));
        }

        @Test
        void updateUnit_WithNonExistingId_ShouldReturnNotFound() throws Exception {
            UnitUpdateDTO unitUpdateDTO = getUnitUpdateDTO();

            when(unitService.updateUnit(eq(999L), any(UnitUpdateDTO.class)))
                    .thenThrow(new EntityNotFoundException("Unit not found with id: 999"));

            mockMvc.perform(put("/api/v1/units/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(unitUpdateDTO)))
                    .andExpect(status().isNotFound());

            verify(unitService).updateUnit(eq(999L), any(UnitUpdateDTO.class));
        }
    }

    @Nested
    class DeleteUnitTests {
        @Test
        void deleteUnit_WithExistingId_ShouldReturnNoContent() throws Exception {
            doNothing().when(unitService).deleteUnit(1L);

            mockMvc.perform(delete("/api/v1/units/1"))
                    .andExpect(status().isNoContent());

            verify(unitService).deleteUnit(1L);
        }

        @Test
        void deleteUnit_WithNonExistingId_ShouldReturnNotFound() throws Exception {
            doNothing().when(unitService).deleteUnit(999L);
            doThrow(new EntityNotFoundException("Unit not found with id: 999")).when(unitService).deleteUnit(999L);

            mockMvc.perform(delete("/api/v1/units/999"))
                    .andExpect(status().isNotFound());

            verify(unitService).deleteUnit(999L);
        }
    }

    @Nested
    class FindByCriteriaUnitTests {
        @Test
        void findByCriteria_ShouldReturnUnitsPage() throws Exception {
            UnitReadDTO unitReadDTO = getUnitReadDTO();
            Page<UnitReadDTO> unitsPage = new PageImpl<>(List.of(unitReadDTO));

            when(unitService.findByCriteria(any(UnitFilter.class), any(Pageable.class))).thenReturn(unitsPage);

            mockMvc.perform(get("/api/v1/units")
                            .param("numberOfRooms", "2")
                            .param("accommodationType", "APARTMENTS")
                            .param("floor", "3")
                            .param("minCost", "100.00")
                            .param("maxCost", "200.00"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(unitReadDTO.getId()))
                    .andExpect(jsonPath("$.content[0].numberOfRooms").value(unitReadDTO.getNumberOfRooms()))
                    .andExpect(jsonPath("$.content[0].accommodationType").value(unitReadDTO.getAccommodationType().toString()));

            verify(unitService).findByCriteria(any(UnitFilter.class), any(Pageable.class));
        }
    }

    @Test
    void checkAvailability_ShouldReturnAvailabilityStatus() throws Exception {
        Instant startDate = Instant.now().plusSeconds(86400);
        Instant endDate = Instant.now().plusSeconds(172800);
        
        when(unitService.isUnitAvailable(eq(1L), any(Instant.class), any(Instant.class))).thenReturn(true);

        mockMvc.perform(get("/api/v1/units/1/availability")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(unitService).isUnitAvailable(eq(1L), any(Instant.class), any(Instant.class));
    }

    @Test
    void getAvailableUnitsCount_ShouldReturnCount() throws Exception {
        when(unitService.getAvailableUnitsCount()).thenReturn(5L);

        mockMvc.perform(get("/api/v1/units/available/count"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableUnits").value(5));

        verify(unitService).getAvailableUnitsCount();
    }

    private UnitReadDTO getUnitReadDTO() {
        UnitReadDTO unitReadDTO = new UnitReadDTO();
        unitReadDTO.setId(1L);
        unitReadDTO.setNumberOfRooms(2);
        unitReadDTO.setAccommodationType(AccommodationType.APARTMENTS);
        unitReadDTO.setFloor(3);
        unitReadDTO.setBaseCost(new BigDecimal("150.00"));
        unitReadDTO.setTotalCost(new BigDecimal("180.00"));
        unitReadDTO.setDescription("Modern apartment with balcony");
        return unitReadDTO;
    }

    private UnitUpdateDTO getUnitUpdateDTO() {
        UnitUpdateDTO unitUpdateDTO = new UnitUpdateDTO();
        unitUpdateDTO.setNumberOfRooms(2);
        unitUpdateDTO.setAccommodationType(AccommodationType.APARTMENTS);
        unitUpdateDTO.setFloor(3);
        unitUpdateDTO.setBaseCost(new BigDecimal("160.00"));
        unitUpdateDTO.setDescription("Updated modern apartment with balcony");
        return unitUpdateDTO;
    }

    private UnitCreateDTO getUnitCreateDTO() {
        UnitCreateDTO unitCreateDTO = new UnitCreateDTO();
        unitCreateDTO.setNumberOfRooms(2);
        unitCreateDTO.setAccommodationType(AccommodationType.APARTMENTS);
        unitCreateDTO.setFloor(3);
        unitCreateDTO.setBaseCost(new BigDecimal("150.00"));
        unitCreateDTO.setDescription("Modern apartment with balcony");
        return unitCreateDTO;
    }
}
