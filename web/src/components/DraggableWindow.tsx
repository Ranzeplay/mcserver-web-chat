import { useState, useRef, useEffect } from 'react';
import type { ReactNode } from 'react';

interface DraggableWindowProps {
  title: string;
  children: ReactNode;
  isMinimized: boolean;
  onMinimize: () => void;
  onRestore: () => void;
  defaultPosition?: { x: number; y: number };
  width?: number;
  height?: number;
  className?: string;
}

export default function DraggableWindow({
  title,
  children,
  isMinimized,
  onMinimize,
  defaultPosition = { x: 100, y: 100 },
  width = 500,
  height = 600,
  className = ''
}: DraggableWindowProps) {
  const [position, setPosition] = useState(defaultPosition);
  const [isDragging, setIsDragging] = useState(false);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const windowRef = useRef<HTMLDivElement>(null);

  // Handle mouse down on title bar to start dragging
  const handleMouseDown = (e: React.MouseEvent) => {
    if (e.target !== e.currentTarget && !(e.target as HTMLElement).closest('.window-title')) {
      return;
    }
    
    const rect = windowRef.current?.getBoundingClientRect();
    if (rect) {
      setDragOffset({
        x: e.clientX - rect.left,
        y: e.clientY - rect.top
      });
      setIsDragging(true);
    }
  };

  // Handle mouse move for dragging
  useEffect(() => {
    const handleMouseMove = (e: MouseEvent) => {
      if (isDragging) {
        const newX = e.clientX - dragOffset.x;
        const newY = e.clientY - dragOffset.y;
        
        // Keep window within viewport bounds
        const maxX = window.innerWidth - width;
        const maxY = window.innerHeight - height;
        
        setPosition({
          x: Math.max(0, Math.min(newX, maxX)),
          y: Math.max(0, Math.min(newY, maxY))
        });
      }
    };

    const handleMouseUp = () => {
      setIsDragging(false);
    };

    if (isDragging) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
    }

    return () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };
  }, [isDragging, dragOffset, width, height]);

  // Don't render the window if it's minimized
  if (isMinimized) {
    return null;
  }

  return (
    <div
      ref={windowRef}
      className={`fixed bg-white dark:bg-gray-800 border border-gray-300 dark:border-gray-600 shadow-2xl z-50 select-none ${className}`}
      style={{
        left: position.x,
        top: position.y,
        width: width,
        height: height,
        cursor: isDragging ? 'grabbing' : 'default'
      }}
    >
      {/* Title Bar */}
      <div
        className="window-title flex items-center justify-between px-4 py-3 bg-gray-100 dark:bg-gray-700 border-b border-gray-300 dark:border-gray-600 cursor-grab active:cursor-grabbing select-none"
        onMouseDown={handleMouseDown}
      >
        <h3 className="font-semibold text-gray-800 dark:text-gray-100 text-sm">
          {title}
        </h3>
        <div className="flex gap-2">
          <button
            onClick={onMinimize}
            className="w-3 h-3 bg-yellow-500 hover:bg-yellow-600 transition-colors duration-200"
            title="Minimize"
          />
          <div className="w-3 h-3 bg-gray-400" />
          <div className="w-3 h-3 bg-gray-400" />
        </div>
      </div>
      
      {/* Content */}
      <div className="flex-1 overflow-hidden h-full">
        <div className="h-full" style={{ height: height - 48 }}>
          {children}
        </div>
      </div>
    </div>
  );
}