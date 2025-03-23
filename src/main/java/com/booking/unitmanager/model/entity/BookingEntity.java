package com.booking.unitmanager.model.entity;

import com.booking.unitmanager.model.enums.BookingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "bookings")
public class BookingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    private UnitEntity unit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "start_date", nullable = false)
    private Instant startDate;

    @Column(name = "end_date", nullable = false)
    private Instant endDate;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "payment_deadline")
    private Instant paymentDeadline;

    public void cancel() {
        this.status = BookingStatus.CANCELLED;
    }

    public void markAsPaid() {
        this.status = BookingStatus.PAID;
    }

    public void markAsExpired() {
        this.status = BookingStatus.EXPIRED;
    }

    public boolean isAvailableStatusToPay() {
        return status == BookingStatus.PENDING;
    }

    public boolean isPaymentDeadlinePassed() {
        return paymentDeadline.isBefore(Instant.now());
    }
}
