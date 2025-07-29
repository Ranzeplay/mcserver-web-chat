# ServerWebChat Authentication API

This document describes the web server authentication API added to the ServerWebChat Minecraft mod.

## Overview

The mod automatically starts a web server when enabled that provides a REST API for player authentication. The API supports:

- New player registration with password and OTP verification
- Existing player login with password and OTP verification  
- JWT token generation for authenticated sessions
- Secure password hashing with salt
- Time-limited OTP codes (120 seconds)

## Configuration

The web server configuration is stored in `config/serverwebchat-config.json`:

```json
{
  "port": 8080,
  "host": "0.0.0.0", 
  "enabled": true
}
```

- `port`: Port number for the web server (default: 8080)
- `host`: Host address to bind to (default: 0.0.0.0 for all interfaces)
- `enabled`: Whether to start the web server (default: true)

## API Endpoints

### POST /api/login

Handles player authentication with multiple flows depending on the request.

**Request Headers:**
- `Content-Type: application/json`

**CORS Support:**
- `Access-Control-Allow-Origin: *`
- `Access-Control-Allow-Methods: POST, OPTIONS`
- `Access-Control-Allow-Headers: Content-Type`

#### New Player Registration

**Request:**
```json
{
  "playerName": "examplePlayer",
  "password": "playerPassword123"
}
```

**Response (200 OK):**
```json
{
  "message": "New player registered. Please provide the OTP sent to you.",
  "requiresOtp": true
}
```

#### Existing Player Login (Password Only)

**Request:**
```json
{
  "playerName": "examplePlayer", 
  "password": "playerPassword123"
}
```

**Response (200 OK):**
```json
{
  "message": "OTP sent. Please provide the OTP to complete login.",
  "requiresOtp": true
}
```

#### Complete Login (Password + OTP)

**Request:**
```json
{
  "playerName": "examplePlayer",
  "password": "playerPassword123", 
  "otp": "123456"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Login successful",
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "playerName": "examplePlayer"
}
```

#### Error Responses

**400 Bad Request:**
```json
{
  "error": "Player name is required"
}
```

```json
{
  "error": "Password is required"
}
```

```json
{
  "error": "Invalid password"
}
```

```json
{
  "error": "Invalid OTP"
}
```

```json
{
  "error": "OTP expired. Please request a new one."
}
```

**405 Method Not Allowed:**
```json
{
  "error": "Method not allowed"
}
```

**500 Internal Server Error:**
```json
{
  "error": "Internal server error"
}
```

## Authentication Flow

1. **New Player Registration:**
   - Send playerName + password
   - System generates UUID, hashes password with salt
   - System generates 6-digit OTP and sends to player (logged to console)
   - Player must provide OTP within 120 seconds
   - Send playerName + password + otp to complete registration
   - Receive JWT token on success

2. **Existing Player Login:**
   - Send playerName + password
   - System verifies password against stored hash
   - System generates new 6-digit OTP and sends to player
   - Player must provide OTP within 120 seconds  
   - Send playerName + password + otp to complete login
   - Receive JWT token on success

3. **Using JWT Token:**
   - Include token in subsequent API requests
   - Token expires after 1 hour
   - Token contains player UUID and name

## Data Storage

Player data is stored in JSON files at `config/players/{uuid}.json`:

```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "playerName": "examplePlayer", 
  "passwordHash": "hashed_password_base64",
  "salt": "random_salt_base64",
  "otpTimestamp": 1640995200000,
  "otp": "123456",
  "createdAt": 1640995200000,
  "lastLogin": 1640995200000
}
```

## Security Features

- **Password Hashing:** SHA-256 with random salt
- **OTP Expiry:** OTP codes expire after 120 seconds
- **JWT Tokens:** Signed tokens with 1-hour expiration
- **CORS Support:** Allows cross-origin requests for web integration
- **Input Validation:** Validates all required fields

## Integration Examples

### JavaScript/Browser

```javascript
// Register new player
async function registerPlayer(playerName, password) {
  const response = await fetch('http://localhost:8080/api/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ playerName, password })
  });
  return await response.json();
}

// Complete login with OTP
async function loginWithOtp(playerName, password, otp) {
  const response = await fetch('http://localhost:8080/api/login', {
    method: 'POST', 
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ playerName, password, otp })
  });
  return await response.json();
}
```

### curl

```bash
# Register new player
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"playerName":"testPlayer","password":"testPass123"}'

# Login with OTP  
curl -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"playerName":"testPlayer","password":"testPass123","otp":"123456"}'
```

## Notes

- OTP codes are currently logged to the server console for testing
- In production, OTP delivery should be implemented via in-game chat or other secure means
- JWT secret key should be changed from the default in production environments
- The web server starts automatically when the mod is loaded
- All endpoints support OPTIONS requests for CORS preflight