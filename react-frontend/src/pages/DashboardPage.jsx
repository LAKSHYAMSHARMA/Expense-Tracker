import { useEffect, useMemo, useState } from 'react';
import { TransactionApi } from '../services/api';
import { getErrorMessage } from '../utils/http';
import { formatCurrency, formatDate } from '../utils/format';

const DashboardPage = ({ userId }) => {
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadRecentTransactions = async () => {
      setLoading(true);
      setError('');

      try {
        const data = await TransactionApi.getRecentTransactions(userId, 0, 0, 8);
        setTransactions(Array.isArray(data) ? data : []);
      } catch (err) {
        setError(getErrorMessage(err, 'Unable to load recent transactions.'));
      } finally {
        setLoading(false);
      }
    };

    loadRecentTransactions();
  }, [userId]);

  const totals = useMemo(() => {
    return transactions.reduce(
      (acc, tx) => {
        const amount = Number(tx.transactionAmount || 0);
        if (tx.transactionType === 'INCOME') {
          acc.income += amount;
        } else {
          acc.expense += amount;
        }
        return acc;
      },
      { income: 0, expense: 0 },
    );
  }, [transactions]);

  const balance = totals.income - totals.expense;

  return (
    <section className="page-section">
      <div className="section-header">
        <h1>Dashboard</h1>
        <p>Recent activity for user #{userId}</p>
      </div>

      <div className="stats-grid">
        <article className="stat-card income-card">
          <h3>Total Income</h3>
          <p>{formatCurrency(totals.income)}</p>
        </article>
        <article className="stat-card expense-card">
          <h3>Total Expense</h3>
          <p>{formatCurrency(totals.expense)}</p>
        </article>
        <article className="stat-card balance-card">
          <h3>Net Balance</h3>
          <p>{formatCurrency(balance)}</p>
        </article>
      </div>

      <div className="panel">
        <h2>Recent Transactions</h2>

        {loading && <p className="status-text">Loading transactions...</p>}
        {!loading && error && <p className="error-text">{error}</p>}

        {!loading && !error && transactions.length === 0 && (
          <p className="status-text">No recent transactions found.</p>
        )}

        {!loading && !error && transactions.length > 0 && (
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Date</th>
                  <th>Type</th>
                  <th className="align-right">Amount</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((tx) => (
                  <tr key={tx.id}>
                    <td>{tx.transactionName}</td>
                    <td>{formatDate(tx.transactionDate)}</td>
                    <td>
                      <span className={`badge ${tx.transactionType === 'INCOME' ? 'badge-income' : 'badge-expense'}`}>
                        {tx.transactionType || 'EXPENSE'}
                      </span>
                    </td>
                    <td className="align-right">{formatCurrency(tx.transactionAmount)}</td>
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

export default DashboardPage;
