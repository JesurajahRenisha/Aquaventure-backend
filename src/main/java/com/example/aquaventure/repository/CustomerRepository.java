package com.example.aquaventure.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.aquaventure.entity.Customer;

public interface CustomerRepository
      extends JpaRepository<Customer, Long> {
}
