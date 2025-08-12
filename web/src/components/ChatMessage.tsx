interface ChatMessageProps {
  username: string;
  message: string;
  timestamp: Date;
  source: 'web' | 'game';
  isOwnMessage?: boolean;
}

export default function ChatMessage({ username, message, timestamp, source, isOwnMessage }: ChatMessageProps) {
  const formatTime = (date: Date) => {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className={`mb-3 p-3 border transition-all duration-100 ${
      isOwnMessage 
        ? 'bg-blue-50 dark:bg-blue-900/20 border-blue-200 dark:border-blue-700 ml-8' 
        : 'bg-gray-50 dark:bg-gray-700/50 border-gray-200 dark:border-gray-600'
    } ${
      source === 'game' 
        ? 'border-l-4 border-l-green-500' 
        : 'border-l-4 border-l-blue-500'
    }`}>
      <div className="flex items-center gap-2 mb-1 text-sm">
        <span className={`font-semibold ${
          isOwnMessage 
            ? 'text-blue-700 dark:text-blue-300' 
            : 'text-gray-800 dark:text-gray-200'
        }`}>
          {username}
        </span>
        <span className="text-xs opacity-70">
          {source === 'game' ? '🎮' : '💬'}
        </span>
        <span className="text-gray-500 dark:text-gray-400 text-xs ml-auto">
          {formatTime(timestamp)}
        </span>
      </div>
      <div className="text-gray-800 dark:text-gray-200 leading-relaxed break-words select-text">
        {message}
      </div>
    </div>
  );
}