package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.Trigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerRepository extends JpaRepository<Trigger, Long> {
    
    List<Trigger> findByStepId(Long stepId);
    
    List<Trigger> findByStepIdAndActiveTrue(Long stepId);
}

