package com.pixelgame.engine.service;

import com.pixelgame.engine.model.entity.*;
import com.pixelgame.engine.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Admin Service - Handles all admin CRUD operations
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final GameRepository gameRepository;
    private final LevelRepository levelRepository;
    private final ScenarioRepository scenarioRepository;
    private final ScenarioStepRepository stepRepository;
    private final MCQRepository mcqRepository;
    private final DialogRepository dialogRepository;
    private final NPCRepository npcRepository;
    private final AudioAssetRepository audioRepository;
    private final UserRepository userRepository;
    private final PlayerProgressRepository progressRepository;
    
    // ===== Game CRUD =====
    
    @Transactional
    public Game createGame(String name, String description) {
        Game game = Game.builder()
                .name(name)
                .description(description)
                .active(true)
                .build();
        return gameRepository.save(game);
    }
    
    @Transactional
    public Game updateGame(Long id, String name, String description, Boolean active) {
        Game game = gameRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        
        if (name != null) game.setName(name);
        if (description != null) game.setDescription(description);
        if (active != null) game.setActive(active);
        
        return gameRepository.save(game);
    }
    
    @Transactional
    public boolean deleteGame(Long id) {
        gameRepository.deleteById(id);
        return true;
    }
    
    // ===== Level CRUD =====
    
    @Transactional
    public Level createLevel(Long gameId, String name, String description, int orderIndex, String mapData) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new RuntimeException("Game not found"));
        
        Level level = Level.builder()
                .game(game)
                .name(name)
                .description(description)
                .orderIndex(orderIndex)
                .mapData(mapData)
                .active(true)
                .build();
        
        return levelRepository.save(level);
    }
    
    @Transactional
    public Level updateLevel(Long id, String name, String description, Integer orderIndex, 
                            String mapData, Boolean active) {
        Level level = levelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Level not found"));
        
        if (name != null) level.setName(name);
        if (description != null) level.setDescription(description);
        if (orderIndex != null) level.setOrderIndex(orderIndex);
        if (mapData != null) level.setMapData(mapData);
        if (active != null) level.setActive(active);
        
        return levelRepository.save(level);
    }
    
    @Transactional
    public boolean deleteLevel(Long id) {
        levelRepository.deleteById(id);
        return true;
    }
    
    // ===== Scenario CRUD =====
    
    @Transactional
    public Scenario createScenario(Long levelId, String name, String description, int orderIndex) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));
        
        Scenario scenario = Scenario.builder()
                .level(level)
                .name(name)
                .description(description)
                .orderIndex(orderIndex)
                .active(true)
                .build();
        
        return scenarioRepository.save(scenario);
    }
    
    @Transactional
    public Scenario updateScenario(Long id, String name, String description, 
                                  Integer orderIndex, Boolean active) {
        Scenario scenario = scenarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        
        if (name != null) scenario.setName(name);
        if (description != null) scenario.setDescription(description);
        if (orderIndex != null) scenario.setOrderIndex(orderIndex);
        if (active != null) scenario.setActive(active);
        
        return scenarioRepository.save(scenario);
    }
    
    @Transactional
    public boolean deleteScenario(Long id) {
        scenarioRepository.deleteById(id);
        return true;
    }
    
    // ===== Scenario Step CRUD =====
    
    @Transactional
    public ScenarioStep createScenarioStep(Long scenarioId, String name, String description,
                                          int orderIndex, String type, String movementMode,
                                          Long dialogId, Long audioId) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        
        Dialog dialog = dialogId != null ? dialogRepository.findById(dialogId).orElse(null) : null;
        AudioAsset audio = audioId != null ? audioRepository.findById(audioId).orElse(null) : null;
        
        ScenarioStep step = ScenarioStep.builder()
                .scenario(scenario)
                .name(name)
                .description(description)
                .orderIndex(orderIndex)
                .type(com.pixelgame.engine.model.enums.StepType.valueOf(type))
                .movementMode(com.pixelgame.engine.model.enums.MovementMode.valueOf(movementMode))
                .dialog(dialog)
                .audio(audio)
                .active(true)
                .build();
        
        return stepRepository.save(step);
    }
    
    @Transactional
    public boolean deleteScenarioStep(Long id) {
        stepRepository.deleteById(id);
        return true;
    }
    
    // ===== MCQ CRUD =====
    
    @Transactional
    public MCQ createMCQ(Long scenarioId, String question, String options, Integer correctAnswerIndex) {
        Scenario scenario = scenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new RuntimeException("Scenario not found"));
        
        MCQ mcq = MCQ.builder()
                .scenario(scenario)
                .question(question)
                .options(options)
                .correctAnswerIndex(correctAnswerIndex)
                .build();
        
        return mcqRepository.save(mcq);
    }
    
    @Transactional
    public MCQ updateMCQ(Long id, String question, String options, Integer correctAnswerIndex) {
        MCQ mcq = mcqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MCQ not found"));
        
        if (question != null) mcq.setQuestion(question);
        if (options != null) mcq.setOptions(options);
        if (correctAnswerIndex != null) mcq.setCorrectAnswerIndex(correctAnswerIndex);
        
        return mcqRepository.save(mcq);
    }
    
    @Transactional
    public boolean deleteMCQ(Long id) {
        mcqRepository.deleteById(id);
        return true;
    }
    
    // ===== Dialog CRUD =====
    
    @Transactional
    public Dialog createDialog(String speakerName, String text, Long npcId, Long voiceAudioId) {
        NPC npc = npcId != null ? npcRepository.findById(npcId).orElse(null) : null;
        AudioAsset voiceAudio = voiceAudioId != null ? audioRepository.findById(voiceAudioId).orElse(null) : null;
        
        Dialog dialog = Dialog.builder()
                .speakerName(speakerName)
                .text(text)
                .npc(npc)
                .voiceAudio(voiceAudio)
                .build();
        
        return dialogRepository.save(dialog);
    }
    
    @Transactional
    public boolean deleteDialog(Long id) {
        dialogRepository.deleteById(id);
        return true;
    }
    
    // ===== NPC CRUD =====
    
    @Transactional
    public NPC createNPC(Long levelId, String name, String description, String movementType,
                        String spriteSheet, String initialPosition, String pathData) {
        Level level = levelRepository.findById(levelId)
                .orElseThrow(() -> new RuntimeException("Level not found"));
        
        NPC npc = NPC.builder()
                .level(level)
                .name(name)
                .description(description)
                .movementType(com.pixelgame.engine.model.enums.NPCMovementType.valueOf(movementType))
                .spriteSheet(spriteSheet)
                .initialPosition(initialPosition)
                .pathData(pathData)
                .active(true)
                .build();
        
        return npcRepository.save(npc);
    }
    
    @Transactional
    public boolean deleteNPC(Long id) {
        npcRepository.deleteById(id);
        return true;
    }
    
    // ===== Audio Asset CRUD =====
    
    @Transactional
    public AudioAsset createAudioAsset(String name, String description, String url, 
                                      Integer durationMs, Boolean loop) {
        AudioAsset audio = AudioAsset.builder()
                .name(name)
                .description(description)
                .url(url)
                .durationMs(durationMs)
                .loop(loop != null ? loop : false)
                .build();
        
        return audioRepository.save(audio);
    }
    
    @Transactional
    public boolean deleteAudioAsset(Long id) {
        audioRepository.deleteById(id);
        return true;
    }
    
    // ===== Query Methods =====
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUser(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    public List<Game> getAllGames() {
        return gameRepository.findAll();
    }
    
    public Game getGame(Long id) {
        return gameRepository.findById(id).orElse(null);
    }
    
    public Level getLevel(Long id) {
        return levelRepository.findById(id).orElse(null);
    }
    
    public Scenario getScenario(Long id) {
        return scenarioRepository.findById(id).orElse(null);
    }
    
    public ScenarioStep getScenarioStep(Long id) {
        return stepRepository.findById(id).orElse(null);
    }
    
    public NPC getNPC(Long id) {
        return npcRepository.findById(id).orElse(null);
    }
    
    public Dialog getDialog(Long id) {
        return dialogRepository.findById(id).orElse(null);
    }
    
    public MCQ getMCQ(Long id) {
        return mcqRepository.findById(id).orElse(null);
    }
    
    public AudioAsset getAudioAsset(Long id) {
        return audioRepository.findById(id).orElse(null);
    }
    
    public PlayerProgress getPlayerProgress(Long userId, Long gameId) {
        return progressRepository.findByUserIdAndGameId(userId, gameId).orElse(null);
    }
}

