package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.MCQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MCQRepository extends JpaRepository<MCQ, Long> {
    
    Optional<MCQ> findByScenarioId(Long scenarioId);
}

