package com.pixelgame.engine.config;

import com.pixelgame.engine.model.entity.*;
import com.pixelgame.engine.model.enums.*;
import com.pixelgame.engine.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {
    
    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final LevelRepository levelRepository;
    private final ScenarioRepository scenarioRepository;
    private final ScenarioStepRepository stepRepository;
    private final MCQRepository mcqRepository;
    private final DialogRepository dialogRepository;
    private final NPCRepository npcRepository;
    private final AudioAssetRepository audioRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    public void run(String... args) {
        log.info("Loading initial data...");
        
        // Create Admin User
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .name("Admin User")
                    .role(UserRole.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            log.info("Created admin user");
        }
        
        // Create Test Player
        if (!userRepository.existsByUsername("player1")) {
            User player = User.builder()
                    .username("player1")
                    .password(passwordEncoder.encode("player123"))
                    .name("Test Player")
                    .empId("EMP001")
                    .role(UserRole.PLAYER)
                    .active(true)
                    .build();
            userRepository.save(player);
            log.info("Created test player");
        }
        
        // Create Demo Game
        if (gameRepository.findByName("Demo Game").isEmpty()) {
            Game game = Game.builder()
                    .name("Demo Game")
                    .description("A demo game to showcase the engine")
                    .active(true)
                    .build();
            game = gameRepository.save(game);
            log.info("Created demo game");
            
            // Create Level
            Level level = Level.builder()
                    .game(game)
                    .name("Office Floor")
                    .description("The main office floor")
                    .orderIndex(0)
                    .mapData("{\"width\": 800, \"height\": 600}")
                    .active(true)
                    .build();
            level = levelRepository.save(level);
            log.info("Created level: {}", level.getName());
            
            // Create NPC
            NPC npc = NPC.builder()
                    .level(level)
                    .name("Manager")
                    .description("The office manager")
                    .movementType(NPCMovementType.STATIC)
                    .spriteSheet("/sprites/manager.png")
                    .initialPosition("{\"x\": 400, \"y\": 300}")
                    .active(true)
                    .build();
            npc = npcRepository.save(npc);
            log.info("Created NPC: {}", npc.getName());
            
            // Create Audio
            AudioAsset bgMusic = AudioAsset.builder()
                    .name("Office Background Music")
                    .description("Calm office music")
                    .url("/audio/office_bg.mp3")
                    .loop(true)
                    .build();
            bgMusic = audioRepository.save(bgMusic);
            
            // Create Dialog
            Dialog dialog1 = Dialog.builder()
                    .speakerName("Manager")
                    .text("Welcome to your first day! Let me show you around.")
                    .npc(npc)
                    .build();
            dialog1 = dialogRepository.save(dialog1);
            
            Dialog dialog2 = Dialog.builder()
                    .speakerName("Manager")
                    .text("Here's a quick quiz about office policies.")
                    .npc(npc)
                    .build();
            dialog2 = dialogRepository.save(dialog2);
            
            // Create Scenario
            Scenario scenario = Scenario.builder()
                    .level(level)
                    .name("First Day Quiz")
                    .description("Answer questions about office policies")
                    .orderIndex(0)
                    .active(true)
                    .build();
            scenario = scenarioRepository.save(scenario);
            log.info("Created scenario: {}", scenario.getName());
            
            // Create MCQ
            MCQ mcq = MCQ.builder()
                    .scenario(scenario)
                    .question("What time does the office open?")
                    .options("[\"8:00 AM\", \"9:00 AM\", \"10:00 AM\", \"11:00 AM\"]")
                    .correctAnswerIndex(1) // 9:00 AM
                    .build();
            mcqRepository.save(mcq);
            
            // Create Scenario Steps
            ScenarioStep step1 = ScenarioStep.builder()
                    .scenario(scenario)
                    .name("Welcome Dialog")
                    .description("Manager welcomes the player")
                    .orderIndex(0)
                    .type(StepType.ENTRY)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(dialog1)
                    .audio(bgMusic)
                    .active(true)
                    .build();
            stepRepository.save(step1);
            
            ScenarioStep step2 = ScenarioStep.builder()
                    .scenario(scenario)
                    .name("Quiz Introduction")
                    .description("Manager introduces the quiz")
                    .orderIndex(1)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(dialog2)
                    .active(true)
                    .build();
            stepRepository.save(step2);
            
            ScenarioStep step3 = ScenarioStep.builder()
                    .scenario(scenario)
                    .name("Answer Quiz")
                    .description("Player answers the MCQ")
                    .orderIndex(2)
                    .type(StepType.MCQ)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build();
            stepRepository.save(step3);
            
            ScenarioStep step4 = ScenarioStep.builder()
                    .scenario(scenario)
                    .name("Complete Scenario")
                    .description("Scenario complete")
                    .orderIndex(3)
                    .type(StepType.EXIT)
                    .movementMode(MovementMode.FREE)
                    .active(true)
                    .build();
            stepRepository.save(step4);
            
            log.info("Demo game setup complete!");
        }
        
        log.info("Data loading complete");
    }
}

