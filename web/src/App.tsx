import { useState, useEffect } from 'react';
import Auth from './components/Auth';
import Chat from './components/Chat';
import { WebSocketService, type ChatMessage, type AuthState, type ServerMessage } from './services/websocket';
import { enableMockMode, mockMessages } from './services/mockData';
import './App.css';

function App() {
  const [authState, setAuthState] = useState<AuthState>({
    isAuthenticated: false,
    username: null,
    token: null
  });
  const [authMode, setAuthMode] = useState<'idle' | 'authenticating' | 'otp_required'>('idle');
  const [authError, setAuthError] = useState<string | null>(null);
  const [otpMessage, setOtpMessage] = useState<string | null>(null);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [wsService] = useState(() => new WebSocketService());

  useEffect(() => {
    const initializeWebSocket = async () => {
      // Check for mock mode in URL
      const urlParams = new URLSearchParams(window.location.search);
      const isMockMode = urlParams.get('mock') === 'true';
      
      if (isMockMode) {
        enableMockMode();
        setMessages(mockMessages);
        console.log('Running in mock mode for UI testing');
        setIsConnected(true); // Mock connection as connected
        // Auto-authenticate in mock mode
        setAuthState({
          isAuthenticated: true,
          username: 'MockUser',
          token: 'mock-token-123'
        });
        return;
      }

      wsService.onMessage = handleServerMessage;
      wsService.onConnectionChange = setIsConnected;
      
      // Small delay to ensure component is fully mounted
      await new Promise(resolve => setTimeout(resolve, 100));
      
      try {
        console.log('Attempting to connect to WebSocket server...');
        await wsService.connect();
        console.log('WebSocket connection established successfully');
      } catch (error) {
        console.error('Failed to connect to WebSocket:', error);
        setAuthError(`Failed to connect to server: ${error instanceof Error ? error.message : 'Unknown error'}. Please ensure the server is running and try again.`);
      }
    };

    initializeWebSocket();

    return () => {
      wsService.disconnect();
    };
  }, [wsService]);

  const handleServerMessage = (message: ServerMessage) => {
    switch (message.type) {
      case 'auth_success':
        setAuthState({
          isAuthenticated: true,
          username: message.username,
          token: message.token
        });
        setAuthMode('idle');
        setAuthError(null);
        break;
        
      case 'auth_failure':
        setAuthError(message.reason);
        setAuthMode('idle');
        break;
        
      case 'otp_required':
        setAuthMode('otp_required');
        setOtpMessage(message.message);
        setAuthError(null);
        break;
        
      case 'chat': {
        // Parse the chat message which comes in format "username: message"
        const chatText = message.message;
        const colonIndex = chatText.indexOf(': ');
        if (colonIndex > 0) {
          const username = chatText.substring(0, colonIndex);
          const messageText = chatText.substring(colonIndex + 2);
          
          const chatMessage: ChatMessage = {
            username,
            message: messageText,
            timestamp: new Date(),
            source: 'game' // Messages from server are from game
          };
          setMessages(prev => [...prev, chatMessage]);
        } else {
          // System message or malformed message
          const chatMessage: ChatMessage = {
            username: 'System',
            message: chatText,
            timestamp: new Date(),
            source: 'game'
          };
          setMessages(prev => [...prev, chatMessage]);
        }
        break;
      }
        
      case 'error':
        setAuthError(message.message);
        break;
    }
  };

  const handleAuth = (username: string, password: string) => {
    setAuthMode('authenticating');
    setAuthError(null);
    wsService.sendMessage({ type: 'auth', username, password });
  };

  const handleOtpVerify = (otp: string) => {
    setAuthMode('authenticating');
    setAuthError(null);
    wsService.sendMessage({ type: 'otp_verify', otp });
  };

  const handleSendMessage = (messageText: string) => {
    if (authState.username) {
      // Add message to local state immediately for better UX
      const localMessage: ChatMessage = {
        username: authState.username,
        message: messageText,
        timestamp: new Date(),
        source: 'web'
      };
      setMessages(prev => [...prev, localMessage]);
      
      // Send to server
      wsService.sendMessage({ type: 'chat', message: messageText });
    }
  };

  const handleDisconnect = () => {
    setAuthState({
      isAuthenticated: false,
      username: null,
      token: null
    });
    setMessages([]);
    setAuthMode('idle');
    setAuthError(null);
    setOtpMessage(null);
    // Keep WebSocket connected for next login
  };

  if (!authState.isAuthenticated) {
    return (
      <Auth
        onAuth={handleAuth}
        onOtpVerify={handleOtpVerify}
        authState={authMode}
        error={authError}
        otpMessage={otpMessage}
        isLoading={authMode === 'authenticating'}
      />
    );
  }

  return (
    <Chat
      messages={messages}
      currentUsername={authState.username!}
      isConnected={isConnected}
      onSendMessage={handleSendMessage}
      onDisconnect={handleDisconnect}
    />
  );
}

export default App;
