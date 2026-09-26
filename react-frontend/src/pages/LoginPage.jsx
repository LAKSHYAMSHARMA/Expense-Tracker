import { useEffect, useRef, useState } from 'react';

const LoginPage = ({ onGoogleSignIn, loading, error }) => {
  const [scriptError, setScriptError] = useState('');
  const googleButtonRef = useRef(null);
  const clientId = import.meta.env.VITE_GOOGLE_CLIENT_ID || '';

  useEffect(() => {
    if (!clientId.trim() || !googleButtonRef.current) {
      return undefined;
    }

    const initializeGoogleSignIn = () => {
      if (!window.google?.accounts?.id || !googleButtonRef.current) {
        setScriptError('Google sign-in could not be loaded. Please try again.');
        return;
      }

      window.google.accounts.id.initialize({
        client_id: clientId,
        callback: ({ credential }) => {
          if (credential) {
            onGoogleSignIn(credential);
          }
        },
        ux_mode: 'popup',
      });
      googleButtonRef.current.replaceChildren();
      window.google.accounts.id.renderButton(googleButtonRef.current, {
        theme: 'outline',
        size: 'large',
        text: 'continue_with',
        shape: 'rectangular',
        width: 320,
      });
    };

    const existingScript = document.querySelector('script[src="https://accounts.google.com/gsi/client"]');
    if (existingScript) {
      if (window.google?.accounts?.id) {
        initializeGoogleSignIn();
      } else {
        existingScript.addEventListener('load', initializeGoogleSignIn, { once: true });
      }
      return undefined;
    }

    const script = document.createElement('script');
    script.src = 'https://accounts.google.com/gsi/client';
    script.async = true;
    script.defer = true;
    script.onload = initializeGoogleSignIn;
    script.onerror = () => setScriptError('Google sign-in could not be loaded. Please try again.');
    document.head.appendChild(script);

    return () => {
      script.onload = null;
      script.onerror = null;
    };
  }, [clientId, onGoogleSignIn]);

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

          <div ref={googleButtonRef} className="google-sign-in-button" aria-busy={loading} />

          {loading && <p className="status-text">Completing sign-in...</p>}
          <p className="login-privacy">Your data stays tied to your account and is never shared with other users.</p>
        </div>
      </section>
    </main>
  );
};

export default LoginPage;
