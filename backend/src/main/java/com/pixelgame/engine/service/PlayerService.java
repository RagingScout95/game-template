package com.pixelgame.engine.service;

import com.pixelgame.engine.model.entity.MCQ;
import com.pixelgame.engine.model.entity.PlayerAnswer;
import com.pixelgame.engine.model.entity.User;
import com.pixelgame.engine.repository.MCQRepository;
import com.pixelgame.engine.repository.PlayerAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlayerService {
    
    private final MCQRepository mcqRepository;
    private final PlayerAnswerRepository playerAnswerRepository;
    
    /**
     * Submit an MCQ answer
     */
    @Transactional
    public PlayerAnswer submitMCQAnswer(User user, Long mcqId, int selectedOptionIndex) {
        MCQ mcq = mcqRepository.findById(mcqId)
                .orElseThrow(() -> new RuntimeException("MCQ not found"));
        
        // Check if already answered
        playerAnswerRepository.findByUserIdAndMcqId(user.getId(), mcqId)
                .ifPresent(existing -> {
                    throw new RuntimeException("MCQ already answered");
                });
        
        // Check if answer is correct (if correctAnswerIndex is set)
        boolean isCorrect = mcq.getCorrectAnswerIndex() != null 
                && mcq.getCorrectAnswerIndex().equals(selectedOptionIndex);
        
        PlayerAnswer answer = PlayerAnswer.builder()
                .user(user)
                .mcq(mcq)
                .selectedOptionIndex(selectedOptionIndex)
                .isCorrect(isCorrect)
                .build();
        
        return playerAnswerRepository.save(answer);
    }
    
    /**
     * Get all answers for a player
     */
    @Transactional(readOnly = true)
    public List<PlayerAnswer> getPlayerAnswers(Long userId) {
        return playerAnswerRepository.findByUserId(userId);
    }
}

