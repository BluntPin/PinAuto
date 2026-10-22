package com.example.pinauto.Controller;

import com.example.pinauto.Entity.Car;
import com.example.pinauto.Entity.WorkOrder;
import com.example.pinauto.Service.MainService;
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

    @PatchMapping("/orders/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam String status) {
        try {
            WorkOrder updatedOrder = mainService.updateOrderStatus(id, status);
            return ResponseEntity.ok(updatedOrder);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
