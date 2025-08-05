import { useState } from 'react';

interface AuthProps {
  onAuth: (username: string, password: string) => void;
  onOtpVerify: (otp: string) => void;
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
    onOtpVerify(otp);
  };

  if (authState === 'otp_required') {
    return (
      <div className="flex items-center justify-center min-h-screen p-4 bg-gradient-to-br from-indigo-500 to-purple-600 select-none">
        <div className="bg-white/95 dark:bg-gray-800/95 backdrop-blur-lg border border-white/20 rounded-2xl p-8 shadow-2xl w-full max-w-md text-center">
          <h2 className="text-3xl font-semibold text-gray-800 dark:text-gray-100 mb-6">🎮 Minecraft Web Chat</h2>
          <h3 className="text-xl font-medium text-gray-600 dark:text-gray-300 mb-4">Verify OTP</h3>
          {otpMessage && (
            <p className="text-green-700 dark:text-green-400 text-sm mb-4 p-3 bg-green-100 dark:bg-green-900/20 border-l-3 border-green-500 rounded-md">
              {otpMessage}
            </p>
          )}
          <form onSubmit={handleOtpSubmit} className="flex flex-col gap-4">
            <div className="text-left">
              <label htmlFor="otp" className="block mb-2 text-gray-700 dark:text-gray-300 font-medium text-sm">
                Enter the OTP from your in-game chat:
              </label>
              <input
                id="otp"
                type="text"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
                placeholder="Enter OTP"
                maxLength={6}
                required
                className="w-full px-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg text-base bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-100 transition-all duration-200 focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/10 select-text"
              />
            </div>
            <div className="text-left">
              <label htmlFor="new-password" className="block mb-2 text-gray-700 dark:text-gray-300 font-medium text-sm">
                Password (already submitted):
              </label>
              <input
                id="new-password"
                type="password"
                value="••••••••"
                placeholder="Password saved securely"
                readOnly
                disabled
                className="w-full px-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg text-base bg-gray-100 dark:bg-gray-600 text-gray-600 dark:text-gray-400 cursor-not-allowed"
              />
            </div>
            <button
              type="submit"
              disabled={isLoading}
              className="mt-2 px-4 py-3.5 bg-indigo-500 hover:bg-indigo-600 disabled:bg-gray-400 text-white font-semibold rounded-lg text-base cursor-pointer transition-all duration-200 hover:transform hover:-translate-y-0.5 hover:shadow-lg hover:shadow-indigo-500/30 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none"
            >
              {isLoading ? 'Verifying...' : 'Verify & Create Account'}
            </button>
          </form>
          {error && (
            <p className="text-red-600 dark:text-red-400 text-sm mt-2 p-3 bg-red-100 dark:bg-red-900/20 border-l-3 border-red-500 rounded-md">
              {error}
            </p>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center min-h-screen p-4 bg-gradient-to-br from-indigo-500 to-purple-600 select-none">
      <div className="bg-white/95 dark:bg-gray-800/95 backdrop-blur-lg border border-white/20 rounded-2xl p-8 shadow-2xl w-full max-w-md text-center">
        <h2 className="text-3xl font-semibold text-gray-800 dark:text-gray-100 mb-6">🎮 Minecraft Web Chat</h2>
        
        <form onSubmit={handleAuth} className="flex flex-col gap-4">
          <div className="text-left">
            <label htmlFor="username" className="block mb-2 text-gray-700 dark:text-gray-300 font-medium text-sm">
              Username:
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Enter your Minecraft username"
              required
              className="w-full px-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg text-base bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-100 transition-all duration-200 focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/10 select-text"
            />
          </div>
          
          <div className="text-left">
            <label htmlFor="password" className="block mb-2 text-gray-700 dark:text-gray-300 font-medium text-sm">
              Password:
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter password"
              required
              className="w-full px-3 py-3 border border-gray-300 dark:border-gray-600 rounded-lg text-base bg-white dark:bg-gray-700 text-gray-800 dark:text-gray-100 transition-all duration-200 focus:outline-none focus:border-indigo-500 focus:ring-3 focus:ring-indigo-500/10 select-text"
            />
          </div>
          
          <button
            type="submit"
            disabled={isLoading}
            className="mt-2 px-4 py-3.5 bg-indigo-500 hover:bg-indigo-600 disabled:bg-gray-400 text-white font-semibold rounded-lg text-base cursor-pointer transition-all duration-200 hover:transform hover:-translate-y-0.5 hover:shadow-lg hover:shadow-indigo-500/30 disabled:cursor-not-allowed disabled:transform-none disabled:shadow-none"
          >
            {isLoading ? 'Connecting...' : 'Login / Register'}
          </button>
        </form>
        
        {error && (
          <p className="text-red-600 dark:text-red-400 text-sm mt-2 p-3 bg-red-100 dark:bg-red-900/20 border-l-3 border-red-500 rounded-md">
            {error}
          </p>
        )}
        
        <p className="text-gray-600 dark:text-gray-400 text-sm mt-4 leading-relaxed">
          Enter your username and password. If you're a new user, you'll receive an OTP in-game to complete registration.
        </p>
      </div>
    </div>
  );
}