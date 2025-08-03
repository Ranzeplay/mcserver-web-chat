package space.ranzeplay.MCServerWebChat.models;

/**
 * Represents the authentication state of a user connection
 */
public enum UserState {
    UNAUTHENTICATED,    // Initial state, no authentication attempted
    OTP_REQUIRED,       // User exists but needs OTP verification for new account creation
    AUTHENTICATED       // User is fully authenticated and can chat
}