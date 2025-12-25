package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.PlayerProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerProgressRepository extends JpaRepository<PlayerProgress, Long> {
    
    Optional<PlayerProgress> findByUserIdAndGameId(Long userId, Long gameId);
}

