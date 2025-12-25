package com.pixelgame.engine.repository;

import com.pixelgame.engine.model.entity.AudioAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AudioAssetRepository extends JpaRepository<AudioAsset, Long> {
    
    Optional<AudioAsset> findByName(String name);
}

