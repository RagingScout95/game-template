package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.NPC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NPCRepository extends JpaRepository<NPC, Long> {
    
    List<NPC> findByLevelId(Long levelId);
    
    List<NPC> findByLevelIdAndActiveTrue(Long levelId);
}

