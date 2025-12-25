package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.TriggerOutcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TriggerOutcomeRepository extends JpaRepository<TriggerOutcome, Long> {
    
    List<TriggerOutcome> findByTriggerIdOrderByOrderIndexAsc(Long triggerId);
}

