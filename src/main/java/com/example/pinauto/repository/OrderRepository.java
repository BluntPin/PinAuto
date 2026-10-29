package com.example.pinauto.repository;

import com.example.pinauto.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<WorkOrder, Long> {
}