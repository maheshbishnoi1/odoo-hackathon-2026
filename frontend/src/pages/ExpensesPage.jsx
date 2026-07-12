import { useState, useEffect, useCallback } from 'react';
import { expenseService, vehicleService } from '../api/services';
import { Plus, Pencil, Trash2, Filter } from 'lucide-react';
import toast from 'react-hot-toast';
import ExpenseModal from '../components/ExpenseModal';

const EXPENSE_TYPES = ['TOLL', 'PARKING', 'DRIVER_ALLOWANCE', 'REPAIR', 'INSURANCE', 'PERMIT', 'OTHER'];
const PAYMENT_MODES = ['CASH', 'CARD', 'UPI', 'BANK_TRANSFER', 'CHEQUE'];
const TYPE_BADGE = {
  TOLL: 'badge-blue', PARKING: 'badge-violet', DRIVER_ALLOWANCE: 'badge-green',
  REPAIR: 'badge-amber', INSURANCE: 'badge-sky', PERMIT: 'badge-rose', OTHER: 'badge-gray',
};

function formatINR(val) {
  const n = parseFloat(val || 0);
  return `₹${n.toFixed(2)}`;
}

export default function ExpensesPage() {
  const [expenses, setExpenses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterType, setFilterType] = useState('');
  const [filterMode, setFilterMode] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editExp, setEditExp] = useState(null);
  const [vehicles, setVehicles] = useState([]);

  const load = useCallback(async () => {
    setLoading(true);
    try {
      const params = { page: 0, size: 20 };
      if (filterType) params.expenseType = filterType;
      if (filterMode) params.paymentMode = filterMode;
      const [eRes, vRes] = await Promise.all([
        expenseService.getAll(params),
        vehicleService.getAll(0, 100),
      ]);
      setExpenses(eRes.data.data?.content || []);
      setVehicles(vRes.data.data?.content || []);
    } catch {
      toast.error('Failed to load expenses');
    } finally {
      setLoading(false);
    }
  }, [filterType, filterMode]);

  useEffect(() => { load(); }, [load]);

  const handleDelete = async (id) => {
    if (!window.confirm('Delete this expense?')) return;
    try {
      await expenseService.delete(id);
      toast.success('Deleted');
      load();
    } catch { toast.error('Delete failed'); }
  };

  const openCreate = () => { setEditExp(null); setShowModal(true); };
  const openEdit = (e) => { setEditExp(e); setShowModal(true); };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Expenses</h1>
          <p className="page-desc">Track vehicle operational expenses</p>
        </div>
        <button id="add-expense-btn" className="btn btn-primary" onClick={openCreate}>
          <Plus size={18} /> Add Expense
        </button>
      </div>

      <div className="toolbar">
        <div className="filter-wrap">
          <Filter size={16} />
          <select id="expense-type-filter" value={filterType} onChange={e => setFilterType(e.target.value)}>
            <option value="">All Types</option>
            {EXPENSE_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>
        <div className="filter-wrap">
          <select id="payment-mode-filter" value={filterMode} onChange={e => setFilterMode(e.target.value)}>
            <option value="">All Payment Modes</option>
            {PAYMENT_MODES.map(m => <option key={m} value={m}>{m}</option>)}
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
                <th>Type</th>
                <th>Amount</th>
                <th>Payment Mode</th>
                <th>Description</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                <tr><td colSpan={8} className="loading-row"><span className="spinner" /></td></tr>
              ) : expenses.length === 0 ? (
                <tr><td colSpan={8} className="empty-row">No expenses found</td></tr>
              ) : expenses.map((e, i) => (
                <tr key={e.id}>
                  <td className="mono">{i + 1}</td>
                  <td>{e.vehicleRegistration || '—'}</td>
                  <td>{e.expenseDate}</td>
                  <td>
                    <span className={`badge ${TYPE_BADGE[e.expenseType] || 'badge-gray'}`}>
                      {e.expenseType}
                    </span>
                  </td>
                  <td><strong>{formatINR(e.amount)}</strong></td>
                  <td>{e.paymentMode}</td>
                  <td className="desc-cell">{e.description || '—'}</td>
                  <td>
                    <div className="action-btns">
                      <button className="icon-btn edit" onClick={() => openEdit(e)} title="Edit">
                        <Pencil size={16} />
                      </button>
                      <button className="icon-btn del" onClick={() => handleDelete(e.id)} title="Delete">
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
        <ExpenseModal expense={editExp} vehicles={vehicles} onClose={() => setShowModal(false)} onSaved={load} />
      )}
    </div>
  );
}
