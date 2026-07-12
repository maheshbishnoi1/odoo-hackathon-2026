import { useState } from "react";
import { driverService } from "../api/services";
import toast from "react-hot-toast";
import { X } from "lucide-react";

const STATUSES = [
  "AVAILABLE",
  "ON_TRIP",
  "ON_LEAVE",
  "INACTIVE",
];

const EMPTY = {
  firstName: "",
  lastName: "",
  email: "",
  phone: "",
  licenseNumber: "",
  licenseExpiryDate: "",
  dateOfBirth: "",
  joiningDate: "",
  status: "AVAILABLE",
  address: "",
  emergencyContactName: "",
  emergencyContactPhone: "",
};

export default function DriverModal({
  driver,
  onClose,
  onSaved,
}) {
  const [form, setForm] = useState(
    driver
      ? {
          firstName: driver.firstName || "",
          lastName: driver.lastName || "",
          email: driver.email || "",
          phone: driver.phone || "",
          licenseNumber: driver.licenseNumber || "",
          licenseExpiryDate:
            driver.licenseExpiryDate || "",
          dateOfBirth:
            driver.dateOfBirth || "",
          joiningDate:
            driver.joiningDate || "",
          status:
            driver.status || "AVAILABLE",
          address:
            driver.address || "",
          emergencyContactName:
            driver.emergencyContactName || "",
          emergencyContactPhone:
            driver.emergencyContactPhone || "",
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

    setLoading(true);

    try {
      console.log("Driver Payload:", form);

      if (driver) {
        await driverService.update(driver.id, form);

        toast.success(
          "Driver updated successfully"
        );
      } else {
        await driverService.create(form);

        toast.success(
          "Driver created successfully"
        );
      }

      onSaved();
      onClose();

    } catch (err) {
      console.error(err.response?.data);

      toast.error(
        err.response?.data?.message ||
          "Failed to save driver"
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
        onClick={(e) =>
          e.stopPropagation()
        }
      >
        <div className="modal-header">

          <h2>
            {driver
              ? "Edit Driver"
              : "Add New Driver"}
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

          <div className="form-grid">{/* First Name */}
<div className="form-group">
  <label>First Name *</label>
  <input
    type="text"
    required
    value={form.firstName}
    onChange={(e) => set("firstName", e.target.value)}
    placeholder="John"
  />
</div>

{/* Last Name */}
<div className="form-group">
  <label>Last Name *</label>
  <input
    type="text"
    required
    value={form.lastName}
    onChange={(e) => set("lastName", e.target.value)}
    placeholder="Doe"
  />
</div>

{/* Email */}
<div className="form-group">
  <label>Email *</label>
  <input
    type="email"
    required
    value={form.email}
    onChange={(e) => set("email", e.target.value)}
    placeholder="driver@example.com"
  />
</div>

{/* Phone */}
<div className="form-group">
  <label>Phone *</label>
  <input
    type="text"
    required
    maxLength={10}
    value={form.phone}
    onChange={(e) => set("phone", e.target.value)}
    placeholder="9876543210"
  />
</div>

{/* License Number */}
<div className="form-group">
  <label>License Number *</label>
  <input
    type="text"
    required
    value={form.licenseNumber}
    onChange={(e) => set("licenseNumber", e.target.value)}
    placeholder="RJ1420230001234"
  />
</div>

{/* License Expiry */}
<div className="form-group">
  <label>License Expiry Date *</label>
  <input
    type="date"
    required
    value={form.licenseExpiryDate}
    onChange={(e) =>
      set("licenseExpiryDate", e.target.value)
    }
  />
</div>

{/* Date of Birth */}
<div className="form-group">
  <label>Date of Birth *</label>
  <input
    type="date"
    required
    value={form.dateOfBirth}
    onChange={(e) =>
      set("dateOfBirth", e.target.value)
    }
  />
</div>

{/* Joining Date */}
<div className="form-group">
  <label>Joining Date *</label>
  <input
    type="date"
    required
    value={form.joiningDate}
    onChange={(e) =>
      set("joiningDate", e.target.value)
    }
  />
</div>

{/* Status */}
<div className="form-group">
  <label>Status</label>
  <select
    value={form.status}
    onChange={(e) =>
      set("status", e.target.value)
    }
  >
    {STATUSES.map((status) => (
      <option
        key={status}
        value={status}
      >
        {status}
      </option>
    ))}
  </select>
</div>

{/* Address */}
<div className="form-group span-2">
  <label>Address</label>
  <textarea
    rows={3}
    value={form.address}
    onChange={(e) =>
      set("address", e.target.value)
    }
    placeholder="Enter complete address"
  />
</div>

{/* Emergency Contact Name */}
<div className="form-group">
  <label>Emergency Contact Name *</label>
  <input
    type="text"
    required
    value={form.emergencyContactName}
    onChange={(e) =>
      set("emergencyContactName", e.target.value)
    }
    placeholder="Emergency Contact Name"
  />
</div>

{/* Emergency Contact Phone */}
<div className="form-group">
  <label>Emergency Contact Phone *</label>
  <input
    type="text"
    required
    maxLength={10}
    value={form.emergencyContactPhone}
    onChange={(e) =>
      set("emergencyContactPhone", e.target.value)
    }
    placeholder="9876543210"
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
                  <span className="spinner sm"></span>
                  Saving...
                </>
              ) : driver ? (
                "Update Driver"
              ) : (
                "Create Driver"
              )}
            </button>
          </div>

        </form>
      </div>
    </div>
  );
}
