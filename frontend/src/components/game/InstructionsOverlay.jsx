import { useState } from 'react';

/**
 * Instructions Overlay Component
 * Shows game controls and how to play
 */
export default function InstructionsOverlay({ onClose }) {
  const [currentPage, setCurrentPage] = useState(0);
  
  const instructions = [
    {
      title: "Welcome to the Game!",
      content: [
        "This is a scenario-based pixel game where you'll interact with NPCs, answer questions, and progress through different levels.",
        "Use the controls below to navigate and interact with the game world."
      ]
    },
    {
      title: "Movement Controls",
      content: [
        "• Use Arrow Keys (↑ ↓ ← →) to move your character",
        "• Or use WASD keys (W A S D)",
        "• Movement may be locked during dialogs or questions",
        "• Watch the Movement status indicator at the top"
      ]
    },
    {
      title: "Interactions",
      content: [
        "• NPCs will approach you and start conversations",
        "• Click 'Continue' on dialogs to advance",
        "• Answer Multiple Choice Questions (MCQs) when they appear",
        "• Complete scenarios to progress to the next one"
      ]
    },
    {
      title: "Game Progress",
      content: [
        "• Each game has multiple levels",
        "• Each level has multiple scenarios",
        "• Complete all scenarios in a level to advance",
        "• Your progress is saved automatically"
      ]
    },
    {
      title: "Tips",
      content: [
        "• Read dialogs carefully - they contain important information",
        "• Think before answering MCQs - they affect your progress",
        "• The game saves your position automatically",
        "• You can exit and return to continue where you left off"
      ]
    }
  ];
  
  const nextPage = () => {
    if (currentPage < instructions.length - 1) {
      setCurrentPage(currentPage + 1);
    } else {
      onClose();
    }
  };
  
  const prevPage = () => {
    if (currentPage > 0) {
      setCurrentPage(currentPage - 1);
    }
  };
  
  const current = instructions[currentPage];
  
  return (
    <div className="fixed inset-0 bg-black/80 flex items-center justify-center z-50 p-4">
      <div className="bg-[#181818] border-2 border-gray-600 p-8 max-w-2xl w-full max-h-[90vh] overflow-y-auto">
        {/* Header */}
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-3xl font-bold">{current.title}</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white text-2xl font-bold"
            aria-label="Close instructions"
          >
            ×
          </button>
        </div>
        
        {/* Content */}
        <div className="mb-6 space-y-4">
          {current.content.map((line, index) => (
            <p key={index} className="text-gray-300 leading-relaxed">
              {line}
            </p>
          ))}
        </div>
        
        {/* Controls */}
        <div className="flex justify-between items-center pt-4 border-t border-gray-600">
          <div className="text-sm text-gray-400">
            Page {currentPage + 1} of {instructions.length}
          </div>
          
          <div className="flex gap-4">
            {currentPage > 0 && (
              <button
                onClick={prevPage}
                className="px-6 py-2 bg-[#212121] border-2 border-gray-600 hover:border-gray-400 transition-colors"
              >
                Previous
              </button>
            )}
            <button
              onClick={nextPage}
              className="px-6 py-2 bg-cyan-600 hover:bg-cyan-700 transition-colors font-semibold"
            >
              {currentPage === instructions.length - 1 ? 'Got it!' : 'Next'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

