import type React from "react";

interface MinimizedWindowProps {
  title: string;
  onRestore: () => void;
  position: number; // Position index for spacing;
  icon?: React.ReactNode; // Optional icon for the window
}

export default function MinimizedWindow({ title, onRestore, position, icon }: MinimizedWindowProps) {
  return (
    <div
      className="fixed bottom-4 z-40"
      style={{ left: 20 + position * 70 }}
    >
      <button
        onClick={onRestore}
        className="w-12 h-12 bg-indigo-600 hover:bg-indigo-700 text-white shadow-lg flex items-center justify-center transition-all duration-200 group"
        title={`Restore ${title}`}
      >
        <span className="text-xs font-bold">
          {icon ?? title.slice(0, 2).toUpperCase()}
        </span>
        {/* Tooltip */}
        <div className="absolute bottom-full mb-2 left-1/2 transform -translate-x-1/2 px-2 py-1 bg-gray-800 text-white text-xs opacity-0 group-hover:opacity-100 transition-opacity duration-200 whitespace-nowrap">
          {title}
        </div>
      </button>
    </div>
  );
}