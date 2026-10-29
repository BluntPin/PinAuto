package com.example.pinauto.service;

import com.example.pinauto.entity.Car;
import com.example.pinauto.entity.OrderStatus;
import com.example.pinauto.entity.OrderStatusHistory;
import com.example.pinauto.entity.WorkOrder;
import com.example.pinauto.repository.CarRepository;
import com.example.pinauto.repository.OrderRepository;
import com.example.pinauto.repository.OrderStatusHistoryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class MainService {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    // --- Машины ---
    public Car createCar(Car car) {
        validateCar(car);
        log.info("Регистрация новой машины в системе: марка/модель = {}, госномер = {}",
                car.getBrandModel(), car.getPlateNumber());
        Car savedCar = carRepository.save(car);
        log.info("Машина успешно сохранена с ID: {}", savedCar.getId());
        return savedCar;
    }

    public List<Car> getAllCars() {
        log.debug("Запрос списка всех автомобилей");
        List<Car> cars = carRepository.findAll();
        log.info("Найдено автомобилей в базе: {}", cars.size());
        return cars;
    }

    @Transactional
    public WorkOrder createOrder(WorkOrder order) {
        validateOrder(order);
        if (order.getStatus() != null && OrderStatus.fromValue(order.getStatus()) != OrderStatus.IN_PROGRESS) {
            throw new IllegalArgumentException("Новый заказ должен иметь статус «В работе»");
        }
        order.setStatus(OrderStatus.IN_PROGRESS.getValue());
        order.setClosedAt(null);
        log.info("Попытка создания заказ-наряда для машины с ID: {}", order.getCarId());

        if (!carRepository.existsById(order.getCarId())) {
            log.warn("Ошибка: Попытка создать заказ для несуществующей машины ID: {}", order.getCarId());
            throw new IllegalArgumentException("Машина с ID " + order.getCarId() + " не найдена");
        }

        WorkOrder savedOrder = orderRepository.save(order);
        addStatusHistory(savedOrder, null, OrderStatus.IN_PROGRESS.getValue());
        log.info("Заказ-наряд успешно создан под ID: {} на сумму: {}", savedOrder.getId(), savedOrder.getCost());
        return savedOrder;
    }

    public List<WorkOrder> getAllOrders() {
        log.debug("Запрос списка всех заказ-нарядов");
        List<WorkOrder> orders = orderRepository.findAll();
        log.info("Найдено заказ-нарядов: {}", orders.size());
        return orders;
    }

    @Transactional(readOnly = true)
    public WorkOrder getOrderById(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("ID заказ-наряда должен быть положительным числом");
        }
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Заказ с ID " + orderId + " не найден"));
    }

    @Transactional
    public WorkOrder updateOrderStatus(Long orderId, String status) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("ID заказ-наряда должен быть положительным числом");
        }
        if (status == null || status.isBlank()) {
            throw new IllegalArgumentException("Статус не должен быть пустым");
        }
        OrderStatus nextStatus = OrderStatus.fromValue(status);
        log.info("Изменение статуса для заказ-наряда ID: {} на новый статус: '{}'", orderId, status);

        WorkOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Ошибка: Заказ-наряд с ID {} не найден", orderId);
                    return new EntityNotFoundException("Заказ с ID " + orderId + " не найден");
                });

        boolean statusWasMissing = order.isStatusWasMissing();
        OrderStatus currentStatus = statusWasMissing
                ? OrderStatus.IN_PROGRESS
                : OrderStatus.fromValue(order.getStatus());

        if (statusWasMissing) {
            order.setStatus(currentStatus.getValue());
            order.setStatusWasMissing(false);
            orderRepository.save(order);
            addStatusHistory(order, null, currentStatus.getValue());
        }
        if (currentStatus == nextStatus) {
            return order;
        }
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new IllegalArgumentException("Недопустимый переход статуса: «" + currentStatus.getValue()
                    + "» → «" + nextStatus.getValue() + "»");
        }

        order.setStatus(nextStatus.getValue());
        if (nextStatus.isFinal()) {
            order.setClosedAt(LocalDateTime.now());
        }
        WorkOrder updatedOrder = orderRepository.save(order);
        addStatusHistory(updatedOrder, currentStatus.getValue(), nextStatus.getValue());
        log.info("Статус заказ-наряда ID: {} успешно обновлен", orderId);
        return updatedOrder;
    }

    @Transactional(readOnly = true)
    public List<OrderStatusHistory> getOrderStatusHistory(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException("ID заказ-наряда должен быть положительным числом");
        }
        if (!orderRepository.existsById(orderId)) {
            throw new EntityNotFoundException("Заказ с ID " + orderId + " не найден");
        }
        return orderStatusHistoryRepository.findByOrder_IdOrderByChangedAtAsc(orderId);
    }

    private void addStatusHistory(WorkOrder order, String fromStatus, String toStatus) {
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);
    }

    private void validateCar(Car car) {
        if (car == null) {
            throw new IllegalArgumentException("Данные автомобиля обязательны");
        }
        requireText(car.getBrandModel(), "Марка и модель автомобиля");
        requireText(car.getPlateNumber(), "Госномер");
        requireText(car.getOwnerName(), "Имя владельца");
        requireText(car.getPhone(), "Телефон");

        if (!car.getPhone().matches("[+()\\d\\s-]{7,20}")) {
            throw new IllegalArgumentException("Телефон содержит недопустимые символы или имеет неверную длину");
        }
    }

    private void validateOrder(WorkOrder order) {
        if (order == null) {
            throw new IllegalArgumentException("Данные заказ-наряда обязательны");
        }
        if (order.getCarId() == null || order.getCarId() <= 0) {
            throw new IllegalArgumentException("ID автомобиля должен быть положительным числом");
        }
        requireText(order.getProblemDescription(), "Описание неисправности");
        if (order.getCost() != null && (!Double.isFinite(order.getCost()) || order.getCost() < 0)) {
            throw new IllegalArgumentException("Стоимость должна быть неотрицательным числом");
        }
        if (order.getStatus() != null && order.getStatus().isBlank()) {
            throw new IllegalArgumentException("Статус не должен быть пустым");
        }
    }

    private void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не должно быть пустым");
        }
    }
}
