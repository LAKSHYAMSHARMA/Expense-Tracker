import { useEffect, useMemo, useState } from 'react';
import { CategoryApi, TransactionApi } from '../services/api';
import { getErrorMessage } from '../utils/http';
import { formatCurrency, formatDate } from '../utils/format';

const EMPTY_FORM = {
  transactionName: '',
  transactionAmount: '',
  transactionType: 'EXPENSE',
  transactionDate: new Date().toISOString().slice(0, 10),
  categoryId: '',
};

const TransactionsPage = ({ userId }) => {
  const [transactions, setTransactions] = useState([]);
  const [categories, setCategories] = useState([]);
  const [years, setYears] = useState([new Date().getFullYear()]);
  const [yearFilter, setYearFilter] = useState(new Date().getFullYear());
  const [monthFilter, setMonthFilter] = useState('');
  const [form, setForm] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const loadTransactions = async () => {
    setLoading(true);
    setError('');

    try {
      const data = await TransactionApi.getTransactionsByUser(
        userId,
        Number(yearFilter),
        monthFilter ? Number(monthFilter) : undefined,
      );
      setTransactions(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to fetch transactions.'));
    } finally {
      setLoading(false);
    }
  };

  const loadYears = async () => {
    try {
      const data = await TransactionApi.getDistinctTransactionYears(userId);
      if (Array.isArray(data) && data.length > 0) {
        const sortedYears = [...data].sort((a, b) => b - a);
        setYears(sortedYears);
        if (!sortedYears.includes(Number(yearFilter))) {
          setYearFilter(sortedYears[0]);
        }
      }
    } catch {
      // Silent fallback to current year.
    }
  };

  const loadCategories = async () => {
    try {
      const data = await CategoryApi.getCategoriesByUser(userId);
      setCategories(Array.isArray(data) ? data : []);
    } catch {
      setCategories([]);
    }
  };

  useEffect(() => {
    loadYears();
    loadCategories();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId]);

  useEffect(() => {
    loadTransactions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [userId, yearFilter, monthFilter]);

  const monthlyTotal = useMemo(() => {
    return transactions.reduce((sum, tx) => sum + Number(tx.transactionAmount || 0), 0);
  }, [transactions]);

  const resetForm = () => {
    setForm(EMPTY_FORM);
    setEditingId(null);
  };

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleEdit = (tx) => {
    setEditingId(tx.id);
    setForm({
      transactionName: tx.transactionName || '',
      transactionAmount: tx.transactionAmount ?? '',
      transactionType: tx.transactionType || 'EXPENSE',
      transactionDate: tx.transactionDate ? String(tx.transactionDate).slice(0, 10) : '',
      categoryId: tx.categoryId || tx.transactionCategoryId || '',
    });
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setError('');

    try {
      const payload = {
        ...(editingId ? { id: editingId } : {}),
        userId,
        transactionName: form.transactionName.trim(),
        transactionAmount: Number(form.transactionAmount),
        transactionType: form.transactionType,
        transactionDate: form.transactionDate,
        ...(form.categoryId
          ? { categoryId: Number(form.categoryId) }
          : {}),
      };

      if (editingId) {
        await TransactionApi.updateTransaction(payload);
      } else {
        await TransactionApi.createTransaction(payload);
      }

      resetForm();
      await loadTransactions();
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to save transaction.'));
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (transactionId) => {
    setError('');

    try {
      await TransactionApi.deleteTransaction(transactionId);
      await loadTransactions();
    } catch (err) {
      setError(getErrorMessage(err, 'Unable to delete transaction.'));
    }
  };

  return (
    <section className="page-section">
      <div className="section-header">
        <h1>Transactions</h1>
        <p>Manage income and expense entries for user #{userId}</p>
      </div>

      <div className="panel">
        <h2>{editingId ? 'Update Transaction' : 'Add Transaction'}</h2>

        <form className="form-grid" onSubmit={handleSubmit}>
          <label>
            <span>Name</span>
            <input
              name="transactionName"
              value={form.transactionName}
              onChange={handleInputChange}
              required
              placeholder="e.g. Groceries"
            />
          </label>

          <label>
            <span>Amount</span>
            <input
              name="transactionAmount"
              value={form.transactionAmount}
              onChange={handleInputChange}
              type="number"
              min="0"
              step="0.01"
              required
              placeholder="0.00"
            />
          </label>

          <label>
            <span>Type</span>
            <select name="transactionType" value={form.transactionType} onChange={handleInputChange}>
              <option value="EXPENSE">Expense</option>
              <option value="INCOME">Income</option>
            </select>
          </label>

          <label>
            <span>Date</span>
            <input name="transactionDate" value={form.transactionDate} onChange={handleInputChange} type="date" required />
          </label>

          <label>
            <span>Category</span>
            <select name="categoryId" value={form.categoryId} onChange={handleInputChange}>
              <option value="">No category</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.categoryName}
                </option>
              ))}
            </select>
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
        <div className="panel-header-row">
          <h2>Transaction List</h2>
          <div className="inline-filters">
            <label>
              <span>Year</span>
              <select value={yearFilter} onChange={(e) => setYearFilter(Number(e.target.value))}>
                {years.map((year) => (
                  <option key={year} value={year}>
                    {year}
                  </option>
                ))}
              </select>
            </label>

            <label>
              <span>Month</span>
              <select value={monthFilter} onChange={(e) => setMonthFilter(e.target.value)}>
                <option value="">All</option>
                {Array.from({ length: 12 }, (_, index) => index + 1).map((month) => (
                  <option key={month} value={month}>
                    {month}
                  </option>
                ))}
              </select>
            </label>
          </div>
        </div>

        {error && <p className="error-text">{error}</p>}
        {loading && <p className="status-text">Loading transactions...</p>}

        {!loading && transactions.length === 0 && <p className="status-text">No transactions available for this filter.</p>}

        {!loading && transactions.length > 0 && (
          <>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Date</th>
                    <th>Category</th>
                    <th>Type</th>
                    <th className="align-right">Amount</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((tx) => (
                    <tr key={tx.id}>
                      <td>{tx.transactionName}</td>
                      <td>{formatDate(tx.transactionDate)}</td>
                      <td>{tx.categoryName || '-'}</td>
                      <td>
                        <span className={`badge ${tx.transactionType === 'INCOME' ? 'badge-income' : 'badge-expense'}`}>
                          {tx.transactionType}
                        </span>
                      </td>
                      <td className="align-right">{formatCurrency(tx.transactionAmount)}</td>
                      <td className="cell-actions">
                        <button type="button" className="btn-ghost" onClick={() => handleEdit(tx)}>
                          Edit
                        </button>
                        <button
                          type="button"
                          className="btn-danger"
                          onClick={() => handleDelete(tx.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <p className="summary-text">Filtered total: {formatCurrency(monthlyTotal)}</p>
          </>
        )}
      </div>
    </section>
  );
};

export default TransactionsPage;
