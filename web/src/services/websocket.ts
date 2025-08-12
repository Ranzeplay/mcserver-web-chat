export interface ChatMessage {
  id?: string;
  username: string;
  message: string;
  timestamp: Date;
  source: 'web' | 'game';
}

export interface AuthState {
  isAuthenticated: boolean;
  username: string | null;
  token: string | null;
}

export type WebSocketMessage = 
  | { type: 'auth'; username: string; password?: string }
  | { type: 'auth_token'; token: string }
  | { type: 'otp_verify'; otp: string }
  | { type: 'chat'; message: string; messageId?: string };

export type ServerMessage = 
  | { type: 'auth_success'; token: string; username: string }
  | { type: 'auth_failure'; reason: string }
  | { type: 'otp_required'; message: string }
  | { type: 'chat'; message: string; messageId?: string }
  | { type: 'error'; message: string };

export class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  private isConnecting = false;
  private shouldReconnect = true;
  
  public onMessage: (message: ServerMessage) => void = () => {};
  public onConnectionChange: (connected: boolean) => void = () => {};
  
  connect(url: string = 'ws://localhost:8080/ws'): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.isConnecting) {
        reject(new Error('Connection already in progress'));
        return;
      }
      
      this.isConnecting = true;
      this.shouldReconnect = true;
      
      try {
        // Clean up any existing connection
        if (this.ws) {
          this.ws.onopen = null;
          this.ws.onmessage = null;
          this.ws.onclose = null;
          this.ws.onerror = null;
          if (this.ws.readyState === WebSocket.OPEN) {
            this.ws.close();
          }
        }
        
        this.ws = new WebSocket(url);
        let connectionResolved = false;
        
        this.ws.onopen = () => {
          console.log('WebSocket connected successfully');
          this.reconnectAttempts = 0;
          this.isConnecting = false;
          this.onConnectionChange(true);
          if (!connectionResolved) {
            connectionResolved = true;
            resolve();
          }
        };
        
        this.ws.onmessage = (event) => {
          try {
            const message: ServerMessage = JSON.parse(event.data);
            this.onMessage(message);
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
          }
        };
        
        this.ws.onclose = (event) => {
          console.log('WebSocket disconnected:', event.code, event.reason);
          this.isConnecting = false;
          this.onConnectionChange(false);
          
          // Only attempt reconnect if this was an established connection
          // and we want to reconnect
          if (this.shouldReconnect && connectionResolved) {
            this.attemptReconnect();
          } else if (!connectionResolved) {
            // Connection failed before it was established
            connectionResolved = true;
            reject(new Error(`WebSocket connection failed: ${event.reason || 'Connection closed'}`));
          }
        };
        
        this.ws.onerror = (error) => {
          console.error('WebSocket error:', error);
          this.isConnecting = false;
          
          if (!connectionResolved) {
            connectionResolved = true;
            reject(new Error('WebSocket connection failed'));
          }
        };
        
        // Set a timeout for connection attempt
        setTimeout(() => {
          if (!connectionResolved && this.isConnecting) {
            this.isConnecting = false;
            connectionResolved = true;
            reject(new Error('WebSocket connection timeout'));
          }
        }, 5000);
        
      } catch (error) {
        this.isConnecting = false;
        reject(error);
      }
    });
  }
  
  private attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts && this.shouldReconnect) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      
      setTimeout(() => {
        if (this.shouldReconnect) {
          this.connect().catch((error) => {
            console.error('Reconnection failed:', error);
            // Will attempt again if under limit
          });
        }
      }, this.reconnectDelay * this.reconnectAttempts);
    } else {
      console.log('Max reconnection attempts reached or reconnection disabled');
    }
  }
  
  sendMessage(message: WebSocketMessage) {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(JSON.stringify(message));
    } else {
      console.error('WebSocket is not connected');
    }
  }
  
  disconnect() {
    this.shouldReconnect = false;
    if (this.ws) {
      this.ws.onopen = null;
      this.ws.onmessage = null;
      this.ws.onclose = null;
      this.ws.onerror = null;
      this.ws.close();
      this.ws = null;
    }
    this.isConnecting = false;
  }
  
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}