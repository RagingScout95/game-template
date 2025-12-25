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
  const { getAuthenticatedClient } = useAuth();
  
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
      const client = getAuthenticatedClient();
      
      // Try to get existing game state
      try {
        const stateData = await client.request(GET_GAME_STATE_QUERY, {
          gameId: gameId
        });
        setGameState(stateData.getGameState);
        updatePlayerPositionFromState(stateData.getGameState);
      } catch (e) {
        // No existing progress, start new game
        const startData = await client.request(START_GAME_MUTATION, {
          gameId: gameId
        });
        setGameState(startData.startGame);
        updatePlayerPositionFromState(startData.startGame);
      }
      
      setLoading(false);
    } catch (err) {
      console.error('Error initializing game:', err);
      setError('Failed to load game');
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
  
  // Check if dialog should be shown
  useEffect(() => {
    if (gameState?.currentStep?.dialog) {
      setShowDialog(true);
    }
  }, [gameState?.currentStep]);
  
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
      await client.request(UPDATE_PLAYER_POSITION_MUTATION, {
        input: {
          gameId: gameId,
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
    
    try {
      const client = getAuthenticatedClient();
      const data = await client.request(TRIGGER_EVENT_MUTATION, {
        input: {
          gameId: gameId,
          condition: 'STEP_COMPLETION',
          params: '{}'
        }
      });
      setGameState(data.triggerEvent);
    } catch (err) {
      console.error('Error advancing game:', err);
    }
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
      const data = await client.request(TRIGGER_EVENT_MUTATION, {
        input: {
          gameId: gameId,
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
      </div>
    </div>
  );
}

