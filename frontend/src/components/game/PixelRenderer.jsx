import { useRef, useEffect } from 'react';

/**
 * PixelRenderer - Handles pixel-art rendering on canvas
 * Responsible for rendering the game world, player, and NPCs
 */
export default function PixelRenderer({ 
  width = 800, 
  height = 600, 
  playerPosition = { x: 400, y: 300 },
  npcs = [],
  mapData 
}) {
  const canvasRef = useRef(null);
  
  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;
    
    const ctx = canvas.getContext('2d');
    ctx.imageSmoothingEnabled = false; // Pixel-perfect rendering
    
    // Clear canvas
    ctx.fillStyle = '#2c2c2c';
    ctx.fillRect(0, 0, width, height);
    
    // Draw grid (for demo purposes)
    ctx.strokeStyle = '#3a3a3a';
    ctx.lineWidth = 1;
    for (let x = 0; x < width; x += 50) {
      ctx.beginPath();
      ctx.moveTo(x, 0);
      ctx.lineTo(x, height);
      ctx.stroke();
    }
    for (let y = 0; y < height; y += 50) {
      ctx.beginPath();
      ctx.moveTo(0, y);
      ctx.lineTo(width, y);
      ctx.stroke();
    }
    
    // Draw NPCs
    npcs.forEach(npc => {
      try {
        const pos = JSON.parse(npc.initialPosition || '{"x": 0, "y": 0}');
        // Draw NPC as a colored square (placeholder for sprite)
        ctx.fillStyle = '#4a90e2';
        ctx.fillRect(pos.x - 16, pos.y - 16, 32, 32);
        
        // NPC name
        ctx.fillStyle = '#ffffff';
        ctx.font = '12px monospace';
        ctx.textAlign = 'center';
        ctx.fillText(npc.name, pos.x, pos.y - 25);
      } catch (e) {
        console.error('Error parsing NPC position:', e);
      }
    });
    
    // Draw player
    ctx.fillStyle = '#50fa7b';
    ctx.fillRect(playerPosition.x - 16, playerPosition.y - 16, 32, 32);
    
    // Player label
    ctx.fillStyle = '#ffffff';
    ctx.font = '12px monospace';
    ctx.textAlign = 'center';
    ctx.fillText('YOU', playerPosition.x, playerPosition.y - 25);
    
  }, [playerPosition, npcs, width, height, mapData]);
  
  return (
    <canvas
      ref={canvasRef}
      width={width}
      height={height}
      className="game-canvas mx-auto"
    />
  );
}

