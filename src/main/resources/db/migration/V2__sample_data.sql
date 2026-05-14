-- Fleet Maintenance System - Sample Data (DML)
-- Version: 1.0.0

-- =====================================================
-- ROLES
-- =====================================================

INSERT INTO roles (name, description) VALUES
('ROLE_ADMIN', 'System Administrator - Full Access'),
('ROLE_MANAGER', 'Manager - Manage operations'),
('ROLE_CLIENT', 'Client User - View own data'),
('ROLE_MECHANIC', 'Mechanic - Update work orders'),
('ROLE_FINANCE', 'Finance - Manage invoicing and payments');

-- =====================================================
-- USERS
-- Password for all users: password123 (BCrypt encoded)
-- =====================================================

INSERT INTO users (username, email, password, full_name, phone, is_active) VALUES
('admin', 'admin@fms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6HG.pJI.72vOJSPRmEhPzNjVSLQni', 'System Administrator', '081234567890', true),
('manager', 'manager@fms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6HG.pJI.72vOJSPRmEhPzNjVSLQni', 'Operations Manager', '081234567891', true),
('finance', 'finance@fms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6HG.pJI.72vOJSPRmEhPzNjVSLQni', 'Finance Officer', '081234567892', true),
('client1', 'client1@rental.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6HG.pJI.72vOJSPRmEhPzNjVSLQni', 'Client Manager 1', '081234567893', true),
('mechanic1', 'mechanic1@fms.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J6HG.pJI.72vOJSPRmEhPzNjVSLQni', 'Mechanic Ahmad', '081234567894', true);

-- =====================================================
-- USER ROLES
-- =====================================================

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1), -- admin -> ROLE_ADMIN
(2, 2), -- manager -> ROLE_MANAGER
(3, 5), -- finance -> ROLE_FINANCE
(4, 3), -- client1 -> ROLE_CLIENT
(5, 4); -- mechanic1 -> ROLE_MECHANIC

-- =====================================================
-- CLIENTS
-- =====================================================

INSERT INTO clients (code, name, business_type, npwp, address, city, province, phone, email, contact_person, contact_phone, payment_term, is_active, created_by) VALUES
('CLI001', 'PT Takari Kokoh Sejahtera', 'Car Rental', '01.234.567.8-901.000', 'Jl. Gatot Subroto No. 123', 'Jakarta Selatan', 'DKI Jakarta', '021-5551234', 'info@takari.com', 'Budi Santoso', '081234567001', 30, true, 1),
('CLI002', 'PT Transport Solution Mandiri (TSM)', 'Car Rental', '01.234.567.8-902.000', 'Jl. Sudirman No. 456', 'Jakarta Pusat', 'DKI Jakarta', '021-5551235', 'info@tsm.com', 'Siti Aminah', '081234567002', 30, true, 1),
('CLI003', 'Orix Rental Corporation', 'Car Rental', '01.234.567.8-903.000', 'Jl. Thamrin No. 789', 'Jakarta Pusat', 'DKI Jakarta', '021-5551236', 'info@orix.com', 'John Doe', '081234567003', 45, true, 1),
('CLI004', 'Prima Rent', 'Car Rental', '01.234.567.8-904.000', 'Jl. Rasuna Said No. 101', 'Jakarta Selatan', 'DKI Jakarta', '021-5551237', 'info@primarent.com', 'Sarah Lee', '081234567004', 30, true, 1),
('CLI005', 'PT Charoen Pokphand Indonesia', 'Logistics', '01.234.567.8-905.000', 'Jl. Ancol No. 202', 'Jakarta Utara', 'DKI Jakarta', '021-5551238', 'info@cp.com', 'Michael Chen', '081234567005', 60, true, 1);

-- =====================================================
-- WORKSHOPS
-- =====================================================

INSERT INTO workshops (code, name, type, address, city, province, phone, email, contact_person, specialization, operating_hours, service_capacity, rating, is_active) VALUES
('WS001', 'Central Workshop', 'INTERNAL', 'Jl. Proklamasi No. 6', 'Garut', 'Jawa Barat', '081234560001', 'central@fms.local', 'Hendri', 'ENGINE, ELECTRICAL, AC, TRANSMISSION', 'Mon-Sat 08:00-17:00', 10, 4.80, true),
('WS002', 'Jakarta Branch Workshop', 'INTERNAL', 'Jl. Kebon Jeruk No. 45', 'Jakarta Barat', 'DKI Jakarta', '081234560002', 'jakarta@fms.local', 'Agus', 'ENGINE, ELECTRICAL, GENERAL', 'Mon-Sat 08:00-17:00', 8, 4.70, true),
('WS003', 'Auto Prima Service', 'PARTNER', 'Jl. MT Haryono No. 88', 'Bandung', 'Jawa Barat', '081234560003', 'info@autoprima.com', 'Dedi', 'ENGINE, TRANSMISSION', 'Mon-Sun 08:00-20:00', 15, 4.50, true),
('WS004', 'Bengkel Jaya Motor', 'PARTNER', 'Jl. Asia Afrika No. 123', 'Surabaya', 'Jawa Timur', '081234560004', 'info@jayamotor.com', 'Budi', 'ELECTRICAL, AC', 'Mon-Sat 08:00-18:00', 12, 4.60, true),
('WS005', 'Toyota Auto2000', 'CLIENT_RECOMMENDED', 'Jl. Sudirman No. 999', 'Jakarta Pusat', 'DKI Jakarta', '081234560005', 'info@auto2000.com', 'Customer Service', 'ENGINE, ELECTRICAL, AC, TRANSMISSION', 'Mon-Sun 08:00-21:00', 20, 4.90, true);

-- =====================================================
-- MECHANICS
-- =====================================================

INSERT INTO mechanics (code, full_name, email, phone, specialization, certification, experience_years, workshop_id, rating, is_active) VALUES
('MEC001', 'Ahmad Hidayat', 'ahmad@fms.local', '081234560101', 'ENGINE', 'Engine Specialist Level 3', 10, 1, 4.75, true),
('MEC002', 'Budi Santoso', 'budi@fms.local', '081234560102', 'ELECTRICAL', 'Automotive Electrician', 8, 1, 4.60, true),
('MEC003', 'Cecep Supriyadi', 'cecep@fms.local', '081234560103', 'AC', 'AC Technician Certified', 5, 1, 4.50, true),
('MEC004', 'Dedi Kurniawan', 'dedi@fms.local', '081234560104', 'TRANSMISSION', 'Transmission Expert', 12, 2, 4.80, true),
('MEC005', 'Eko Prasetyo', 'eko@fms.local', '081234560105', 'GENERAL', 'General Mechanic', 7, 2, 4.55, true);

-- =====================================================
-- VEHICLES
-- =====================================================

INSERT INTO vehicles (client_id, license_plate, brand, model, year, color, vin, fuel_type, transmission_type, current_odometer, is_active) VALUES
(1, 'B 1234 ABC', 'Toyota', 'Avanza', 2022, 'Silver', 'MHKA1234567890123', 'GASOLINE', 'AUTOMATIC', 25000, true),
(1, 'B 5678 DEF', 'Honda', 'Jazz', 2023, 'White', 'MHKA1234567890124', 'GASOLINE', 'CVT', 15000, true),
(1, 'B 9012 GHI', 'Toyota', 'Innova', 2021, 'Black', 'MHKA1234567890125', 'DIESEL', 'MANUAL', 45000, true),
(2, 'B 3456 JKL', 'Mitsubishi', 'Xpander', 2022, 'Red', 'MHKA1234567890126', 'GASOLINE', 'AUTOMATIC', 30000, true),
(2, 'B 7890 MNO', 'Daihatsu', 'Terios', 2023, 'Blue', 'MHKA1234567890127', 'GASOLINE', 'AUTOMATIC', 12000, true),
(3, 'B 2345 PQR', 'Suzuki', 'Ertiga', 2022, 'Gray', 'MHKA1234567890128', 'GASOLINE', 'AUTOMATIC', 28000, true),
(3, 'B 6789 STU', 'Toyota', 'Fortuner', 2023, 'White', 'MHKA1234567890129', 'DIESEL', 'AUTOMATIC', 18000, true),
(4, 'B 1357 VWX', 'Honda', 'CR-V', 2022, 'Black', 'MHKA1234567890130', 'GASOLINE', 'CVT', 22000, true),
(4, 'B 2468 YZA', 'Mazda', 'CX-5', 2023, 'Red', 'MHKA1234567890131', 'GASOLINE', 'AUTOMATIC', 14000, true),
(5, 'B 3579 BCD', 'Isuzu', 'D-Max', 2021, 'Silver', 'MHKA1234567890132', 'DIESEL', 'MANUAL', 55000, true);

-- =====================================================
-- SPARE PARTS
-- =====================================================

INSERT INTO spare_parts (part_number, name, description, category, brand, unit, unit_cost, selling_price, min_stock_level, reorder_point, is_active) VALUES
('OIL-5W30-001', 'Engine Oil 5W-30 4L', 'Fully Synthetic Engine Oil', 'ENGINE', 'Shell', 'LITER', 85000, 120000, 20, 30, true),
('FILTER-OIL-001', 'Oil Filter Standard', 'Standard Oil Filter', 'ENGINE', 'Sakura', 'PCS', 35000, 50000, 50, 70, true),
('FILTER-AIR-001', 'Air Filter Standard', 'Standard Air Filter', 'ENGINE', 'Denso', 'PCS', 45000, 65000, 40, 60, true),
('FILTER-FUEL-001', 'Fuel Filter Diesel', 'Diesel Fuel Filter', 'ENGINE', 'Mann', 'PCS', 55000, 80000, 30, 45, true),
('BRAKE-PAD-F', 'Brake Pad Front', 'Front Brake Pad Set', 'BRAKES', 'Bendix', 'SET', 250000, 350000, 20, 30, true),
('BRAKE-PAD-R', 'Brake Pad Rear', 'Rear Brake Pad Set', 'BRAKES', 'Bendix', 'SET', 220000, 310000, 20, 30, true),
('BATTERY-12V-45A', 'Battery 12V 45Ah', 'Maintenance Free Battery', 'ELECTRICAL', 'GS Astra', 'PCS', 650000, 900000, 10, 15, true),
('SPARK-PLUG-001', 'Spark Plug Iridium', 'Iridium Spark Plug', 'ENGINE', 'NGK', 'PCS', 85000, 120000, 30, 45, true),
('BELT-TIMING-001', 'Timing Belt', 'Timing Belt Kit', 'ENGINE', 'Gates', 'SET', 450000, 650000, 15, 25, true),
('AC-FREON-R134', 'AC Freon R134a', 'AC Refrigerant', 'AC', 'Dupont', 'KG', 180000, 250000, 25, 35, true),
('WIPER-BLADE-001', 'Wiper Blade 24 inch', 'Wiper Blade', 'GENERAL', 'Bosch', 'PCS', 65000, 95000, 30, 45, true),
('COOLANT-5L', 'Radiator Coolant 5L', 'Engine Coolant', 'ENGINE', 'Prestone', 'LITER', 75000, 105000, 20, 30, true);

-- =====================================================
-- INVENTORY LOCATIONS
-- =====================================================

INSERT INTO inventory_locations (code, name, type, workshop_id, address, city, is_active) VALUES
('LOC001', 'Central Warehouse Garut', 'CENTRAL_WAREHOUSE', 1, 'Jl. Proklamasi No. 6', 'Garut', true),
('LOC002', 'Jakarta Workshop Stock', 'OWN_WAREHOUSE', 2, 'Jl. Kebon Jeruk No. 45', 'Jakarta Barat', true),
('LOC003', 'Auto Prima Partner Stock', 'PARTNER_WORKSHOP', 3, 'Jl. MT Haryono No. 88', 'Bandung', true);

-- =====================================================
-- INVENTORY
-- =====================================================

INSERT INTO inventory (spare_part_id, location_id, quantity, min_stock_level, max_stock_level, reorder_point, unit_cost) VALUES
-- Central Warehouse
(1, 1, 50, 20, 100, 30, 85000),
(2, 1, 80, 50, 150, 70, 35000),
(3, 1, 60, 40, 120, 60, 45000),
(4, 1, 45, 30, 100, 45, 55000),
(5, 1, 35, 20, 80, 30, 250000),
(6, 1, 35, 20, 80, 30, 220000),
(7, 1, 20, 10, 40, 15, 650000),
(8, 1, 50, 30, 100, 45, 85000),
(9, 1, 25, 15, 60, 25, 450000),
(10, 1, 40, 25, 80, 35, 180000),
(11, 1, 50, 30, 100, 45, 65000),
(12, 1, 35, 20, 80, 30, 75000),
-- Jakarta Workshop
(1, 2, 30, 15, 60, 20, 85000),
(2, 2, 50, 30, 100, 45, 35000),
(3, 2, 40, 25, 80, 35, 45000),
(7, 2, 15, 8, 30, 12, 650000),
-- Partner Workshop
(1, 3, 20, 10, 50, 15, 85000),
(2, 3, 35, 20, 80, 30, 35000);

-- =====================================================
-- WORK ORDERS - Sample Data
-- =====================================================

INSERT INTO work_orders (
    work_order_number, vehicle_id, client_id, workshop_id, assigned_mechanic_id,
    status, execution_type, service_type, request_description, diagnosis,
    current_odometer, estimated_cost, labor_cost, parts_cost,
    requested_date, estimated_date, approved_date, started_date, completed_date,
    is_urgent, created_by
) VALUES
-- Completed Work Orders
('WO-2024-001', 1, 1, 1, 1, 'COMPLETED', 'INTERNAL', 'ENGINE', 
 'Ganti oli mesin dan filter', 'Service rutin 25.000 km', 
 25000, 300000, 100000, 200000,
 '2024-01-15 08:30:00', '2024-01-15 09:00:00', '2024-01-15 09:15:00', 
 '2024-01-15 10:00:00', '2024-01-15 12:00:00', 
 false, 2),

('WO-2024-002', 2, 1, 1, 2, 'COMPLETED', 'INTERNAL', 'ELECTRICAL', 
 'Ganti aki mobil mati', 'Aki sudah lemah, perlu penggantian', 
 15000, 950000, 50000, 900000,
 '2024-01-18 14:00:00', '2024-01-18 14:30:00', '2024-01-18 14:45:00', 
 '2024-01-18 15:00:00', '2024-01-18 16:30:00', 
 true, 2),

('WO-2024-003', 3, 1, 1, 1, 'COMPLETED', 'INTERNAL', 'ENGINE', 
 'Service besar 45.000 km', 'Ganti oli, filter, busi, timing belt', 
 45000, 1500000, 300000, 1200000,
 '2024-01-20 08:00:00', '2024-01-20 09:00:00', '2024-01-20 09:30:00', 
 '2024-01-20 10:00:00', '2024-01-20 16:00:00', 
 false, 2),

-- In Progress
('WO-2024-004', 4, 2, 2, 4, 'IN_PROGRESS', 'INTERNAL', 'TRANSMISSION', 
 'Transmisi matic bermasalah', 'Perlu turun transmisi untuk pengecekan detail', 
 30000, 2500000, 500000, 2000000,
 '2024-02-01 09:00:00', '2024-02-01 10:00:00', '2024-02-01 10:30:00', 
 '2024-02-01 11:00:00', NULL, 
 true, 2),

-- Approved - Waiting to Start
('WO-2024-005', 5, 2, 2, 5, 'APPROVED', 'INTERNAL', 'AC', 
 'AC tidak dingin', 'Perlu isi freon dan cek kompresor', 
 12000, 450000, 150000, 300000,
 '2024-02-05 10:00:00', '2024-02-05 11:00:00', '2024-02-05 11:30:00', 
 NULL, NULL, 
 false, 2),

-- Estimated - Waiting Approval
('WO-2024-006', 6, 3, 1, 1, 'ESTIMATED', 'INTERNAL', 'BRAKES', 
 'Rem bunyi dan kurang pakem', 'Brake pad sudah tipis, perlu ganti depan-belakang', 
 28000, 850000, 150000, 700000,
 '2024-02-08 13:00:00', '2024-02-08 14:00:00', NULL, 
 NULL, NULL, 
 false, 2),

-- Requested - New Request
('WO-2024-007', 7, 3, 1, NULL, 'REQUESTED', 'INTERNAL', 'ENGINE', 
 'Mesin terasa kurang bertenaga', NULL, 
 18000, NULL, NULL, NULL,
 '2024-02-10 15:30:00', NULL, NULL, 
 NULL, NULL, 
 false, 4),

-- Partner Workshop
('WO-2024-008', 8, 4, 3, NULL, 'COMPLETED', 'PARTNER_WORKSHOP', 'ENGINE', 
 'Service rutin di bengkel partner', 'Service 20.000 km', 
 22000, 350000, 120000, 230000,
 '2024-01-25 09:00:00', '2024-01-25 10:00:00', '2024-01-25 10:30:00', 
 '2024-01-25 11:00:00', '2024-01-25 14:00:00', 
 false, 2);

-- =====================================================
-- WORK ORDER ITEMS
-- =====================================================

-- WO-2024-001 Items
INSERT INTO work_order_items (work_order_id, item_type, spare_part_id, quantity, unit_price, subtotal) VALUES
(1, 'LABOR', NULL, 1, 100000, 100000),
(1, 'PART', 1, 4, 25000, 100000),
(1, 'PART', 2, 1, 50000, 50000),
(1, 'PART', 3, 1, 65000, 65000);

-- WO-2024-002 Items
INSERT INTO work_order_items (work_order_id, item_type, spare_part_id, quantity, unit_price, subtotal) VALUES
(2, 'LABOR', NULL, 1, 50000, 50000),
(2, 'PART', 7, 1, 900000, 900000);

-- WO-2024-003 Items
INSERT INTO work_order_items (work_order_id, item_type, spare_part_id, quantity, unit_price, subtotal) VALUES
(3, 'LABOR', NULL, 1, 300000, 300000),
(3, 'PART', 1, 5, 25000, 125000),
(3, 'PART', 2, 1, 50000, 50000),
(3, 'PART', 3, 1, 65000, 65000),
(3, 'PART', 8, 4, 120000, 480000),
(3, 'PART', 9, 1, 650000, 650000);

-- =====================================================
-- INVOICES
-- =====================================================

INSERT INTO invoices (
    invoice_number, work_order_id, client_id, invoice_date, due_date,
    subtotal, tax_amount, total_amount, paid_amount, balance, status,
    created_by
) VALUES
('INV-2024-001', 1, 1, '2024-01-15', '2024-02-14', 
 300000, 33000, 333000, 333000, 0, 'PAID', 3),

('INV-2024-002', 2, 1, '2024-01-18', '2024-02-17', 
 950000, 104500, 1054500, 1054500, 0, 'PAID', 3),

('INV-2024-003', 3, 1, '2024-01-20', '2024-02-19', 
 1500000, 165000, 1665000, 0, 1665000, 'SENT', 3),

('INV-2024-004', 8, 4, '2024-01-25', '2024-02-24', 
 350000, 38500, 388500, 0, 388500, 'SENT', 3);

-- =====================================================
-- PAYMENTS
-- =====================================================

INSERT INTO payments (
    payment_number, invoice_id, client_id, payment_date, payment_method,
    amount, reference_number, created_by
) VALUES
('PAY-2024-001', 1, 1, '2024-01-20', 'TRANSFER', 333000, 'TRF20240120001', 3),
('PAY-2024-002', 2, 1, '2024-01-22', 'TRANSFER', 1054500, 'TRF20240122001', 3);

-- =====================================================
-- MAINTENANCE SCHEDULES
-- =====================================================

INSERT INTO maintenance_schedules (
    vehicle_id, schedule_type, service_name, description, 
    interval_type, interval_value, last_service_date, last_service_odometer,
    next_service_date, next_service_odometer, is_active
) VALUES
(1, 'MILEAGE_BASED', 'Service Rutin', 'Ganti oli dan filter', 'KILOMETERS', 5000, '2024-01-15', 25000, NULL, 30000, true),
(2, 'MILEAGE_BASED', 'Service Rutin', 'Ganti oli dan filter', 'KILOMETERS', 5000, '2024-01-10', 15000, NULL, 20000, true),
(3, 'MILEAGE_BASED', 'Service Besar', 'Service lengkap', 'KILOMETERS', 10000, '2024-01-20', 45000, NULL, 55000, true);

-- =====================================================
-- SYSTEM SETTINGS
-- =====================================================

INSERT INTO system_settings (setting_key, setting_value, setting_type, description, is_public) VALUES
('company_name', 'FleetOps', 'STRING', 'Company name', true),
('company_address', 'Jl. Proklamasi No. 6 Garut, Jawa Barat', 'STRING', 'Company address', true),
('company_phone', '+62 895 3914 54779', 'STRING', 'Company phone', true),
('company_email', 'info@fms.local', 'STRING', 'Company email', true),
('tax_percentage', '11', 'INTEGER', 'Tax percentage (PPN)', true),
('auto_invoice_generation', 'true', 'BOOLEAN', 'Auto generate invoice on work order completion', false),
('default_payment_term', '30', 'INTEGER', 'Default payment term in days', false);

-- =====================================================
-- NOTIFICATIONS (Sample)
-- =====================================================

INSERT INTO notifications (user_id, notification_type, title, message, reference_type, reference_id, is_read) VALUES
(4, 'WORK_ORDER', 'Work Order Completed', 'Work Order WO-2024-001 has been completed', 'WORK_ORDER', 1, true),
(4, 'INVOICE', 'New Invoice Generated', 'Invoice INV-2024-001 has been generated', 'INVOICE', 1, true),
(4, 'WORK_ORDER', 'Work Order Needs Approval', 'Work Order WO-2024-006 is waiting for your approval', 'WORK_ORDER', 6, false),
(2, 'REMINDER', 'Vehicle Service Due', 'Vehicle B 1234 ABC is due for service at 30,000 km', 'VEHICLE', 1, false);

-- =====================================================
-- END OF SAMPLE DATA
-- =====================================================
