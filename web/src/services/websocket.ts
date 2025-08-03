export interface ChatMessage {
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
  | { type: 'otp_verify'; otp: string }
  | { type: 'chat'; message: string };

export type ServerMessage = 
  | { type: 'auth_success'; token: string; username: string }
  | { type: 'auth_failure'; reason: string }
  | { type: 'otp_required'; message: string }
  | { type: 'chat'; message: string }
  | { type: 'error'; message: string };

export class WebSocketService {
  private ws: WebSocket | null = null;
  private reconnectAttempts = 0;
  private maxReconnectAttempts = 5;
  private reconnectDelay = 1000;
  
  public onMessage: (message: ServerMessage) => void = () => {};
  public onConnectionChange: (connected: boolean) => void = () => {};
  
  connect(url: string = 'ws://localhost:8080/ws'): Promise<void> {
    return new Promise((resolve, reject) => {
      try {
        this.ws = new WebSocket(url);
        
        this.ws.onopen = () => {
          console.log('WebSocket connected');
          this.reconnectAttempts = 0;
          this.onConnectionChange(true);
          resolve();
        };
        
        this.ws.onmessage = (event) => {
          try {
            const message: ServerMessage = JSON.parse(event.data);
            this.onMessage(message);
          } catch (error) {
            console.error('Failed to parse WebSocket message:', error);
          }
        };
        
        this.ws.onclose = () => {
          console.log('WebSocket disconnected');
          this.onConnectionChange(false);
          this.attemptReconnect();
        };
        
        this.ws.onerror = (error) => {
          console.error('WebSocket error:', error);
          reject(error);
        };
      } catch (error) {
        reject(error);
      }
    });
  }
  
  private attemptReconnect() {
    if (this.reconnectAttempts < this.maxReconnectAttempts) {
      this.reconnectAttempts++;
      console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
      
      setTimeout(() => {
        this.connect().catch(() => {
          // Reconnection failed, will try again
        });
      }, this.reconnectDelay * this.reconnectAttempts);
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
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
  
  isConnected(): boolean {
    return this.ws !== null && this.ws.readyState === WebSocket.OPEN;
  }
}