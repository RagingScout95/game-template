import { useState } from 'react';

/**
 * MCQOverlay - Displays multiple choice questions
 */
export default function MCQOverlay({ mcq, onSubmit, disabled = false }) {
  const [selectedOption, setSelectedOption] = useState(null);
  
  if (!mcq) return null;
  
  const options = JSON.parse(mcq.options);
  
  const handleSubmit = () => {
    if (selectedOption !== null) {
      onSubmit(selectedOption);
    }
  };
  
  return (
    <div className="mcq-overlay">
      <div className="mcq-container">
        <h2 className="text-2xl font-bold mb-6">{mcq.question}</h2>
        
        <div className="space-y-3 mb-6">
          {options.map((option, index) => (
            <button
              key={index}
              onClick={() => setSelectedOption(index)}
              disabled={disabled}
              className={`w-full text-left p-4 border-2 transition-colors
                ${selectedOption === index 
                  ? 'border-cyan-400 bg-cyan-900/30' 
                  : 'border-gray-600 hover:border-gray-400'}
                ${disabled ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}
              `}
            >
              <span className="font-mono mr-3">{String.fromCharCode(65 + index)}.</span>
              {option}
            </button>
          ))}
        </div>
        
        <div className="flex justify-end">
          <button
            onClick={handleSubmit}
            disabled={selectedOption === null || disabled}
            className="btn-primary disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {disabled ? 'Submitting...' : 'Submit Answer'}
          </button>
        </div>
      </div>
    </div>
  );
}

