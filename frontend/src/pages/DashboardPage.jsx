import { useState, useEffect } from 'react';
import { dashboardService, analyticsService } from '../api/services';
import {
  Truck, Users, Navigation, Wrench, DollarSign,
  TrendingUp, AlertTriangle, Activity, Fuel, CreditCard
} from 'lucide-react';
import {
  AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts';
import toast from 'react-hot-toast';

const STATUS_COLORS = {
  AVAILABLE: '#10b981',
  ON_TRIP: '#6366f1',
  IN_SHOP: '#f59e0b',
  RETIRED: '#ef4444',
};

const PIE_COLORS = ['#10b981', '#6366f1', '#f59e0b', '#ef4444'];

function KpiCard({ icon: Icon, label, value, sub, color, trend }) {
  return (
    <div className={`kpi-card kpi-${color}`}>
      <div className="kpi-icon-wrap">
        <Icon size={22} />
      </div>
      <div className="kpi-body">
        <p className="kpi-label">{label}</p>
        <h3 className="kpi-value">{value}</h3>
        {sub && <p className="kpi-sub">{sub}</p>}
        {trend !== undefined && (
          <p className={`kpi-trend ${trend >= 0 ? 'up' : 'down'}`}>
            {trend >= 0 ? '▲' : '▼'} {Math.abs(trend)}%
          </p>
        )}
      </div>
    </div>
  );
}

function formatINR(val) {
  if (!val && val !== 0) return '₹0';
  const num = parseFloat(val);
  if (num >= 10000000) return `₹${(num / 10000000).toFixed(1)}Cr`;
  if (num >= 100000) return `₹${(num / 100000).toFixed(1)}L`;
  if (num >= 1000) return `₹${(num / 1000).toFixed(1)}K`;
  return `₹${num.toFixed(0)}`;
}

const STATUS_BADGE = {
  SCHEDULED: 'badge-blue',
  IN_PROGRESS: 'badge-green',
  COMPLETED: 'badge-gray',
  CANCELLED: 'badge-red',
};

export default function DashboardPage() {
  const [summary, setSummary] = useState(null);
  const [costTrend, setCostTrend] = useState(null);
  const [fleetHealth, setFleetHealth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const load = async () => {
      try {
        const [sumRes, trendRes, healthRes] = await Promise.all([
          dashboardService.getSummary(),
          analyticsService.getCostTrends(),
          analyticsService.getFleetHealth(),
        ]);
        setSummary(sumRes.data.data);
        setCostTrend(trendRes.data.data);
        setFleetHealth(healthRes.data.data);
      } catch {
        toast.error('Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  if (loading) return (
    <div className="page-loader">
      <span className="spinner lg" />
      <p>Loading Dashboard…</p>
    </div>
  );

  const pieData = summary ? [
    { name: 'Available', value: Number(summary.availableVehicles) },
    { name: 'On Trip', value: Number(summary.vehiclesOnTrip) },
    { name: 'In Shop', value: Number(summary.vehiclesInMaintenance) },
    { name: 'Retired', value: Number(summary.retiredVehicles) },
  ].filter(d => d.value > 0) : [];

  const trendData = costTrend?.monthlyCosts?.map(m => ({
    month: m.monthLabel,
    Fuel: parseFloat(m.fuelCost || 0),
    Maintenance: parseFloat(m.maintenanceCost || 0),
    Expenses: parseFloat(m.expenseCost || 0),
  })) || [];

  const healthScore = fleetHealth?.healthScore ?? 0;
  const healthGrade = fleetHealth?.healthGrade ?? 'N/A';

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Dashboard</h1>
          <p className="page-desc">Executive fleet overview — live data</p>
        </div>
        <div className="health-badge">
          <Activity size={18} />
          <span>Fleet Health: </span>
          <strong className={`health-score ${healthScore >= 80 ? 'good' : healthScore >= 60 ? 'fair' : 'poor'}`}>
            {healthScore}/100 ({healthGrade})
          </strong>
        </div>
      </div>

      {/* KPI Row */}
      <div className="kpi-grid">
        <KpiCard icon={Truck} label="Total Vehicles" value={summary?.totalVehicles ?? 0}
          sub={`${summary?.availableVehicles ?? 0} available`} color="indigo" />
        <KpiCard icon={Users} label="Total Drivers" value={summary?.totalDrivers ?? 0}
          sub={`${summary?.availableDrivers ?? 0} available`} color="emerald" />
        <KpiCard icon={Navigation} label="Total Trips" value={summary?.totalTrips ?? 0}
          sub={`${summary?.activeTrips ?? 0} active`} color="violet" />
        <KpiCard icon={Wrench} label="Maintenance" value={summary?.openMaintenanceRequests ?? 0}
          sub={`${summary?.inProgressMaintenance ?? 0} in progress`} color="amber" />
        <KpiCard icon={Fuel} label="Fuel Cost" value={formatINR(summary?.totalFuelCost)} color="sky" />
        <KpiCard icon={CreditCard} label="Total Expenses" value={formatINR(summary?.totalExpenses)} color="rose" />
        <KpiCard icon={DollarSign} label="Maintenance Cost" value={formatINR(summary?.totalMaintenanceCost)} color="orange" />
        <KpiCard icon={TrendingUp} label="Total Operational" value={formatINR(summary?.totalOperationalCost)} color="teal" />
      </div>

      {/* Charts Row */}
      <div className="charts-row">
        {/* Cost Trend Chart */}
        <div className="chart-card wide">
          <h2 className="chart-title">Monthly Cost Trend</h2>
          {trendData.length > 0 ? (
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart data={trendData} margin={{ top: 10, right: 20, left: 10, bottom: 0 }}>
                <defs>
                  <linearGradient id="gradFuel" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="gradMaint" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#f59e0b" stopOpacity={0} />
                  </linearGradient>
                  <linearGradient id="gradExp" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.3} />
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e2a3a" />
                <XAxis dataKey="month" tick={{ fill: '#94a3b8', fontSize: 12 }} />
                <YAxis tickFormatter={(v) => formatINR(v)} tick={{ fill: '#94a3b8', fontSize: 11 }} />
                <Tooltip
                  contentStyle={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: 8 }}
                  labelStyle={{ color: '#e2e8f0' }}
                  formatter={(v) => formatINR(v)}
                />
                <Legend wrapperStyle={{ color: '#94a3b8' }} />
                <Area type="monotone" dataKey="Fuel" stroke="#6366f1" fill="url(#gradFuel)" strokeWidth={2} />
                <Area type="monotone" dataKey="Maintenance" stroke="#f59e0b" fill="url(#gradMaint)" strokeWidth={2} />
                <Area type="monotone" dataKey="Expenses" stroke="#10b981" fill="url(#gradExp)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-chart">No cost data available yet</div>
          )}
        </div>

        {/* Fleet Status Pie */}
        <div className="chart-card">
          <h2 className="chart-title">Fleet Status</h2>
          {pieData.length > 0 ? (
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={100}
                  paddingAngle={4} dataKey="value">
                  {pieData.map((_, i) => (
                    <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip
                  contentStyle={{ background: '#0f172a', border: '1px solid #1e293b', borderRadius: 8 }}
                  formatter={(v, n) => [v, n]}
                />
                <Legend wrapperStyle={{ color: '#94a3b8' }} />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <div className="empty-chart">No vehicle data</div>
          )}
          <div className="utilization">
            <span>Fleet Utilization</span>
            <div className="util-bar-wrap">
              <div className="util-bar" style={{ width: `${summary?.fleetUtilizationPercent ?? 0}%` }} />
            </div>
            <strong>{parseFloat(summary?.fleetUtilizationPercent ?? 0).toFixed(1)}%</strong>
          </div>
        </div>
      </div>

      {/* Bottom Row */}
      <div className="bottom-row">
        {/* Recent Trips */}
        <div className="table-card wide">
          <h2 className="chart-title">Recent Trips</h2>
          <div className="table-wrap">
            <table className="data-table">
              <thead>
                <tr>
                  <th>Trip #</th>
                  <th>Vehicle</th>
                  <th>Driver</th>
                  <th>Route</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {(summary?.recentTrips ?? []).length === 0 ? (
                  <tr><td colSpan={5} className="empty-row">No recent trips</td></tr>
                ) : (
                  summary.recentTrips.map((trip) => (
                    <tr key={trip.tripId}>
                      <td><span className="mono">{trip.tripNumber}</span></td>
                      <td>{trip.vehicleRegistration}</td>
                      <td>{trip.driverName}</td>
                      <td className="route">{trip.source} → {trip.destination}</td>
                      <td>
                        <span className={`badge ${STATUS_BADGE[trip.status] || 'badge-gray'}`}>
                          {trip.status}
                        </span>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </div>

        {/* Maintenance Alerts */}
        <div className="table-card">
          <h2 className="chart-title">
            <AlertTriangle size={18} className="title-icon amber" />
            Maintenance Alerts
          </h2>
          <div className="alert-list">
            {(summary?.maintenanceAlerts ?? []).length === 0 ? (
              <div className="empty-row">No active alerts 🎉</div>
            ) : (
              summary.maintenanceAlerts.map((alert) => (
                <div key={alert.maintenanceId} className="alert-item">
                  <div className="alert-dot" />
                  <div className="alert-body">
                    <strong>{alert.vehicleRegistration}</strong>
                    <p>{alert.serviceType}</p>
                    <span className="alert-meta">{alert.scheduledDate} · {formatINR(alert.estimatedCost)}</span>
                  </div>
                  <span className={`badge ${STATUS_BADGE[alert.status] || 'badge-amber'}`}>
                    {alert.status}
                  </span>
                </div>
              ))
            )}
          </div>
        </div>
      </div>

      {/* Fleet Health Alerts */}
      {fleetHealth?.alerts?.length > 0 && (
        <div className="chart-card mt-4">
          <h2 className="chart-title">
            <AlertTriangle size={18} className="title-icon red" />
            Smart Fleet Alerts
          </h2>
          <div className="health-alerts">
            {fleetHealth.alerts.map((a, i) => (
              <div key={i} className={`health-alert ${a.severity === 'CRITICAL' ? 'critical' : a.severity === 'WARNING' ? 'warning' : 'info'}`}>
                <AlertTriangle size={16} />
                <span>{a.message}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
