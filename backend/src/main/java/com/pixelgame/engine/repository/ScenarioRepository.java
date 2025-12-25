package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.Scenario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScenarioRepository extends JpaRepository<Scenario, Long> {
    
    List<Scenario> findByLevelIdOrderByOrderIndexAsc(Long levelId);
    
    List<Scenario> findByLevelIdAndActiveTrue(Long levelId);
}

