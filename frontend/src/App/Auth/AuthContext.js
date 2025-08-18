/**
 * Authentication Context Manager
 * 
 * Overview:
 * - Manages user login status and JWT tokens
 * - Provides unified authentication interface (login, logout, token refresh)
 * - Integrates with Keycloak identity authentication service
 * - Automatically handles token expiration and refresh
 * 
 * Core Features:
 * - Persistent storage: tokens saved in localStorage
 * - Auto refresh: automatically refreshes tokens 1 minute before expiration
 * - State synchronization: integrates with Redux state management
 * - Error handling: comprehensive exception handling mechanism
 * 
 * Tech Stack:
 * - React Context API: state sharing
 * - Keycloak: identity authentication service
 * - Axios: HTTP requests
 * - Redux: global state management
 */

import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';
import { keycloakInstance, KEYCLOAK_URL, KEYCLOAK_REALM, KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET } from './keycloak';
import { useDispatch } from 'react-redux';
import { setAuthenticated, logout as reduxLogout } from '../redux/slice/authSlice';
import { decodeToken } from './JwtUtils';

// Create authentication context for sharing auth state in component tree
const AuthContext = createContext();

/**
 * Authentication Provider Component
 * 
 * Purpose: Provides authentication-related state and methods for the entire app
 * Wrapper: Usually wraps the root component of the application
 * 
 * @param {React.ReactNode} children - Child components
 */
export const AuthProvider = ({ children }) => {
    // Authentication state management
    const [isAuthenticated, setIsAuthenticated] = useState(false); // Whether user is authenticated
    const [token, setToken] = useState(localStorage.getItem('token')); // JWT access token
    const [refreshToken, setRefreshToken] = useState(localStorage.getItem('refreshToken')); // Refresh token
    const [loading, setLoading] = useState(true); // Initial loading state
    
    const dispatch = useDispatch(); // Redux state dispatcher

    /**
     * Token refresh function
     * 
     * Function: Uses refresh token to get new access token
     * Trigger: Automatically called when access token is about to expire
     * Security: Prevents unexpected user session interruption
     * 
     * Execution flow:
     * 1. Check if refresh token is available
     * 2. Send refresh request to Keycloak
     * 3. Update local storage and state
     * 4. Set next refresh timer
     */
    const refresh = async () => {
        const currentRefreshToken = localStorage.getItem('refreshToken');
        if (!currentRefreshToken) {
            console.error('No refresh token available');
            return;
        }

        console.log('token refreshed!');
        try {
            // Send token refresh request to Keycloak
            const response = await axios.post(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, new URLSearchParams({
                client_id: KEYCLOAK_CLIENT_ID,
                client_secret: KEYCLOAK_CLIENT_SECRET,
                grant_type: 'refresh_token', // Specify refresh token mode
                refresh_token: currentRefreshToken
            }), {
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded'
                }
            });

            if (response.status === 200) {
                // Refresh successful, update token information
                const newToken = response.data.access_token;
                const newRefreshToken = response.data.refresh_token;
                
                // Update state and local storage
                setToken(newToken);
                setRefreshToken(newRefreshToken);
                localStorage.setItem('token', newToken);
                localStorage.setItem('refreshToken', newRefreshToken);
                dispatch(setAuthenticated({ isAuthenticated: true, token: newToken }));

                // Parse token and set next refresh timer
                const tokenParsed = decodeToken(newToken);
                if (tokenParsed) {
                    // Refresh 1 minute before token expires
                    const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                    setTimeout(refresh, timeout);
                }
            } else {
                console.error('Token refresh failed', response.data);
                handleLogout(false); // Auto logout on refresh failure
            }
        } catch (error) {
            console.error('Error refreshing token', error);
            handleLogout(false); // Auto logout on exception
        }
    };

    /**
     * Initialize Keycloak authentication system
     * 
     * Execution timing: Automatically executed when component mounts
     * Main tasks:
     * 1. Check if locally stored tokens are valid
     * 2. Initialize Keycloak instance
     * 3. Set up automatic token refresh mechanism
     * 4. Synchronize authentication state to Redux
     */
    useEffect(() => {
        const initializeKeycloak = async () => {
            // If local tokens exist, use them directly
            if (token && refreshToken) {
                keycloakInstance.token = token;
                keycloakInstance.refreshToken = refreshToken;
                setIsAuthenticated(true);
                setLoading(false);
                dispatch(setAuthenticated({ isAuthenticated: true, token }));

                // Set token refresh timer
                const tokenParsed = decodeToken(token);
                if (tokenParsed) {
                    const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                    setTimeout(refresh, timeout);
                }
            } else {
                // No local tokens, try SSO login check
                try {
                    const authenticated = await keycloakInstance.init({ onLoad: 'check-sso' });
                    if (authenticated) {
                        // SSO login successful, get new tokens
                        const newToken = keycloakInstance.token;
                        const newRefreshToken = keycloakInstance.refreshToken;
                        setToken(newToken);
                        setRefreshToken(newRefreshToken);
                        localStorage.setItem('token', newToken);
                        localStorage.setItem('refreshToken', newRefreshToken);
                        setIsAuthenticated(true);
                        dispatch(setAuthenticated({ isAuthenticated: true, token: newToken }));

                    } else {
                        // SSO check failed, clear authentication state
                        handleLogout(false);
                    }
                    setLoading(false);
                    console.log('Keycloak initialized!');
                } catch (error) {
                    console.error('Error initializing Keycloak', error);
                    setLoading(false);
                }
            }
            
            // Set token refresh timer
            const tokenParsed = decodeToken(token);
            if (tokenParsed) {
                const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                setTimeout(refresh, timeout);
            }
        };

        initializeKeycloak();
    }, [dispatch]);

    /**
     * Handle user logout logic
     * 
     * Function: Clear all authentication-related state and data
     * Cleanup content:
     * - Local state (authentication status, tokens)
     * - Local storage (localStorage)
     * - Redux state
     * 
     * @param {boolean} shouldReload - Whether to reload page
     */
    const handleLogout = (shouldReload) => {
        setIsAuthenticated(false);
        setToken(null);
        setRefreshToken(null);
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');
        dispatch(reduxLogout());
        if (shouldReload) {
            window.location.reload();
        }
    };

    /**
     * User login method
     * 
     * Authentication flow:
     * 1. Send username/password to Keycloak
     * 2. Get access token and refresh token
     * 3. Save tokens to local storage
     * 4. Update authentication state
     * 5. Set up automatic token refresh
     * 
     * Security features:
     * - Uses OAuth2 password mode
     * - Automatic token refresh mechanism
     * - Local state synchronization with Redux
     * 
     * @param {string} username - Username/email
     * @param {string} password - Password
     * @returns {Promise<void>}
     */
    const login = async (username, password) => {
        // Send login request to Keycloak
        const response = await axios.post(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/token`, new URLSearchParams({
            client_id: KEYCLOAK_CLIENT_ID,
            client_secret: KEYCLOAK_CLIENT_SECRET,
            username,
            password,
            grant_type: 'password' // OAuth2 password mode
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded',
            }
        });

        if (response.status === 200) {
            // Login successful, handle tokens
            const newToken = response.data.access_token;
            const newRefreshToken = response.data.refresh_token;
            
            // Update state and storage
            setToken(newToken);
            setRefreshToken(newRefreshToken);
            localStorage.setItem('token', newToken);
            localStorage.setItem('refreshToken', newRefreshToken);
            setIsAuthenticated(true);
            dispatch(setAuthenticated({ isAuthenticated: true, token: newToken }));
            
            // Set up automatic token refresh
            const tokenParsed = decodeToken(newToken);
            if (tokenParsed) {
                const timeout = (tokenParsed.exp * 1000) - Date.now() - 60000;
                setTimeout(refresh, timeout);
            }
        } else {
            console.error('Login failed', response.data);
        }
    };

    /**
     * User logout method
     * 
     * Function: Actively logout user
     * Execution steps:
     * 1. Send logout request to Keycloak
     * 2. Clear local authentication state
     * 3. Refresh page to ensure state consistency
     * 
     * @returns {Promise<void>}
     */
    const logout = async () => {
        // Send logout request to Keycloak
        await axios.post(`${KEYCLOAK_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/logout`, new URLSearchParams({
            client_id: KEYCLOAK_CLIENT_ID,
            client_secret: KEYCLOAK_CLIENT_SECRET,
            refresh_token: refreshToken
        }), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });

        // Clear local state and refresh page
        handleLogout(true);
    };

    // Provide authentication context value, including state and methods
    return (
        <AuthContext.Provider value={{ 
            isAuthenticated,  // Authentication status
            token,            // Access token
            login,            // Login method
            logout,           // Logout method
            loading,          // Loading status
            refresh,          // Token refresh method
            handleLogout      // Internal logout handler
        }}>
            {children}
        </AuthContext.Provider>
    );
};

/**
 * Authentication context hook
 * 
 * Usage: const { login, logout, isAuthenticated } = useAuth();
 * Returns: Object containing all authentication-related state and methods
 * 
 * @returns {Object} Authentication context value
 */
export const useAuth = () => useContext(AuthContext);