/**
 * DialogOverlay - Displays NPC dialogs
 */
export default function DialogOverlay({ dialog, onClose }) {
  if (!dialog) return null;
  
  return (
    <div className="dialog-overlay">
      <div className="mb-2">
        <span className="text-cyan-400 font-bold text-lg">
          {dialog.speakerName}
        </span>
      </div>
      <p className="text-white text-lg leading-relaxed mb-4">
        {dialog.text}
      </p>
      <div className="flex justify-end">
        <button
          onClick={onClose}
          className="btn-primary"
        >
          Continue
        </button>
      </div>
    </div>
  );
}

