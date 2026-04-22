import { useEffect, useMemo, useState } from 'react';
import { TransactionApi } from '../services/api';
import { getErrorMessage } from '../utils/http';
import { formatCurrency, formatDate } from '../utils/format';
import SpendingBreakdownChart from '../components/SpendingBreakdownChart';

const DashboardPage = ({ userId }) => {
  const [transactions, setTransactions] = useState([]);
  const [breakdown, setBreakdown] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const loadData = async () => {
      setLoading(true);
      setError('');

      try {
        const [transactionsData, breakdownData] = await Promise.all([
          TransactionApi.getRecentTransactions(userId, 0, 0, 8),
          TransactionApi.getSpendingBreakdown(userId),
        ]);
        setTransactions(Array.isArray(transactionsData) ? transactionsData : []);
        setBreakdown(breakdownData);
      } catch (err) {
        setError(getErrorMessage(err, 'Unable to load dashboard data.'));
      } finally {
        setLoading(false);
      }
    };

    loadData();
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
          <p>{formatCurrency(breakdown?.totalIncome || 0)}</p>
        </article>
        <article className="stat-card expense-card">
          <h3>Total Spent</h3>
          <p>{formatCurrency((breakdown?.needsAmount || 0) + (breakdown?.wantsAmount || 0) + (breakdown?.investmentAmount || 0))}</p>
        </article>
        <article className="stat-card balance-card">
          <h3>Remaining/Savings</h3>
          <p>{formatCurrency(breakdown?.savingsAmount || 0)}</p>
        </article>
      </div>

      <div className="panel">
        <h2>Spending Breakdown</h2>
        {loading ? (
          <p className="status-text">Loading breakdown...</p>
        ) : breakdown ? (
          <SpendingBreakdownChart data={breakdown} />
        ) : (
          <p className="status-text">No spending data available.</p>
        )}
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
