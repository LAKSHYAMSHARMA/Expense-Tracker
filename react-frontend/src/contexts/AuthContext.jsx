import { createContext, useContext, useState, useCallback } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [authToken, setAuthToken] = useState(() => localStorage.getItem('expense-tracker.auth-token'));
  const [authUser, setAuthUser] = useState(() => {
    const rawValue = localStorage.getItem('expense-tracker.auth-user');
    if (!rawValue) return null;
    try {
      const parsed = JSON.parse(rawValue);
      return parsed?.id && parsed?.email ? parsed : null;
    } catch {
      return null;
    }
  });
  const [isAuthenticated, setIsAuthenticated] = useState(!!authToken && !!authUser);

  const login = useCallback((token, user) => {
    localStorage.setItem('expense-tracker.auth-token', token);
    localStorage.setItem('expense-tracker.auth-user', JSON.stringify(user));
    setAuthToken(token);
    setAuthUser(user);
    setIsAuthenticated(true);
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('expense-tracker.auth-token');
    localStorage.removeItem('expense-tracker.auth-user');
    setAuthToken(null);
    setAuthUser(null);
    setIsAuthenticated(false);
  }, []);

  const value = {
    authToken,
    authUser,
    isAuthenticated,
    login,
    logout,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};
