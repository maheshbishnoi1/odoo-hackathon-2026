import { useState } from "react";
import { tripService } from "../api/services";
import toast from "react-hot-toast";
import { X } from "lucide-react";

const EMPTY = {
  vehicleId: "",
  driverId: "",
  source: "",
  destination: "",
  startTime: "",
  distance: "",
  estimatedDuration: "",
  notes: "",
};

export default function TripModal({
  trip,
  vehicles,
  drivers,
  onClose,
  onSaved,
}) {
  const [form, setForm] = useState(
    trip
      ? {
          vehicleId: trip.vehicleId ?? "",
          driverId: trip.driverId ?? "",
          source: trip.source ?? "",
          destination: trip.destination ?? "",
          startTime: trip.startTime
            ? new Date(trip.startTime)
                .toISOString()
                .slice(0, 16)
            : "",
          distance: trip.distance ?? "",
          estimatedDuration:
            trip.estimatedDuration ?? "",
          notes: trip.notes ?? "",
        }
      : { ...EMPTY }
  );

  const [loading, setLoading] = useState(false);

  const set = (key, value) => {
    setForm((prev) => ({
      ...prev,
      [key]: value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const payload = {
      vehicleId: Number(form.vehicleId),
      driverId: Number(form.driverId),
      source: form.source.trim(),
      destination: form.destination.trim(),
      startTime: form.startTime,
      distance: Number(form.distance),
      estimatedDuration: Number(
        form.estimatedDuration
      ),
      notes: form.notes.trim(),
    };

    console.log("Trip Payload:", payload);

    setLoading(true);

    try {
      if (trip) {
        await tripService.update(trip.id, payload);
        toast.success("Trip updated successfully");
      } else {
        await tripService.create(payload);
        toast.success("Trip created successfully");
      }

      onSaved();
      onClose();
    } catch (err) {
      console.error(err.response?.data);

      if (err.response?.data?.data) {
        Object.values(err.response.data.data).forEach(
          (msg) => toast.error(msg)
        );
      } else {
        toast.error(
          err.response?.data?.message ||
            "Failed to save trip"
        );
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="modal-overlay"
      onClick={onClose}
    >
      <div
        className="modal"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="modal-header">
          <h2>
            {trip
              ? "Edit Trip"
              : "Schedule Trip"}
          </h2>

          <button
            className="modal-close"
            onClick={onClose}
          >
            <X size={20} />
          </button>
        </div>

        <form
          className="modal-form"
          onSubmit={handleSubmit}
        >
          <div className="form-grid">{/* Vehicle */}
<div className="form-group">
  <label>Vehicle *</label>
  <select
    required
    value={form.vehicleId}
    onChange={(e) => set("vehicleId", e.target.value)}
  >
    <option value="">Select Vehicle</option>

    {vehicles.map((vehicle) => (
      <option
        key={vehicle.id}
        value={vehicle.id}
      >
        {vehicle.registrationNumber} -{" "}
        {vehicle.vehicleName}
      </option>
    ))}
  </select>
</div>

{/* Driver */}
<div className="form-group">
  <label>Driver *</label>
  <select
    required
    value={form.driverId}
    onChange={(e) => set("driverId", e.target.value)}
  >
    <option value="">Select Driver</option>

    {drivers.map((driver) => (
      <option
        key={driver.id}
        value={driver.id}
      >
        {driver.name}
      </option>
    ))}
  </select>
</div>

{/* Source */}
<div className="form-group">
  <label>Source *</label>
  <input
    required
    maxLength={100}
    placeholder="Jaipur"
    value={form.source}
    onChange={(e) =>
      set("source", e.target.value)
    }
  />
</div>

{/* Destination */}
<div className="form-group">
  <label>Destination *</label>
  <input
    required
    maxLength={100}
    placeholder="Delhi"
    value={form.destination}
    onChange={(e) =>
      set("destination", e.target.value)
    }
  />
</div>

{/* Start Time */}
<div className="form-group">
  <label>Start Time *</label>
  <input
    type="datetime-local"
    required
    value={form.startTime}
    onChange={(e) =>
      set("startTime", e.target.value)
    }
  />
</div>

{/* Distance */}
<div className="form-group">
  <label>Distance (km) *</label>
  <input
    type="number"
    required
    step="0.1"
    min="0"
    placeholder="250"
    value={form.distance}
    onChange={(e) =>
      set(
        "distance",
        e.target.value === ""
          ? ""
          : parseFloat(e.target.value)
      )
    }
  />
</div>

{/* Estimated Duration */}
<div className="form-group">
  <label>Estimated Duration (Minutes) *</label>
  <input
    type="number"
    required
    min="1"
    placeholder="300"
    value={form.estimatedDuration}
    onChange={(e) =>
      set(
        "estimatedDuration",
        e.target.value === ""
          ? ""
          : parseInt(e.target.value)
      )
    }
  />
</div>

{/* Notes */}
<div className="form-group full-width">
  <label>Notes</label>

  <textarea
    rows={4}
    maxLength={500}
    placeholder="Additional trip details..."
    value={form.notes}
    onChange={(e) =>
      set("notes", e.target.value)
    }
  />
</div>          </div>

          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-ghost"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>

            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner sm" />
                  Saving...
                </>
              ) : trip ? (
                "Update Trip"
              ) : (
                "Schedule Trip"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}