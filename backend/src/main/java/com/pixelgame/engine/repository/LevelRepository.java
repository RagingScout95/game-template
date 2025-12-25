package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LevelRepository extends JpaRepository<Level, Long> {
    
    List<Level> findByGameIdOrderByOrderIndexAsc(Long gameId);
    
    List<Level> findByGameIdAndActiveTrue(Long gameId);
}

