interface MinimizedWindowProps {
  title: string;
  onRestore: () => void;
  position: number; // Position index for spacing
}

export default function MinimizedWindow({ title, onRestore, position }: MinimizedWindowProps) {
  return (
    <div
      className="fixed bottom-4 z-40"
      style={{ left: 20 + position * 70 }}
    >
      <button
        onClick={onRestore}
        className="w-12 h-12 bg-indigo-500 hover:bg-indigo-600 text-white rounded-full shadow-lg flex items-center justify-center transition-all duration-200 hover:scale-110 group"
        title={`Restore ${title}`}
      >
        <span className="text-xs font-bold">
          {title.slice(0, 2).toUpperCase()}
        </span>
        {/* Tooltip */}
        <div className="absolute bottom-full mb-2 left-1/2 transform -translate-x-1/2 px-2 py-1 bg-gray-800 text-white text-xs rounded opacity-0 group-hover:opacity-100 transition-opacity duration-200 whitespace-nowrap">
          {title}
        </div>
      </button>
    </div>
  );
}