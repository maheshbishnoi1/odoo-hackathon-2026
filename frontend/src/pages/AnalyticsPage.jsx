import { useState, useEffect } from 'react';
import { analyticsService } from '../api/services';
import {
  BarChart, Bar, LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer, Cell
} from 'recharts';
import { TrendingUp, Award, Wrench, Activity } from 'lucide-react';
import toast from 'react-hot-toast';

function formatINR(val) {
  const n = parseFloat(val || 0);
  if (n >= 100000) return `₹${(n / 100000).toFixed(1)}L`;
  if (n >= 1000) return `₹${(n / 1000).toFixed(1)}K`;
  return `₹${n.toFixed(0)}`;
}

export default function AnalyticsPage() {
  const [costTrend, setCostTrend] = useState(null);
  const [driverPerf, setDriverPerf] = useState(null);
  const [maintVar, setMaintVar] = useState(null);
  const [fleetHealth, setFleetHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [ct, dp, mv, fh] = await Promise.all([
          analyticsService.getCostTrends(),
          analyticsService.getDriverPerformance(),
          analyticsService.getMaintenanceVariance(),
          analyticsService.getFleetHealth(),
        ]);
        setCostTrend(ct.data.data);
        setDriverPerf(dp.data.data);
        setMaintVar(mv.data.data);
        setFleetHealth(fh.data.data);
      } catch {
        toast.error('Failed to load analytics');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return (
    <div className="page-loader">
      <span className="spinner lg" />
      <p>Loading Analytics…</p>
    </div>
  );

  const costData = costTrend?.monthlyCosts?.map(m => ({
    month: m.monthLabel,
    Fuel: parseFloat(m.fuelCost || 0),
    Maintenance: parseFloat(m.maintenanceCost || 0),
    Expenses: parseFloat(m.expenseCost || 0),
  })) || [];

  const driverData = (driverPerf?.driverPerformances || [])
    .slice(0, 10)
    .map(d => ({
      name: d.driverName.split(' ')[0],
      Trips: Number(d.completedTrips),
      'Completion %': parseFloat(d.completionRate || 0),
      Cost: parseFloat(d.totalTripCost || 0),
    }));

  const maintData = (maintVar?.vehicleVariances || []).slice(0, 10).map(v => ({
    vehicle: v.vehicleRegistration,
    Estimated: parseFloat(v.totalEstimatedCost || 0),
    Actual: parseFloat(v.totalActualCost || 0),
    Variance: parseFloat(v.totalVariance || 0),
  }));

  const healthScore = fleetHealth?.healthScore ?? 0;
  const healthGrade = fleetHealth?.healthGrade ?? 'N/A';

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Analytics</h1>
          <p className="page-desc">Business intelligence & fleet performance metrics</p>
        </div>
      </div>

      {/* Fleet Health Score */}
      <div className="health-score-banner">
        <div className="health-score-left">
          <Activity size={32} className="health-icon" />
          <div>
            <h2>Fleet Health Score</h2>
            <p>Based on active alerts, maintenance, and driver status</p>
          </div>
        </div>
        <div className={`health-score-num ${healthScore >= 80 ? 'good' : healthScore >= 60 ? 'fair' : 'poor'}`}>
          <span className="score-big">{healthScore}</span>
          <span className="score-max">/100</span>
          <span className="score-grade">{healthGrade}</span>
        </div>
      </div>

      {/* Cost Trend */}
      <div className="chart-card mb-4">
        <h2 className="chart-title">
          <TrendingUp size={20} className="title-icon" /> Monthly Cost Breakdown
        </h2>
        <div className="kpi-summary">
          <div className="kpi-sum-item">
            <span>Total Fuel</span>
            <strong>{formatINR(costTrend?.totalFuelCost)}</strong>
          </div>
          <div className="kpi-sum-item">
            <span>Total Maintenance</span>
            <strong>{formatINR(costTrend?.totalMaintenanceCost)}</strong>
          </div>
          <div className="kpi-sum-item">
            <span>Total Expenses</span>
            <strong>{formatINR(costTrend?.totalExpenseCost)}</strong>
          </div>
          <div className="kpi-sum-item highlight">
            <span>Grand Total</span>
            <strong>{formatINR(costTrend?.grandTotal)}</strong>
          </div>
        </div>
        {costData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={costData} margin={{ top: 10, right: 20, left: 10, bottom: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#1e2a3a" />
              <XAxis dataKey="month" tick={{ fill: '#94a3b8', fontSize: 12 }} />
              <YAxis tickFormatter={formatINR} tick={{ fill: '#94a3b8', fontSize: 11 }} />
              <Tooltip
                contentStyle={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: 8 }}
                formatter={(v) => formatINR(v)}
              />
              <Legend wrapperStyle={{ color: '#94a3b8' }} />
              <Bar dataKey="Fuel" fill="#6366f1" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Maintenance" fill="#f59e0b" radius={[4, 4, 0, 0]} />
              <Bar dataKey="Expenses" fill="#10b981" radius={[4, 4, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        ) : <div className="empty-chart">No cost data available</div>}
      </div>

      <div className="charts-row">
        {/* Driver Performance */}
        <div className="chart-card wide">
          <h2 className="chart-title">
            <Award size={20} className="title-icon" /> Driver Performance
          </h2>
          <p className="chart-sub">
            Total trips: <strong>{driverPerf?.totalTrips ?? 0}</strong> |
            Total distance: <strong>{(driverPerf?.totalDistanceCovered ?? 0).toFixed(1)} km</strong>
          </p>
          {driverData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={driverData} layout="vertical" margin={{ top: 0, right: 20, left: 40, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2a3a" horizontal={false} />
                <XAxis type="number" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <YAxis dataKey="name" type="category" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <Tooltip
                  contentStyle={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: 8 }}
                />
                <Bar dataKey="Trips" fill="#6366f1" radius={[0, 4, 4, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <div className="empty-chart">No driver data</div>}

          {/* Driver table */}
          {driverData.length > 0 && (
            <div className="table-wrap mt-3">
              <table className="data-table compact">
                <thead>
                  <tr>
                    <th>Driver</th>
                    <th>Completed</th>
                    <th>Total Assigned</th>
                    <th>Completion %</th>
                    <th>Total Cost</th>
                  </tr>
                </thead>
                <tbody>
                  {(driverPerf?.driverPerformances || []).map(d => (
                    <tr key={d.driverId}>
                      <td><strong>{d.driverName}</strong></td>
                      <td>{d.completedTrips}</td>
                      <td>{d.totalTripsAssigned}</td>
                      <td>
                        <div className="progress-cell">
                          <div className="progress-bar" style={{ width: `${d.completionRate}%` }} />
                          <span>{parseFloat(d.completionRate || 0).toFixed(1)}%</span>
                        </div>
                      </td>
                      <td>{formatINR(d.totalTripCost)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>

        {/* Maintenance Variance */}
        <div className="chart-card">
          <h2 className="chart-title">
            <Wrench size={20} className="title-icon" /> Maintenance Variance
          </h2>
          <p className="chart-sub">Estimated vs Actual cost per vehicle</p>
          {maintData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={maintData} margin={{ top: 0, right: 10, left: 10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2a3a" />
                <XAxis dataKey="vehicle" tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <YAxis tickFormatter={formatINR} tick={{ fill: '#94a3b8', fontSize: 10 }} />
                <Tooltip
                  contentStyle={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: 8 }}
                  formatter={(v) => formatINR(v)}
                />
                <Legend wrapperStyle={{ color: '#94a3b8' }} />
                <Bar dataKey="Estimated" fill="#94a3b8" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Actual" fill="#f59e0b" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          ) : <div className="empty-chart">No maintenance variance data</div>}

          <div className="variance-summary">
            <div className="var-item">
              <span>Total Est.</span>
              <strong>{formatINR(maintVar?.totalEstimatedCost)}</strong>
            </div>
            <div className="var-item">
              <span>Total Actual</span>
              <strong>{formatINR(maintVar?.totalActualCost)}</strong>
            </div>
            <div className={`var-item ${parseFloat(maintVar?.totalVariance || 0) > 0 ? 'over' : 'under'}`}>
              <span>Variance</span>
              <strong>{formatINR(maintVar?.totalVariance)}</strong>
            </div>
          </div>
        </div>
      </div>

      {/* Fleet Alerts */}
      {fleetHealth?.alerts?.length > 0 && (
        <div className="chart-card mt-4">
          <h2 className="chart-title">Fleet Alerts ({fleetHealth.alerts.length})</h2>
          <div className="health-alerts">
            {fleetHealth.alerts.map((a, i) => (
              <div key={i} className={`health-alert ${a.severity?.toLowerCase()}`}>
                <span className="alert-severity">{a.severity}</span>
                <span>{a.message}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
