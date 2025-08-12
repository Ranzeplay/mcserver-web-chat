export interface SessionData {
  token: string;
  username: string;
  expiresAt: number;
}

export class SessionService {
  private static readonly STORAGE_KEY = 'mcserver-webchat-session';
  private static readonly SESSION_DURATION = 7 * 24 * 60 * 60 * 1000; // 7 days

  static saveSession(token: string, username: string): void {
    const sessionData: SessionData = {
      token,
      username,
      expiresAt: Date.now() + this.SESSION_DURATION
    };
    
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(sessionData));
    } catch (error) {
      console.warn('Failed to save session to localStorage:', error);
    }
  }

  static getSession(): SessionData | null {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (!stored) return null;

      const sessionData: SessionData = JSON.parse(stored);
      
      // Check if session is expired
      if (Date.now() > sessionData.expiresAt) {
        this.clearSession();
        return null;
      }

      return sessionData;
    } catch (error) {
      console.warn('Failed to retrieve session from localStorage:', error);
      this.clearSession();
      return null;
    }
  }

  static clearSession(): void {
    try {
      localStorage.removeItem(this.STORAGE_KEY);
    } catch (error) {
      console.warn('Failed to clear session from localStorage:', error);
    }
  }

  static isSessionValid(): boolean {
    const session = this.getSession();
    return session !== null;
  }

  static extendSession(): void {
    const session = this.getSession();
    if (session) {
      // Extend the session by updating the expiration time
      this.saveSession(session.token, session.username);
    }
  }
}