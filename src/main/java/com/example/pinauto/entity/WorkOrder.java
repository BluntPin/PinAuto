package com.example.pinauto.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long carId;
    private String problemDescription;
    private String status = "В работе";
    private Double cost;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;

    @JsonIgnore
    @Transient
    private boolean statusWasMissing;

    @PostLoad
    private void normalizeLegacyStatus() {
        if (status == null || status.isBlank()) {
            statusWasMissing = true;
            status = OrderStatus.IN_PROGRESS.getValue();
        } else {
            status = OrderStatus.normalizeKnownValue(status);
        }
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
