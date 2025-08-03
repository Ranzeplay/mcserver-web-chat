import { useState } from 'react';
import './Auth.css';

interface AuthProps {
  onAuth: (username: string, password: string) => void;
  onOtpVerify: (username: string, otp: string, password: string) => void;
  authState: 'idle' | 'authenticating' | 'otp_required';
  error: string | null;
  otpMessage: string | null;
  isLoading?: boolean;
}

export default function Auth({ onAuth, onOtpVerify, authState, error, otpMessage, isLoading = false }: AuthProps) {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [otp, setOtp] = useState('');

  const handleAuth = (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim() || !password.trim()) return;
    
    onAuth(username, password);
  };

  const handleOtpSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!otp.trim()) return;
    onOtpVerify(username, otp, password);
  };

  if (authState === 'otp_required') {
    return (
      <div className="auth-container">
        <div className="auth-card">
          <h2>🎮 Minecraft Web Chat</h2>
          <h3>Verify OTP</h3>
          {otpMessage && <p className="otp-message">{otpMessage}</p>}
          <form onSubmit={handleOtpSubmit} className="auth-form">
            <div className="form-group">
              <label htmlFor="otp">Enter the OTP from your in-game chat:</label>
              <input
                id="otp"
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                placeholder="Enter OTP"
                maxLength={6}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="new-password">Your password:</label>
              <input
                id="new-password"
                type="password"
                value={password}
                placeholder="Password set"
                readOnly
                disabled
              />
            </div>
            <button type="submit" disabled={isLoading}>
              {isLoading ? 'Verifying...' : 'Verify & Create Account'}
            </button>
          </form>
          {error && <p className="error-message">{error}</p>}
        </div>
      </div>
    );
  }

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h2>🎮 Minecraft Web Chat</h2>
        
        <form onSubmit={handleAuth} className="auth-form">
          <div className="form-group">
            <label htmlFor="username">Username:</label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your Minecraft username"
              required
            />
          </div>
          
          <div className="form-group">
            <label htmlFor="password">Password:</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
              required
            />
          </div>
          
          <button type="submit" disabled={isLoading}>
            {isLoading ? 'Connecting...' : 'Login / Register'}
          </button>
        </form>
        
        {error && <p className="error-message">{error}</p>}
        
        <p className="auth-info">
          Enter your username and password. If you're a new user, you'll receive an OTP in-game to complete registration.
        </p>
      </div>
    </div>
  );
}