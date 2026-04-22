import './SpendingBreakdownChart.css';

const SpendingBreakdownChart = ({ data }) => {
  if (!data) {
    return <div>No data available</div>;
  }

  const {
    needsPercentage = 0,
    wantsPercentage = 0,
    investmentPercentage = 0,
    savingsPercentage = 0,
    needsAmount = 0,
    wantsAmount = 0,
    investmentAmount = 0,
    savingsAmount = 0,
    totalIncome = 0,
  } = data;

  const segments = [
    { label: 'Needs', percentage: needsPercentage, amount: needsAmount, color: '#FFD700', code: 'needs' },
    { label: 'Wants', percentage: wantsPercentage, amount: wantsAmount, color: '#FF6B6B', code: 'wants' },
    { label: 'Savings', percentage: investmentPercentage + savingsPercentage, amount: investmentAmount + savingsAmount, color: '#51CF66', code: 'savings' },
  ];

  // Calculate pie chart path
  let currentAngle = -90; // Start from top
  const centerX = 50;
  const centerY = 50;
  const radius = 40;

  const getPieSlice = (percentage) => {
    const sliceAngle = (percentage / 100) * 360;
    const startAngle = currentAngle * (Math.PI / 180);
    const endAngle = (currentAngle + sliceAngle) * (Math.PI / 180);

    const x1 = centerX + radius * Math.cos(startAngle);
    const y1 = centerY + radius * Math.sin(startAngle);
    const x2 = centerX + radius * Math.cos(endAngle);
    const y2 = centerY + radius * Math.sin(endAngle);

    const largeArc = sliceAngle > 180 ? 1 : 0;

    const path = `M ${centerX} ${centerY} L ${x1} ${y1} A ${radius} ${radius} 0 ${largeArc} 1 ${x2} ${y2} Z`;
    const nextAngle = currentAngle + sliceAngle;
    currentAngle = nextAngle;

    return { path, startAngle, endAngle };
  };

  return (
    <div className="spending-breakdown">
      <div className="breakdown-content">
        <div className="pie-chart-container">
          <svg viewBox="0 0 100 100" className="pie-chart">
            {segments.map((segment, index) => {
              if (segment.percentage === 0) return null;
              const { path } = getPieSlice(segment.percentage);
              return (
                <path
                  key={index}
                  d={path}
                  fill={segment.color}
                  stroke="white"
                  strokeWidth="1"
                />
              );
            })}
          </svg>
        </div>

        <div className="breakdown-legend">
          {segments.map((segment, index) => (
            segment.percentage > 0 && (
              <div key={index} className="legend-item">
                <div
                  className="legend-color"
                  style={{ backgroundColor: segment.color }}
                ></div>
                <div className="legend-text">
                  <div className="legend-label">{segment.label}</div>
                  <div className="legend-percentage">{segment.percentage.toFixed(1)}%</div>
                  <div className="legend-amount">({new Intl.NumberFormat('en-US', { style: 'currency', currency: 'INR' }).format(segment.amount)})</div>
                </div>
              </div>
            )
          ))}
        </div>
      </div>

      <div className="breakdown-stats">
        <div className="stat-row">
          <span>Total Income:</span>
          <span>{new Intl.NumberFormat('en-US', { style: 'currency', currency: 'INR' }).format(totalIncome)}</span>
        </div>
        <div className="stat-row">
          <span>Total Spent:</span>
          <span>{new Intl.NumberFormat('en-US', { style: 'currency', currency: 'INR' }).format(needsAmount + wantsAmount + investmentAmount)}</span>
        </div>
        <div className="stat-row remaining">
          <span>Remaining/Savings:</span>
          <span>{new Intl.NumberFormat('en-US', { style: 'currency', currency: 'INR' }).format(savingsAmount)}</span>
        </div>
      </div>
    </div>
  );
};

export default SpendingBreakdownChart;
