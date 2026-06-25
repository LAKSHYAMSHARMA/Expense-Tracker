import { useState } from 'react';
import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import DashboardPage from './pages/DashboardPage';
import TransactionsPage from './pages/TransactionsPage';
import CategoriesPage from './pages/CategoriesPage';
import LoginPage from './pages/LoginPage';
import { AuthProvider } from './contexts/AuthContext.jsx';
import useAuth from './contexts/useAuth';
import { AuthApi } from './services/api';
import './App.css';

function AppContent() {
  const { authToken, authUser, login, logout, isAuthenticated } = useAuth();
  const [loginError, setLoginError] = useState('');
  const [loginLoading, setLoginLoading] = useState(false);

  const handleGoogleSignIn = async (idToken) => {
    setLoginLoading(true);
    setLoginError('');

    try {
      const response = await AuthApi.googleSignIn(idToken);
      
      // Store user info via auth context
      const userData = {
        id: response.userId,
        name: response.name,
        email: response.email,
      };
      login(response.token, userData);
    } catch (error) {
      const message = error?.response?.data?.message || 'Google sign-in failed. Please try again.';
      setLoginError(message);
    } finally {
      setLoginLoading(false);
    }
  };

  if (!isAuthenticated || !authToken || !authUser) {
    return <LoginPage onGoogleSignIn={handleGoogleSignIn} loading={loginLoading} error={loginError} />;
  }

  return (
    <div className="app-container">
      <header className="topbar">
        <div>
          <p className="eyebrow">Personal Finance Console</p>
          <h2>Expense Tracker</h2>
        </div>

        <div className="user-session-panel">
          <p>Signed in as</p>
          <strong>{authUser.name || authUser.email}</strong>
          <small>{authUser.email}</small>
          <button type="button" className="btn-ghost" onClick={logout}>
            Sign out
          </button>
        </div>
      </header>

      <div className="app-body">
        <aside className="sidebar">
          <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Dashboard
          </NavLink>
          <NavLink to="/transactions" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Transactions
          </NavLink>
          <NavLink to="/categories" className={({ isActive }) => `nav-link ${isActive ? 'active' : ''}`}>
            Categories
          </NavLink>
        </aside>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route path="/transactions" element={<TransactionsPage />} />
            <Route path="/categories" element={<CategoriesPage />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

function App() {
  return (
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  );
}

export default App;