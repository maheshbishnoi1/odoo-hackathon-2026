import { useState, useEffect, useCallback } from 'react';
import { fuelService, vehicleService } from '../api/services';
import { Plus, Pencil, Trash2, Filter } from 'lucide-react';
import toast from 'react-hot-toast';
import FuelModal from '../components/FuelModal';

const FUEL_TYPES = ['PETROL', 'DIESEL', 'CNG', 'ELECTRIC', 'HYBRID'];

function formatINR(val) {
  const n = parseFloat(val || 0);
  return `₹${n.toFixed(2)}`;
}

export default function FuelPage() {
  const [logs, setLogs] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page] = useState(0);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editLog, setEditLog] = useState(null);
  const [vehicles, setVehicles] = useState([]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page, size: 20 };
      if (filterType) params.fuelType = filterType;
      const [fuelRes, vRes] = await Promise.all([
        fuelService.getAll(params),
        vehicleService.getAll(0, 100),
      ]);
      setLogs(fuelRes.data.data?.content || []);
      setTotalPages(fuelRes.data.data?.totalPages || 0);
      setVehicles(vRes.data.data?.content || []);
    } catch {
      toast.error('Failed to load fuel logs');
    } finally {
      setLoading(false);
    }
  }, [page, filterType]);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this fuel log?')) return;
    try {
      await fuelService.delete(id);
      toast.success('Deleted');
      load();
    } catch { toast.error('Delete failed'); }
  };

  const openCreate = () => { setEditLog(null); setShowModal(true); };
  const openEdit = (l) => { setEditLog(l); setShowModal(true); };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Fuel Logs</h1>
          <p className="page-desc">Track vehicle fuel consumption</p>
        </div>
        <button id="add-fuel-btn" className="btn btn-primary" onClick={openCreate}>
          <Plus size={18} /> Log Fuel
        </button>
      </div>

      <div className="toolbar">
        <div className="filter-wrap">
          <Filter size={16} />
          <select id="fuel-type-filter" value={filterType} onChange={e => setFilterType(e.target.value)}>
            <option value="">All Fuel Types</option>
            {FUEL_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
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
                <th>Date</th>
                <th>Fuel Type</th>
                <th>Quantity (L)</th>
                <th>Total Cost</th>
                <th>Cost/Liter</th>
                <th>Odometer</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={9} className="loading-row"><span className="spinner" /></td></tr>
              ) : logs.length === 0 ? (
                <tr><td colSpan={9} className="empty-row">No fuel logs found</td></tr>
              ) : logs.map((l, i) => (
                <tr key={l.id}>
                  <td className="mono">{i + 1}</td>
                  <td>{l.vehicleRegistration || '—'}</td>
                  <td>{l.fuelDate}</td>
                  <td>
                    <span className="badge badge-sky">{l.fuelType}</span>
                  </td>
                  <td>{l.quantityLiters}</td>
                  <td><strong>{formatINR(l.totalCost)}</strong></td>
                  <td>{formatINR(l.costPerLiter)}</td>
                  <td>{l.odometerReading?.toLocaleString() ?? '—'}</td>
                  <td>
                    <div className="action-btns">
                      <button className="icon-btn edit" onClick={() => openEdit(l)} title="Edit">
                        <Pencil size={16} />
                      </button>
                      <button className="icon-btn del" onClick={() => handleDelete(l.id)} title="Delete">
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
        <FuelModal log={editLog} vehicles={vehicles} onClose={() => setShowModal(false)} onSaved={load} />
      )}
    </div>
  );
}
