import { useState } from "react";
import { vehicleService } from "../api/services";
import toast from "react-hot-toast";
import { X } from "lucide-react";

const VEHICLE_TYPES = [
  "TRUCK",
  "VAN",
  "BUS",
  "CAR",
  "MOTORCYCLE",
  "PICKUP",
];

const STATUSES = [
  "AVAILABLE",
  "ON_TRIP",
  "IN_SHOP",
  "RETIRED",
];

const FUEL_TYPES = [
  "PETROL",
  "DIESEL",
  "CNG",
  "ELECTRIC",
  "HYBRID",
];

const EMPTY = {
  registrationNumber: "",
  vehicleName: "",
  vehicleType: "TRUCK",
  maximumLoadCapacity: 0,
  odometer: 0,
  acquisitionCost: 0,
  manufactureYear: new Date().getFullYear(),
  fuelType: "DIESEL",
  color: "",
  registrationExpiry: "",
  insuranceExpiry: "",
  status: "AVAILABLE",
};

export default function VehicleModal({
  vehicle,
  onClose,
  onSaved,
}) {
  const [form, setForm] = useState(
    vehicle
      ? {
          registrationNumber: vehicle.registrationNumber || "",
          vehicleName: vehicle.vehicleName || "",
          vehicleType: vehicle.vehicleType || "TRUCK",
          maximumLoadCapacity:
            vehicle.maximumLoadCapacity || 0,
          odometer: vehicle.odometer || 0,
          acquisitionCost:
            vehicle.acquisitionCost || 0,
          manufactureYear:
            vehicle.manufactureYear ||
            new Date().getFullYear(),
          fuelType: vehicle.fuelType || "DIESEL",
          color: vehicle.color || "",
          registrationExpiry:
            vehicle.registrationExpiry || "",
          insuranceExpiry:
            vehicle.insuranceExpiry || "",
          status: vehicle.status || "AVAILABLE",
        }
      : EMPTY
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

    setLoading(true);

    try {
      console.log("Vehicle Payload:", form);

      if (vehicle) {
        await vehicleService.update(vehicle.id, form);
        toast.success("Vehicle updated successfully");
      } else {
        await vehicleService.create(form);
        toast.success("Vehicle created successfully");
      }

      onSaved();
      onClose();
    } catch (err) {
      console.error(err.response?.data);

      toast.error(
        err.response?.data?.message ||
          "Failed to save vehicle"
      );
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
            {vehicle
              ? "Edit Vehicle"
              : "Add New Vehicle"}
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
          <div className="form-grid">
          {/* Registration Number */}
<div className="form-group">
  <label>Registration Number *</label>
  <input
    required
    value={form.registrationNumber}
    onChange={(e) => set("registrationNumber", e.target.value)}
    placeholder="RJ14AB1234"
  />
</div>

{/* Vehicle Name */}
<div className="form-group">
  <label>Vehicle Name *</label>
  <input
    required
    value={form.vehicleName}
    onChange={(e) => set("vehicleName", e.target.value)}
    placeholder="Tata Prima 4928"
  />
</div>

{/* Vehicle Type */}
<div className="form-group">
  <label>Vehicle Type</label>
  <select
    value={form.vehicleType}
    onChange={(e) => set("vehicleType", e.target.value)}
  >
    {VEHICLE_TYPES.map((type) => (
      <option key={type} value={type}>
        {type}
      </option>
    ))}
  </select>
</div>

{/* Fuel Type */}
<div className="form-group">
  <label>Fuel Type</label>
  <select
    value={form.fuelType}
    onChange={(e) => set("fuelType", e.target.value)}
  >
    {FUEL_TYPES.map((type) => (
      <option key={type} value={type}>
        {type}
      </option>
    ))}
  </select>
</div>

{/* Manufacture Year */}
<div className="form-group">
  <label>Manufacture Year *</label>
  <input
    type="number"
    min="1990"
    max="2100"
    required
    value={form.manufactureYear}
    onChange={(e) =>
      set("manufactureYear", parseInt(e.target.value))
    }
  />
</div>

{/* Maximum Load Capacity */}
<div className="form-group">
  <label>Maximum Load Capacity (kg) *</label>
  <input
    type="number"
    step="0.01"
    required
    value={form.maximumLoadCapacity}
    onChange={(e) =>
      set("maximumLoadCapacity", parseFloat(e.target.value))
    }
  />
</div>

{/* Odometer */}
<div className="form-group">
  <label>Odometer (km) *</label>
  <input
    type="number"
    step="0.01"
    required
    value={form.odometer}
    onChange={(e) =>
      set("odometer", parseFloat(e.target.value))
    }
  />
</div>

{/* Acquisition Cost */}
<div className="form-group">
  <label>Acquisition Cost</label>
  <input
    type="number"
    step="0.01"
    value={form.acquisitionCost}
    onChange={(e) =>
      set("acquisitionCost", parseFloat(e.target.value))
    }
  />
</div>

{/* Color */}
<div className="form-group">
  <label>Color</label>
  <input
    value={form.color}
    onChange={(e) => set("color", e.target.value)}
    placeholder="White"
  />
</div>

{/* Registration Expiry */}
<div className="form-group">
  <label>Registration Expiry</label>
  <input
    type="date"
    value={form.registrationExpiry}
    onChange={(e) =>
      set("registrationExpiry", e.target.value)
    }
  />
</div>

{/* Insurance Expiry */}
<div className="form-group">
  <label>Insurance Expiry</label>
  <input
    type="date"
    value={form.insuranceExpiry}
    onChange={(e) =>
      set("insuranceExpiry", e.target.value)
    }
  />
</div>

{/* Status */}
<div className="form-group">
  <label>Status</label>
  <select
    value={form.status}
    onChange={(e) => set("status", e.target.value)}
  >
    {STATUSES.map((status) => (
      <option key={status} value={status}>
        {status}
      </option>
    ))}
  </select>
</div>
          </div>

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
              ) : vehicle ? (
                "Update Vehicle"
              ) : (
                "Create Vehicle"
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}