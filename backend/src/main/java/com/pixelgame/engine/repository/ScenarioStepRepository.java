package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.ScenarioStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioStepRepository extends JpaRepository<ScenarioStep, Long> {
    
    List<ScenarioStep> findByScenarioIdOrderByOrderIndexAsc(Long scenarioId);
    
    List<ScenarioStep> findByScenarioIdAndActiveTrue(Long scenarioId);
}

