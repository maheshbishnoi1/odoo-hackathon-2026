import { useState, useEffect, useCallback } from 'react';
import { vehicleService } from '../api/services';
import { Plus, Search, Pencil, Trash2, ChevronLeft, ChevronRight, Filter } from 'lucide-react';
import toast from 'react-hot-toast';
import VehicleModal from '../components/VehicleModal';

const STATUS_OPTIONS = ['AVAILABLE', 'ON_TRIP', 'IN_SHOP', 'RETIRED'];
const STATUS_BADGE = {
  AVAILABLE: 'badge-green',
  ON_TRIP: 'badge-blue',
  IN_SHOP: 'badge-amber',
  RETIRED: 'badge-red',
};

export default function VehiclesPage() {
  const [vehicles, setVehicles] = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [filterStatus, setFilterStatus] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editVehicle, setEditVehicle] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      let res;
      if (search) {
        res = await vehicleService.search(search, page);
      } else if (filterStatus) {
        res = await vehicleService.getByStatus(filterStatus, page);
      } else {
        res = await vehicleService.getAll(page);
      }
      const data = res.data.data;
      setVehicles(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch {
      toast.error('Failed to load vehicles');
    } finally {
      setLoading(false);
    }
  }, [page, search, filterStatus]);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this vehicle?')) return;
    try {
      await vehicleService.delete(id);
      toast.success('Vehicle deleted');
      load();
    } catch (e) {
      toast.error(e?.response?.data?.message || 'Delete failed');
    }
  };

  const handleStatusChange = async (id, status) => {
    try {
      await vehicleService.updateStatus(id, status);
      toast.success('Status updated');
      load();
    } catch {
      toast.error('Status update failed');
    }
  };

  const openCreate = () => { setEditVehicle(null); setShowModal(true); };
  const openEdit = (v) => { setEditVehicle(v); setShowModal(true); };
  const closeModal = () => { setShowModal(false); setEditVehicle(null); };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Vehicles</h1>
          <p className="page-desc">Manage your fleet vehicles</p>
        </div>
        <button id="add-vehicle-btn" className="btn btn-primary" onClick={openCreate}>
          <Plus size={18} /> Add Vehicle
        </button>
      </div>

      {/* Toolbar */}
      <div className="toolbar">
        <div className="search-wrap">
          <Search size={16} className="search-icon" />
          <input
            id="vehicle-search"
            type="text"
            placeholder="Search by registration, make, model…"
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          />
        </div>
        <div className="filter-wrap">
          <Filter size={16} />
          <select
            id="vehicle-status-filter"
            value={filterStatus}
            onChange={(e) => { setFilterStatus(e.target.value); setPage(0); }}
          >
            <option value="">All Statuses</option>
            {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s}</option>)}
          </select>
        </div>
      </div>

      {/* Table */}
      <div className="table-card">
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>#</th>
                <th>Registration</th>
                <th>Make / Model</th>
                <th>Year</th>
                <th>Type</th>
                <th>Status</th>
                <th>Fuel Type</th>
                <th>Odometer (km)</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={9} className="loading-row"><span className="spinner" /></td></tr>
              ) : vehicles.length === 0 ? (
                <tr><td colSpan={9} className="empty-row">No vehicles found</td></tr>
              ) : vehicles.map((v, i) => (
                <tr key={v.id}>
                  <td className="mono">{page * 10 + i + 1}</td>
                  <td><strong>{v.registrationNumber}</strong></td>
                  <td>{v.make} {v.model}</td>
                  <td>{v.year}</td>
                  <td>{v.vehicleType || '—'}</td>
                  <td>
                    <select
                      className={`status-select ${STATUS_BADGE[v.status]}`}
                      value={v.status}
                      onChange={(e) => handleStatusChange(v.id, e.target.value)}
                    >
                      {STATUS_OPTIONS.map(s => <option key={s} value={s}>{s}</option>)}
                    </select>
                  </td>
                  <td>{v.fuelType}</td>
                  <td>{v.currentOdometerReading?.toLocaleString() ?? '—'}</td>
                  <td>
                    <div className="action-btns">
                      <button className="icon-btn edit" onClick={() => openEdit(v)} title="Edit">
                        <Pencil size={16} />
                      </button>
                      <button className="icon-btn del" onClick={() => handleDelete(v.id)} title="Delete">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="pagination">
          <button className="pg-btn" onClick={() => setPage(p => p - 1)} disabled={page === 0}>
            <ChevronLeft size={16} />
          </button>
          <span className="pg-info">Page {page + 1} of {Math.max(totalPages, 1)}</span>
          <button className="pg-btn" onClick={() => setPage(p => p + 1)} disabled={page >= totalPages - 1}>
            <ChevronRight size={16} />
          </button>
        </div>
      </div>

      {showModal && (
        <VehicleModal vehicle={editVehicle} onClose={closeModal} onSaved={load} />
      )}
    </div>
  );
}
