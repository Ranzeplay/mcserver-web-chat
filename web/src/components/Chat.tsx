import { useState, useEffect, useRef } from 'react';
import ChatMessage from './ChatMessage';
import type { ChatMessage as ChatMessageType } from '../services/websocket';

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
    <div className="flex flex-col h-screen bg-gray-50 dark:bg-gray-900 select-none">
      {/* Header */}
      <div className="flex items-center justify-between px-6 py-4 bg-white dark:bg-gray-800 border-b border-gray-200 dark:border-gray-700 shadow-sm">
        <div className="flex-1">
          <h2 className="text-2xl font-semibold text-gray-800 dark:text-gray-100 mb-2">🎮 Minecraft Web Chat</h2>
          <div className="flex items-center gap-4 text-sm">
            <span className="text-gray-600 dark:text-gray-400">
              Logged in as: <strong className="text-gray-800 dark:text-gray-200">{currentUsername}</strong>
            </span>
            <div className={`flex items-center gap-1.5 font-medium ${
              isConnected 
                ? 'text-green-600 dark:text-green-400' 
                : 'text-red-600 dark:text-red-400'
            }`}>
              <div className={`w-2 h-2 rounded-full ${
                isConnected ? 'bg-green-500' : 'bg-red-500'
              }`}></div>
              {isConnected ? 'Connected' : 'Disconnected'}
            </div>
          </div>
        </div>
        <button 
          onClick={onDisconnect}
          className="px-4 py-2 bg-red-500 hover:bg-red-600 text-white font-medium rounded-lg text-sm transition-colors duration-200"
        >
          Logout
        </button>
      </div>

      {/* Messages */}
      <div className="flex-1 overflow-y-auto p-4 bg-white dark:bg-gray-800 scrollbar-thin scrollbar-thumb-gray-300 dark:scrollbar-thumb-gray-600 scrollbar-track-gray-100 dark:scrollbar-track-gray-700">
        {messages.length === 0 ? (
          <div className="text-center mt-8 text-gray-500 dark:text-gray-400">
            <p className="text-xl font-semibold mb-2">🎮 Welcome to Minecraft Web Chat!</p>
            <p className="text-lg">Start chatting with players in the game...</p>
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

      {/* Input Form */}
      <form onSubmit={handleSubmit} className="p-6 bg-white dark:bg-gray-800 border-t border-gray-200 dark:border-gray-700">
        <div className="flex gap-3 mb-2">
          <input
            ref={inputRef}
            type="text"
            value={inputMessage}
            onChange={(e) => setInputMessage(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={isConnected ? "Type your message..." : "Connecting..."}
            disabled={!isConnected}
            maxLength={256}
            className="flex-1 px-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg text-base bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-100 transition-all duration-200 focus:outline-none focus:border-blue-500 focus:ring-3 focus:ring-blue-500/10 disabled:bg-gray-100 dark:disabled:bg-gray-600 disabled:text-gray-500 dark:disabled:text-gray-400 select-text"
          />
          <button 
            type="submit" 
            disabled={!isConnected || !inputMessage.trim()}
            className="px-6 py-3 bg-blue-500 hover:bg-blue-600 disabled:bg-gray-400 text-white font-semibold rounded-lg text-base transition-all duration-200 hover:transform hover:-translate-y-0.5 hover:shadow-lg hover:shadow-blue-500/30 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none min-w-[80px]"
          >
            Send
          </button>
        </div>
        <div className="flex justify-between items-center text-xs text-gray-500 dark:text-gray-400">
          <span className="font-mono">{inputMessage.length}/256</span>
          <span className="italic">Press Enter to send</span>
        </div>
      </form>
    </div>
  );
}