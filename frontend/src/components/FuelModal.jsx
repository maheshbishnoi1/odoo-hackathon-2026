import { useState } from 'react';
import { fuelService } from '../api/services';
import toast from 'react-hot-toast';
import { X } from 'lucide-react';

const FUEL_TYPES = ['PETROL', 'DIESEL', 'CNG', 'ELECTRIC', 'HYBRID'];

const EMPTY = {
  vehicleId: '', fuelDate: new Date().toISOString().slice(0, 10),
  fuelType: 'DIESEL', quantityLiters: '', totalCost: '',
  odometerReading: '', fuelStation: '', tripId: '', notes: '',
};

export default function FuelModal({ log, vehicles, onClose, onSaved }) {
  const [form, setForm] = useState(log ? {
    vehicleId: log.vehicleId || '',
    fuelDate: log.fuelDate || new Date().toISOString().slice(0, 10),
    fuelType: log.fuelType || 'DIESEL',
    quantityLiters: log.quantityLiters || '',
    totalCost: log.totalCost || '',
    odometerReading: log.odometerReading || '',
    fuelStation: log.fuelStation || '',
    tripId: log.tripId || '',
    notes: log.notes || '',
  } : { ...EMPTY });
  const [loading, setLoading] = useState(false);
  const set = (k, v) => setForm(f => ({ ...f, [k]: v }));

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    const payload = {
      ...form,
      vehicleId: parseInt(form.vehicleId),
      quantityLiters: parseFloat(form.quantityLiters),
      totalCost: parseFloat(form.totalCost),
      odometerReading: form.odometerReading ? parseFloat(form.odometerReading) : null,
      tripId: form.tripId ? parseInt(form.tripId) : null,
    };
    try {
      if (log) {
        await fuelService.update(log.id, payload);
        toast.success('Fuel log updated');
      } else {
        await fuelService.log(payload);
        toast.success('Fuel log recorded');
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
          <h2>{log ? 'Edit Fuel Log' : 'Log Fuel Fill-Up'}</h2>
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
              <label>Fuel Date *</label>
              <input type="date" required value={form.fuelDate} onChange={e => set('fuelDate', e.target.value)} />
            </div>
            <div className="form-group">
              <label>Fuel Type *</label>
              <select value={form.fuelType} onChange={e => set('fuelType', e.target.value)}>
                {FUEL_TYPES.map(t => <option key={t}>{t}</option>)}
              </select>
            </div>
            <div className="form-group">
              <label>Quantity (Liters) *</label>
              <input type="number" required value={form.quantityLiters}
                onChange={e => set('quantityLiters', e.target.value)} step="0.01" min="0" />
            </div>
            <div className="form-group">
              <label>Total Cost (₹) *</label>
              <input type="number" required value={form.totalCost}
                onChange={e => set('totalCost', e.target.value)} step="0.01" min="0" />
            </div>
            <div className="form-group">
              <label>Odometer Reading (km)</label>
              <input type="number" value={form.odometerReading}
                onChange={e => set('odometerReading', e.target.value)} step="0.1" min="0" />
            </div>
            <div className="form-group">
              <label>Fuel Station</label>
              <input value={form.fuelStation} onChange={e => set('fuelStation', e.target.value)} placeholder="Station name" />
            </div>
            <div className="form-group">
              <label>Trip ID (optional)</label>
              <input type="number" value={form.tripId} onChange={e => set('tripId', e.target.value)} placeholder="Linked trip ID" />
            </div>
            <div className="form-group span-2">
              <label>Notes</label>
              <textarea value={form.notes} onChange={e => set('notes', e.target.value)} rows={2} placeholder="Additional notes…" />
            </div>
          </div>
          <div className="modal-footer">
            <button type="button" className="btn btn-ghost" onClick={onClose}>Cancel</button>
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? <span className="spinner sm" /> : (log ? 'Update' : 'Log Fuel')}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
