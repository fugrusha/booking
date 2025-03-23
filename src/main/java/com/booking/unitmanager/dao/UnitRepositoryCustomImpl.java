package com.booking.unitmanager.dao;

import com.booking.unitmanager.model.entity.BookingEntity;
import com.booking.unitmanager.model.entity.UnitEntity;
import com.booking.unitmanager.model.enums.AccommodationType;
import com.booking.unitmanager.model.enums.BookingStatus;
import com.booking.unitmanager.model.dto.UnitFilter;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Repository
class UnitRepositoryCustomImpl implements UnitRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<UnitEntity> findByCriteriaUsingCriteriaApi(UnitFilter unitFilter, Pageable pageable) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        CriteriaQuery<UnitEntity> query = cb.createQuery(UnitEntity.class);
        Root<UnitEntity> unitRoot = query.from(UnitEntity.class);

        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<UnitEntity> countRoot = countQuery.from(UnitEntity.class);

        List<Predicate> predicates = buildPredicates(cb, unitRoot, unitFilter);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, unitFilter);

        query.select(unitRoot).distinct(true).where(cb.and(predicates.toArray(new Predicate[0])));
        countQuery.select(cb.countDistinct(countRoot)).where(cb.and(countPredicates.toArray(new Predicate[0])));

        applySorting(pageable, cb, unitRoot, query);

        TypedQuery<UnitEntity> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<UnitEntity> units = typedQuery.getResultList();
        Long count = entityManager.createQuery(countQuery).getSingleResult();

        return new PageImpl<>(units, pageable, count);
    }

    private static void applySorting(Pageable pageable, CriteriaBuilder cb, Root<UnitEntity> unitRoot, CriteriaQuery<UnitEntity> query) {
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order order : pageable.getSort()) {
                if (order.getDirection() == Sort.Direction.ASC) {
                    orders.add(cb.asc(unitRoot.get(order.getProperty())));
                } else {
                    orders.add(cb.desc(unitRoot.get(order.getProperty())));
                }
            }
            query.orderBy(orders);
        }
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<UnitEntity> unit,
            UnitFilter unitFilter
    ) {
        List<Predicate> predicates = new ArrayList<>();

        Integer numberOfRooms = unitFilter.numberOfRooms();
        if (numberOfRooms != null) {
            predicates.add(cb.equal(unit.get("numberOfRooms"), numberOfRooms));
        }

        AccommodationType accommodationType = unitFilter.accommodationType();
        if (accommodationType != null) {
            predicates.add(cb.equal(unit.get("accommodationType"), accommodationType));
        }

        Integer floor = unitFilter.floor();
        if (floor != null) {
            predicates.add(cb.equal(unit.get("floor"), floor));
        }

        BigDecimal minCost = unitFilter.minCost();
        if (minCost != null) {
            predicates.add(cb.greaterThanOrEqualTo(unit.get("totalCost"), minCost));
        }

        BigDecimal maxCost = unitFilter.maxCost();
        if (maxCost != null) {
            predicates.add(cb.lessThanOrEqualTo(unit.get("totalCost"), maxCost));
        }

        LocalDate startDate = unitFilter.startDate();
        LocalDate endDate = unitFilter.endDate();
        if (startDate != null && endDate != null) {
            Join<UnitEntity, BookingEntity> bookingJoin = unit.join("bookings", JoinType.LEFT);

            java.time.Instant startInstant = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
            java.time.Instant endInstant = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant();

            // predicate for booking availability
            Predicate noBookings = cb.isNull(bookingJoin.get("id"));

            // predicate for cancelled or expired bookings
            Predicate cancelledOrExpired = bookingJoin.get("status")
                    .in(BookingStatus.CANCELLED, BookingStatus.EXPIRED);

            // predicate for non-overlapping bookings
            Predicate notOverlapping = cb.or(
                    cb.greaterThanOrEqualTo(bookingJoin.get("startDate"), endInstant),
                    cb.lessThanOrEqualTo(bookingJoin.get("endDate"), startInstant)
            );

            predicates.add(cb.or(noBookings, cancelledOrExpired, notOverlapping));
        }

        return predicates;
    }
}
