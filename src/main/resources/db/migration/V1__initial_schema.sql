-- Fleet Maintenance System - Database DDL
-- Version: 1.0.0
-- Database: PostgreSQL

-- =====================================================
-- USERS & AUTHENTICATION
-- =====================================================

CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    is_active BOOLEAN DEFAULT TRUE,
    is_locked BOOLEAN DEFAULT FALSE,
    failed_login_attempts INTEGER DEFAULT 0,
    last_login_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- =====================================================
-- CLIENTS
-- =====================================================

CREATE TABLE clients (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    business_type VARCHAR(100),
    npwp VARCHAR(50),
    address TEXT,
    city VARCHAR(100),
    province VARCHAR(100),
    postal_code VARCHAR(10),
    phone VARCHAR(20),
    email VARCHAR(100),
    contact_person VARCHAR(100),
    contact_phone VARCHAR(20),
    payment_term INTEGER DEFAULT 30, -- days
    credit_limit DECIMAL(15,2),
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_clients_code ON clients(code);
CREATE INDEX idx_clients_name ON clients(name);

-- =====================================================
-- VEHICLES
-- =====================================================

CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    license_plate VARCHAR(20) NOT NULL UNIQUE,
    brand VARCHAR(50) NOT NULL,
    model VARCHAR(50) NOT NULL,
    year INTEGER NOT NULL,
    color VARCHAR(30),
    vin VARCHAR(50) UNIQUE,
    engine_number VARCHAR(50) UNIQUE,
    fuel_type VARCHAR(20), -- GASOLINE, DIESEL, ELECTRIC, HYBRID
    transmission_type VARCHAR(20), -- MANUAL, AUTOMATIC, CVT
    current_odometer INTEGER,
    purchase_date DATE,
    registration_expiry DATE,
    insurance_expiry DATE,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (client_id) REFERENCES clients(id)
);

CREATE INDEX idx_vehicles_license_plate ON vehicles(license_plate);
CREATE INDEX idx_vehicles_client ON vehicles(client_id);

-- =====================================================
-- WORKSHOPS
-- =====================================================

CREATE TABLE workshops (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(30) NOT NULL, -- INTERNAL, PARTNER, CLIENT_RECOMMENDED
    address TEXT,
    city VARCHAR(100),
    province VARCHAR(100),
    postal_code VARCHAR(10),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    phone VARCHAR(20),
    email VARCHAR(100),
    contact_person VARCHAR(100),
    specialization TEXT,
    operating_hours VARCHAR(100),
    service_capacity INTEGER,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_reviews INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_workshops_code ON workshops(code);
CREATE INDEX idx_workshops_type ON workshops(type);
CREATE INDEX idx_workshops_city ON workshops(city);

-- =====================================================
-- MECHANICS
-- =====================================================

CREATE TABLE mechanics (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20) NOT NULL,
    specialization VARCHAR(100), -- ENGINE, ELECTRICAL, AC, TRANSMISSION, GENERAL
    certification TEXT,
    experience_years INTEGER,
    workshop_id BIGINT,
    is_active BOOLEAN DEFAULT TRUE,
    rating DECIMAL(3,2) DEFAULT 0.00,
    total_jobs INTEGER DEFAULT 0,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workshop_id) REFERENCES workshops(id)
);

CREATE INDEX idx_mechanics_code ON mechanics(code);
CREATE INDEX idx_mechanics_workshop ON mechanics(workshop_id);

-- =====================================================
-- SPARE PARTS
-- =====================================================

CREATE TABLE spare_parts (
    id BIGSERIAL PRIMARY KEY,
    part_number VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(100), -- ENGINE, ELECTRICAL, SUSPENSION, BRAKES, etc
    brand VARCHAR(100),
    unit VARCHAR(20), -- PCS, SET, LITER, KG
    unit_cost DECIMAL(15,2) NOT NULL,
    selling_price DECIMAL(15,2),
    min_stock_level INTEGER DEFAULT 0,
    reorder_point INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_spare_parts_number ON spare_parts(part_number);
CREATE INDEX idx_spare_parts_name ON spare_parts(name);
CREATE INDEX idx_spare_parts_category ON spare_parts(category);

-- =====================================================
-- INVENTORY LOCATIONS
-- =====================================================

CREATE TABLE inventory_locations (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(30), -- OWN_WAREHOUSE, PARTNER_WORKSHOP, CENTRAL_WAREHOUSE
    workshop_id BIGINT,
    address TEXT,
    city VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (workshop_id) REFERENCES workshops(id)
);

-- =====================================================
-- INVENTORY
-- =====================================================

CREATE TABLE inventory (
    id BIGSERIAL PRIMARY KEY,
    spare_part_id BIGINT NOT NULL,
    location_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER DEFAULT 0,
    available_quantity INTEGER GENERATED ALWAYS AS (quantity - COALESCE(reserved_quantity, 0)) STORED,
    min_stock_level INTEGER DEFAULT 0,
    max_stock_level INTEGER DEFAULT 0,
    reorder_point INTEGER DEFAULT 0,
    unit_cost DECIMAL(15,2),
    total_value DECIMAL(15,2) GENERATED ALWAYS AS (quantity * unit_cost) STORED,
    batch_number VARCHAR(50),
    serial_number VARCHAR(50),
    last_restock_date TIMESTAMP,
    expiry_date DATE,
    is_available BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id),
    FOREIGN KEY (location_id) REFERENCES inventory_locations(id),
    UNIQUE (spare_part_id, location_id, batch_number)
);

CREATE INDEX idx_inventory_spare_part ON inventory(spare_part_id);
CREATE INDEX idx_inventory_location ON inventory(location_id);

-- =====================================================
-- INVENTORY MOVEMENTS
-- =====================================================

CREATE TABLE inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    inventory_id BIGINT NOT NULL,
    movement_type VARCHAR(30) NOT NULL, -- IN, OUT, TRANSFER, ADJUSTMENT
    quantity INTEGER NOT NULL,
    reference_type VARCHAR(50), -- PURCHASE, WORK_ORDER, TRANSFER, ADJUSTMENT
    reference_id BIGINT,
    from_location_id BIGINT,
    to_location_id BIGINT,
    unit_cost DECIMAL(15,2),
    notes TEXT,
    movement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (inventory_id) REFERENCES inventory(id),
    FOREIGN KEY (from_location_id) REFERENCES inventory_locations(id),
    FOREIGN KEY (to_location_id) REFERENCES inventory_locations(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_inv_movements_inventory ON inventory_movements(inventory_id);
CREATE INDEX idx_inv_movements_type ON inventory_movements(movement_type);
CREATE INDEX idx_inv_movements_date ON inventory_movements(movement_date);

-- =====================================================
-- WORK ORDERS
-- =====================================================

CREATE TABLE work_orders (
    id BIGSERIAL PRIMARY KEY,
    work_order_number VARCHAR(30) NOT NULL UNIQUE,
    vehicle_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    workshop_id BIGINT,
    assigned_mechanic_id BIGINT,
    status VARCHAR(30) NOT NULL, -- REQUESTED, ESTIMATED, APPROVED, REJECTED, IN_PROGRESS, COMPLETED, BILLED, CANCELLED
    execution_type VARCHAR(30) NOT NULL, -- INTERNAL, PARTNER_WORKSHOP, CLIENT_RECOMMENDED
    service_type VARCHAR(30) NOT NULL, -- ENGINE, ELECTRICAL, AC, TRANSMISSION, GENERAL, PREVENTIVE
    request_description TEXT NOT NULL,
    diagnosis TEXT,
    current_odometer INTEGER,
    estimated_cost DECIMAL(15,2),
    actual_cost DECIMAL(15,2),
    labor_cost DECIMAL(15,2),
    parts_cost DECIMAL(15,2),
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2),
    requested_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    estimated_date TIMESTAMP,
    approved_date TIMESTAMP,
    started_date TIMESTAMP,
    completed_date TIMESTAMP,
    is_urgent BOOLEAN DEFAULT FALSE,
    is_warranty BOOLEAN DEFAULT FALSE,
    warranty_reference VARCHAR(100),
    approved_by BIGINT,
    approval_notes TEXT,
    rejection_reason TEXT,
    customer_rating INTEGER, -- 1-5
    customer_feedback TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (workshop_id) REFERENCES workshops(id),
    FOREIGN KEY (assigned_mechanic_id) REFERENCES mechanics(id),
    FOREIGN KEY (approved_by) REFERENCES users(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_wo_number ON work_orders(work_order_number);
CREATE INDEX idx_wo_vehicle ON work_orders(vehicle_id);
CREATE INDEX idx_wo_client ON work_orders(client_id);
CREATE INDEX idx_wo_status ON work_orders(status);
CREATE INDEX idx_wo_requested_date ON work_orders(requested_date);

-- =====================================================
-- WORK ORDER ITEMS (Parts & Services)
-- =====================================================

CREATE TABLE work_order_items (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    item_type VARCHAR(20) NOT NULL, -- PART, LABOR
    spare_part_id BIGINT,
    service_name VARCHAR(200),
    description TEXT,
    quantity INTEGER NOT NULL,
    unit_price DECIMAL(15,2) NOT NULL,
    discount_percent DECIMAL(5,2) DEFAULT 0,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    subtotal DECIMAL(15,2),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (spare_part_id) REFERENCES spare_parts(id)
);

CREATE INDEX idx_wo_items_work_order ON work_order_items(work_order_id);

-- =====================================================
-- WORK ORDER DOCUMENTS
-- =====================================================

CREATE TABLE work_order_documents (
    id BIGSERIAL PRIMARY KEY,
    work_order_id BIGINT NOT NULL,
    document_type VARCHAR(50), -- PHOTO_BEFORE, PHOTO_AFTER, ESTIMATION, INVOICE, OTHER
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    mime_type VARCHAR(100),
    description TEXT,
    uploaded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    uploaded_by BIGINT,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id) ON DELETE CASCADE,
    FOREIGN KEY (uploaded_by) REFERENCES users(id)
);

CREATE INDEX idx_wo_docs_work_order ON work_order_documents(work_order_id);

-- =====================================================
-- INVOICES
-- =====================================================

CREATE TABLE invoices (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(30) NOT NULL UNIQUE,
    work_order_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    invoice_date DATE NOT NULL,
    due_date DATE NOT NULL,
    subtotal DECIMAL(15,2) NOT NULL,
    discount_amount DECIMAL(15,2) DEFAULT 0,
    tax_amount DECIMAL(15,2) DEFAULT 0,
    total_amount DECIMAL(15,2) NOT NULL,
    paid_amount DECIMAL(15,2) DEFAULT 0,
    balance DECIMAL(15,2),
    status VARCHAR(30) NOT NULL, -- DRAFT, SENT, PARTIAL_PAID, PAID, OVERDUE, CANCELLED
    payment_method VARCHAR(30), -- CASH, TRANSFER, CREDIT_CARD, CHECK
    payment_date DATE,
    payment_reference VARCHAR(100),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (work_order_id) REFERENCES work_orders(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_invoices_number ON invoices(invoice_number);
CREATE INDEX idx_invoices_client ON invoices(client_id);
CREATE INDEX idx_invoices_status ON invoices(status);
CREATE INDEX idx_invoices_due_date ON invoices(due_date);

-- =====================================================
-- PAYMENTS
-- =====================================================

CREATE TABLE payments (
    id BIGSERIAL PRIMARY KEY,
    payment_number VARCHAR(30) NOT NULL UNIQUE,
    invoice_id BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    payment_date DATE NOT NULL,
    payment_method VARCHAR(30) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    reference_number VARCHAR(100),
    bank_name VARCHAR(100),
    account_number VARCHAR(50),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id),
    FOREIGN KEY (client_id) REFERENCES clients(id),
    FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_payments_invoice ON payments(invoice_id);
CREATE INDEX idx_payments_date ON payments(payment_date);

-- =====================================================
-- MAINTENANCE SCHEDULES
-- =====================================================

CREATE TABLE maintenance_schedules (
    id BIGSERIAL PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    schedule_type VARCHAR(30) NOT NULL, -- PERIODIC, MILEAGE_BASED
    service_name VARCHAR(200) NOT NULL,
    description TEXT,
    interval_type VARCHAR(20), -- DAYS, MONTHS, KILOMETERS
    interval_value INTEGER,
    last_service_date DATE,
    last_service_odometer INTEGER,
    next_service_date DATE,
    next_service_odometer INTEGER,
    is_active BOOLEAN DEFAULT TRUE,
    reminder_sent BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE INDEX idx_maint_schedule_vehicle ON maintenance_schedules(vehicle_id);
CREATE INDEX idx_maint_schedule_next_date ON maintenance_schedules(next_service_date);

-- =====================================================
-- NOTIFICATIONS
-- =====================================================

CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    notification_type VARCHAR(50), -- WORK_ORDER, INVOICE, PAYMENT, REMINDER
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    reference_type VARCHAR(50),
    reference_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_user ON notifications(user_id);
CREATE INDEX idx_notifications_read ON notifications(is_read);

-- =====================================================
-- AUDIT LOGS
-- =====================================================

CREATE TABLE audit_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(50) NOT NULL, -- CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    entity_type VARCHAR(100),
    entity_id BIGINT,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(50),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_audit_logs_user ON audit_logs(user_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_created ON audit_logs(created_at);

-- =====================================================
-- SYSTEM SETTINGS
-- =====================================================

CREATE TABLE system_settings (
    id BIGSERIAL PRIMARY KEY,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    setting_type VARCHAR(30), -- STRING, INTEGER, BOOLEAN, JSON
    description TEXT,
    is_public BOOLEAN DEFAULT FALSE,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (updated_by) REFERENCES users(id)
);

-- =====================================================
-- INDEXES FOR PERFORMANCE
-- =====================================================

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_active ON users(is_active);

-- =====================================================
-- SEQUENCES FOR NUMBERING
-- =====================================================

CREATE SEQUENCE seq_work_order_number START 1 INCREMENT 1;
CREATE SEQUENCE seq_invoice_number START 1 INCREMENT 1;
CREATE SEQUENCE seq_payment_number START 1 INCREMENT 1;
CREATE SEQUENCE seq_client_code START 1 INCREMENT 1;
CREATE SEQUENCE seq_workshop_code START 1 INCREMENT 1;
CREATE SEQUENCE seq_mechanic_code START 1 INCREMENT 1;

-- =====================================================
-- FUNCTIONS & TRIGGERS
-- =====================================================

-- Function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Apply updated_at trigger to tables
CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_clients_updated_at BEFORE UPDATE ON clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicles_updated_at BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_workshops_updated_at BEFORE UPDATE ON workshops
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mechanics_updated_at BEFORE UPDATE ON mechanics
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_spare_parts_updated_at BEFORE UPDATE ON spare_parts
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_inventory_updated_at BEFORE UPDATE ON inventory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_work_orders_updated_at BEFORE UPDATE ON work_orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_invoices_updated_at BEFORE UPDATE ON invoices
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- COMMENTS
-- =====================================================

COMMENT ON TABLE users IS 'User accounts for system access';
COMMENT ON TABLE clients IS 'Corporate clients who own vehicle fleets';
COMMENT ON TABLE vehicles IS 'Fleet vehicles managed by the system';
COMMENT ON TABLE workshops IS 'Service workshops (internal, partner, client-recommended)';
COMMENT ON TABLE mechanics IS 'Technicians who perform maintenance work';
COMMENT ON TABLE work_orders IS 'Maintenance and repair work orders';
COMMENT ON TABLE invoices IS 'Billing invoices for completed work';
COMMENT ON TABLE inventory IS 'Spare parts inventory across locations';
