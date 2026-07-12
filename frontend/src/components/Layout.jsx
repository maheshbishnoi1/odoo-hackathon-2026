import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Truck, Users, Navigation, Wrench,
  Fuel, CreditCard, BarChart2, LogOut, ChevronLeft, ChevronRight, Menu
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useState } from 'react';
import toast from 'react-hot-toast';

const NAV_ITEMS = [
  { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
  { to: '/vehicles', icon: Truck, label: 'Vehicles' },
  { to: '/drivers', icon: Users, label: 'Drivers' },
  { to: '/trips', icon: Navigation, label: 'Trips' },
  { to: '/fuel', icon: Fuel, label: 'Fuel Logs' },
  { to: '/maintenance', icon: Wrench, label: 'Maintenance' },
  { to: '/expenses', icon: CreditCard, label: 'Expenses' },
  { to: '/analytics', icon: BarChart2, label: 'Analytics' },
];

export default function Layout({ children }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = () => {
    logout();
    toast.success('Logged out');
    navigate('/login');
  };

  return (
    <div className={`app-layout ${collapsed ? 'sidebar-collapsed' : ''}`}>
      {/* Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="sidebar-logo">
            <Truck size={22} />
            {!collapsed && <span className="sidebar-brand">TransitOps</span>}
          </div>
          <button
            className="collapse-btn"
            onClick={() => setCollapsed(!collapsed)}
            title={collapsed ? 'Expand' : 'Collapse'}
            id="sidebar-toggle"
          >
            {collapsed ? <ChevronRight size={18} /> : <ChevronLeft size={18} />}
          </button>
        </div>

        <nav className="sidebar-nav">
          {NAV_ITEMS.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
              title={label}
            >
              <Icon size={20} className="nav-icon" />
              {!collapsed && <span className="nav-label">{label}</span>}
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          {!collapsed && user && (
            <div className="user-info">
              <div className="user-avatar">{(user.name || user.email || 'U')[0].toUpperCase()}</div>
              <div className="user-details">
                <span className="user-name">{user.name || user.email}</span>
                <span className="user-role">{user.role || 'Admin'}</span>
              </div>
            </div>
          )}
          <button
            id="logout-btn"
            className="logout-btn"
            onClick={handleLogout}
            title="Logout"
          >
            <LogOut size={18} />
            {!collapsed && <span>Logout</span>}
          </button>
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        {children}
      </main>
    </div>
  );
}
