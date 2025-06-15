import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ThemeProvider, createTheme } from '@mui/material';
import Layout from './components/Layout';
import Dashboard from './pages/Dashboard';
import Projects from './pages/Projects';
import Tasks from './pages/Tasks';
import Suppliers from './pages/Suppliers';
import Login from './pages/Login';
import RegisterAdmin from './pages/RegisterAdmin';
import RegisterSupplier from './pages/RegisterSupplier';
import { AuthProvider, useAuth } from './contexts/AuthContext';

// Create a theme instance
const theme = createTheme({
  palette: {
    primary: {
      main: '#1976d2'
    },
    secondary: {
      main: '#dc004e'
    }
  }
});

// Protected Route component
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { token, user, isLoading } = useAuth();
  
  console.log('ProtectedRoute detailed check:', { 
    hasToken: !!token, 
    tokenValue: token,
    hasUser: !!user,
    userValue: user,
    isLoading,
    path: window.location.pathname 
  });

  if (isLoading) {
    return null; // or a loading spinner
  }

  if (!token || !user) {
    console.log('No auth data found, redirecting to login. Details:', {
      tokenExists: !!token,
      userExists: !!user
    });
    return <Navigate to="/login" replace />;
  }

  return <Layout>{children}</Layout>;
};

const App: React.FC = () => {
  return (
    <ThemeProvider theme={theme}>
      <AuthProvider>
        <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
          <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register/admin" element={<RegisterAdmin />} />
            <Route path="/register/supplier" element={<RegisterSupplier />} />
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/projects"
              element={
                <ProtectedRoute>
                  <Projects />
                </ProtectedRoute>
              }
            />
            <Route
              path="/tasks"
              element={
                <ProtectedRoute>
                  <Tasks />
                </ProtectedRoute>
              }
            />
            <Route
              path="/suppliers"
              element={
                <ProtectedRoute>
                  <Suppliers />
                </ProtectedRoute>
              }
            />
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </Router>
      </AuthProvider>
    </ThemeProvider>
  );
};

export default App; 