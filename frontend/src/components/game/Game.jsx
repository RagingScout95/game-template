import { useState, useEffect, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import {
  START_GAME_MUTATION,
  GET_GAME_STATE_QUERY,
  SUBMIT_MCQ_ANSWER_MUTATION,
  UPDATE_PLAYER_POSITION_MUTATION,
  TRIGGER_EVENT_MUTATION
} from '../../graphql/queries';
import PixelRenderer from './PixelRenderer';
import PlayerController from './PlayerController';
import DialogOverlay from './DialogOverlay';
import MCQOverlay from './MCQOverlay';

/**
 * Main Game Component
 * Orchestrates the game flow, renders the game world, handles player input
 * All game logic is controlled by the backend
 */
export default function Game() {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const { getAuthenticatedClient, user, token } = useAuth();
  
  const [gameState, setGameState] = useState(null);
  const [playerPos, setPlayerPos] = useState({ x: 400, y: 300 });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showDialog, setShowDialog] = useState(false);
  const [showMCQ, setShowMCQ] = useState(false);
  const [submittingMCQ, setSubmittingMCQ] = useState(false);
  
  // Load or start game
  useEffect(() => {
    initGame();
  }, [gameId]);
  
  const initGame = async () => {
    try {
      // Check authentication
      if (!token) {
        throw new Error('Not authenticated. Please log in again.');
      }
      if (!user) {
        throw new Error('User information not available. Please log in again.');
      }
      if (user.role !== 'PLAYER' && user.role !== 'ADMIN') {
        throw new Error(`Invalid user role: ${user.role}. Expected PLAYER or ADMIN.`);
      }
      
      const client = getAuthenticatedClient();
      const gameIdNum = parseInt(gameId, 10);
      
      if (isNaN(gameIdNum)) {
        throw new Error(`Invalid game ID: ${gameId}`);
      }
      
      console.log('Initializing game with ID:', gameIdNum);
      console.log('User:', user);
      console.log('Token present:', !!token);
      
      try {
        // Try to get existing progress
        const stateData = await client.request(GET_GAME_STATE_QUERY, {
          gameId: gameIdNum
        });
        console.log('Existing game state:', stateData);
        setGameState(stateData.getGameState);
        updatePlayerPositionFromState(stateData.getGameState);
      } catch (e) {
        console.log('No existing progress, starting new game. Error:', e);
        // No existing progress, start new game
        console.log('Starting new game...');
        const startData = await client.request(START_GAME_MUTATION, {
          gameId: gameIdNum
        });
        console.log('Game started:', startData);
        setGameState(startData.startGame);
        updatePlayerPositionFromState(startData.startGame);
      }
      
      setLoading(false);
    } catch (err) {
      console.error('Error initializing game:', err);
      console.error('Full error object:', JSON.stringify(err, null, 2));
      const errorMessage = err.response?.errors?.[0]?.message || 
                          err.message || 
                          'Failed to load game';
      setError(`Failed to load game: ${errorMessage}`);
      setLoading(false);
    }
  };
  
  const updatePlayerPositionFromState = (state) => {
    if (state?.progress?.playerPosition) {
      try {
        const pos = JSON.parse(state.progress.playerPosition);
        setPlayerPos(pos);
      } catch (e) {
        console.error('Error parsing player position:', e);
      }
    }
  };
  
  // Auto-advance helper for steps without dialogs
  const handleStepAdvance = useCallback(async () => {
    try {
      const client = getAuthenticatedClient();
      const gameIdNum = parseInt(gameId, 10);
      const data = await client.request(TRIGGER_EVENT_MUTATION, {
        input: {
          gameId: gameIdNum,
          condition: 'STEP_COMPLETION',
          params: '{}'
        }
      });
      setGameState(data.triggerEvent);
    } catch (err) {
      console.error('Error advancing step:', err);
    }
  }, [gameId, getAuthenticatedClient]);
  
  // Check if dialog should be shown
  useEffect(() => {
    console.log('Current step:', gameState?.currentStep);
    if (gameState?.currentStep?.dialog) {
      console.log('Dialog found:', gameState.currentStep.dialog);
      setShowDialog(true);
    } else if (gameState?.currentStep?.type === 'ENTRY' && !gameState?.currentStep?.dialog) {
      // ENTRY step without dialog - auto-advance after a short delay
      console.log('ENTRY step without dialog, auto-advancing...');
      const timer = setTimeout(() => {
        handleStepAdvance();
      }, 1000);
      return () => clearTimeout(timer);
    } else {
      setShowDialog(false);
    }
  }, [gameState?.currentStep, handleStepAdvance]);
  
  // Check if MCQ should be shown
  useEffect(() => {
    if (gameState?.currentStep?.type === 'MCQ' && gameState?.currentScenario?.mcq) {
      setShowMCQ(true);
    }
  }, [gameState?.currentStep, gameState?.currentScenario]);
  
  // Handle player movement
  const handlePlayerMove = useCallback(async (dx, dy) => {
    const newX = Math.max(16, Math.min(784, playerPos.x + dx));
    const newY = Math.max(16, Math.min(584, playerPos.y + dy));
    
    setPlayerPos({ x: newX, y: newY });
    
    // Debounce position updates to backend
    // In production, you'd want to batch these or update less frequently
    try {
      const client = getAuthenticatedClient();
      const gameIdNum = parseInt(gameId, 10);
      await client.request(UPDATE_PLAYER_POSITION_MUTATION, {
        input: {
          gameId: gameIdNum,
          x: newX,
          y: newY
        }
      });
    } catch (err) {
      console.error('Error updating position:', err);
    }
  }, [playerPos, gameId, getAuthenticatedClient]);
  
  // Handle dialog close - advance to next step
  const handleDialogClose = async () => {
    setShowDialog(false);
    await handleStepAdvance();
  };
  
  // Handle MCQ submission
  const handleMCQSubmit = async (selectedOption) => {
    setSubmittingMCQ(true);
    
    try {
      const client = getAuthenticatedClient();
      
      // Submit answer
      await client.request(SUBMIT_MCQ_ANSWER_MUTATION, {
        input: {
          mcqId: gameState.currentScenario.mcq.id,
          selectedOptionIndex: selectedOption
        }
      });
      
      setShowMCQ(false);
      
      // Advance to next step
      const gameIdNum = parseInt(gameId, 10);
      const data = await client.request(TRIGGER_EVENT_MUTATION, {
        input: {
          gameId: gameIdNum,
          condition: 'MCQ_ANSWERED',
          params: '{}'
        }
      });
      
      setGameState(data.triggerEvent);
    } catch (err) {
      console.error('Error submitting MCQ:', err);
      alert('Error submitting answer');
    } finally {
      setSubmittingMCQ(false);
    }
  };
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-2xl">Loading game...</div>
      </div>
    );
  }
  
  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="text-2xl text-red-400 mb-4">{error}</div>
          <button onClick={() => navigate('/games')} className="btn-primary">
            Back to Games
          </button>
        </div>
      </div>
    );
  }
  
  if (gameState?.progress?.completed) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <h1 className="text-4xl font-bold mb-4">Game Complete!</h1>
          <p className="text-xl text-gray-400 mb-8">Congratulations on finishing the game!</p>
          <button onClick={() => navigate('/games')} className="btn-primary">
            Back to Games
          </button>
        </div>
      </div>
    );
  }
  
  const movementMode = gameState?.currentStep?.movementMode || 'FREE';
  const movementLocked = movementMode === 'LOCKED' || showDialog || showMCQ;
  
  return (
    <div className="min-h-screen p-8">
      <div className="max-w-6xl mx-auto">
        {/* Header */}
        <div className="mb-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold">
                {gameState?.currentLevel?.name || 'Loading...'}
              </h1>
              {gameState?.currentScenario && (
                <p className="text-gray-400">
                  Scenario: {gameState.currentScenario.name}
                </p>
              )}
            </div>
            <button
              onClick={() => navigate('/games')}
              className="btn-secondary"
            >
              Exit Game
            </button>
          </div>
        </div>
        
        {/* Game Status */}
        {gameState?.currentStep && (
          <div className="mb-4 p-4 bg-[#181818] border-2 border-gray-600">
            <div className="flex justify-between items-center">
              <div>
                <span className="text-sm text-gray-400">Current Step: </span>
                <span className="font-semibold">{gameState.currentStep.name}</span>
                <span className="ml-4 text-sm text-gray-400">Type: </span>
                <span className="text-cyan-400">{gameState.currentStep.type}</span>
              </div>
              <div>
                <span className="text-sm text-gray-400">Movement: </span>
                <span className={movementLocked ? 'text-red-400' : 'text-green-400'}>
                  {movementLocked ? 'LOCKED' : movementMode}
                </span>
              </div>
            </div>
          </div>
        )}
        
        {/* Game Canvas */}
        <div className="mb-4">
          <PixelRenderer
            width={800}
            height={600}
            playerPosition={playerPos}
            npcs={gameState?.availableNPCs || []}
            mapData={gameState?.currentLevel?.mapData}
          />
        </div>
        
        {/* Controls Info */}
        <div className="text-center text-sm text-gray-400">
          {movementLocked ? (
            <p>Movement is locked. Complete the current action to continue.</p>
          ) : (
            <p>Use Arrow Keys or WASD to move</p>
          )}
        </div>
        
        {/* Player Controller */}
        <PlayerController
          onMove={handlePlayerMove}
          movementMode={movementMode}
          enabled={!movementLocked}
        />
        
        {/* Dialog Overlay */}
        {showDialog && gameState?.currentStep?.dialog && (
          <DialogOverlay
            dialog={gameState.currentStep.dialog}
            onClose={handleDialogClose}
          />
        )}
        
        {/* MCQ Overlay */}
        {showMCQ && gameState?.currentScenario?.mcq && (
          <MCQOverlay
            mcq={gameState.currentScenario.mcq}
            onSubmit={handleMCQSubmit}
            disabled={submittingMCQ}
          />
        )}
        
        {/* Fallback: Movement locked but no dialog/MCQ - show continue button */}
        {movementLocked && !showDialog && !showMCQ && gameState?.currentStep && (
          <div className="fixed bottom-8 left-1/2 transform -translate-x-1/2 bg-[#181818] border-2 border-gray-600 p-6 max-w-2xl w-11/12">
            <div className="text-center">
              <p className="text-white text-lg mb-4">
                {gameState.currentStep.type === 'ENTRY' 
                  ? 'Starting scenario...' 
                  : `Current step: ${gameState.currentStep.name}`}
              </p>
              <button
                onClick={handleStepAdvance}
                className="btn-primary"
              >
                Continue
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
