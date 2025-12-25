package com.pixelgame.engine.dto;

import com.pixelgame.engine.model.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameState {
    private PlayerProgress progress;
    private Level currentLevel;
    private Scenario currentScenario;
    private ScenarioStep currentStep;
    private List<NPC> availableNPCs;
}

