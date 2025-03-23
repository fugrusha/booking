package com.booking.unitmanager.model.entity;

import com.booking.unitmanager.model.enums.AccommodationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "units")
public class UnitEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numberOfRooms;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccommodationType accommodationType;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal baseCost;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "unit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BookingEntity> bookings = new HashSet<>();

    public void addBooking(BookingEntity booking) {
        bookings.add(booking);
        booking.setUnit(this);
    }

    public void removeBooking(BookingEntity booking) {
        bookings.remove(booking);
        booking.setUnit(null);
    }
}
