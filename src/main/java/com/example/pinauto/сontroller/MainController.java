package com.example.pinauto.сontroller;

import com.example.pinauto.entity.Car;
import com.example.pinauto.entity.OrderStatusHistory;
import com.example.pinauto.entity.WorkOrder;
import com.example.pinauto.service.MainService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MainController {
    @Autowired
    private MainService mainService;

    @PostMapping("/cars")
    public Car createCar(@RequestBody Car car) {
        return mainService.createCar(car);
    }

    @GetMapping("/cars")
    public List<Car> getCars() {
        return mainService.getAllCars();
    }

    @PostMapping("/orders")
    public ResponseEntity<?> createOrder(@RequestBody WorkOrder order) {
        try {
            WorkOrder savedOrder = mainService.createOrder(order);
            return ResponseEntity.ok(savedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/orders")
    public List<WorkOrder> getOrders() {
        return mainService.getAllOrders();
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<?> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mainService.getOrderById(id));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/orders/{id}/history")
    public ResponseEntity<?> getOrderStatusHistory(@PathVariable Long id) {
        try {
            List<OrderStatusHistory> history = mainService.getOrderStatusHistory(id);
            return ResponseEntity.ok(history);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            WorkOrder updatedOrder = mainService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
