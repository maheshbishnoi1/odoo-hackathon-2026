import { useState, useEffect, useCallback } from "react";
import {
  tripService,
  vehicleService,
  driverService,
} from "../api/services";
import {
  Plus,
  Pencil,
  Trash2,
  Play,
  CheckCircle,
  XCircle,
  Filter,
} from "lucide-react";
import toast from "react-hot-toast";
import TripModal from "../components/TripModal";

const STATUS_OPTIONS = [
  "SCHEDULED",
  "IN_PROGRESS",
  "COMPLETED",
  "CANCELLED",
];

const STATUS_BADGE = {
  SCHEDULED: "badge-blue",
  IN_PROGRESS: "badge-green",
  COMPLETED: "badge-gray",
  CANCELLED: "badge-red",
};

export default function TripsPage() {
  const [trips, setTrips] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filterStatus, setFilterStatus] = useState("");
  const [showModal, setShowModal] = useState(false);
  const [editTrip, setEditTrip] = useState(null);
  const [vehicles, setVehicles] = useState([]);
  const [drivers, setDrivers] = useState([]);

  const formatDuration = (minutes) => {
    if (minutes == null) return "—";

    const hrs = Math.floor(minutes / 60);
    const mins = minutes % 60;

    if (hrs === 0) return `${mins} min`;
    if (mins === 0) return `${hrs} hr`;

    return `${hrs} hr ${mins} min`;
  };

  const formatDateTime = (date) => {
    if (!date) return "—";

    return new Date(date).toLocaleString("en-IN", {
      dateStyle: "short",
      timeStyle: "short",
    });
  };

  const load = useCallback(async () => {
    setLoading(true);

    try {
      const [tripRes, vehicleRes, driverRes] = await Promise.all([
        filterStatus
          ? tripService.getByStatus(filterStatus)
          : tripService.getAll(),
        vehicleService.getAll(0, 100),
        driverService.getAll(),
      ]);

      setTrips(tripRes.data || []);
      setVehicles(vehicleRes.data.data?.content || []);
      setDrivers(driverRes.data || []);
    } catch (error) {
      toast.error("Failed to load trips");
    } finally {
      setLoading(false);
    }
  }, [filterStatus]);

  useEffect(() => {
    load();
  }, [load]);

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this trip?")) return;

    try {
      await tripService.delete(id);
      toast.success("Trip deleted successfully");
      load();
    } catch (error) {
      toast.error(
        error?.response?.data?.message || "Delete failed"
      );
    }
  };

  const handleStart = async (id) => {
    try {
      await tripService.start(id);
      toast.success("Trip started");
      load();
    } catch (error) {
      toast.error(
        error?.response?.data?.message || "Failed to start trip"
      );
    }
  };

  const handleComplete = async (id) => {
    try {
      await tripService.complete(id);
      toast.success("Trip completed");
      load();
    } catch (error) {
      toast.error(
        error?.response?.data?.message ||
          "Failed to complete trip"
      );
    }
  };

  const handleCancel = async (id) => {
    if (!window.confirm("Cancel this trip?")) return;

    try {
      await tripService.cancel(id);
      toast.success("Trip cancelled");
      load();
    } catch (error) {
      toast.error(
        error?.response?.data?.message ||
          "Failed to cancel trip"
      );
    }
  };

  const openCreate = () => {
    setEditTrip(null);
    setShowModal(true);
  };

  const openEdit = (trip) => {
    setEditTrip(trip);
    setShowModal(true);
  };

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <h1 className="page-title">Trips</h1>
          <p className="page-desc">
            Plan, track and manage fleet trips
          </p>
        </div>

        <button
          className="btn btn-primary"
          onClick={openCreate}
        >
          <Plus size={18} />
          Schedule Trip
        </button>
      </div>

      <div className="toolbar">
        <div className="filter-wrap">
          <Filter size={16} />

          <select
            value={filterStatus}
            onChange={(e) =>
              setFilterStatus(e.target.value)
            }
          >
            <option value="">All Statuses</option>

            {STATUS_OPTIONS.map((status) => (
              <option
                key={status}
                value={status}
              >
                {status}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="table-card">
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Trip #</th>
                <th>Vehicle</th>
                <th>Driver</th>
                <th>Route</th>
                <th>Start Time</th>
                <th>End Time</th>
                <th>Distance</th>
                <th>Estimated</th>
                <th>Actual</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>              {loading ? (
                <tr>
                  <td colSpan={11} className="loading-row">
                    <span className="spinner" />
                  </td>
                </tr>
              ) : trips.length === 0 ? (
                <tr>
                  <td colSpan={11} className="empty-row">
                    No trips found
                  </td>
                </tr>
              ) : (
                trips.map((trip) => (
                  <tr key={trip.id}>
                    <td>
                      <span className="mono">
                        {trip.tripNumber}
                      </span>
                    </td>

                    <td>
                      {trip.vehicleRegistrationNumber || "—"}
                    </td>

                    <td>
                      {trip.driverName || "—"}
                    </td>

                    <td className="route">
                      {trip.source} → {trip.destination}
                    </td>

                    <td>
                      {formatDateTime(trip.startTime)}
                    </td>

                    <td>
                      {formatDateTime(trip.endTime)}
                    </td>

                    <td>
                      {trip.distance ?? "—"} km
                    </td>

                    <td>
                      {formatDuration(
                        trip.estimatedDuration
                      )}
                    </td>

                    <td>
                      {formatDuration(
                        trip.actualDuration
                      )}
                    </td>

                    <td>
                      <span
                        className={`badge ${
                          STATUS_BADGE[trip.status] ||
                          "badge-gray"
                        }`}
                      >
                        {trip.status}
                      </span>
                    </td>

                    <td>
                      <div className="action-btns">

                        {trip.status === "SCHEDULED" && (
                          <button
                            className="icon-btn start"
                            title="Start Trip"
                            onClick={() =>
                              handleStart(trip.id)
                            }
                          >
                            <Play size={15} />
                          </button>
                        )}

                        {trip.status ===
                          "IN_PROGRESS" && (
                          <button
                            className="icon-btn complete"
                            title="Complete Trip"
                            onClick={() =>
                              handleComplete(trip.id)
                            }
                          >
                            <CheckCircle size={15} />
                          </button>
                        )}

                        {(trip.status ===
                          "SCHEDULED" ||
                          trip.status ===
                            "IN_PROGRESS") && (
                          <button
                            className="icon-btn cancel"
                            title="Cancel Trip"
                            onClick={() =>
                              handleCancel(trip.id)
                            }
                          >
                            <XCircle size={15} />
                          </button>
                        )}

                        <button
                          className="icon-btn edit"
                          title="Edit Trip"
                          onClick={() =>
                            openEdit(trip)
                          }
                        >
                          <Pencil size={15} />
                        </button>

                        <button
                          className="icon-btn del"
                          title="Delete Trip"
                          onClick={() =>
                            handleDelete(trip.id)
                          }
                        >
                          <Trash2 size={15} />
                        </button>

                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {showModal && (
        <TripModal
          trip={editTrip}
          vehicles={vehicles}
          drivers={drivers}
          onClose={() => setShowModal(false)}
          onSaved={load}
        />
      )}
    </div>
  );
}