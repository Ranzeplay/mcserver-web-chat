import './ChatMessage.css';

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
    <div className={`chat-message ${isOwnMessage ? 'own-message' : ''} ${source}`}>
      <div className="message-header">
        <span className="username">{username}</span>
        <span className="source-indicator">{source === 'game' ? '🎮' : '💬'}</span>
        <span className="timestamp">{formatTime(timestamp)}</span>
      </div>
      <div className="message-content">
        {message}
      </div>
    </div>
  );
}