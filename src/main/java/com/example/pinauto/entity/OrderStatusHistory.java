package com.example.pinauto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private WorkOrder order;

    private String fromStatus;
    private String toStatus;
    private LocalDateTime changedAt;

    @PostLoad
    private void normalizeLegacyStatuses() {
        fromStatus = OrderStatus.normalizeKnownValue(fromStatus);
        toStatus = OrderStatus.normalizeKnownValue(toStatus);
    }

    @PrePersist
    private void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }
}
