import { useState } from 'react';
import { UserApi } from '../services/api';
import { getErrorMessage } from '../utils/http';

const EMPTY_USER = {
  name: '',
  email: '',
  password: '',
};

const UsersPage = ({ currentUserId, onUserSelect }) => {
  const [searchId, setSearchId] = useState(String(currentUserId));
  const [searchEmail, setSearchEmail] = useState('');
  const [createForm, setCreateForm] = useState(EMPTY_USER);
  const [resultUser, setResultUser] = useState(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleFetchById = async () => {
    if (!searchId) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = await UserApi.getUserById(Number(searchId));
      setResultUser(data);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to fetch user by id.'));
      setResultUser(null);
    } finally {
      setLoading(false);
    }
  };

  const handleFetchByEmail = async () => {
    if (!searchEmail.trim()) {
      return;
    }

    setLoading(true);
    setError('');

    try {
      const data = await UserApi.getUserByEmail(searchEmail.trim());
      setResultUser(data);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to fetch user by email.'));
      setResultUser(null);
    } finally {
      setLoading(false);
    }
  };

  const handleCreateChange = (event) => {
    const { name, value } = event.target;
    setCreateForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleCreateUser = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const payload = {
        name: createForm.name.trim(),
        email: createForm.email.trim(),
        password: createForm.password,
      };

      const createdUser = await UserApi.createUser(payload);
      setResultUser(createdUser);
      if (createdUser?.id) {
        onUserSelect(createdUser.id);
        setSearchId(String(createdUser.id));
      }
      setCreateForm(EMPTY_USER);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to create user.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="page-section">
      <div className="section-header">
        <h1>Users</h1>
        <p>Find users and create new user accounts</p>
      </div>

      <div className="panel">
        <h2>Find User</h2>
        <div className="search-grid">
          <label>
            <span>Search by ID</span>
            <div className="search-row">
              <input
                type="number"
                min="1"
                value={searchId}
                onChange={(e) => setSearchId(e.target.value)}
                placeholder="User id"
              />
              <button type="button" onClick={handleFetchById} disabled={loading}>
                Find
              </button>
            </div>
          </label>

          <label>
            <span>Search by Email</span>
            <div className="search-row">
              <input
                type="email"
                value={searchEmail}
                onChange={(e) => setSearchEmail(e.target.value)}
                placeholder="email@example.com"
              />
              <button type="button" onClick={handleFetchByEmail} disabled={loading}>
                Find
              </button>
            </div>
          </label>
        </div>
      </div>

      <div className="panel">
        <h2>Create User</h2>
        <form className="form-grid" onSubmit={handleCreateUser}>
          <label>
            <span>Name</span>
            <input name="name" value={createForm.name} onChange={handleCreateChange} required />
          </label>

          <label>
            <span>Email</span>
            <input name="email" type="email" value={createForm.email} onChange={handleCreateChange} required />
          </label>

          <label>
            <span>Password</span>
            <input
              name="password"
              type="password"
              value={createForm.password}
              onChange={handleCreateChange}
              minLength={8}
              required
            />
          </label>

          <div className="actions-row">
            <button type="submit" disabled={submitting}>
              {submitting ? 'Creating...' : 'Create User'}
            </button>
          </div>
        </form>
      </div>

      <div className="panel">
        <div className="panel-header-row">
          <h2>Current API User</h2>
          <button type="button" className="btn-ghost" onClick={() => onUserSelect(Number(searchId || currentUserId))}>
            Set As Active User
          </button>
        </div>

        {error && <p className="error-text">{error}</p>}
        {loading && <p className="status-text">Loading user...</p>}

        {!loading && !resultUser && <p className="status-text">No user loaded yet.</p>}

        {resultUser && (
          <dl className="details-grid">
            <div>
              <dt>ID</dt>
              <dd>{resultUser.id || '-'}</dd>
            </div>
            <div>
              <dt>Name</dt>
              <dd>{resultUser.name || '-'}</dd>
            </div>
            <div>
              <dt>Email</dt>
              <dd>{resultUser.email || '-'}</dd>
            </div>
          </dl>
        )}
      </div>
    </section>
  );
};

export default UsersPage;
