import React, { createContext, useContext, useState, useEffect } from 'react';
import { Admin, Supplier, LoginRequest } from '../types';
import { authAPI } from '../services/api';

interface AuthContextType {
    user: Admin | Supplier | null;
    token: string | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (data: LoginRequest) => Promise<void>;
    logout: () => void;
    registerAdmin: (data: Partial<Admin>) => Promise<void>;
    registerSupplier: (data: Partial<Supplier>) => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const [user, setUser] = useState<Admin | Supplier | null>(null);
    const [token, setToken] = useState<string | null>(localStorage.getItem('token'));
    const [isLoading, setIsLoading] = useState(true);

    useEffect(() => {
        // Check if there's a token in localStorage
        const storedToken = localStorage.getItem('token');
        if (storedToken) {
            setToken(storedToken);
            // You might want to validate the token here
        }
        setIsLoading(false);
    }, []);

    const login = async (data: LoginRequest) => {
        try {
            const response = await authAPI.login(data);
            if (response.success && response.data) {
                const { token, user } = response.data;
                localStorage.setItem('token', token);
                setToken(token);
                setUser(user);
            }
        } catch (error) {
            console.error('Login failed:', error);
            throw error;
        }
    };

    const logout = () => {
        localStorage.removeItem('token');
        setToken(null);
        setUser(null);
    };

    const registerAdmin = async (data: Partial<Admin>) => {
        try {
            const response = await authAPI.registerAdmin(data);
            if (response.success && response.data) {
                // You might want to automatically log in the user after registration
                // or redirect them to the login page
            }
        } catch (error) {
            console.error('Admin registration failed:', error);
            throw error;
        }
    };

    const registerSupplier = async (data: Partial<Supplier>) => {
        try {
            const response = await authAPI.registerSupplier(data);
            if (response.success && response.data) {
                // You might want to automatically log in the user after registration
                // or redirect them to the login page
            }
        } catch (error) {
            console.error('Supplier registration failed:', error);
            throw error;
        }
    };

    const value = {
        user,
        token,
        isAuthenticated: !!token,
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