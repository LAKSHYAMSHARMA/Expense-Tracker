import { useState } from 'react';
import { NavLink, Navigate, Route, Routes } from 'react-router-dom';
import DashboardPage from './pages/DashboardPage';
import TransactionsPage from './pages/TransactionsPage';
import CategoriesPage from './pages/CategoriesPage';
import LoginPage from './pages/LoginPage';
import { AuthApi } from './services/api';
import './App.css';

const AUTH_STORAGE_KEY = 'expense-tracker.auth-user';

const parseStoredAuthUser = () => {
  const rawValue = localStorage.getItem(AUTH_STORAGE_KEY);
  if (!rawValue) {
    return null;
  }

  try {
    const parsed = JSON.parse(rawValue);
    if (!parsed?.id || !parsed?.email) {
      return null;
    }
    return parsed;
  } catch {
    return null;
  }
};

function App() {
  const [authUser, setAuthUser] = useState(() => parseStoredAuthUser());
  const [loginError, setLoginError] = useState('');
  const [loginLoading, setLoginLoading] = useState(false);

  const handleGoogleSignIn = async (idToken) => {
    setLoginLoading(true);
    setLoginError('');

    try {
      const user = await AuthApi.googleSignIn(idToken);
      const nextUser = {
        id: user.id,
        name: user.name,
        email: user.email,
      };

      localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(nextUser));
      setAuthUser(nextUser);
    } catch (error) {
      const message = error?.response?.data?.message || 'Google sign-in failed. Please try again.';
      setLoginError(message);
    } finally {
      setLoginLoading(false);
    }
  };

  const handleLogout = () => {
    localStorage.removeItem(AUTH_STORAGE_KEY);
    setAuthUser(null);
  };

  if (!authUser) {
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
          <button type="button" className="btn-ghost" onClick={handleLogout}>
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
            <Route path="/dashboard" element={<DashboardPage userId={authUser.id} />} />
            <Route path="/transactions" element={<TransactionsPage userId={authUser.id} />} />
            <Route path="/categories" element={<CategoriesPage userId={authUser.id} />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </main>
      </div>
    </div>
  );
}

export default App;