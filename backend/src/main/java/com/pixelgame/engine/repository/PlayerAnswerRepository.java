package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.PlayerAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerAnswerRepository extends JpaRepository<PlayerAnswer, Long> {
    
    List<PlayerAnswer> findByUserId(Long userId);
    
    Optional<PlayerAnswer> findByUserIdAndMcqId(Long userId, Long mcqId);
}

