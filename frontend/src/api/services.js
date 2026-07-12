import api from './axios';

// ─── AUTH ───────────────────────────────────────────────────────────────────
export const authService = {
  login: (email, password) => api.post('/api/auth/login', { email, password }),
  register: (data) => api.post('/api/auth/register', data),
  me: () => api.get('/api/auth/me'),
};

// ─── DASHBOARD ────────────────────────────────────────────────────────────
export const dashboardService = {
  getSummary: () => api.get('/api/v1/dashboard/summary'),
};

// ─── ANALYTICS ────────────────────────────────────────────────────────────
export const analyticsService = {
  getCostTrends: () => api.get('/api/v1/analytics/cost-trends'),
  getDriverPerformance: () => api.get('/api/v1/analytics/driver-performance'),
  getMaintenanceVariance: () => api.get('/api/v1/analytics/maintenance-variance'),
  getFleetHealth: () => api.get('/api/v1/analytics/fleet-health'),
};

// ─── VEHICLES ─────────────────────────────────────────────────────────────
export const vehicleService = {
  getAll: (page = 0, size = 10) => api.get(`/api/vehicles?page=${page}&size=${size}`),
  getById: (id) => api.get(`/api/vehicles/${id}`),
  create: (data) => api.post('/api/vehicles', data),
  update: (id, data) => api.put(`/api/vehicles/${id}`, data),
  delete: (id) => api.delete(`/api/vehicles/${id}`),
  updateStatus: (id, status) => api.patch(`/api/vehicles/${id}/status?status=${status}`),
  search: (keyword, page = 0) => api.get(`/api/vehicles/search?keyword=${keyword}&page=${page}`),
  getByStatus: (status, page = 0) => api.get(`/api/vehicles/status/${status}?page=${page}`),
};

// ─── DRIVERS ──────────────────────────────────────────────────────────────
export const driverService = {
  getAll: () => api.get('/api/drivers'),
  getById: (id) => api.get(`/api/drivers/${id}`),
  create: (data) => api.post('/api/drivers', data),
  update: (id, data) => api.put(`/api/drivers/${id}`, data),
  delete: (id) => api.delete(`/api/drivers/${id}`),
  search: (keyword) => api.get(`/api/drivers/search?keyword=${keyword}`),
  getByStatus: (status) => api.get(`/api/drivers/status/${status}`),
  getAvailable: () => api.get('/api/drivers/available'),
};

// ─── TRIPS ────────────────────────────────────────────────────────────────
export const tripService = {
  getAll: () => api.get('/api/trips'),
  getById: (id) => api.get(`/api/trips/${id}`),
  create: (data) => api.post('/api/trips', data),
  update: (id, data) => api.put(`/api/trips/${id}`, data),
  delete: (id) => api.delete(`/api/trips/${id}`),
  start: (id) => api.patch(`/api/trips/${id}/start`),
  complete: (id) => api.patch(`/api/trips/${id}/complete`),
  cancel: (id) => api.patch(`/api/trips/${id}/cancel`),
  getByStatus: (status) => api.get(`/api/trips/status/${status}`),
};

// ─── FUEL ─────────────────────────────────────────────────────────────────
export const fuelService = {
  getAll: (params = {}) => api.get('/api/v1/fuel', { params }),
  getById: (id) => api.get(`/api/v1/fuel/${id}`),
  log: (data) => api.post('/api/v1/fuel', data),
  update: (id, data) => api.put(`/api/v1/fuel/${id}`, data),
  delete: (id) => api.delete(`/api/v1/fuel/${id}`),
  getByVehicle: (vehicleId) => api.get(`/api/v1/fuel/vehicle/${vehicleId}`),
  getSummary: (vehicleId) => api.get(`/api/v1/fuel/vehicle/${vehicleId}/summary`),
};

// ─── MAINTENANCE ──────────────────────────────────────────────────────────
export const maintenanceService = {
  getAll: (params = {}) => api.get('/api/v1/maintenance', { params }),
  getById: (id) => api.get(`/api/v1/maintenance/${id}`),
  create: (data) => api.post('/api/v1/maintenance', data),
  update: (id, data) => api.put(`/api/v1/maintenance/${id}`, data),
  delete: (id) => api.delete(`/api/v1/maintenance/${id}`),
  updateStatus: (id, status) => api.patch(`/api/v1/maintenance/${id}/status?status=${status}`),
  getByVehicle: (vehicleId) => api.get(`/api/v1/maintenance/vehicle/${vehicleId}`),
};

// ─── EXPENSES ─────────────────────────────────────────────────────────────
export const expenseService = {
  getAll: (params = {}) => api.get('/api/v1/expenses', { params }),
  getById: (id) => api.get(`/api/v1/expenses/${id}`),
  create: (data) => api.post('/api/v1/expenses', data),
  update: (id, data) => api.put(`/api/v1/expenses/${id}`, data),
  delete: (id) => api.delete(`/api/v1/expenses/${id}`),
  getByVehicle: (vehicleId) => api.get(`/api/v1/expenses/vehicle/${vehicleId}`),
  getSummary: (vehicleId) => api.get(`/api/v1/expenses/vehicle/${vehicleId}/summary`),
};
