package com.pixelgame.engine.model.entity;

import com.pixelgame.engine.model.enums.TriggerCondition;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "triggers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Trigger {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TriggerCondition condition;
    
    // Condition parameters stored as JSON
    @Column(columnDefinition = "TEXT")
    private String conditionParams;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "step_id")
    private ScenarioStep step;
    
    @OneToMany(mappedBy = "trigger", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TriggerOutcome> outcomes = new ArrayList<>();
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

