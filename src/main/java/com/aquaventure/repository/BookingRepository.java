package com.aquaventure.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.aquaventure.entity.Booking;
import com.aquaventure.entity.BookingStatus;
import com.aquaventure.entity.Provider;
import com.aquaventure.entity.Tourist;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByTourist(Tourist tourist);

    List<Booking> findByTouristAndStatus(Tourist tourist, BookingStatus status);

    List<Booking> findByActivity_Provider(Provider provider);

    List<Booking> findByActivity_ProviderAndStatus(Provider provider, BookingStatus status);
}
