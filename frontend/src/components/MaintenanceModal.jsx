import { useState } from 'react';
import { maintenanceService } from '../api/services';
import toast from 'react-hot-toast';
import { X } from 'lucide-react';

const TYPES = ['ROUTINE', 'REPAIR', 'INSPECTION', 'EMERGENCY', 'UPGRADE'];
const STATUSES = ['OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED'];

const EMPTY = {
  vehicleId: '', serviceType: '', maintenanceType: 'ROUTINE',
  startDate: new Date().toISOString().slice(0, 10),
  endDate: '', estimatedCost: '', actualCost: '',
  serviceProvider: '', notes: '', status: 'OPEN',
};

export default function MaintenanceModal({ record, vehicles, onClose, onSaved }) {
  const [form, setForm] = useState(record ? {
    vehicleId: record.vehicleId || '',
    serviceType: record.serviceType || '',
    maintenanceType: record.maintenanceType || 'ROUTINE',
    startDate: record.startDate || '',
    endDate: record.endDate || '',
    estimatedCost: record.estimatedCost || '',
    actualCost: record.actualCost || '',
    serviceProvider: record.serviceProvider || '',
    notes: record.notes || '',
    status: record.status || 'OPEN',
  } : { ...EMPTY });
  const [loading, setLoading] = useState(false);
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const payload = {
      ...form,
      vehicleId: parseInt(form.vehicleId),
      estimatedCost: parseFloat(form.estimatedCost),
      actualCost: form.actualCost ? parseFloat(form.actualCost) : null,
    };
    try {
      if (record) {
        await maintenanceService.update(record.id, payload);
        toast.success('Record updated');
      } else {
        await maintenanceService.create(payload);
        toast.success('Maintenance record created');
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
          <h2>{record ? 'Edit Maintenance' : 'New Maintenance Record'}</h2>
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
              <label>Maintenance Type</label>
              <select value={form.maintenanceType} onChange={e => set('maintenanceType', e.target.value)}>
                {TYPES.map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div className="form-group span-2">
              <label>Service Type *</label>
              <input required value={form.serviceType}
                onChange={e => set('serviceType', e.target.value)} placeholder="e.g. Oil Change, Tire Replacement" />
            </div>
            <div className="form-group">
              <label>Start Date *</label>
              <input type="date" required value={form.startDate} onChange={e => set('startDate', e.target.value)} />
            </div>
            <div className="form-group">
              <label>End Date</label>
              <input type="date" value={form.endDate} onChange={e => set('endDate', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Estimated Cost (₹) *</label>
              <input type="number" required value={form.estimatedCost}
                onChange={e => set('estimatedCost', e.target.value)} step="0.01" min="0" />
            </div>
            <div className="form-group">
              <label>Actual Cost (₹)</label>
              <input type="number" value={form.actualCost}
                onChange={e => set('actualCost', e.target.value)} step="0.01" min="0" />
            </div>
            <div className="form-group">
              <label>Service Provider</label>
              <input value={form.serviceProvider}
                onChange={e => set('serviceProvider', e.target.value)} placeholder="Workshop name" />
            </div>
            <div className="form-group">
              <label>Status</label>
              <select value={form.status} onChange={e => set('status', e.target.value)}>
                {STATUSES.map(s => <option key={s}>{s}</option>)}
              </select>
            </div>
            <div className="form-group span-2">
              <label>Notes</label>
              <textarea value={form.notes} onChange={e => set('notes', e.target.value)} rows={2} placeholder="Additional notes…" />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <span className="spinner sm" /> : (record ? 'Update' : 'Create')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
