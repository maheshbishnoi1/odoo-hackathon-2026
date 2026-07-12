import { useState } from 'react';
import { expenseService } from '../api/services';
import toast from 'react-hot-toast';
import { X } from 'lucide-react';

const EXPENSE_TYPES = ['TOLL', 'PARKING', 'DRIVER_ALLOWANCE', 'REPAIR', 'INSURANCE', 'PERMIT', 'OTHER'];
const PAYMENT_MODES = ['CASH', 'CARD', 'UPI', 'BANK_TRANSFER', 'CHEQUE'];

const EMPTY = {
  vehicleId: '', expenseDate: new Date().toISOString().slice(0, 10),
  expenseType: 'TOLL', amount: '', paymentMode: 'CASH',
  description: '', receiptNumber: '', tripId: '',
};

export default function ExpenseModal({ expense, vehicles, onClose, onSaved }) {
  const [form, setForm] = useState(expense ? {
    vehicleId: expense.vehicleId || '',
    expenseDate: expense.expenseDate || new Date().toISOString().slice(0, 10),
    expenseType: expense.expenseType || 'TOLL',
    amount: expense.amount || '',
    paymentMode: expense.paymentMode || 'CASH',
    description: expense.description || '',
    receiptNumber: expense.receiptNumber || '',
    tripId: expense.tripId || '',
  } : { ...EMPTY });
  const [loading, setLoading] = useState(false);
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const payload = {
      ...form,
      vehicleId: parseInt(form.vehicleId),
      amount: parseFloat(form.amount),
      tripId: form.tripId ? parseInt(form.tripId) : null,
    };
    try {
      if (expense) {
        await expenseService.update(expense.id, payload);
        toast.success('Expense updated');
      } else {
        await expenseService.create(payload);
        toast.success('Expense recorded');
      }
      onSaved();
      onClose();
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Save failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal" onClick={e => e.stopPropagation()}>
        <div className="modal-header">
          <h2>{expense ? 'Edit Expense' : 'Add Expense'}</h2>
          <button className="modal-close" onClick={onClose}><X size={20} /></button>
        </div>
        <form className="modal-form" onSubmit={handleSubmit}>
          <div className="form-grid">
            <div className="form-group">
              <label>Vehicle *</label>
              <select required value={form.vehicleId} onChange={e => set('vehicleId', e.target.value)}>
                <option value="">Select vehicle…</option>
                {vehicles.map(v => (
                  <option key={v.id} value={v.id}>{v.registrationNumber} — {v.make} {v.model}</option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Expense Date *</label>
              <input type="date" required value={form.expenseDate} onChange={e => set('expenseDate', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Expense Type *</label>
              <select value={form.expenseType} onChange={e => set('expenseType', e.target.value)}>
                {EXPENSE_TYPES.map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Amount (₹) *</label>
              <input type="number" required value={form.amount}
                onChange={e => set('amount', e.target.value)} step="0.01" min="0.01" />
            </div>
            <div className="form-group">
              <label>Payment Mode</label>
              <select value={form.paymentMode} onChange={e => set('paymentMode', e.target.value)}>
                {PAYMENT_MODES.map(m => <option key={m}>{m}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Receipt Number</label>
              <input value={form.receiptNumber} onChange={e => set('receiptNumber', e.target.value)} placeholder="Receipt / Bill no." />
            </div>
            <div className="form-group">
              <label>Trip ID (optional)</label>
              <input type="number" value={form.tripId} onChange={e => set('tripId', e.target.value)} placeholder="Linked trip ID" />
            </div>
            <div className="form-group span-2">
              <label>Description</label>
              <textarea value={form.description} onChange={e => set('description', e.target.value)} rows={2} placeholder="Expense description…" />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <span className="spinner sm" /> : (expense ? 'Update' : 'Add Expense')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
