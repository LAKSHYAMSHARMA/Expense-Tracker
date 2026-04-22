import { useEffect, useState } from 'react';
import { CategoryApi } from '../services/api';
import { getErrorMessage } from '../utils/http';

const EMPTY_FORM = {
  categoryName: '',
};

const CategoriesPage = ({ userId }) => {
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const loadCategories = async () => {
    setLoading(true);
    setError('');

    try {
      const data = await CategoryApi.getCategoriesByUser(userId);
      setCategories(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to load categories.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadCategories();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleEdit = (category) => {
    setEditingId(category.id);
    setForm({
      categoryName: category.categoryName || '',
    });
  };

  const resetForm = () => {
    setForm(EMPTY_FORM);
    setEditingId(null);
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const payload = {
        userId,
        categoryName: form.categoryName.trim(),
      };

      if (editingId) {
        await CategoryApi.updateCategory(editingId, payload);
      } else {
        await CategoryApi.createCategory(payload);
      }

      resetForm();
      await loadCategories();
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to save category.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (id) => {
    setError('');

    try {
      await CategoryApi.deleteCategory(id);
      await loadCategories();
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to delete category.'));
    }
  };

  return (
    <section className="page-section">
      <div className="section-header">
        <h1>Categories</h1>
        <p>Create and manage transaction categories for user #{userId}</p>
      </div>

      <div className="panel">
        <h2>{editingId ? 'Update Category' : 'Add Category'}</h2>
        <form className="form-grid form-grid-compact" onSubmit={handleSubmit}>
          <label>
            <span>Category Name</span>
            <input
              name="categoryName"
              value={form.categoryName}
              onChange={handleInputChange}
              required
              placeholder="e.g. Rent"
            />
          </label>

          <div className="actions-row">
            <button type="submit" disabled={submitting}>
              {submitting ? 'Saving...' : editingId ? 'Update' : 'Create'}
            </button>
            {editingId && (
              <button type="button" className="btn-ghost" onClick={resetForm}>
                Cancel
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="panel">
        <h2>Category List</h2>

        {error && <p className="error-text">{error}</p>}
        {loading && <p className="status-text">Loading categories...</p>}

        {!loading && categories.length === 0 && <p className="status-text">No categories found.</p>}

        {!loading && categories.length > 0 && (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {categories.map((category) => (
                  <tr key={category.id}>
                    <td>{category.categoryName}</td>
                    <td className="cell-actions">
                      <button type="button" className="btn-ghost" onClick={() => handleEdit(category)}>
                        Edit
                      </button>
                      <button type="button" className="btn-danger" onClick={() => handleDelete(category.id)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  );
};

export default CategoriesPage;
