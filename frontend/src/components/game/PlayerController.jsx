import { useEffect, useCallback } from 'react';

/**
 * PlayerController - Handles player input and movement
 * Only sends input events, doesn't enforce rules (backend controls that)
 */
export default function PlayerController({ 
  onMove, 
  movementMode = 'FREE',
  enabled = true 
}) {
  
  const handleKeyDown = useCallback((e) => {
    if (!enabled || movementMode === 'LOCKED') return;
    
    const moveSpeed = 10;
    let dx = 0;
    let dy = 0;
    
    switch (e.key) {
      case 'ArrowUp':
      case 'w':
      case 'W':
        if (movementMode !== 'HORIZONTAL_ONLY') dy = -moveSpeed;
        break;
      case 'ArrowDown':
      case 's':
      case 'S':
        if (movementMode !== 'HORIZONTAL_ONLY') dy = moveSpeed;
        break;
      case 'ArrowLeft':
      case 'a':
      case 'A':
        if (movementMode !== 'VERTICAL_ONLY') dx = -moveSpeed;
        break;
      case 'ArrowRight':
      case 'd':
      case 'D':
        if (movementMode !== 'VERTICAL_ONLY') dx = moveSpeed;
        break;
      default:
        return;
    }
    
    if (dx !== 0 || dy !== 0) {
      e.preventDefault();
      onMove(dx, dy);
    }
  }, [enabled, movementMode, onMove]);
  
  useEffect(() => {
    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [handleKeyDown]);
  
  return null; // This component doesn't render anything
}

