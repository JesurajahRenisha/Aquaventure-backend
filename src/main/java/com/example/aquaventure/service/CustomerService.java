package com.example.aquaventure.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.aquaventure.entity.Customer;
import com.example.aquaventure.repository.CustomerRepository;

@Service
public class CustomerService {

    @Autowired  
    private CustomerRepository customerRepository;

    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public Customer save(Customer customer) {
        return customerRepository.save(customer);
    }

}

