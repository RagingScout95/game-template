package com.pixelgame.engine.resolver;

import com.pixelgame.engine.dto.GameState;
import com.pixelgame.engine.model.entity.*;
import com.pixelgame.engine.service.AdminService;
import com.pixelgame.engine.service.AuthService;
import com.pixelgame.engine.service.GameEngineService;
import com.pixelgame.engine.service.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class QueryResolver {
    
    private final AuthService authService;
    private final GameEngineService gameEngineService;
    private final PlayerService playerService;
    private final AdminService adminService;
    
    // ===== Player Queries =====
    
    @QueryMapping
    @PreAuthorize("isAuthenticated()")
    public User me(Authentication authentication) {
        return authService.getCurrentUser(authentication.getName());
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('PLAYER') or hasRole('ADMIN')")
    public GameState getGameState(@Argument String gameId, Authentication authentication) {
        User user = authService.getCurrentUser(authentication.getName());
        Long gameIdLong = parseGameId(gameId);
        return gameEngineService.getGameState(user, gameIdLong);
    }
    
    private Long parseGameId(String gameId) {
        try {
            return Long.parseLong(gameId);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid game ID format: " + gameId + ". Expected numeric ID.");
        }
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('PLAYER') or hasRole('ADMIN')")
    public List<Game> getAllGames() {
        return adminService.getAllGames();
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('PLAYER') or hasRole('ADMIN')")
    public Game getGame(@Argument Long id) {
        return adminService.getGame(id);
    }
    
    // ===== Admin Queries =====
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return adminService.getAllUsers();
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User getUser(@Argument Long id) {
        return adminService.getUser(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Level getLevel(@Argument Long id) {
        return adminService.getLevel(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Scenario getScenario(@Argument Long id) {
        return adminService.getScenario(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ScenarioStep getScenarioStep(@Argument Long id) {
        return adminService.getScenarioStep(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public NPC getNPC(@Argument Long id) {
        return adminService.getNPC(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Dialog getDialog(@Argument Long id) {
        return adminService.getDialog(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MCQ getMCQ(@Argument Long id) {
        return adminService.getMCQ(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public AudioAsset getAudioAsset(@Argument Long id) {
        return adminService.getAudioAsset(id);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public PlayerProgress getPlayerProgress(@Argument Long userId, @Argument Long gameId) {
        return adminService.getPlayerProgress(userId, gameId);
    }
    
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<PlayerAnswer> getPlayerAnswers(@Argument Long userId) {
        return playerService.getPlayerAnswers(userId);
    }
}

