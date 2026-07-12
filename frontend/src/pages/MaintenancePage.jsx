import { useState, useEffect, useCallback } from 'react';
import { maintenanceService, vehicleService } from '../api/services';
import { Plus, Pencil, Trash2, Filter } from 'lucide-react';
import toast from 'react-hot-toast';
import MaintenanceModal from '../components/MaintenanceModal';

const STATUS_OPTIONS = ['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];
const TYPE_OPTIONS = ['ROUTINE', 'REPAIR', 'INSPECTION', 'EMERGENCY', 'UPGRADE'];
const STATUS_BADGE = {
  OPEN: 'badge-amber',
  IN_PROGRESS: 'badge-blue',
  COMPLETED: 'badge-green',
  CANCELLED: 'badge-red',
};

function formatINR(val) {
  const n = parseFloat(val || 0);
  return `₹${n.toFixed(2)}`;
}

export default function MaintenancePage() {
  const [records, setRecords] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState('');
  const [filterType, setFilterType] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editRec, setEditRec] = useState(null);
  const [vehicles, setVehicles] = useState([]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page: 0, size: 20 };
      if (filterStatus) params.status = filterStatus;
      if (filterType) params.maintenanceType = filterType;
      const [mRes, vRes] = await Promise.all([
        maintenanceService.getAll(params),
        vehicleService.getAll(0, 100),
      ]);
      setRecords(mRes.data.data?.content || []);
      setVehicles(vRes.data.data?.content || []);
    } catch {
      toast.error('Failed to load maintenance records');
    } finally {
      setLoading(false);
    }
  }, [filterStatus, filterType]);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this record?')) return;
    try {
      await maintenanceService.delete(id);
      toast.success('Deleted');
      load();
    } catch { toast.error('Delete failed'); }
  };

  const handleStatusChange = async (id, status) => {
    try {
      await maintenanceService.updateStatus(id, status);
      toast.success('Status updated');
      load();
    } catch { toast.error('Update failed'); }
  };

  const openCreate = () => { setEditRec(null); setShowModal(true); };
  const openEdit = (r) => { setEditRec(r); setShowModal(true); };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Maintenance</h1>
          <p className="page-desc">Track and manage vehicle maintenance records</p>
        </div>
        <button id="add-maintenance-btn" className="btn btn-primary" onClick={openCreate}>
          <Plus size={18} /> New Record
        </button>
      </div>

      <div className="toolbar">
        <div className="filter-wrap">
          <Filter size={16} />
          <select id="maint-status-filter" value={filterStatus} onChange={e => setFilterStatus(e.target.value)}>
            <option value="">All Statuses</option>
            {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
        <div className="filter-wrap">
          <select id="maint-type-filter" value={filterType} onChange={e => setFilterType(e.target.value)}>
            <option value="">All Types</option>
            {TYPE_OPTIONS.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>
      </div>

      <div className="table-card">
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Vehicle</th>
                <th>Service Type</th>
                <th>Type</th>
                <th>Start Date</th>
                <th>Est. Cost</th>
                <th>Actual Cost</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={9} className="loading-row"><span className="spinner" /></td></tr>
              ) : records.length === 0 ? (
                <tr><td colSpan={9} className="empty-row">No maintenance records found</td></tr>
              ) : records.map((r, i) => (
                <tr key={r.id}>
                  <td className="mono">{i + 1}</td>
                  <td>{r.vehicleRegistration || '—'}</td>
                  <td>{r.serviceType}</td>
                  <td><span className="badge badge-gray">{r.maintenanceType}</span></td>
                  <td>{r.startDate}</td>
                  <td>{formatINR(r.estimatedCost)}</td>
                  <td>{r.actualCost ? formatINR(r.actualCost) : <span className="text-muted">—</span>}</td>
                  <td>
                    <select
                      className={`status-select ${STATUS_BADGE[r.status]}`}
                      value={r.status}
                      onChange={e => handleStatusChange(r.id, e.target.value)}
                    >
                      {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </td>
                  <td>
                    <div className="action-btns">
                      <button className="icon-btn edit" onClick={() => openEdit(r)} title="Edit">
                        <Pencil size={16} />
                      </button>
                      <button className="icon-btn del" onClick={() => handleDelete(r.id)} title="Delete">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <MaintenanceModal record={editRec} vehicles={vehicles} onClose={() => setShowModal(false)} onSaved={load} />
      )}
    </div>
  );
}
