import { useState, useEffect, useRef } from 'react';
import ChatMessage from './ChatMessage';
import type { ChatMessage as ChatMessageType } from '../services/websocket';
import './Chat.css';

interface ChatProps {
  messages: ChatMessageType[];
  currentUsername: string;
  isConnected: boolean;
  onSendMessage: (message: string) => void;
  onDisconnect: () => void;
}

export default function Chat({ messages, currentUsername, isConnected, onSendMessage, onDisconnect }: ChatProps) {
  const [inputMessage, setInputMessage] = useState('');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Auto-scroll to bottom when new messages arrive
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  // Focus input on mount
  useEffect(() => {
    inputRef.current?.focus();
  }, []);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const trimmedMessage = inputMessage.trim();
    if (trimmedMessage && isConnected) {
      onSendMessage(trimmedMessage);
      setInputMessage('');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSubmit(e);
    }
  };

  return (
    <div className="chat-container">
      <div className="chat-header">
        <div className="header-info">
          <h2>🎮 Minecraft Web Chat</h2>
          <div className="user-info">
            <span className="username">Logged in as: <strong>{currentUsername}</strong></span>
            <div className={`connection-status ${isConnected ? 'connected' : 'disconnected'}`}>
              <span className="status-dot"></span>
              {isConnected ? 'Connected' : 'Disconnected'}
            </div>
          </div>
        </div>
        <button onClick={onDisconnect} className="disconnect-btn">
          Logout
        </button>
      </div>

      <div className="chat-messages">
        {messages.length === 0 ? (
          <div className="no-messages">
            <p>🎮 Welcome to Minecraft Web Chat!</p>
            <p>Start chatting with players in the game...</p>
          </div>
        ) : (
          messages.map((msg, index) => (
            <ChatMessage
              key={index}
              username={msg.username}
              message={msg.message}
              timestamp={msg.timestamp}
              source={msg.source}
              isOwnMessage={msg.username === currentUsername}
            />
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      <form onSubmit={handleSubmit} className="chat-input-form">
        <div className="input-container">
          <input
            ref={inputRef}
            type="text"
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={isConnected ? "Type your message..." : "Connecting..."}
            disabled={!isConnected}
            maxLength={256}
          />
          <button 
            type="submit" 
            disabled={!isConnected || !inputMessage.trim()}
            className="send-btn"
          >
            Send
          </button>
        </div>
        <div className="input-info">
          <span className="char-count">{inputMessage.length}/256</span>
          <span className="send-hint">Press Enter to send</span>
        </div>
      </form>
    </div>
  );
}