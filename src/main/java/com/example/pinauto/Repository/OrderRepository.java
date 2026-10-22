package com.example.pinauto.Repository;

import com.example.pinauto.Entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<WorkOrder, Long> {
}