package com.example.pinauto.Service;

import com.example.pinauto.Entity.Car;
import com.example.pinauto.Entity.WorkOrder;
import com.example.pinauto.Repository.CarRepository;
import com.example.pinauto.Repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MainService {
    @Autowired
    private CarRepository carRepository;

    @Autowired
    private OrderRepository orderRepository;

    public Car createCar(Car car) {
        return carRepository.save(car);
    }

    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public WorkOrder createOrder(WorkOrder order) {
        if (!carRepository.existsById(order.getCarId())) {
            throw new IllegalArgumentException("Машина с ID " + order.getCarId() + " не найдена");
        }
        return orderRepository.save(order);
    }

    public List<WorkOrder> getAllOrders() {
        return orderRepository.findAll();
    }

    public WorkOrder updateOrderStatus(Long orderId, String status) {
        WorkOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ с ID " + orderId + " не найден"));

        order.setStatus(status);
        return orderRepository.save(order);
    }
}
