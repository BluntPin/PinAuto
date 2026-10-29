package com.example.pinauto.entity;

import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
    IN_PROGRESS("IN_PROGRESS", "В работе", Set.of("WAITING_FOR_PARTS", "READY_FOR_PICKUP", "CANCELLED")),
    WAITING_FOR_PARTS("WAITING_FOR_PARTS", "Ожидает запчасти", Set.of("IN_PROGRESS", "CANCELLED")),
    READY_FOR_PICKUP("READY_FOR_PICKUP", "Готов к выдаче", Set.of("ISSUED", "CANCELLED")),
    ISSUED("ISSUED", "Выдан", Set.of()),
    CANCELLED("CANCELLED", "Отменён", Set.of());

    private final String value;
    private final String legacyValue;
    private final Set<String> allowedNextStatuses;

    OrderStatus(String value, String legacyValue, Set<String> allowedNextStatuses) {
        this.value = value;
        this.legacyValue = legacyValue;
        this.allowedNextStatuses = allowedNextStatuses;
    }

    public String getValue() {
        return value;
    }

    public boolean canTransitionTo(OrderStatus nextStatus) {
        return allowedNextStatuses.contains(nextStatus.value);
    }

    public boolean isFinal() {
        return this == ISSUED || this == CANCELLED;
    }

    public static OrderStatus fromValue(String value) {
        String canonicalValue = normalizeKnownValue(value);
        for (OrderStatus status : values()) {
            if (status.value.equals(canonicalValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус. Допустимые значения: " + allowedValues());
    }

    public static String normalizeKnownValue(String value) {
        if (value == null) {
            return null;
        }
        for (OrderStatus status : values()) {
            if (status.value.equals(value) || status.legacyValue.equals(value)) {
                return status.value;
            }
        }
        return value;
    }

    public static String allowedValues() {
        return String.join(", ", EnumSet.allOf(OrderStatus.class).stream()
                .map(OrderStatus::getValue)
                .toList());
    }
}
