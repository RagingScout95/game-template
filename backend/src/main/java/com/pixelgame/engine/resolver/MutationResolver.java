package com.pixelgame.engine.resolver;

import com.pixelgame.engine.dto.AuthPayload;
import com.pixelgame.engine.dto.GameState;
import com.pixelgame.engine.model.entity.*;
import com.pixelgame.engine.service.AdminService;
import com.pixelgame.engine.service.AuthService;
import com.pixelgame.engine.service.GameEngineService;
import com.pixelgame.engine.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MutationResolver {
    
    private final AuthService authService;
    private final GameEngineService gameEngineService;
    private final PlayerService playerService;
    private final AdminService adminService;
    
    // ===== Authentication =====
    
    @MutationMapping
    public AuthPayload registerPlayer(@Argument Map<String, Object> input) {
        return authService.registerPlayer(
                (String) input.get("username"),
                (String) input.get("password"),
                (String) input.get("name"),
                (String) input.get("empId")
        );
    }
    
    @MutationMapping
    public AuthPayload login(@Argument Map<String, Object> input) {
        return authService.login(
                (String) input.get("username"),
                (String) input.get("password")
        );
    }
    
    // ===== Player Mutations =====
    
    @MutationMapping
    @PreAuthorize("hasRole('PLAYER')")
    public GameState startGame(@Argument Long gameId, Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        return gameEngineService.startGame(user, gameId);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('PLAYER')")
    public PlayerAnswer submitMCQAnswer(@Argument Map<String, Object> input, Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Long mcqId = Long.parseLong(input.get("mcqId").toString());
        int selectedOptionIndex = (int) input.get("selectedOptionIndex");
        
        PlayerAnswer answer = playerService.submitMCQAnswer(user, mcqId, selectedOptionIndex);
        
        // Optionally advance scenario after MCQ
        // gameEngineService.advanceToNextStep(user, gameId);
        
        return answer;
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('PLAYER')")
    public PlayerProgress updatePlayerPosition(@Argument Map<String, Object> input, Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Long gameId = Long.parseLong(input.get("gameId").toString());
        double x = ((Number) input.get("x")).doubleValue();
        double y = ((Number) input.get("y")).doubleValue();
        
        return gameEngineService.updatePlayerPosition(user, gameId, x, y);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('PLAYER')")
    public GameState triggerEvent(@Argument Map<String, Object> input, Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Long gameId = Long.parseLong(input.get("gameId").toString());
        
        // For now, just advance to next step
        // In full implementation, evaluate triggers and outcomes
        return gameEngineService.advanceToNextStep(user, gameId);
    }
    
    // ===== Admin Game Management =====
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Game createGame(@Argument Map<String, Object> input) {
        return adminService.createGame(
                (String) input.get("name"),
                (String) input.get("description")
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Game updateGame(@Argument Map<String, Object> input) {
        return adminService.updateGame(
                Long.parseLong(input.get("id").toString()),
                (String) input.get("name"),
                (String) input.get("description"),
                (Boolean) input.get("active")
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteGame(@Argument Long id) {
        return adminService.deleteGame(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Level createLevel(@Argument Map<String, Object> input) {
        return adminService.createLevel(
                Long.parseLong(input.get("gameId").toString()),
                (String) input.get("name"),
                (String) input.get("description"),
                (int) input.get("orderIndex"),
                (String) input.get("mapData")
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteLevel(@Argument Long id) {
        return adminService.deleteLevel(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Scenario createScenario(@Argument Map<String, Object> input) {
        return adminService.createScenario(
                Long.parseLong(input.get("levelId").toString()),
                (String) input.get("name"),
                (String) input.get("description"),
                (int) input.get("orderIndex")
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteScenario(@Argument Long id) {
        return adminService.deleteScenario(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ScenarioStep createScenarioStep(@Argument Map<String, Object> input) {
        Long dialogId = input.get("dialogId") != null ? Long.parseLong(input.get("dialogId").toString()) : null;
        Long audioId = input.get("audioId") != null ? Long.parseLong(input.get("audioId").toString()) : null;
        
        return adminService.createScenarioStep(
                Long.parseLong(input.get("scenarioId").toString()),
                (String) input.get("name"),
                (String) input.get("description"),
                (int) input.get("orderIndex"),
                (String) input.get("type"),
                (String) input.get("movementMode"),
                dialogId,
                audioId
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteScenarioStep(@Argument Long id) {
        return adminService.deleteScenarioStep(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MCQ createMCQ(@Argument Map<String, Object> input) {
        Integer correctAnswerIndex = input.get("correctAnswerIndex") != null 
                ? (Integer) input.get("correctAnswerIndex") 
                : null;
        
        return adminService.createMCQ(
                Long.parseLong(input.get("scenarioId").toString()),
                (String) input.get("question"),
                (String) input.get("options"),
                correctAnswerIndex
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteMCQ(@Argument Long id) {
        return adminService.deleteMCQ(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Dialog createDialog(@Argument Map<String, Object> input) {
        Long npcId = input.get("npcId") != null ? Long.parseLong(input.get("npcId").toString()) : null;
        Long voiceAudioId = input.get("voiceAudioId") != null ? Long.parseLong(input.get("voiceAudioId").toString()) : null;
        
        return adminService.createDialog(
                (String) input.get("speakerName"),
                (String) input.get("text"),
                npcId,
                voiceAudioId
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteDialog(@Argument Long id) {
        return adminService.deleteDialog(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public NPC createNPC(@Argument Map<String, Object> input) {
        return adminService.createNPC(
                Long.parseLong(input.get("levelId").toString()),
                (String) input.get("name"),
                (String) input.get("description"),
                (String) input.get("movementType"),
                (String) input.get("spriteSheet"),
                (String) input.get("initialPosition"),
                (String) input.get("pathData")
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteNPC(@Argument Long id) {
        return adminService.deleteNPC(id);
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AudioAsset createAudioAsset(@Argument Map<String, Object> input) {
        Integer durationMs = input.get("durationMs") != null ? (Integer) input.get("durationMs") : null;
        Boolean loop = input.get("loop") != null ? (Boolean) input.get("loop") : false;
        
        return adminService.createAudioAsset(
                (String) input.get("name"),
                (String) input.get("description"),
                (String) input.get("url"),
                durationMs,
                loop
        );
    }
    
    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteAudioAsset(@Argument Long id) {
        return adminService.deleteAudioAsset(id);
    }
}

