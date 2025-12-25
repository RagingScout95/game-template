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
        // Handle duplicates by getting the most recent one
        List<PlayerProgress> progressList = progressRepository.findAllByUserIdAndGameIdOrderByUpdatedAtDesc(user.getId(), gameId);
        PlayerProgress progress = null;
        
        if (!progressList.isEmpty()) {
            progress = progressList.get(0); // Get most recent
            // If there are duplicates, delete the older ones
            if (progressList.size() > 1) {
                log.warn("Found {} duplicate progress records for user {} and game {}. Keeping most recent (ID: {})", 
                        progressList.size(), user.getId(), gameId, progress.getId());
                for (int i = 1; i < progressList.size(); i++) {
                    progressRepository.delete(progressList.get(i));
                    log.info("Deleted duplicate progress record ID: {}", progressList.get(i).getId());
                }
            }
        }
        
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
                    .playerPosition("{\"x\": 400, \"y\": 300}")
                    .completed(false)
                    .build();
            
            progress = progressRepository.save(progress);
            log.info("Created new progress for user: {}, game: {}", user.getUsername(), gameId);
        } else {
            // Refresh existing progress to ensure all associations are loaded
            log.info("Resuming existing progress for user: {}, game: {}, progress ID: {}", 
                    user.getUsername(), gameId, progress.getId());
            // Refresh the entity to ensure it's attached to the current session
            progress = progressRepository.findById(progress.getId())
                    .orElseThrow(() -> new RuntimeException("Progress not found after refresh"));
        }
        
        return buildGameState(progress);
    }
    
    /**
     * Get current game state for a player
     */
    @Transactional(readOnly = true)
    public GameState getGameState(User user, Long gameId) {
        // Handle duplicates by getting the most recent one
        List<PlayerProgress> progressList = progressRepository.findAllByUserIdAndGameIdOrderByUpdatedAtDesc(user.getId(), gameId);
        
        if (progressList.isEmpty()) {
            // No progress exists, return null or throw exception
            // Frontend will catch this and call startGame
            throw new RuntimeException("No progress found. Please start the game first.");
        }
        
        PlayerProgress progress = progressList.get(0); // Get most recent
        
        // If there are duplicates, log warning (cleanup will happen in startGame)
        if (progressList.size() > 1) {
            log.warn("Found {} duplicate progress records for user {} and game {}. Using most recent (ID: {})", 
                    progressList.size(), user.getId(), gameId, progress.getId());
        }
        
        // Refresh to ensure entity is attached to current session
        progress = progressRepository.findById(progress.getId())
                .orElseThrow(() -> new RuntimeException("Progress not found after refresh"));
        
        log.debug("Getting game state for user: {}, game: {}, progress ID: {}", 
                user.getUsername(), gameId, progress.getId());
        
        return buildGameState(progress);
    }
    
    /**
     * Advance to next step in current scenario
     */
    @Transactional
    public GameState advanceToNextStep(User user, Long gameId) {
        List<PlayerProgress> progressList = progressRepository.findAllByUserIdAndGameIdOrderByUpdatedAtDesc(user.getId(), gameId);
        if (progressList.isEmpty()) {
            throw new RuntimeException("No progress found");
        }
        PlayerProgress progress = progressList.get(0);
        
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
        List<PlayerProgress> progressList = progressRepository.findAllByUserIdAndGameIdOrderByUpdatedAtDesc(user.getId(), gameId);
        if (progressList.isEmpty()) {
            throw new RuntimeException("No progress found");
        }
        PlayerProgress progress = progressList.get(0);
        
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
        List<PlayerProgress> progressList = progressRepository.findAllByUserIdAndGameIdOrderByUpdatedAtDesc(user.getId(), gameId);
        if (progressList.isEmpty()) {
            throw new RuntimeException("No progress found");
        }
        PlayerProgress progress = progressList.get(0);
        
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
        List<PlayerProgress> progressList = progressRepository.findAllByUserIdAndGameIdOrderByUpdatedAtDesc(user.getId(), gameId);
        if (progressList.isEmpty()) {
            throw new RuntimeException("No progress found");
        }
        PlayerProgress progress = progressList.get(0);
        
        progress.setPlayerPosition(String.format("{\"x\": %.2f, \"y\": %.2f}", x, y));
        return progressRepository.save(progress);
    }
    
    /**
     * Build complete game state
     */
    private GameState buildGameState(PlayerProgress progress) {
        try {
            log.debug("Building game state for progress ID: {}", progress.getId());
            
            // Ensure progress is fully loaded
            if (progress.getId() == null) {
                throw new RuntimeException("Progress ID is null");
            }
            
            // Force load lazy associations to avoid LazyInitializationException
            // Access user and game to ensure they're loaded
            if (progress.getUser() != null) {
                progress.getUser().getId();
            }
            if (progress.getGame() != null) {
                progress.getGame().getId();
            }
            
            Level currentLevel = progress.getCurrentLevel();
            if (currentLevel != null) {
                // Touch all fields that GraphQL will access
                currentLevel.getId();
                currentLevel.getName();
                currentLevel.getDescription();
                currentLevel.getOrderIndex();
                currentLevel.getActive();
                currentLevel.getMapData();
                log.debug("Loaded level: {} (ID: {})", currentLevel.getName(), currentLevel.getId());
            } else {
                log.debug("No current level in progress");
            }
            
            Scenario currentScenario = progress.getCurrentScenario();
            if (currentScenario != null) {
                currentScenario.getId();
                currentScenario.getName();
                currentScenario.getDescription();
                currentScenario.getOrderIndex();
                currentScenario.getActive();
                log.debug("Loaded scenario: {} (ID: {})", currentScenario.getName(), currentScenario.getId());
            } else {
                log.debug("No current scenario in progress");
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
                
                log.debug("Loaded step: {} (ID: {})", currentStep.getName(), currentStep.getId());
            } else {
                log.debug("No current step in progress");
            }
            
            // Load NPCs
            List<NPC> availableNPCs = List.of();
            if (currentLevel != null && currentLevel.getId() != null) {
                try {
                    availableNPCs = npcRepository.findByLevelIdAndActiveTrue(currentLevel.getId());
                    log.debug("Loaded {} NPCs for level {}", availableNPCs.size(), currentLevel.getId());
                } catch (Exception e) {
                    log.warn("Error loading NPCs: {}", e.getMessage());
                    availableNPCs = List.of();
                }
            }
            
            GameState state = GameState.builder()
                    .progress(progress)
                    .currentLevel(currentLevel)
                    .currentScenario(currentScenario)
                    .currentStep(currentStep)
                    .availableNPCs(availableNPCs)
                    .build();
            
            log.debug("Game state built successfully for progress ID: {}", progress.getId());
            return state;
        } catch (Exception e) {
            log.error("Error building game state for progress ID: {}", progress != null ? progress.getId() : "null", e);
            throw new RuntimeException("Error building game state: " + e.getMessage(), e);
        }
    }
}

