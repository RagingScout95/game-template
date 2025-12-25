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
        
        // Create Testing Game - Comprehensive test with multiple features
        if (gameRepository.findByName("Testing Game").isEmpty()) {
            Game testGame = Game.builder()
                    .name("Testing Game")
                    .description("A comprehensive testing game with multiple levels, scenarios, NPCs, and MCQs")
                    .active(true)
                    .build();
            testGame = gameRepository.save(testGame);
            log.info("Created testing game");
            
            // ===== LEVEL 1: Training Center =====
            Level level1 = Level.builder()
                    .game(testGame)
                    .name("Training Center")
                    .description("The main training facility")
                    .orderIndex(0)
                    .mapData("{\"width\": 1000, \"height\": 800, \"background\": \"training_center.png\"}")
                    .active(true)
                    .build();
            level1 = levelRepository.save(level1);
            log.info("Created level: {}", level1.getName());
            
            // NPCs for Level 1
            NPC trainer = NPC.builder()
                    .level(level1)
                    .name("Master Trainer")
                    .description("The head trainer who guides new players")
                    .movementType(NPCMovementType.STATIC)
                    .spriteSheet("/sprites/trainer.png")
                    .initialPosition("{\"x\": 200, \"y\": 300}")
                    .active(true)
                    .build();
            trainer = npcRepository.save(trainer);
            
            NPC assistant = NPC.builder()
                    .level(level1)
                    .name("Training Assistant")
                    .description("Helps with practice questions")
                    .movementType(NPCMovementType.PATROL)
                    .spriteSheet("/sprites/assistant.png")
                    .initialPosition("{\"x\": 600, \"y\": 400}")
                    .active(true)
                    .build();
            assistant = npcRepository.save(assistant);
            
            // Audio for Level 1
            AudioAsset trainingMusic = AudioAsset.builder()
                    .name("Training Center Music")
                    .description("Motivational training music")
                    .url("/audio/training_bg.mp3")
                    .loop(true)
                    .build();
            trainingMusic = audioRepository.save(trainingMusic);
            
            // SCENARIO 1: Welcome & Introduction
            Scenario scenario1 = Scenario.builder()
                    .level(level1)
                    .name("Welcome to Training")
                    .description("Introduction to the training center")
                    .orderIndex(0)
                    .active(true)
                    .build();
            scenario1 = scenarioRepository.save(scenario1);
            
            Dialog welcomeDialog1 = Dialog.builder()
                    .speakerName("Master Trainer")
                    .text("Welcome, new recruit! I'm here to guide you through your training.")
                    .npc(trainer)
                    .build();
            welcomeDialog1 = dialogRepository.save(welcomeDialog1);
            
            Dialog welcomeDialog2 = Dialog.builder()
                    .speakerName("Master Trainer")
                    .text("Let's start with some basic knowledge. Are you ready?")
                    .npc(trainer)
                    .build();
            welcomeDialog2 = dialogRepository.save(welcomeDialog2);
            
            MCQ basicMCQ = MCQ.builder()
                    .scenario(scenario1)
                    .question("What is the primary goal of this training program?")
                    .options("[\"Learn new skills\", \"Pass assessments\", \"Complete all scenarios\", \"Master the system\"]")
                    .correctAnswerIndex(0)
                    .build();
            basicMCQ = mcqRepository.save(basicMCQ);
            
            Dialog correctDialog = Dialog.builder()
                    .speakerName("Master Trainer")
                    .text("Excellent! You're on the right track. Let's continue.")
                    .npc(trainer)
                    .build();
            correctDialog = dialogRepository.save(correctDialog);
            
            // Steps for Scenario 1
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario1)
                    .name("Entry")
                    .description("Enter training center")
                    .orderIndex(0)
                    .type(StepType.ENTRY)
                    .movementMode(MovementMode.LOCKED)
                    .audio(trainingMusic)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario1)
                    .name("Welcome Message 1")
                    .description("First welcome dialog")
                    .orderIndex(1)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(welcomeDialog1)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario1)
                    .name("Welcome Message 2")
                    .description("Second welcome dialog")
                    .orderIndex(2)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(welcomeDialog2)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario1)
                    .name("Basic Knowledge Quiz")
                    .description("Answer basic MCQ")
                    .orderIndex(3)
                    .type(StepType.MCQ)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario1)
                    .name("Correct Answer Feedback")
                    .description("Feedback for correct answer")
                    .orderIndex(4)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(correctDialog)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario1)
                    .name("Exit")
                    .description("Complete scenario")
                    .orderIndex(5)
                    .type(StepType.EXIT)
                    .movementMode(MovementMode.FREE)
                    .active(true)
                    .build());
            
            // SCENARIO 2: Advanced Training Question 1
            Scenario scenario2 = Scenario.builder()
                    .level(level1)
                    .name("Advanced Training - Question 1")
                    .description("First advanced knowledge question")
                    .orderIndex(1)
                    .active(true)
                    .build();
            scenario2 = scenarioRepository.save(scenario2);
            
            Dialog advancedDialog1 = Dialog.builder()
                    .speakerName("Training Assistant")
                    .text("Great job! Now let's test your advanced knowledge with the first question.")
                    .npc(assistant)
                    .build();
            advancedDialog1 = dialogRepository.save(advancedDialog1);
            
            MCQ advancedMCQ1 = MCQ.builder()
                    .scenario(scenario2)
                    .question("Which of these is NOT a valid step type?")
                    .options("[\"ENTRY\", \"DIALOG\", \"MCQ\", \"BATTLE\"]")
                    .correctAnswerIndex(3)
                    .build();
            advancedMCQ1 = mcqRepository.save(advancedMCQ1);
            
            Dialog correctDialog1 = Dialog.builder()
                    .speakerName("Training Assistant")
                    .text("Correct! BATTLE is not a valid step type. Let's continue to the next question.")
                    .npc(assistant)
                    .build();
            correctDialog1 = dialogRepository.save(correctDialog1);
            
            // Steps for Scenario 2
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario2)
                    .name("Entry")
                    .description("Enter advanced training question 1")
                    .orderIndex(0)
                    .type(StepType.ENTRY)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario2)
                    .name("Introduction")
                    .description("Assistant introduces first advanced question")
                    .orderIndex(1)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(advancedDialog1)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario2)
                    .name("Advanced MCQ 1")
                    .description("First advanced question")
                    .orderIndex(2)
                    .type(StepType.MCQ)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario2)
                    .name("Feedback")
                    .description("Feedback for answer")
                    .orderIndex(3)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(correctDialog1)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario2)
                    .name("Exit")
                    .description("Complete first advanced question")
                    .orderIndex(4)
                    .type(StepType.EXIT)
                    .movementMode(MovementMode.FREE)
                    .active(true)
                    .build());
            
            // SCENARIO 3: Advanced Training Question 2
            Scenario scenario3 = Scenario.builder()
                    .level(level1)
                    .name("Advanced Training - Question 2")
                    .description("Second advanced knowledge question")
                    .orderIndex(2)
                    .active(true)
                    .build();
            scenario3 = scenarioRepository.save(scenario3);
            
            Dialog advancedDialog2 = Dialog.builder()
                    .speakerName("Training Assistant")
                    .text("Excellent! Now here's your second advanced question.")
                    .npc(assistant)
                    .build();
            advancedDialog2 = dialogRepository.save(advancedDialog2);
            
            MCQ advancedMCQ2 = MCQ.builder()
                    .scenario(scenario3)
                    .question("What happens when a scenario is completed?")
                    .options("[\"Next scenario loads\", \"Game ends\", \"Player gets rewards\", \"All of the above\"]")
                    .correctAnswerIndex(3)
                    .build();
            advancedMCQ2 = mcqRepository.save(advancedMCQ2);
            
            Dialog completionDialog = Dialog.builder()
                    .speakerName("Training Assistant")
                    .text("Perfect! You've mastered the training center. Time to move to the next level!")
                    .npc(assistant)
                    .build();
            completionDialog = dialogRepository.save(completionDialog);
            
            // Steps for Scenario 3
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario3)
                    .name("Entry")
                    .description("Enter advanced training question 2")
                    .orderIndex(0)
                    .type(StepType.ENTRY)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario3)
                    .name("Introduction")
                    .description("Assistant introduces second advanced question")
                    .orderIndex(1)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(advancedDialog2)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario3)
                    .name("Advanced MCQ 2")
                    .description("Second advanced question")
                    .orderIndex(2)
                    .type(StepType.MCQ)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario3)
                    .name("Completion Message")
                    .description("Training complete")
                    .orderIndex(3)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(completionDialog)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario3)
                    .name("Exit")
                    .description("Complete advanced training")
                    .orderIndex(4)
                    .type(StepType.EXIT)
                    .movementMode(MovementMode.FREE)
                    .active(true)
                    .build());
            
            // ===== LEVEL 2: Assessment Hall =====
            Level level2 = Level.builder()
                    .game(testGame)
                    .name("Assessment Hall")
                    .description("Final assessment area")
                    .orderIndex(1)
                    .mapData("{\"width\": 1200, \"height\": 900, \"background\": \"assessment_hall.png\"}")
                    .active(true)
                    .build();
            level2 = levelRepository.save(level2);
            log.info("Created level: {}", level2.getName());
            
            NPC examiner = NPC.builder()
                    .level(level2)
                    .name("Chief Examiner")
                    .description("Conducts final assessments")
                    .movementType(NPCMovementType.STATIC)
                    .spriteSheet("/sprites/examiner.png")
                    .initialPosition("{\"x\": 500, \"y\": 350}")
                    .active(true)
                    .build();
            examiner = npcRepository.save(examiner);
            
            AudioAsset assessmentMusic = AudioAsset.builder()
                    .name("Assessment Hall Music")
                    .description("Serious assessment music")
                    .url("/audio/assessment_bg.mp3")
                    .loop(true)
                    .build();
            assessmentMusic = audioRepository.save(assessmentMusic);
            
            // SCENARIO 4: Final Assessment Question 1
            Scenario scenario4 = Scenario.builder()
                    .level(level2)
                    .name("Final Assessment - Question 1")
                    .description("First assessment question")
                    .orderIndex(0)
                    .active(true)
                    .build();
            scenario4 = scenarioRepository.save(scenario4);
            
            Dialog examDialog1 = Dialog.builder()
                    .speakerName("Chief Examiner")
                    .text("Welcome to the Assessment Hall. This is your final test. Let's begin with the first question.")
                    .npc(examiner)
                    .build();
            examDialog1 = dialogRepository.save(examDialog1);
            
            MCQ finalMCQ1 = MCQ.builder()
                    .scenario(scenario4)
                    .question("What is the correct way to handle player progress?")
                    .options("[\"Store in database\", \"Keep in memory only\", \"Use cookies\", \"Ignore it\"]")
                    .correctAnswerIndex(0)
                    .build();
            finalMCQ1 = mcqRepository.save(finalMCQ1);
            
            Dialog continueDialog1 = Dialog.builder()
                    .speakerName("Chief Examiner")
                    .text("Good! Let's continue to the next question.")
                    .npc(examiner)
                    .build();
            continueDialog1 = dialogRepository.save(continueDialog1);
            
            // Steps for Scenario 4
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario4)
                    .name("Entry")
                    .description("Enter assessment hall")
                    .orderIndex(0)
                    .type(StepType.ENTRY)
                    .movementMode(MovementMode.LOCKED)
                    .audio(assessmentMusic)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario4)
                    .name("Examiner Introduction")
                    .description("Examiner introduces first question")
                    .orderIndex(1)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(examDialog1)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario4)
                    .name("Final MCQ 1")
                    .description("First final question")
                    .orderIndex(2)
                    .type(StepType.MCQ)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario4)
                    .name("Continue Message")
                    .description("Continue to next question")
                    .orderIndex(3)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(continueDialog1)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario4)
                    .name("Exit")
                    .description("Complete first assessment question")
                    .orderIndex(4)
                    .type(StepType.EXIT)
                    .movementMode(MovementMode.FREE)
                    .active(true)
                    .build());
            
            // SCENARIO 5: Final Assessment Question 2
            Scenario scenario5 = Scenario.builder()
                    .level(level2)
                    .name("Final Assessment - Question 2")
                    .description("Second assessment question")
                    .orderIndex(1)
                    .active(true)
                    .build();
            scenario5 = scenarioRepository.save(scenario5);
            
            Dialog examDialog2 = Dialog.builder()
                    .speakerName("Chief Examiner")
                    .text("Here's your second assessment question.")
                    .npc(examiner)
                    .build();
            examDialog2 = dialogRepository.save(examDialog2);
            
            MCQ finalMCQ2 = MCQ.builder()
                    .scenario(scenario5)
                    .question("Which component controls game logic?")
                    .options("[\"Frontend\", \"Backend\", \"Database\", \"Browser\"]")
                    .correctAnswerIndex(1)
                    .build();
            finalMCQ2 = mcqRepository.save(finalMCQ2);
            
            Dialog continueDialog2 = Dialog.builder()
                    .speakerName("Chief Examiner")
                    .text("Well done! One more question to go.")
                    .npc(examiner)
                    .build();
            continueDialog2 = dialogRepository.save(continueDialog2);
            
            // Steps for Scenario 5
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario5)
                    .name("Entry")
                    .description("Enter second assessment question")
                    .orderIndex(0)
                    .type(StepType.ENTRY)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario5)
                    .name("Examiner Introduction")
                    .description("Examiner introduces second question")
                    .orderIndex(1)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(examDialog2)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario5)
                    .name("Final MCQ 2")
                    .description("Second final question")
                    .orderIndex(2)
                    .type(StepType.MCQ)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario5)
                    .name("Continue Message")
                    .description("Continue to final question")
                    .orderIndex(3)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(continueDialog2)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario5)
                    .name("Exit")
                    .description("Complete second assessment question")
                    .orderIndex(4)
                    .type(StepType.EXIT)
                    .movementMode(MovementMode.FREE)
                    .active(true)
                    .build());
            
            // SCENARIO 6: Final Assessment Question 3
            Scenario scenario6 = Scenario.builder()
                    .level(level2)
                    .name("Final Assessment - Question 3")
                    .description("Final assessment question")
                    .orderIndex(2)
                    .active(true)
                    .build();
            scenario6 = scenarioRepository.save(scenario6);
            
            Dialog examDialog3 = Dialog.builder()
                    .speakerName("Chief Examiner")
                    .text("This is your final question. Answer it correctly to pass the assessment!")
                    .npc(examiner)
                    .build();
            examDialog3 = dialogRepository.save(examDialog3);
            
            MCQ finalMCQ3 = MCQ.builder()
                    .scenario(scenario6)
                    .question("What is the purpose of triggers in the game engine?")
                    .options("[\"Control flow\", \"Animate sprites\", \"Play sounds\", \"Render graphics\"]")
                    .correctAnswerIndex(0)
                    .build();
            finalMCQ3 = mcqRepository.save(finalMCQ3);
            
            Dialog passDialog = Dialog.builder()
                    .speakerName("Chief Examiner")
                    .text("Congratulations! You've passed the assessment with flying colors!")
                    .npc(examiner)
                    .build();
            passDialog = dialogRepository.save(passDialog);
            
            // Steps for Scenario 6
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario6)
                    .name("Entry")
                    .description("Enter final assessment question")
                    .orderIndex(0)
                    .type(StepType.ENTRY)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario6)
                    .name("Examiner Introduction")
                    .description("Examiner introduces final question")
                    .orderIndex(1)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(examDialog3)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario6)
                    .name("Final MCQ 3")
                    .description("Third final question")
                    .orderIndex(2)
                    .type(StepType.MCQ)
                    .movementMode(MovementMode.LOCKED)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario6)
                    .name("Pass Message")
                    .description("Success message")
                    .orderIndex(3)
                    .type(StepType.DIALOG)
                    .movementMode(MovementMode.LOCKED)
                    .dialog(passDialog)
                    .active(true)
                    .build());
            
            stepRepository.save(ScenarioStep.builder()
                    .scenario(scenario6)
                    .name("Exit")
                    .description("Complete assessment")
                    .orderIndex(4)
                    .type(StepType.EXIT)
                    .movementMode(MovementMode.FREE)
                    .active(true)
                    .build());
            
            log.info("Testing game setup complete!");
            log.info("Created: 2 levels, 6 scenarios (one per MCQ), 3 NPCs, 15+ dialogs, 6 MCQs, 30+ steps");
        }
        
        log.info("Data loading complete");
    }
}

