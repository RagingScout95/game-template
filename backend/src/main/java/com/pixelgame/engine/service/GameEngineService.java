package com.pixelgame.engine.service;

import com.pixelgame.engine.dto.GameState;
import com.pixelgame.engine.model.entity.*;
import com.pixelgame.engine.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Core Game Engine Service - Controls game flow and scenario progression
 * This is the "brain" of the engine that enforces game rules
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GameEngineService {
    
    private final GameRepository gameRepository;
    private final LevelRepository levelRepository;
    private final ScenarioRepository scenarioRepository;
    private final ScenarioStepRepository stepRepository;
    private final NPCRepository npcRepository;
    private final PlayerProgressRepository progressRepository;
    
    /**
     * Start a new game for a player
     */
    @Transactional
    public GameState startGame(User user, Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        
        if (!game.getActive()) {
            throw new RuntimeException("Game is not active");
        }
        
        // Check if player already has progress
        PlayerProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), gameId)
                .orElse(null);
        
        if (progress == null) {
            // Create new progress
            Level firstLevel = levelRepository.findByGameIdOrderByOrderIndexAsc(gameId)
                    .stream()
                    .filter(Level::getActive)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No active levels found"));
            
            Scenario firstScenario = scenarioRepository.findByLevelIdOrderByOrderIndexAsc(firstLevel.getId())
                    .stream()
                    .filter(Scenario::getActive)
                    .findFirst()
                    .orElse(null);
            
            ScenarioStep firstStep = null;
            if (firstScenario != null) {
                firstStep = stepRepository.findByScenarioIdOrderByOrderIndexAsc(firstScenario.getId())
                        .stream()
                        .filter(ScenarioStep::getActive)
                        .findFirst()
                        .orElse(null);
            }
            
            progress = PlayerProgress.builder()
                    .user(user)
                    .game(game)
                    .currentLevel(firstLevel)
                    .currentScenario(firstScenario)
                    .currentStep(firstStep)
                    .playerPosition("{\"x\": 0, \"y\": 0}")
                    .completed(false)
                    .build();
            
            progress = progressRepository.save(progress);
        }
        
        return buildGameState(progress);
    }
    
    /**
     * Get current game state for a player
     */
    @Transactional(readOnly = true)
    public GameState getGameState(User user, Long gameId) {
        PlayerProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseThrow(() -> new RuntimeException("No progress found. Please start the game first."));
        
        return buildGameState(progress);
    }
    
    /**
     * Advance to next step in current scenario
     */
    @Transactional
    public GameState advanceToNextStep(User user, Long gameId) {
        PlayerProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseThrow(() -> new RuntimeException("No progress found"));
        
        if (progress.getCurrentScenario() == null) {
            throw new RuntimeException("No active scenario");
        }
        
        List<ScenarioStep> steps = stepRepository.findByScenarioIdOrderByOrderIndexAsc(
                progress.getCurrentScenario().getId()
        ).stream()
                .filter(ScenarioStep::getActive)
                .toList();
        
        if (progress.getCurrentStep() == null) {
            // Start first step
            if (!steps.isEmpty()) {
                progress.setCurrentStep(steps.get(0));
            }
        } else {
            // Find next step
            int currentIndex = steps.indexOf(progress.getCurrentStep());
            if (currentIndex >= 0 && currentIndex < steps.size() - 1) {
                progress.setCurrentStep(steps.get(currentIndex + 1));
            } else {
                // Scenario complete, advance to next scenario
                return advanceToNextScenario(user, gameId);
            }
        }
        
        progress = progressRepository.save(progress);
        return buildGameState(progress);
    }
    
    /**
     * Advance to next scenario
     */
    @Transactional
    public GameState advanceToNextScenario(User user, Long gameId) {
        PlayerProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseThrow(() -> new RuntimeException("No progress found"));
        
        if (progress.getCurrentLevel() == null) {
            throw new RuntimeException("No active level");
        }
        
        List<Scenario> scenarios = scenarioRepository.findByLevelIdOrderByOrderIndexAsc(
                progress.getCurrentLevel().getId()
        ).stream()
                .filter(Scenario::getActive)
                .toList();
        
        if (progress.getCurrentScenario() == null) {
            // Start first scenario
            if (!scenarios.isEmpty()) {
                progress.setCurrentScenario(scenarios.get(0));
                List<ScenarioStep> steps = stepRepository.findByScenarioIdOrderByOrderIndexAsc(
                        scenarios.get(0).getId()
                ).stream()
                        .filter(ScenarioStep::getActive)
                        .toList();
                progress.setCurrentStep(steps.isEmpty() ? null : steps.get(0));
            }
        } else {
            // Find next scenario
            int currentIndex = scenarios.indexOf(progress.getCurrentScenario());
            if (currentIndex >= 0 && currentIndex < scenarios.size() - 1) {
                Scenario nextScenario = scenarios.get(currentIndex + 1);
                progress.setCurrentScenario(nextScenario);
                
                List<ScenarioStep> steps = stepRepository.findByScenarioIdOrderByOrderIndexAsc(
                        nextScenario.getId()
                ).stream()
                        .filter(ScenarioStep::getActive)
                        .toList();
                progress.setCurrentStep(steps.isEmpty() ? null : steps.get(0));
            } else {
                // Level complete, advance to next level
                return advanceToNextLevel(user, gameId);
            }
        }
        
        progress = progressRepository.save(progress);
        return buildGameState(progress);
    }
    
    /**
     * Advance to next level
     */
    @Transactional
    public GameState advanceToNextLevel(User user, Long gameId) {
        PlayerProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseThrow(() -> new RuntimeException("No progress found"));
        
        List<Level> levels = levelRepository.findByGameIdOrderByOrderIndexAsc(gameId);
        
        int currentIndex = levels.indexOf(progress.getCurrentLevel());
        if (currentIndex >= 0 && currentIndex < levels.size() - 1) {
            Level nextLevel = levels.get(currentIndex + 1);
            progress.setCurrentLevel(nextLevel);
            
            List<Scenario> scenarios = scenarioRepository.findByLevelIdOrderByOrderIndexAsc(nextLevel.getId())
                    .stream()
                    .filter(Scenario::getActive)
                    .toList();
            if (!scenarios.isEmpty()) {
                progress.setCurrentScenario(scenarios.get(0));
                List<ScenarioStep> steps = stepRepository.findByScenarioIdOrderByOrderIndexAsc(
                        scenarios.get(0).getId()
                ).stream()
                        .filter(ScenarioStep::getActive)
                        .toList();
                progress.setCurrentStep(steps.isEmpty() ? null : steps.get(0));
            }
        } else {
            // Game complete!
            progress.setCompleted(true);
            progress.setCurrentScenario(null);
            progress.setCurrentStep(null);
        }
        
        progress = progressRepository.save(progress);
        return buildGameState(progress);
    }
    
    /**
     * Update player position
     */
    @Transactional
    public PlayerProgress updatePlayerPosition(User user, Long gameId, double x, double y) {
        PlayerProgress progress = progressRepository.findByUserIdAndGameId(user.getId(), gameId)
                .orElseThrow(() -> new RuntimeException("No progress found"));
        
        progress.setPlayerPosition(String.format("{\"x\": %.2f, \"y\": %.2f}", x, y));
        return progressRepository.save(progress);
    }
    
    /**
     * Build complete game state
     */
    private GameState buildGameState(PlayerProgress progress) {
        try {
            log.debug("Building game state for progress ID: {}", progress.getId());
            
            // Force load lazy associations to avoid LazyInitializationException
            Level currentLevel = progress.getCurrentLevel();
            if (currentLevel != null) {
                // Touch all fields that GraphQL will access
                currentLevel.getId();
                currentLevel.getName();
                currentLevel.getDescription();
                currentLevel.getOrderIndex();
                currentLevel.getActive();
                currentLevel.getMapData();
                log.debug("Loaded level: {}", currentLevel.getName());
            }
            
            Scenario currentScenario = progress.getCurrentScenario();
            if (currentScenario != null) {
                currentScenario.getId();
                currentScenario.getName();
                currentScenario.getDescription();
                currentScenario.getOrderIndex();
                currentScenario.getActive();
                log.debug("Loaded scenario: {}", currentScenario.getName());
            }
            
            ScenarioStep currentStep = progress.getCurrentStep();
            if (currentStep != null) {
                currentStep.getId();
                currentStep.getName();
                currentStep.getDescription();
                currentStep.getType();
                currentStep.getMovementMode();
                currentStep.getOrderIndex();
                currentStep.getActive();
                
                // Load dialog if present
                if (currentStep.getDialog() != null) {
                    currentStep.getDialog().getId();
                    currentStep.getDialog().getSpeakerName();
                    currentStep.getDialog().getText();
                    log.debug("Loaded dialog: {}", currentStep.getDialog().getSpeakerName());
                }
                
                log.debug("Loaded step: {}", currentStep.getName());
            }
            
            // Load NPCs
            List<NPC> availableNPCs = currentLevel != null 
                    ? npcRepository.findByLevelIdAndActiveTrue(currentLevel.getId())
                    : List.of();
            log.debug("Loaded {} NPCs", availableNPCs.size());
            
            GameState state = GameState.builder()
                    .progress(progress)
                    .currentLevel(currentLevel)
                    .currentScenario(currentScenario)
                    .currentStep(currentStep)
                    .availableNPCs(availableNPCs)
                    .build();
            
            log.debug("Game state built successfully");
            return state;
        } catch (Exception e) {
            log.error("Error building game state", e);
            throw new RuntimeException("Error building game state: " + e.getMessage(), e);
        }
    }
}

