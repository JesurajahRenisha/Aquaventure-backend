package com.example.aquaventure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aquaventure.entity.BookingEquipmentItem;

public interface BookingEquipmentItemRepository extends JpaRepository<BookingEquipmentItem, Long> {

    List<BookingEquipmentItem> findByBookingId(Long bookingId);
}
