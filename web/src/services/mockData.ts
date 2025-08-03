import { WebSocketService } from './websocket';

// Mock data for testing the chat UI without backend
export const mockMessages = [
  {
    username: 'Steve',
    message: 'Hey everyone, anyone want to build a castle together?',
    timestamp: new Date(Date.now() - 300000), // 5 minutes ago
    source: 'game' as const
  },
  {
    username: 'Alex',
    message: 'Sure! I have tons of stone blocks to share',
    timestamp: new Date(Date.now() - 240000), // 4 minutes ago
    source: 'game' as const
  },
  {
    username: 'TestUser',
    message: 'I\'d love to help! Where should we meet?',
    timestamp: new Date(Date.now() - 120000), // 2 minutes ago
    source: 'web' as const
  },
  {
    username: 'Steve',
    message: 'Let\'s meet at spawn, coordinates 0, 64, 0',
    timestamp: new Date(Date.now() - 60000), // 1 minute ago
    source: 'game' as const
  },
  {
    username: 'Creeper_Hunter',
    message: 'Watch out for creepers at night! I saw a bunch near the village',
    timestamp: new Date(Date.now() - 30000), // 30 seconds ago
    source: 'game' as const
  }
];

export const enableMockMode = () => {
  // Override the WebSocket service for testing  
  WebSocketService.prototype.connect = async function() {
    console.log('Mock mode enabled - simulating WebSocket connection');
    
    // Simulate successful connection
    setTimeout(() => {
      this.onConnectionChange(true);
    }, 100);
    
    return Promise.resolve();
  };
  
  WebSocketService.prototype.sendMessage = function(message) {
    console.log('Mock WebSocket send:', message);
    
    // Simulate auth success for any auth attempt
    if (message.type === 'auth') {
      setTimeout(() => {
        this.onMessage({
          type: 'auth_success',
          token: 'mock-token-123',
          username: message.username
        });
      }, 500);
    }
    
    // Echo chat messages back
    if (message.type === 'chat') {
      setTimeout(() => {
        this.onMessage({
          type: 'chat',
          message: `Echo: ${message.message}`
        });
      }, 1000);
    }
  };
  
  WebSocketService.prototype.isConnected = function() {
    return true;
  };
};