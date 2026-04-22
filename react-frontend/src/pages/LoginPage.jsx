import { useEffect, useState } from 'react';

const LoginPage = ({ onGoogleSignIn, loading, error }) => {
  const [scriptError, setScriptError] = useState('');
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';
  const currentOrigin = typeof window !== 'undefined' ? window.location.origin : '';

  console.log('LoginPage mounted. Client ID:', clientId ? clientId.substring(0, 20) + '...' : 'NOT SET');
  console.log('Current origin:', currentOrigin);

  // Check for OAuth callback in URL
  useEffect(() => {
    const params = new URLSearchParams(window.location.hash.substring(1));
    const idToken = params.get('id_token');
    
    if (idToken) {
      console.log('Found ID token in callback, signing in...');
      onGoogleSignIn(idToken);
    }
  }, [onGoogleSignIn]);

  const handleContinueWithGoogle = () => {
    setScriptError('');
    console.log('Button clicked - initiating Google OAuth flow');

    if (!clientId || clientId.trim() === '') {
      setScriptError('Google client ID is not configured.');
      return;
    }

    try {
      // Direct OAuth2 implicit flow redirect
      const redirectUri = `${currentOrigin}/`;
      const scope = 'openid profile email';
      const responseType = 'id_token';
      const nonce = Math.random().toString(36).substr(2, 9);

      const authUrl = new URL('https://accounts.google.com/o/oauth2/v2/auth');
      authUrl.searchParams.set('client_id', clientId);
      authUrl.searchParams.set('redirect_uri', redirectUri);
      authUrl.searchParams.set('response_type', responseType);
      authUrl.searchParams.set('scope', scope);
      authUrl.searchParams.set('nonce', nonce);

      console.log('Redirecting to Google OAuth:', authUrl.toString());
      window.location.href = authUrl.toString();
    } catch (e) {
      console.error('Error initiating OAuth flow:', e);
      setScriptError('Failed to initiate Google sign-in. Please try again.');
    }
  };

  return (
    <main className="login-screen">
      <section className="login-card">
        <p className="eyebrow login-eyebrow">Personal Finance Console</p>
        <h1>Welcome Back</h1>
        <p className="login-copy">Sign in to access your Expense Tracker workspace.</p>

        {(scriptError || error) && <p className="error-text">{scriptError || error}</p>}

        <button
          type="button"
          className="google-manual-btn"
          onClick={handleContinueWithGoogle}
          disabled={loading}
        >
          {loading ? 'Signing you in...' : 'Continue with Google'}
        </button>

        {loading && <p className="status-text">Completing sign-in...</p>}
      </section>
    </main>
  );
};

export default LoginPage;
