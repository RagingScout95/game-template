package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.PlayerProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerProgressRepository extends JpaRepository<PlayerProgress, Long> {
    
    Optional<PlayerProgress> findByUserIdAndGameId(Long userId, Long gameId);
    
    // Handle duplicates by getting the most recent one
    @Query("SELECT p FROM PlayerProgress p WHERE p.user.id = :userId AND p.game.id = :gameId ORDER BY p.updatedAt DESC")
    List<PlayerProgress> findAllByUserIdAndGameIdOrderByUpdatedAtDesc(@Param("userId") Long userId, @Param("gameId") Long gameId);
}

