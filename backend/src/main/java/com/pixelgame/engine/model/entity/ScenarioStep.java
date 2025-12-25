package com.pixelgame.engine.model.entity;

import com.pixelgame.engine.model.enums.MovementMode;
import com.pixelgame.engine.model.enums.StepType;
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
@Table(name = "scenario_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ScenarioStep {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false)
    private Integer orderIndex;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StepType type;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementMode movementMode = MovementMode.FREE;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", nullable = false)
    private Scenario scenario;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dialog_id")
    private Dialog dialog;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audio_id")
    private AudioAsset audio;
    
    @OneToMany(mappedBy = "step", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Trigger> triggers = new ArrayList<>();
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}

