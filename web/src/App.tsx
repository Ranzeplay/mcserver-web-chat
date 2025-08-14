import { useState, useEffect } from 'react';
import Auth from './components/Auth';
import Chat from './components/Chat';
import DraggableWindow from './components/DraggableWindow';
import MinimizedWindow from './components/MinimizedWindow';
import { WebSocketService, type ChatMessage, type AuthState, type ServerMessage } from './services/websocket';
import { SessionService } from './services/sessionService';
import { enableMockMode, mockMessages } from './services/mockData';
import { LogIn, MessageCircleMore } from 'lucide-react';
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
  const [sentMessageIds, setSentMessageIds] = useState<Set<string>>(new Set());
  const [playerList, setPlayerList] = useState<string[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [wsService] = useState(() => new WebSocketService());
  
  // Window state management
  const [isAuthMinimized, setIsAuthMinimized] = useState(false);
  const [isChatMinimized, setIsChatMinimized] = useState(false);

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
        
        // Check for existing session after connection
        const savedSession = SessionService.getSession();
        if (savedSession) {
          console.log('Found saved session, attempting to restore...');
          setAuthMode('authenticating');
          wsService.sendMessage({ type: 'auth_token', token: savedSession.token });
        }
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
        // Save session data for persistence
        SessionService.saveSession(message.token, message.username);
        setAuthState({
          isAuthenticated: true,
          username: message.username,
          token: message.token
        });
        setAuthMode('idle');
        setAuthError(null);
        
        // Update player list if provided
        if (message.serverInfo?.playerList) {
          setPlayerList(message.serverInfo.playerList);
        }
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
        // Check if this is a message we sent and already have locally
        if (message.messageId && sentMessageIds.has(message.messageId)) {
          // This is our own message coming back, ignore it
          console.log('Ignoring duplicate message:', message.messageId);
          return;
        }
        
        // Parse the chat message which comes in format "username: message"
        const chatText = message.message;
        const colonIndex = chatText.indexOf(': ');
        if (colonIndex > 0) {
          const username = chatText.substring(0, colonIndex);
          const messageText = chatText.substring(colonIndex + 2);
          
          const chatMessage: ChatMessage = {
            id: message.messageId,
            username,
            message: messageText,
            timestamp: new Date(),
            source: 'game' // Messages from server are from game
          };
          setMessages(prev => [...prev, chatMessage]);
        } else {
          // System message or malformed message
          const chatMessage: ChatMessage = {
            id: message.messageId,
            username: 'System',
            message: chatText,
            timestamp: new Date(),
            source: 'game'
          };
          setMessages(prev => [...prev, chatMessage]);
        }
        break;
      }
        
      case 'player_join': {
        const joinMessage: ChatMessage = {
          username: 'System',
          message: `${message.username} joined the game`,
          timestamp: new Date(),
          source: 'game'
        };
        setMessages(prev => [...prev, joinMessage]);
        // Add to player list if not already there
        setPlayerList(prev => prev.includes(message.username) ? prev : [...prev, message.username]);
        break;
      }
      
      case 'player_leave': {
        const leaveMessage: ChatMessage = {
          username: 'System',
          message: `${message.username} left the game`,
          timestamp: new Date(),
          source: 'game'
        };
        setMessages(prev => [...prev, leaveMessage]);
        // Remove from player list
        setPlayerList(prev => prev.filter(name => name !== message.username));
        break;
      }
      
      case 'player_list_update': {
        setPlayerList(message.playerList);
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
      // Generate a unique message ID
      const messageId = `msg-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
      
      // Add message to local state immediately for better UX
      const localMessage: ChatMessage = {
        id: messageId,
        username: authState.username,
        message: messageText,
        timestamp: new Date(),
        source: 'web'
      };
      setMessages(prev => [...prev, localMessage]);
      
      // Track this message ID to prevent duplication
      setSentMessageIds(prev => new Set([...prev, messageId]));
      
      // Send to server with message ID
      wsService.sendMessage({ type: 'chat', message: messageText, messageId });
    }
  };

  const handleDisconnect = () => {
    // Clear session data
    SessionService.clearSession();
    setAuthState({
      isAuthenticated: false,
      username: null,
      token: null
    });
    setMessages([]);
    setSentMessageIds(new Set());
    setPlayerList([]);
    setAuthMode('idle');
    setAuthError(null);
    setOtpMessage(null);
    // Keep WebSocket connected for next login
  };

  if (!authState.isAuthenticated) {
    return (
      <div className="min-h-screen bg-indigo-600">
        <DraggableWindow
          title="Login"
          isMinimized={isAuthMinimized}
          onMinimize={() => setIsAuthMinimized(true)}
          onRestore={() => setIsAuthMinimized(false)}
          defaultPosition={{ x: (window.innerWidth - 400) / 2, y: (window.innerHeight - 500) / 2 }}
          width={400}
          height={500}
        >
          <Auth
            onAuth={handleAuth}
            onOtpVerify={handleOtpVerify}
            authState={authMode}
            error={authError}
            otpMessage={otpMessage}
            isLoading={authMode === 'authenticating'}
          />
        </DraggableWindow>
        
        {/* Minimized window indicators */}
        {isAuthMinimized && (
          <MinimizedWindow
            title="Login"
            onRestore={() => setIsAuthMinimized(false)}
            position={0}
            icon={<LogIn />}
          />
        )}
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-900">
      <DraggableWindow
        title="Minecraft Web Chat"
        isMinimized={isChatMinimized}
        onMinimize={() => setIsChatMinimized(true)}
        onRestore={() => setIsChatMinimized(false)}
        defaultPosition={{ x: 50, y: 50 }}
        width={900}
        height={600}
      >
        <Chat
          messages={messages}
          currentUsername={authState.username!}
          isConnected={isConnected}
          playerList={playerList}
          onSendMessage={handleSendMessage}
          onDisconnect={handleDisconnect}
        />
      </DraggableWindow>
      
      {/* Minimized window indicators */}
      {isChatMinimized && (
        <MinimizedWindow
          title="Chat"
          onRestore={() => setIsChatMinimized(false)}
          position={0}
          icon={<MessageCircleMore />}
        />
      )}
    </div>
  );
}

export default App;
