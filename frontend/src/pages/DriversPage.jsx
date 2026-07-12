import { useState, useEffect, useCallback } from "react";
import { driverService } from "../api/services";
import {
  Plus,
  Search,
  Pencil,
  Trash2,
  Filter,
  ChevronLeft,
  ChevronRight,
} from "lucide-react";
import toast from "react-hot-toast";
import DriverModal from "../components/DriverModal";

const STATUS_OPTIONS = [
  "AVAILABLE",
  "ON_TRIP",
  "ON_LEAVE",
  "INACTIVE",
];

const STATUS_BADGE = {
  AVAILABLE: "badge-green",
  ON_TRIP: "badge-blue",
  ON_LEAVE: "badge-amber",
  INACTIVE: "badge-red",
};

export default function DriversPage() {

  const [drivers, setDrivers] = useState([]);
  const [loading, setLoading] = useState(true);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [search, setSearch] = useState("");
  const [filterStatus, setFilterStatus] = useState("");

  const [showModal, setShowModal] = useState(false);
  const [editDriver, setEditDriver] = useState(null);

  const load = useCallback(async () => {

    setLoading(true);

    try {

      let response;

      if (search.trim()) {

        response = await driverService.search(search);

      } else if (filterStatus) {

        response = await driverService.getByStatus(filterStatus);

      } else {

        response = await driverService.getAll();

      }

      const data = response.data.data || response.data;

      if (Array.isArray(data)) {

        setDrivers(data);
        setTotalPages(1);

      } else {

        setDrivers(data.content || []);
        setTotalPages(data.totalPages || 1);

      }

    } catch (err) {

      console.error(err);

      toast.error("Failed to load drivers");

    } finally {

      setLoading(false);

    }

  }, [search, filterStatus, page]);

  useEffect(() => {

    load();

  }, [load]);

  const openCreate = () => {

    setEditDriver(null);
    setShowModal(true);

  };

  const openEdit = (driver) => {

    setEditDriver(driver);
    setShowModal(true);

  };

  const closeModal = () => {

    setEditDriver(null);
    setShowModal(false);

  };

  const handleDelete = async (id) => {

    if (!window.confirm("Delete this driver?")) return;

    try {

      await driverService.delete(id);

      toast.success("Driver deleted successfully");

      load();

    } catch (err) {

      console.error(err);

      toast.error(
        err.response?.data?.message ||
        "Failed to delete driver"
      );

    }

  };

  const isExpiringSoon = (date) => {

    if (!date) return false;

    const expiry = new Date(date);

    const diff =
      (expiry - new Date()) /
      (1000 * 60 * 60 * 24);

    return diff <= 30;

  };

  return (

    <div className="page">      {/* Page Header */}
      <div className="page-header">

        <div>
          <h1 className="page-title">
            Drivers
          </h1>

          <p className="page-desc">
            Manage your fleet drivers
          </p>
        </div>

        <button
          className="btn btn-primary"
          onClick={openCreate}
        >
          <Plus size={18} />
          Add Driver
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
            type="text"
            placeholder="Search by driver name, email, phone or license..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

        </div>

        <div className="filter-wrap">

          <Filter size={16} />

          <select
            value={filterStatus}
            onChange={(e) =>
              setFilterStatus(e.target.value)
            }
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

                <th>Driver Name</th>

                <th>Email</th>

                <th>Phone</th>

                <th>License No.</th>

                <th>License Expiry</th>

                <th>Status</th>

                <th>Actions</th>

              </tr>

            </thead>

            <tbody>

              {loading ? (

                <tr>

                  <td
                    colSpan={8}
                    className="loading-row"
                  >
                    <span className="spinner" />
                  </td>

                </tr>

              ) : drivers.length === 0 ? (

                <tr>

                  <td
                    colSpan={8}
                    className="empty-row"
                  >
                    No drivers found
                  </td>

                </tr>

              ) : (                 drivers.map((driver, index) => (

                  <tr key={driver.id}>

                    <td className="mono">
                      {page * 10 + index + 1}
                    </td>

                    <td>
                      <strong>
                        {driver.firstName} {driver.lastName}
                      </strong>
                    </td>

                    <td>
                      {driver.email}
                    </td>

                    <td>
                      {driver.phone}
                    </td>

                    <td className="mono">
                      {driver.licenseNumber}
                    </td>

                    <td>
                      <span
                        className={
                          isExpiringSoon(driver.licenseExpiryDate)
                            ? "text-amber"
                            : ""
                        }
                      >
                        {driver.licenseExpiryDate || "—"}
                      </span>
                    </td>

                    <td>
                      <span
                        className={`badge ${
                          STATUS_BADGE[driver.status] ||
                          "badge-gray"
                        }`}
                      >
                        {driver.status}
                      </span>
                    </td>

                    <td>

                      <div className="action-btns">

                        <button
                          className="icon-btn edit"
                          title="Edit Driver"
                          onClick={() =>
                            openEdit(driver)
                          }
                        >
                          <Pencil size={16} />
                        </button>

                        <button
                          className="icon-btn del"
                          title="Delete Driver"
                          onClick={() =>
                            handleDelete(driver.id)
                          }
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
        <DriverModal
          driver={editDriver}
          onClose={closeModal}
          onSaved={load}
        />
      )}

    </div>
  );
}