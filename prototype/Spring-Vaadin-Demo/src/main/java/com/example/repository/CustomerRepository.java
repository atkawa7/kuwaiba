package com.example.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.entity.Customer;

import java.util.List;
/**
 * Interface for implemented CRUD methods
 * 
 * @author Hardy Ryan Chingal Martinez {@literal <ryan.chingal@kuwaiba.org>}
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByLastNameStartsWithIgnoreCase(String lastName);
}
