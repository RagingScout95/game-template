package com.pixelgame.engine.model.entity;

import com.pixelgame.engine.model.enums.OutcomeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "trigger_outcomes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class TriggerOutcome {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OutcomeType type;
    
    // Outcome parameters stored as JSON
    @Column(columnDefinition = "TEXT")
    private String params;
    
    @Column(nullable = false)
    private Integer orderIndex = 0;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trigger_id", nullable = false)
    private Trigger trigger;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

