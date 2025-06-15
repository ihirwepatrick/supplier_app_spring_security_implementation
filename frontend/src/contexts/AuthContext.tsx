import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { Admin, Supplier, LoginRequest, LoginResponse } from '../types';
import { authAPI } from '../services/api';

interface AuthContextType {
    user: Admin | Supplier | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (data: LoginRequest) => Promise<void>;
    logout: () => void;
    registerAdmin: (data: Partial<Admin> & { password: string }) => Promise<void>;
    registerSupplier: (data: Partial<Supplier> & { password: string }) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<Admin | Supplier | null>(null);
    const [token, setToken] = useState<string | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    // Initialize auth state from localStorage
    const initializeAuth = useCallback(() => {
        try {
            const storedToken = localStorage.getItem('token');
            const storedUser = localStorage.getItem('user');
            
            console.log('Initializing auth state:', {
                hasStoredToken: !!storedToken,
                hasStoredUser: !!storedUser,
                storedUserValue: storedUser
            });
            
            if (storedToken && storedUser) {
                try {
                    const parsedUser = JSON.parse(storedUser);
                    // Validate token format
                    if (!storedToken.startsWith('Bearer ')) {
                        throw new Error('Invalid token format');
                    }
                    setToken(storedToken);
                    setUser(parsedUser);
                    console.log('Auth state initialized successfully:', {
                        hasToken: true,
                        userType: parsedUser.roles?.[0]
                    });
                } catch (e) {
                    console.error('Failed to parse stored auth data:', e);
                    // Clear invalid data
                    localStorage.removeItem('token');
                    localStorage.removeItem('user');
                    setToken(null);
                    setUser(null);
                }
            } else {
                console.log('No stored auth data found');
                setToken(null);
                setUser(null);
            }
        } catch (error) {
            console.error('Error initializing auth state:', error);
            // Clear invalid data
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setToken(null);
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    }, []);

    // Initialize auth state on mount and handle storage events
    useEffect(() => {
        initializeAuth();

        // Listen for storage events (e.g., when token is cleared in another tab)
        const handleStorageChange = (e: StorageEvent) => {
            if (e.key === 'token' || e.key === 'user') {
                console.log('Auth storage changed:', { key: e.key, newValue: e.newValue });
                initializeAuth();
            }
        };

        window.addEventListener('storage', handleStorageChange);
        return () => window.removeEventListener('storage', handleStorageChange);
    }, [initializeAuth]);

    const login = async (data: LoginRequest) => {
        try {
            setIsLoading(true);
            // Clear any existing auth data before login
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setToken(null);
            setUser(null);

            const response = await authAPI.login(data);
            console.log('Raw Login API Response:', {
                status: response.status,
                statusText: response.statusText,
                headers: response.headers,
                data: response.data
            });

            if (!response.data.success) {
                console.error('Login failed - API returned success: false:', response.data);
                throw new Error(response.data.message || 'Login failed');
            }

            if (!response.data.data) {
                console.error('Login failed - No data in response:', response.data);
                throw new Error('Invalid login response: missing data');
            }

            const loginData = response.data.data as unknown as LoginResponse;
            
            if (!loginData.token) {
                console.error('Login failed - No token in response:', loginData);
                throw new Error('Invalid login response: missing token');
            }

            if (!loginData.userId || !loginData.userType) {
                console.error('Login failed - Missing user info in response:', loginData);
                throw new Error('Invalid login response: missing user information');
            }

            // Ensure token has Bearer prefix
            const authToken = loginData.token.startsWith('Bearer ') 
                ? loginData.token 
                : `Bearer ${loginData.token}`;

            // Construct a basic user object from the login response
            const userData = {
                id: loginData.userId,
                email: data.email,
                roles: [loginData.userType],
                ...(loginData.userType === 'admin' ? { fullName: '' } : { supplierName: '' })
            } as Admin | Supplier;

            console.log('Login successful - Storing auth data:', {
                hasToken: !!authToken,
                tokenLength: authToken.length,
                hasUser: !!userData,
                userData
            });
            
            // Store in localStorage first
            localStorage.setItem('token', authToken);
            localStorage.setItem('user', JSON.stringify(userData));
            
            // Then update state
            setToken(authToken);
            setUser(userData);

            // Verify the state was updated
            console.log('Auth state after login:', {
                hasToken: !!authToken,
                hasUser: !!userData,
                userType: userData.roles?.[0]
            });
        } catch (error) {
            console.error('Login failed:', error);
            // Clear any partial auth data
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            setToken(null);
            setUser(null);
            throw error;
        } finally {
            setIsLoading(false);
        }
    };

    const logout = useCallback(() => {
        console.log('Logging out - clearing auth data');
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        setToken(null);
        setUser(null);
    }, []);

    const registerAdmin = async (data: Partial<Admin> & { password: string }) => {
        try {
            const response = await authAPI.registerAdmin(data);
            if (response.data.success) {
                // Auto-login after registration
                await login({
                    email: data.email!,
                    password: data.password
                });
            } else {
                throw new Error(response.data.message || 'Registration failed');
            }
        } catch (error) {
            console.error('Admin registration failed:', error);
            throw error;
        }
    };

    const registerSupplier = async (data: Partial<Supplier> & { password: string }) => {
        try {
            const response = await authAPI.registerSupplier(data);
            if (response.data.success) {
                // Auto-login after registration
                await login({
                    email: data.email!,
                    password: data.password
                });
            } else {
                throw new Error(response.data.message || 'Registration failed');
            }
        } catch (error) {
            console.error('Supplier registration failed:', error);
            throw error;
        }
    };

    const value = {
        user,
        token,
        isAuthenticated: !!token && !!user,
        isLoading,
        login,
        logout,
        registerAdmin,
        registerSupplier,
    };

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (context === undefined) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}; 