import { useEffect, useState } from 'react';

const LoginPage = ({ onGoogleSignIn, loading, error }) => {
  const [scriptError, setScriptError] = useState('');
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';
  const currentOrigin = typeof window !== 'undefined' ? window.location.origin : '';
  useEffect(() => {
    const params = new URLSearchParams(window.location.hash.substring(1));
    const idToken = params.get('id_token');
    
    if (idToken) {
      onGoogleSignIn(idToken);
    }
  }, [onGoogleSignIn]);

  const handleContinueWithGoogle = () => {
    setScriptError('');

    if (!clientId || clientId.trim() === '') {
      setScriptError('Google client ID is not configured.');
      return;
    }

    try {
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

      window.location.href = authUrl.toString();
    } catch (e) {
      console.error('Error initiating OAuth flow:', e);
      setScriptError('Failed to initiate Google sign-in. Please try again.');
    }
  };

  return (
    <main className="login-screen">
      <section className="login-layout">
        <div className="login-intro">
          <p className="eyebrow login-eyebrow">Personal Finance Console</p>
          <h1>Your money,<br /><em>clearer.</em></h1>
          <p className="login-copy">
            A calm, focused place to understand what comes in, what goes out, and what you are building toward.
          </p>

          <div className="login-highlights">
            <div>
              <strong>See the pattern</strong>
              <span>Turn everyday transactions into a useful financial picture.</span>
            </div>
            <div>
              <strong>Stay in control</strong>
              <span>Keep income, needs, wants, and investments in one private workspace.</span>
            </div>
          </div>
        </div>

        <div className="login-card">
          <div className="login-card-mark" aria-hidden="true">
            <img src="/expense-tracker-logo.png" alt="" />
          </div>
          <div>
            <p className="login-card-kicker">Your workspace</p>
            <h2>Welcome!</h2>
            <p className="login-card-copy">Sign in to pick up where your financial picture left off.</p>
          </div>

          {(scriptError || error) && <p className="error-text">{scriptError || error}</p>}

          <button
            type="button"
            className="google-manual-btn"
            onClick={handleContinueWithGoogle}
            disabled={loading}
          >
            <span className="google-button-icon" aria-hidden="true">G</span>
            {loading ? 'Signing you in...' : 'Continue with Google'}
          </button>

          {loading && <p className="status-text">Completing sign-in...</p>}
          <p className="login-privacy">Your data stays tied to your account and is never shared with other users.</p>
        </div>
      </section>
    </main>
  );
};

export default LoginPage;
