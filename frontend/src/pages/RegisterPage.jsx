import { useState, useEffect, useCallback } from "react";
import { vehicleService } from "../api/services";
import {
  Plus,
  Search,
  Pencil,
  Trash2,
  ChevronLeft,
  ChevronRight,
  Filter,
} from "lucide-react";
import toast from "react-hot-toast";
import VehicleModal from "../components/VehicleModal";

const STATUS_OPTIONS = [
  "AVAILABLE",
  "ON_TRIP",
  "IN_SHOP",
  "RETIRED",
];

const STATUS_BADGE = {
  AVAILABLE: "badge-green",
  ON_TRIP: "badge-blue",
  IN_SHOP: "badge-amber",
  RETIRED: "badge-red",
};

export default function VehiclesPage() {
  const [vehicles, setVehicles] = useState([]);
  const [loading, setLoading] = useState(true);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [search, setSearch] = useState("");
  const [filterStatus, setFilterStatus] = useState("");

  const [showModal, setShowModal] = useState(false);
  const [editVehicle, setEditVehicle] = useState(null);

  const load = useCallback(async () => {
    setLoading(true);

    try {
      let response;

      if (search.trim()) {
        response = await vehicleService.search(search, page);
      } else if (filterStatus) {
        response = await vehicleService.getByStatus(
          filterStatus,
          page
        );
      } else {
        response = await vehicleService.getAll(page);
      }

      const data = response.data.data;

      setVehicles(data.content || []);
      setTotalPages(data.totalPages || 0);

    } catch (err) {
      console.error(err);
      toast.error("Failed to load vehicles");
    } finally {
      setLoading(false);
    }
  }, [page, search, filterStatus]);

  useEffect(() => {
    load();
  }, [load]);

  const openCreate = () => {
    setEditVehicle(null);
    setShowModal(true);
  };

  const openEdit = (vehicle) => {
    setEditVehicle(vehicle);
    setShowModal(true);
  };

  const closeModal = () => {
    setEditVehicle(null);
    setShowModal(false);
  };

  const handleDelete = async (id) => {
    if (!window.confirm("Delete this vehicle?")) return;

    try {
      await vehicleService.delete(id);

      toast.success("Vehicle deleted successfully");

      load();

    } catch (err) {
      console.error(err);

      toast.error(
        err.response?.data?.message ||
          "Failed to delete vehicle"
      );
    }
  };

  const handleStatusChange = async (
    id,
    status
  ) => {
    try {
      await vehicleService.updateStatus(id, status);

      toast.success("Status updated");

      load();

    } catch (err) {
      console.error(err);

      toast.error("Failed to update status");
    }
  };

  return (
    <div className="page">
          <div className="page-header">
        <div>
          <h1 className="page-title">Vehicles</h1>
          <p className="page-desc">
            Manage your fleet vehicles
          </p>
        </div>

        <button
          id="add-vehicle-btn"
          className="btn btn-primary"
          onClick={openCreate}
        >
          <Plus size={18} />
          Add Vehicle
        </button>
      </div>

      {/* Toolbar */}
      <div className="toolbar">

        <div className="search-wrap">
          <Search
            size={16}
            className="search-icon"
          />

          <input
            id="vehicle-search"
            type="text"
            placeholder="Search by registration number or vehicle name..."
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
          />
        </div>

        <div className="filter-wrap">
          <Filter size={16} />

          <select
            value={filterStatus}
            onChange={(e) => {
              setFilterStatus(e.target.value);
              setPage(0);
            }}
          >
            <option value="">
              All Status
            </option>

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

      {/* Table */}

      <div className="table-card">

        <div className="table-wrap">

          <table className="data-table">

            <thead>

              <tr>
                <th>#</th>

                <th>Registration</th>

                <th>Vehicle Name</th>

                <th>Vehicle Type</th>

                <th>Year</th>

                <th>Load Capacity</th>

                <th>Odometer</th>

                <th>Status</th>

                <th>Actions</th>
              </tr>

            </thead>

            <tbody>

              {loading ? (

                <tr>
                  <td
                    colSpan={9}
                    className="loading-row"
                  >
                    <span className="spinner" />
                  </td>
                </tr>

              ) : vehicles.length === 0 ? (

                <tr>
                  <td
                    colSpan={9}
                    className="empty-row"
                  >
                    No vehicles found
                  </td>
                </tr>

              ) : (
                                vehicles.map((vehicle, index) => (

                  <tr key={vehicle.id}>

                    <td>
                      {page * 10 + index + 1}
                    </td>

                    <td>
                      <strong>
                        {vehicle.registrationNumber}
                      </strong>
                    </td>

                    <td>
                      {vehicle.vehicleName}
                    </td>

                    <td>
                      {vehicle.vehicleType}
                    </td>

                    <td>
                      {vehicle.manufactureYear}
                    </td>

                    <td>
                      {vehicle.maximumLoadCapacity?.toLocaleString()} kg
                    </td>

                    <td>
                      {vehicle.odometer?.toLocaleString()} km
                    </td>

                    <td>

                      <select
                        className={`status-select ${STATUS_BADGE[vehicle.status]}`}
                        value={vehicle.status}
                        onChange={(e) =>
                          handleStatusChange(
                            vehicle.id,
                            e.target.value
                          )
                        }
                      >
                        {STATUS_OPTIONS.map((status) => (
                          <option
                            key={status}
                            value={status}
                          >
                            {status}
                          </option>
                        ))}
                      </select>

                    </td>

                    <td>

                      <div className="action-btns">

                        <button
                          className="icon-btn edit"
                          onClick={() =>
                            openEdit(vehicle)
                          }
                          title="Edit Vehicle"
                        >
                          <Pencil size={16} />
                        </button>

                        <button
                          className="icon-btn del"
                          onClick={() =>
                            handleDelete(vehicle.id)
                          }
                          title="Delete Vehicle"
                        >
                          <Trash2 size={16} />
                        </button>

                      </div>

                    </td>

                  </tr>

                ))
              )}

            </tbody>

          </table>

        </div>

        {/* Pagination */}

        <div className="pagination">

          <button
            className="pg-btn"
            disabled={page === 0}
            onClick={() =>
              setPage((p) => p - 1)
            }
          >
            <ChevronLeft size={16} />
          </button>

          <span className="pg-info">
            Page {page + 1} of {Math.max(totalPages, 1)}
          </span>

          <button
            className="pg-btn"
            disabled={page >= totalPages - 1}
            onClick={() =>
              setPage((p) => p + 1)
            }
          >
            <ChevronRight size={16} />
          </button>

        </div>

      </div>

      {showModal && (
        <VehicleModal
          vehicle={editVehicle}
          onClose={closeModal}
          onSaved={load}
        />
      )}

    </div>
  );
}