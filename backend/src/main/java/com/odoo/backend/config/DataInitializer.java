package com.odoo.backend.config;

import com.odoo.backend.entity.*;
import com.odoo.backend.enums.*;
import com.odoo.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Automatically seeds the database with initial users, vehicles, drivers,
 * trips, fuel logs, maintenance records, and expenses if the database is empty.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final DriverRepository driverRepository;
    private final TripRepository tripRepository;
    private final FuelRepository fuelRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final ExpenseRepository expenseRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Database already contains data. Skipping seeding.");
            return;
        }

        log.info("Seeding database with sample TransitOps data...");

        // 1. Seed Users
        User admin = User.builder()
                .fullName("Mahesh Bishnoi")
                .email("admin@transitops.com")
                .password(passwordEncoder.encode("admin123"))
                .phoneNumber("9876543210")
                .role(Role.ADMIN)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        User dispatcher = User.builder()
                .fullName("Nitesh Kumar")
                .email("dispatcher@transitops.com")
                .password(passwordEncoder.encode("dispatcher123"))
                .phoneNumber("9876543211")
                .role(Role.DISPATCHER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(admin);
        userRepository.save(dispatcher);
        log.info("Seeded Admin and Dispatcher users.");

        // 2. Seed Vehicles
        List<Vehicle> vehicles = new ArrayList<>();
        
        vehicles.add(Vehicle.builder()
                .registrationNumber("MH-12-PQ-1234")
                .vehicleName("Tata Prima Cargo")
                .vehicleType(VehicleType.TRUCK)
                .maximumLoadCapacity(15000.0)
                .odometer(45000.0)
                .acquisitionCost(3500000.0)
                .manufactureYear(2021)
                .fuelType("DIESEL")
                .color("White")
                .make("Tata Motors")
                .model("Prima 2825.K")
                .year(2021)
                .status(VehicleStatus.AVAILABLE)
                .registrationExpiry(LocalDate.now().plusYears(2))
                .insuranceExpiry(LocalDate.now().plusMonths(6))
                .build());

        vehicles.add(Vehicle.builder()
                .registrationNumber("DL-01-AB-7890")
                .vehicleName("Mahindra Bolero Pickup")
                .vehicleType(VehicleType.PICKUP)
                .maximumLoadCapacity(1500.0)
                .odometer(12500.0)
                .acquisitionCost(950000.0)
                .manufactureYear(2022)
                .fuelType("DIESEL")
                .color("Silver")
                .make("Mahindra")
                .model("Bolero Maxx")
                .year(2022)
                .status(VehicleStatus.AVAILABLE)
                .registrationExpiry(LocalDate.now().plusYears(4))
                .insuranceExpiry(LocalDate.now().plusMonths(11))
                .build());

        vehicles.add(Vehicle.builder()
                .registrationNumber("KA-03-MN-4567")
                .vehicleName("Eicher Pro Delivery Van")
                .vehicleType(VehicleType.VAN)
                .maximumLoadCapacity(5000.0)
                .odometer(28400.0)
                .acquisitionCost(1800000.0)
                .manufactureYear(2020)
                .fuelType("CNG")
                .color("Blue")
                .make("Eicher")
                .model("Pro 2049")
                .year(2020)
                .status(VehicleStatus.ON_TRIP) // Currently on trip
                .registrationExpiry(LocalDate.now().plusYears(1))
                .insuranceExpiry(LocalDate.now().plusMonths(2))
                .build());

        vehicles.add(Vehicle.builder()
                .registrationNumber("HR-26-XY-5678")
                .vehicleName("Ashok Leyland Dost")
                .vehicleType(VehicleType.VAN)
                .maximumLoadCapacity(2000.0)
                .odometer(68000.0)
                .acquisitionCost(800000.0)
                .manufactureYear(2019)
                .fuelType("DIESEL")
                .color("White")
                .make("Ashok Leyland")
                .model("Dost Strong")
                .year(2019)
                .status(VehicleStatus.IN_SHOP) // Under maintenance
                .registrationExpiry(LocalDate.now().plusMonths(3))
                .insuranceExpiry(LocalDate.now().plusDays(20))
                .build());

        vehicles.add(Vehicle.builder()
                .registrationNumber("GJ-01-ZZ-9999")
                .vehicleName("BharatBenz Multi-axle Trailer")
                .vehicleType(VehicleType.TRAILER)
                .maximumLoadCapacity(40000.0)
                .odometer(95000.0)
                .acquisitionCost(5500000.0)
                .manufactureYear(2018)
                .fuelType("DIESEL")
                .color("Orange")
                .make("BharatBenz")
                .model("5528TT")
                .year(2018)
                .status(VehicleStatus.AVAILABLE)
                .registrationExpiry(LocalDate.now().plusMonths(8))
                .insuranceExpiry(LocalDate.now().plusMonths(5))
                .build());

        for (int i = 0; i < vehicles.size(); i++) {
            vehicles.set(i, vehicleRepository.save(vehicles.get(i)));
        }
        log.info("Seeded 5 sample vehicles.");

        // 3. Seed Drivers
        List<Driver> drivers = new ArrayList<>();

        drivers.add(Driver.builder()
                .firstName("Rajesh")
                .lastName("Kumar")
                .licenseNumber("DL-1234567890123")
                .email("rajesh@transitops.com")
                .phone("9988776655")
                .licenseExpiryDate(LocalDate.now().plusYears(5))
                .dateOfBirth(LocalDate.now().minusYears(30))
                .joiningDate(LocalDate.now().minusYears(2))
                .address("123, MG Road, Mumbai")
                .emergencyContactName("Sunita Kumar")
                .emergencyContactPhone("9988776601")
                .status(DriverStatus.AVAILABLE)
                .build());

        drivers.add(Driver.builder()
                .firstName("Suresh")
                .lastName("Singh")
                .licenseNumber("DL-9876543210987")
                .email("suresh@transitops.com")
                .phone("9988776656")
                .licenseExpiryDate(LocalDate.now().plusYears(3))
                .dateOfBirth(LocalDate.now().minusYears(35))
                .joiningDate(LocalDate.now().minusYears(3))
                .address("456, Gole Market, Delhi")
                .emergencyContactName("Ramesh Singh")
                .emergencyContactPhone("9988776602")
                .status(DriverStatus.AVAILABLE)
                .build());

        drivers.add(Driver.builder()
                .firstName("Amit")
                .lastName("Sharma")
                .licenseNumber("DL-5555555555555")
                .email("amit@transitops.com")
                .phone("9988776657")
                .licenseExpiryDate(LocalDate.now().plusMonths(10))
                .dateOfBirth(LocalDate.now().minusYears(28))
                .joiningDate(LocalDate.now().minusMonths(6))
                .address("789, Indiranagar, Bengaluru")
                .emergencyContactName("Karan Sharma")
                .emergencyContactPhone("9988776603")
                .status(DriverStatus.ON_TRIP) // Currently on trip
                .build());

        drivers.add(Driver.builder()
                .firstName("Harpreet")
                .lastName("Singh")
                .licenseNumber("DL-4444444444444")
                .email("harpreet@transitops.com")
                .phone("9988776658")
                .licenseExpiryDate(LocalDate.now().minusMonths(2)) // Expired license
                .dateOfBirth(LocalDate.now().minusYears(42))
                .joiningDate(LocalDate.now().minusYears(5))
                .address("101, Sector 21, Chandigarh")
                .emergencyContactName("Baldev Singh")
                .emergencyContactPhone("9988776604")
                .status(DriverStatus.AVAILABLE)
                .build());

        for (int i = 0; i < drivers.size(); i++) {
            drivers.set(i, driverRepository.save(drivers.get(i)));
        }
        log.info("Seeded 4 sample drivers.");

        // 4. Seed Trips
        // Trip 1: Completed Trip (Tata Prima Cargo with Rajesh Kumar)
        Trip trip1 = Trip.builder()
                .tripNumber("TRIP-2026-0001")
                .vehicle(vehicles.get(0))
                .driver(drivers.get(0))
                .source("Mumbai")
                .destination("Pune")
                .startTime(LocalDateTime.now().minusDays(3))
                .endTime(LocalDateTime.now().minusDays(3).plusHours(4))
                .distance(150.0)
                .estimatedDuration(240)
                .actualDuration(230)
                .status(TripStatus.COMPLETED)
                .notes("Cargo delivered on time. Smooth drive.")
                .build();

        // Trip 2: Completed Trip (Mahindra Bolero with Suresh Singh)
        Trip trip2 = Trip.builder()
                .tripNumber("TRIP-2026-0002")
                .vehicle(vehicles.get(1))
                .driver(drivers.get(1))
                .source("Delhi")
                .destination("Gurugram")
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().minusDays(1).plusHours(2))
                .distance(45.0)
                .estimatedDuration(120)
                .actualDuration(135)
                .status(TripStatus.COMPLETED)
                .notes("Heavy traffic near toll plaza.")
                .build();

        // Trip 3: Active Trip (Eicher Pro Delivery Van with Amit Sharma)
        Trip trip3 = Trip.builder()
                .tripNumber("TRIP-2026-0003")
                .vehicle(vehicles.get(2))
                .driver(drivers.get(2))
                .source("Bengaluru")
                .destination("Mysuru")
                .startTime(LocalDateTime.now().minusHours(2))
                .distance(140.0)
                .estimatedDuration(180)
                .status(TripStatus.IN_PROGRESS)
                .notes("Carrying electronics supply.")
                .build();

        // Trip 4: Scheduled Trip (BharatBenz with Rajesh Kumar)
        Trip trip4 = Trip.builder()
                .tripNumber("TRIP-2026-0004")
                .vehicle(vehicles.get(4))
                .driver(drivers.get(0))
                .source("Ahmedabad")
                .destination("Surat")
                .startTime(LocalDateTime.now().plusDays(2))
                .distance(260.0)
                .estimatedDuration(300)
                .status(TripStatus.SCHEDULED)
                .notes("Steel coils shipment.")
                .build();

        trip1 = tripRepository.save(trip1);
        trip2 = tripRepository.save(trip2);
        trip3 = tripRepository.save(trip3);
        trip4 = tripRepository.save(trip4);
        log.info("Seeded 4 sample trips.");

        // 5. Seed Fuel Logs
        FuelLog fuel1 = FuelLog.builder()
                .vehicle(vehicles.get(0))
                .trip(trip1)
                .fuelDate(LocalDate.now().minusDays(3))
                .fuelType(FuelType.DIESEL)
                .quantity(new BigDecimal("80.00"))
                .cost(new BigDecimal("7200.00"))
                .vendor("HP Pump Worli")
                .odometerReading(44900L)
                .costPerLiter(new BigDecimal("90.0000"))
                .remarks("Filled full tank before Mumbai-Pune trip.")
                .build();

        FuelLog fuel2 = FuelLog.builder()
                .vehicle(vehicles.get(1))
                .trip(trip2)
                .fuelDate(LocalDate.now().minusDays(1))
                .fuelType(FuelType.DIESEL)
                .quantity(new BigDecimal("25.00"))
                .cost(new BigDecimal("2250.00"))
                .vendor("Indian Oil Delhi")
                .odometerReading(12480L)
                .costPerLiter(new BigDecimal("90.0000"))
                .remarks("Regular fill-up.")
                .build();

        fuelRepository.save(fuel1);
        fuelRepository.save(fuel2);
        log.info("Seeded 2 fuel logs.");

        // 6. Seed Maintenance Records
        MaintenanceRecord maint1 = MaintenanceRecord.builder()
                .vehicle(vehicles.get(3)) // Ashok Leyland Dost (IN_SHOP)
                .maintenanceType(MaintenanceType.ROUTINE)
                .description("Regular 60K km major servicing including engine oil change, air filter replacement, and brake pad inspection.")
                .estimatedCost(new BigDecimal("12000.00"))
                .startDate(LocalDate.now().minusDays(2))
                .status(MaintenanceStatus.IN_PROGRESS)
                .remarks("Awaiting spare parts delivery for front suspension struts.")
                .build();

        MaintenanceRecord maint2 = MaintenanceRecord.builder()
                .vehicle(vehicles.get(0))
                .maintenanceType(MaintenanceType.TIRE)
                .description("Replaced two front tires due to wear and tear.")
                .estimatedCost(new BigDecimal("25000.00"))
                .actualCost(new BigDecimal("24500.00"))
                .startDate(LocalDate.now().minusDays(10))
                .completionDate(LocalDate.now().minusDays(9))
                .status(MaintenanceStatus.COMPLETED)
                .remarks("Tires aligned and balanced.")
                .build();

        maintenanceRepository.save(maint1);
        maintenanceRepository.save(maint2);
        log.info("Seeded 2 maintenance records.");

        // 7. Seed Expenses
        Expense exp1 = Expense.builder()
                .vehicle(vehicles.get(0))
                .trip(trip1)
                .expenseType(ExpenseType.TOLL)
                .title("Mumbai-Pune Expressway Toll")
                .description("Toll payment at Khalapur Toll Plaza.")
                .amount(new BigDecimal("450.00"))
                .expenseDate(LocalDate.now().minusDays(3))
                .paymentMode(PaymentMode.UPI)
                .remarks("Fastag auto-deducted.")
                .build();

        Expense exp2 = Expense.builder()
                .vehicle(vehicles.get(1))
                .trip(trip2)
                .expenseType(ExpenseType.OTHER)
                .title("State Entry Tax")
                .description("Entry tax for commercial vehicle into Haryana.")
                .amount(new BigDecimal("200.00"))
                .expenseDate(LocalDate.now().minusDays(1))
                .paymentMode(PaymentMode.CASH)
                .remarks("Paid at border checkpost.")
                .build();

        expenseRepository.save(exp1);
        expenseRepository.save(exp2);
        log.info("Seeded 2 general expenses.");
        log.info("TransitOps seed complete!");
    }
}
