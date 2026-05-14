# CURSOR AI - Instructions untuk Generate Fleet Maintenance System

## Instruksi Umum

Gunakan file ini sebagai panduan untuk generate complete Spring Boot + Thymeleaf project menggunakan Cursor AI.

---

## Step 1: Generate Database Schema (DDL)

### File: `src/main/resources/db/migration/V1__initial_schema.sql`

```
@Cursor: Generate PostgreSQL DDL script dengan spesifikasi:

CREATE TABLES:
1. users (id, username UNIQUE, email UNIQUE, password BCrypt, full_name, phone, is_active, is_locked, failed_login_attempts, last_login_at, created_at, updated_at)
2. roles (id, name UNIQUE, description, created_at)
3. user_roles (user_id, role_id) - composite PK
4. clients (id, code UNIQUE, name, business_type, npwp, address, city, province, postal_code, phone, email, contact_person, contact_phone, payment_term DEFAULT 30, credit_limit, is_active, notes, created_at, updated_at, created_by FK users)
5. vehicles (id, client_id FK, license_plate UNIQUE, brand, model, year, color, vin UNIQUE, engine_number UNIQUE, fuel_type VARCHAR(20), transmission_type VARCHAR(20), current_odometer, purchase_date, registration_expiry, insurance_expiry, is_active, notes, created_at, updated_at)
6. workshops (id, code UNIQUE, name, type VARCHAR(30), address, city, province, postal_code, latitude DECIMAL(10,7), longitude DECIMAL(10,7), phone, email, contact_person, specialization TEXT, operating_hours, service_capacity, rating DECIMAL(3,2) DEFAULT 0, total_reviews DEFAULT 0, is_active, notes, created_at, updated_at)
7. mechanics (id, code UNIQUE, full_name, email, phone, specialization, certification TEXT, experience_years, workshop_id FK, is_active, rating DECIMAL(3,2) DEFAULT 0, total_jobs DEFAULT 0, notes, created_at, updated_at)
8. spare_parts (id, part_number UNIQUE, name, description TEXT, category, brand, unit, unit_cost DECIMAL(15,2), selling_price DECIMAL(15,2), min_stock_level DEFAULT 0, reorder_point DEFAULT 0, is_active, notes, created_at, updated_at)
9. inventory_locations (id, code UNIQUE, name, type, workshop_id FK, address TEXT, city, is_active, created_at)
10. inventory (id, spare_part_id FK, location_id FK, quantity DEFAULT 0, reserved_quantity DEFAULT 0, available_quantity COMPUTED, min_stock_level, max_stock_level, reorder_point, unit_cost DECIMAL(15,2), total_value COMPUTED, batch_number, serial_number, last_restock_date, expiry_date, is_available, notes, created_at, updated_at)
    - available_quantity = quantity - COALESCE(reserved_quantity, 0) STORED
    - total_value = quantity * unit_cost STORED
11. inventory_movements (id, inventory_id FK, movement_type VARCHAR(30), quantity, reference_type, reference_id, from_location_id FK, to_location_id FK, unit_cost, notes, movement_date DEFAULT CURRENT_TIMESTAMP, created_by FK)
12. work_orders (id, work_order_number UNIQUE, vehicle_id FK, client_id FK, workshop_id FK, assigned_mechanic_id FK, status VARCHAR(30), execution_type VARCHAR(30), service_type VARCHAR(30), request_description TEXT, diagnosis TEXT, current_odometer, estimated_cost DECIMAL(15,2), actual_cost, labor_cost, parts_cost, discount_amount DEFAULT 0, tax_amount DEFAULT 0, total_amount, requested_date, estimated_date, approved_date, started_date, completed_date, is_urgent DEFAULT FALSE, is_warranty DEFAULT FALSE, warranty_reference, approved_by FK users, approval_notes TEXT, rejection_reason TEXT, customer_rating, customer_feedback TEXT, created_at, updated_at, created_by FK)
13. work_order_items (id, work_order_id FK CASCADE, item_type VARCHAR(20), spare_part_id FK, service_name, description TEXT, quantity, unit_price DECIMAL(15,2), discount_percent DECIMAL(5,2) DEFAULT 0, discount_amount DEFAULT 0, subtotal, notes TEXT, created_at)
14. work_order_documents (id, work_order_id FK CASCADE, document_type VARCHAR(50), file_name, file_path, file_size, mime_type, description TEXT, uploaded_at, uploaded_by FK)
15. invoices (id, invoice_number UNIQUE, work_order_id FK, client_id FK, invoice_date, due_date, subtotal DECIMAL(15,2), discount_amount DEFAULT 0, tax_amount DEFAULT 0, total_amount, paid_amount DEFAULT 0, balance, status VARCHAR(30), payment_method, payment_date, payment_reference, notes TEXT, created_at, updated_at, created_by FK)
16. payments (id, payment_number UNIQUE, invoice_id FK, client_id FK, payment_date, payment_method VARCHAR(30), amount DECIMAL(15,2), reference_number, bank_name, account_number, notes TEXT, created_at, created_by FK)
17. maintenance_schedules (id, vehicle_id FK, schedule_type VARCHAR(30), service_name, description TEXT, interval_type VARCHAR(20), interval_value, last_service_date, last_service_odometer, next_service_date, next_service_odometer, is_active, reminder_sent DEFAULT FALSE, created_at)
18. notifications (id, user_id FK, notification_type VARCHAR(50), title, message TEXT, reference_type, reference_id, is_read DEFAULT FALSE, read_at, created_at)
19. audit_logs (id, user_id FK, action VARCHAR(50), entity_type, entity_id, old_value JSONB, new_value JSONB, ip_address, user_agent TEXT, created_at)
20. system_settings (id, setting_key UNIQUE, setting_value TEXT, setting_type VARCHAR(30), description TEXT, is_public DEFAULT FALSE, updated_at, updated_by FK)

CREATE SEQUENCES:
- seq_work_order_number START 1
- seq_invoice_number START 1
- seq_payment_number START 1
- seq_client_code START 1
- seq_workshop_code START 1
- seq_mechanic_code START 1

CREATE INDEXES:
- All foreign keys
- username, email (users)
- license_plate (vehicles)
- work_order_number, status, requested_date (work_orders)
- invoice_number, status, due_date (invoices)
- code fields (clients, workshops, mechanics)

CREATE TRIGGER FUNCTION:
- update_updated_at_column() untuk auto-update updated_at

APPLY TRIGGERS to tables with updated_at column

ADD COMMENTS untuk setiap table
```

---

## Step 2: Generate Sample Data (DML)

### File: `src/main/resources/db/migration/V2__sample_data.sql`

```
@Cursor: Generate PostgreSQL DML script dengan sample data:

INSERT roles:
- ROLE_ADMIN, ROLE_MANAGER, ROLE_CLIENT, ROLE_MECHANIC, ROLE_FINANCE

INSERT users (password BCrypt hash untuk 'password123'):
- admin (ADMIN)
- manager (MANAGER)
- finance (FINANCE)
- client1 (CLIENT)
- mechanic1 (MECHANIC)

BCrypt hash: $2a$10$N9qo8uLOickgx2ZMRZoMye1J6HG.pJI.72vOJSPRmEhPzNjVSLQni

INSERT clients (5 companies):
- CLI001: PT Takari Kokoh Sejahtera (Car Rental)
- CLI002: PT TSM (Car Rental)
- CLI003: Orix (Car Rental)
- CLI004: Prima Rent (Car Rental)
- CLI005: PT Charoen Pokphand (Logistics)

INSERT workshops (5):
- WS001: Central Workshop (INTERNAL) - Garut
- WS002: Jakarta Branch Workshop (INTERNAL) - Jakarta
- WS003: Auto Prima (PARTNER) - Bandung
- WS004: Bengkel Jaya (PARTNER) - Surabaya
- WS005: Toyota Auto2000 (CLIENT_RECOMMENDED) - Jakarta

INSERT mechanics (5):
- MEC001-005 dengan spesialisasi berbeda

INSERT vehicles (10):
- Mix Toyota Avanza, Honda Jazz, Innova, Xpander, Terios, Ertiga, Fortuner, CR-V, CX-5, D-Max
- License plates: B 1234 ABC, B 5678 DEF, dst

INSERT spare_parts (12):
- Engine Oil, Oil Filter, Air Filter, Fuel Filter, Brake Pads, Battery, Spark Plug, Timing Belt, AC Freon, Wiper Blade, Coolant

INSERT inventory_locations (3):
- LOC001: Central Warehouse Garut
- LOC002: Jakarta Workshop
- LOC003: Partner Stock Bandung

INSERT inventory:
- Stock di 3 lokasi dengan berbagai quantities

INSERT work_orders (8):
- 3 COMPLETED
- 1 IN_PROGRESS
- 1 APPROVED
- 1 ESTIMATED
- 1 REQUESTED
- 1 COMPLETED (partner workshop)

INSERT work_order_items untuk completed work orders

INSERT invoices (4):
- 2 PAID
- 2 SENT

INSERT payments (2) untuk invoice yang paid

INSERT maintenance_schedules (3)

INSERT system_settings:
- company_name, company_address, company_phone, company_email
- tax_percentage = 11
- auto_invoice_generation = true
- default_payment_term = 30

INSERT notifications (4 sample)
```

---

## Step 3: Generate Spring Boot Project Structure

### Maven pom.xml

```
@Cursor: Generate pom.xml dengan dependencies:

Parent: spring-boot-starter-parent 3.2.5
Java: 17
GroupId: com.fms
ArtifactId: fleet-maintenance-system
Version: 1.0.0

Dependencies:
- spring-boot-starter-web
- spring-boot-starter-thymeleaf
- spring-boot-starter-data-jpa
- spring-boot-starter-security
- spring-boot-starter-validation
- spring-boot-starter-mail
- thymeleaf-extras-springsecurity6
- thymeleaf-layout-dialect
- postgresql (runtime)
- flyway-core
- flyway-database-postgresql
- lombok (optional)
- mapstruct 1.5.5.Final
- mapstruct-processor 1.5.5.Final (annotationProcessorPath)
- io.jsonwebtoken:jjwt-api:0.11.5
- io.jsonwebtoken:jjwt-impl:0.11.5 (runtime)
- io.jsonwebtoken:jjwt-jackson:0.11.5 (runtime)
- com.itextpdf:itext7-core:7.2.5 (type: pom)
- org.apache.poi:poi-ooxml:5.2.3
- commons-io:2.13.0
- spring-boot-devtools (runtime, optional)
- spring-boot-starter-test (test)
- spring-security-test (test)

Plugins:
- spring-boot-maven-plugin
- maven-compiler-plugin dengan annotationProcessorPaths untuk Lombok + MapStruct
```

### application.properties

```
@Cursor: Generate application.properties:

spring.application.name=Fleet Maintenance System
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/fleet_maintenance_db
spring.datasource.username=postgres
spring.datasource.password=postgres

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true

# Thymeleaf
spring.thymeleaf.cache=false

# File Upload
spring.servlet.multipart.max-file-size=10MB
file.upload-dir=./uploads

# JWT
jwt.secret=MySecretKeyForJWTTokenGenerationFleetMaintenanceSystem2024
jwt.expiration=86400000

# Logging
logging.level.com.fms=DEBUG

# Date Format
spring.mvc.format.date=yyyy-MM-dd
spring.jackson.time-zone=Asia/Jakarta
```

---

## Step 4: Generate Java Enums

```
@Cursor: Generate enums di package com.fms.enums:

1. WorkOrderStatus: REQUESTED, ESTIMATED, APPROVED, REJECTED, IN_PROGRESS, COMPLETED, BILLED, CANCELLED

2. ExecutionType: INTERNAL, PARTNER_WORKSHOP, CLIENT_RECOMMENDED

3. ServiceType: ENGINE, ELECTRICAL, AC, TRANSMISSION, BRAKES, SUSPENSION, GENERAL, PREVENTIVE

4. FuelType: GASOLINE, DIESEL, ELECTRIC, HYBRID

5. TransmissionType: MANUAL, AUTOMATIC, CVT

6. WorkshopType: INTERNAL, PARTNER, CLIENT_RECOMMENDED

7. InvoiceStatus: DRAFT, SENT, PARTIAL_PAID, PAID, OVERDUE, CANCELLED

8. PaymentMethod: CASH, TRANSFER, CREDIT_CARD, CHECK

9. MovementType: IN, OUT, TRANSFER, ADJUSTMENT

10. ItemType: PART, LABOR
```

---

## Step 5: Generate JPA Entities

```
@Cursor: Generate entities di package com.fms.entity dengan Lombok annotations:

Gunakan pattern:
- @Entity @Table(name="...")
- @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
- @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
- @Column dengan constraints (nullable, unique, length)
- @Enumerated(EnumType.STRING)
- @ManyToOne(fetch = FetchType.LAZY) @JoinColumn
- @OneToMany(mappedBy, cascade = CascadeType.ALL)
- @CreationTimestamp, @UpdateTimestamp untuk audit fields

Entities:
1. User (users table) dengan Set<Role> roles
2. Role (roles table)
3. Client (clients table) dengan createdBy User
4. Vehicle (vehicles table) dengan client FK
5. Workshop (workshops table)
6. Mechanic (mechanics table) dengan workshop FK
7. SparePart (spare_parts table)
8. InventoryLocation (inventory_locations table) dengan workshop FK optional
9. Inventory (inventory table) dengan sparePart dan location FK
   - Add @Transient method: getAvailableQuantity() return quantity - reservedQuantity
10. InventoryMovement (inventory_movements table) dengan inventory FK
11. WorkOrder (work_orders table) dengan vehicle, client, workshop, mechanic, approvedBy, createdBy FK
    - Add List<WorkOrderItem> items dengan @OneToMany
12. WorkOrderItem (work_order_items table) dengan workOrder, sparePart FK
13. WorkOrderDocument (work_order_documents table) dengan workOrder FK
14. Invoice (invoices table) dengan workOrder, client, createdBy FK
15. Payment (payments table) dengan invoice, client, createdBy FK
16. MaintenanceSchedule (maintenance_schedules table) dengan vehicle FK
17. Notification (notifications table) dengan user FK
18. AuditLog (audit_logs table) dengan user FK
19. SystemSetting (system_settings table) dengan updatedBy FK
```

---

## Step 6: Generate Repositories

```
@Cursor: Generate repositories di package com.fms.repository:

Semua extends JpaRepository<Entity, Long>

Tambahkan custom methods:

UserRepository:
- Optional<User> findByUsername(String username)
- Optional<User> findByEmail(String email)
- Boolean existsByUsername(String username)

RoleRepository:
- Optional<Role> findByName(String name)

ClientRepository:
- Page<Client> findByNameContainingIgnoreCase(String name, Pageable pageable)
- List<Client> findByIsActiveTrue()

VehicleRepository:
- Optional<Vehicle> findByLicensePlate(String licensePlate)
- List<Vehicle> findByClientId(Long clientId)
- Page<Vehicle> findByClientId(Long clientId, Pageable pageable)

WorkshopRepository:
- List<Workshop> findByTypeAndIsActiveTrue(WorkshopType type)
- List<Workshop> findByIsActiveTrue()

MechanicRepository:
- List<Mechanic> findByWorkshopIdAndIsActiveTrue(Long workshopId)
- List<Mechanic> findByIsActiveTrue()

SparePartRepository:
- Optional<SparePart> findByPartNumber(String partNumber)
- List<SparePart> findByCategory(String category)
- Page<SparePart> findByNameContainingIgnoreCase(String name, Pageable pageable)

InventoryRepository:
- List<Inventory> findBySparePartId(Long sparePartId)
- List<Inventory> findByLocationId(Long locationId)
- Optional<Inventory> findBySparePartIdAndLocationId(Long sparePartId, Long locationId)
- @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderPoint") List<Inventory> findLowStockItems()

WorkOrderRepository:
- Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable)
- @Query dengan search multi-field (workOrderNumber, vehicle.licensePlate, client.name)
- List<WorkOrder> findByClientIdAndStatusIn(Long clientId, List<WorkOrderStatus> statuses)
- List<WorkOrder> findByAssignedMechanicIdAndStatus(Long mechanicId, WorkOrderStatus status)
- @Query count by status
- @Query findByDateRange

InvoiceRepository:
- Page<Invoice> findByStatus(InvoiceStatus status, Pageable pageable)
- List<Invoice> findByClientId(Long clientId)
- @Query find overdue invoices

PaymentRepository:
- List<Payment> findByInvoiceId(Long invoiceId)
- List<Payment> findByClientId(Long clientId)

NotificationRepository:
- List<Notification> findByUserIdAndIsReadFalse(Long userId)
- Page<Notification> findByUserId(Long userId, Pageable pageable)
```

---

## Step 7: Generate Service Layer

```
@Cursor: Generate services di package com.fms.service dengan @Service @Transactional:

Core Services (implement business logic):

1. UserService:
   - findById, findAll, create, update, delete
   - findByUsername, existsByUsername
   - encodePassword dengan BCryptPasswordEncoder

2. AuthService:
   - login(username, password) return JWT
   - validateToken(token)
   - getCurrentUser()

3. ClientService:
   - CRUD operations
   - search, pagination

4. VehicleService:
   - CRUD operations
   - findByClient, search

5. WorkshopService:
   - CRUD operations
   - findByType, findActive

6. MechanicService:
   - CRUD operations
   - findByWorkshop, findActive

7. SparePartService:
   - CRUD operations
   - search, findByCategory

8. InventoryService:
   - checkAvailability(sparePartId, locationId, quantity) -> boolean
   - reserveStock(inventoryId, quantity, workOrderId)
   - releaseReservedStock(workOrderId)
   - consumeStock(inventoryId, quantity, workOrderId)
   - restock(inventoryId, quantity, notes)
   - transferStock(sparePartId, fromLocationId, toLocationId, quantity)
   - getLowStockAlerts() -> List<Inventory>

9. WorkOrderService (CORE BUSINESS LOGIC):
   - create(WorkOrderCreateRequest) -> status: REQUESTED
   - createEstimation(id, items, estimatedCost) -> status: ESTIMATED
   - approve(id, notes, username) -> status: APPROVED, reserve stock
   - reject(id, reason) -> status: REJECTED
   - start(id, mechanicId) -> status: IN_PROGRESS
   - complete(id, actualCost, items) -> status: COMPLETED, consume stock, auto-generate invoice
   - cancel(id, reason) -> status: CANCELLED, release stock
   - assignMechanic(id, mechanicId)
   - updateStatus(id, status)

10. InvoiceService:
    - generateFromWorkOrder(workOrderId) -> create invoice
    - sendInvoice(invoiceId) -> status: SENT, send email
    - recordPayment(invoiceId, PaymentRequest)
    - checkOverdueInvoices() -> scheduled task

11. PaymentService:
    - create payment, update invoice status

12. NotificationService:
    - createNotification(userId, type, title, message, referenceType, referenceId)
    - markAsRead(id)
    - getUserNotifications(userId)

13. DashboardService:
    - getStatistics() -> counts, charts data
```

---

## Step 8: Generate DTOs

```
@Cursor: Generate DTOs di package com.fms.dto:

auth/LoginRequest.java:
- String username
- String password

auth/LoginResponse.java:
- String token
- String type = "Bearer"
- String username
- String fullName
- Set<String> roles

workorder/WorkOrderDTO.java:
- All work order fields untuk display
- VehicleDTO vehicle
- ClientDTO client

workorder/WorkOrderCreateRequest.java:
- Long vehicleId
- String serviceType
- String executionType
- String requestDescription
- Boolean isUrgent
- @Valid annotations

inventory/LowStockAlert.java:
- SparePart sparePart
- InventoryLocation location
- Integer currentQuantity
- Integer reorderPoint
```

---

## Step 9: Generate Security Configuration

```
@Cursor: Generate security classes di package com.fms.config:

1. SecurityConfig.java:
   - @Configuration @EnableWebSecurity @EnableMethodSecurity
   - SecurityFilterChain for web (form login)
   - SecurityFilterChain for API (JWT)
   - PasswordEncoder bean (BCrypt)
   - AuthenticationManager bean

2. com.fms.security.CustomUserDetailsService:
   - implements UserDetailsService
   - loadUserByUsername dari UserRepository

3. com.fms.security.JwtTokenProvider:
   - generateToken(Authentication) -> String
   - getUsernameFromToken(String) -> String
   - validateToken(String) -> boolean
   - Use io.jsonwebtoken

4. com.fms.security.JwtAuthenticationFilter:
   - extends OncePerRequestFilter
   - Extract JWT from header
   - Validate and set authentication

5. com.fms.security.SecurityUtils:
   - getCurrentUsername() -> String
   - getCurrentUser() -> User
```

---

## Step 10: Generate Web Controllers (Thymeleaf)

```
@Cursor: Generate controllers di package com.fms.controller.web:

Pattern untuk semua:
- @Controller @RequestMapping
- Inject services via constructor
- Return view names (String)
- Use Model untuk pass data
- Use RedirectAttributes untuk flash messages
- Use @Valid untuk form validation
- Handle pagination dengan Pageable

1. DashboardController (@GetMapping "/"):
   - Show statistics (total clients, vehicles, active work orders, pending invoices)
   - Show recent work orders
   - Show low stock alerts

2. WorkOrderController (@RequestMapping "/workorders"):
   - list(status, search, page, Model) -> "workorder/list"
   - showCreateForm(Model) -> "workorder/create"
   - create(@Valid request, BindingResult) -> redirect
   - detail(id, Model) -> "workorder/detail"
   - showEstimationForm(id, Model) -> "workorder/estimation"
   - createEstimation(id, request) -> redirect
   - approve(id, notes) -> redirect
   - reject(id, reason) -> redirect
   - start(id, mechanicId) -> redirect
   - showCompleteForm(id, Model) -> "workorder/complete"
   - complete(id, request) -> redirect
   - cancel(id, reason) -> redirect

3. VehicleController (@RequestMapping "/vehicles"):
   - list, create, detail, update, delete
   - maintenanceHistory(id) -> "vehicle/maintenance-history"

4. WorkshopController (@RequestMapping "/workshops"):
   - list, create, detail, update, delete

5. InventoryController (@RequestMapping "/inventory"):
   - list(search, locationId, lowStock, Model)
   - showStockMovement() -> "inventory/stock-movement"
   - recordMovement(request)
   - lowStockAlerts() -> "inventory/reorder-alert"

6. InvoiceController (@RequestMapping "/invoices"):
   - list(status, clientId, Model)
   - detail(id, Model)
   - send(id)
   - showPaymentForm(id, Model) -> "invoice/payment"
   - recordPayment(id, request)

7. ClientController (@RequestMapping "/clients"):
   - list, create, detail, update

8. MechanicController (@RequestMapping "/mechanics"):
   - list, create, detail, update

9. ReportController (@RequestMapping "/reports"):
   - index() -> "report/index"
   - workOrderReport(startDate, endDate, Model)
   - inventoryReport(Model)
   - financialReport(startDate, endDate, Model)
   - exportPdf(reportType) -> ResponseEntity<byte[]>
   - exportExcel(reportType) -> ResponseEntity<byte[]>

10. AuthController (@RequestMapping "/auth"):
    - showLoginForm() -> "auth/login"
    - login(handled by Spring Security)
    - logout(handled by Spring Security)
```

---

## Step 11: Generate REST API Controllers (Mobile)

```
@Cursor: Generate API controllers di package com.fms.controller.api:

Pattern:
- @RestController @RequestMapping("/api/mobile")
- Return DTOs atau ResponseEntity
- Use @Valid
- JWT authentication required (except login)

1. MobileAuthController:
   - @PostMapping("/auth/login")
   - login(@Valid LoginRequest) -> ResponseEntity<LoginResponse>

2. MobileWorkOrderController:
   - @GetMapping("/workorders")
   - list(status, Pageable) -> Page<WorkOrderDTO>
   - @GetMapping("/workorders/{id}")
   - detail(id) -> WorkOrderDTO
   - @PostMapping("/workorders/{id}/start")
   - start(id, request) -> WorkOrderDTO
   - @PostMapping("/workorders/{id}/complete")
   - complete(id, request) -> WorkOrderDTO
   - @PostMapping("/workorders/{id}/upload-photo")
   - uploadPhoto(id, MultipartFile photo, String documentType) -> WorkOrderDocument

3. MobileInventoryController:
   - @GetMapping("/inventory")
   - list(search, locationId) -> List<InventoryDTO>
   - @GetMapping("/inventory/{id}")
   - detail(id) -> InventoryDTO
   - @PostMapping("/inventory/check-availability")
   - checkAvailability(request) -> Map<String, Object>

4. MobileProfileController:
   - @GetMapping("/profile")
   - getProfile() -> UserDTO with Mechanic info
   - @GetMapping("/notifications")
   - getNotifications(Pageable) -> Page<NotificationDTO>
```

---

## Step 12: Generate Exception Handlers

```
@Cursor: Generate exception classes di package com.fms.exception:

1. ResourceNotFoundException extends RuntimeException

2. BusinessException extends RuntimeException

3. UnauthorizedException extends RuntimeException

4. ValidationException extends RuntimeException

5. GlobalExceptionHandler (@ControllerAdvice):
   - handleResourceNotFound() -> ErrorResponse atau view "error/404"
   - handleBusinessException() -> ErrorResponse atau flash message
   - handleValidationException() -> ErrorResponse
   - handleUnauthorized() -> 401
   - handleGenericException() -> 500
```

---

## Step 13: Generate Thymeleaf Templates

```
@Cursor: Generate templates di src/main/resources/templates:

1. layout/main.html:
   - Master layout dengan Thymeleaf Layout Dialect
   - <!DOCTYPE html> <html xmlns:th xmlns:layout xmlns:sec>
   - <head> dengan Bootstrap 5, Font Awesome
   - <body> dengan header, sidebar, content area, footer
   - layout:fragment="content"

2. layout/header.html:
   - Top navbar
   - User info, logout button
   - Breadcrumb

3. layout/sidebar.html:
   - Left navigation menu
   - Dashboard, Work Orders, Vehicles, Workshops, Inventory, Invoices, Clients, Mechanics, Reports
   - sec:authorize untuk role-based menu

4. dashboard/index.html:
   - layout:decorate="~{layout/main}"
   - Statistics cards (Bootstrap cards)
   - Recent work orders table
   - Low stock alerts

5. workorder/list.html:
   - Filter form (status, search)
   - Work orders table dengan pagination
   - Status badges dengan warna
   - Action buttons

6. workorder/create.html:
   - Form dengan vehicle select, service type, description
   - th:field binding
   - th:errors untuk validation

7. workorder/detail.html:
   - Work order info
   - Items table
   - Status timeline
   - Approval/Rejection buttons jika status = ESTIMATED
   - Complete button jika status = IN_PROGRESS

8. inventory/list.html:
   - Filter by location, low stock
   - Stock table dengan available quantity
   - Color coding untuk low stock (text-danger jika <= reorderPoint)

9. invoice/list.html:
   - Filter by status, client
   - Invoice table dengan due date, amount, balance
   - Status badges
   - Payment button untuk unpaid

10. auth/login.html:
    - Login form (username, password)
    - No layout decorator (standalone page)
```

---

## Step 14: Generate Utility Classes

```
@Cursor: Generate utils di package com.fms.util:

1. DateUtils.java:
   - formatDate(LocalDateTime) -> String
   - parseDate(String) -> LocalDate
   - daysBetween(LocalDate, LocalDate) -> long

2. FileUploadUtil.java:
   - saveFile(String uploadDir, String fileName, MultipartFile file)
   - deleteFile(String filePath)
   - generateUniqueFileName(String originalFilename) -> String

3. PdfGenerator.java (using iText7):
   - generateWorkOrderPdf(WorkOrder) -> byte[]
   - generateInvoicePdf(Invoice) -> byte[]

4. ExcelExporter.java (using Apache POI):
   - exportWorkOrders(List<WorkOrder>) -> byte[]
   - exportInventory(List<Inventory>) -> byte[]
```

---

## Step 15: Generate Main Application Class

```
@Cursor: Generate FleetMaintenanceApplication.java:

package com.fms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class FleetMaintenanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FleetMaintenanceApplication.class, args);
    }
}
```

---

## Step 16: Generate Static Resources

```
@Cursor: Create di src/main/resources/static:

1. css/style.css:
   - Custom styles
   - Sidebar layout
   - Card styles
   - Badge colors by status
   - Table styles

2. js/app.js:
   - Initialize Bootstrap tooltips/popovers
   - Form validation enhancements
   - Auto-dismiss alerts after 5s
   - Confirmation dialogs for delete

3. Create directory: img/
   - Placeholder untuk logo, default vehicle images
```

---

## Step 17: Generate Tests (Optional)

```
@Cursor: Generate test classes di src/test/java/com/fms:

1. WorkOrderServiceTest:
   - testCreateWorkOrder()
   - testApproveWorkOrder()
   - testCompleteWorkOrder()
   - Use @SpringBootTest atau @DataJpaTest

2. InventoryServiceTest:
   - testReserveStock()
   - testConsumeStock()
   - testTransferStock()

3. WorkOrderControllerTest:
   - testListWorkOrders()
   - testCreateWorkOrder()
   - Use @WebMvcTest atau MockMvc
```

---

## Final Checklist

Generate semua files sesuai urutan di atas, lalu:

1. ✅ DDL migration script (V1__initial_schema.sql)
2. ✅ DML sample data (V2__sample_data.sql)
3. ✅ pom.xml dengan semua dependencies
4. ✅ application.properties
5. ✅ 10 Enums
6. ✅ 19 Entity classes
7. ✅ 15+ Repository interfaces
8. ✅ 13+ Service classes
9. ✅ DTOs untuk request/response
10. ✅ Security configuration (JWT + Form Login)
11. ✅ 10 Web Controllers (Thymeleaf)
12. ✅ 4 REST API Controllers (Mobile)
13. ✅ Exception handlers
14. ✅ 10+ Thymeleaf templates
15. ✅ Utility classes (PDF, Excel, FileUpload)
16. ✅ Main application class
17. ✅ Static resources (CSS, JS)
18. ✅ README.md

---

## How to Use with Cursor AI

### Method 1: Sequential Generation
```
1. Open PROJECT_SPECIFICATION.md
2. Copy instruksi untuk Step 1 (DDL)
3. Paste ke Cursor Chat: "Generate this SQL DDL file"
4. Review, adjust if needed
5. Repeat untuk Step 2-17
```

### Method 2: Bulk Generation
```
1. Copy entire CURSOR_INSTRUCTIONS.md
2. Paste ke Cursor Chat
3. Say: "Generate complete Spring Boot project following all steps"
4. Cursor will generate files iteratively
5. Review each generated file
```

### Method 3: Feature-by-Feature
```
1. Generate Database first (Step 1-2)
2. Test database with PostgreSQL
3. Generate Entities + Repositories (Step 4-6)
4. Generate Services (Step 7)
5. Generate Controllers (Step 10-11)
6. Generate Templates (Step 13)
7. Generate remaining files
```

---

## Post-Generation Steps

After Cursor generates all files:

1. **Setup Database:**
   ```sql
   CREATE DATABASE fleet_maintenance_db;
   ```

2. **Build Project:**
   ```bash
   mvn clean install
   ```

3. **Run Application:**
   ```bash
   mvn spring-boot:run
   ```

4. **Access:**
   - Web: http://localhost:8080
   - Login: admin/password123

5. **Test API:**
   ```bash
   curl -X POST http://localhost:8080/api/mobile/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"mechanic1","password":"password123"}'
   ```

---

## Tips for Cursor AI

1. **Be Specific:** Sebutkan package names, class names, annotations
2. **One Step at Time:** Generate DDL dulu, test, baru entities
3. **Review Generated Code:** Cursor kadang skip validations atau relationships
4. **Adjust if Needed:** Edit generated code untuk business rules spesifik
5. **Test Incrementally:** Test setiap layer sebelum lanjut ke layer berikutnya

---

## Common Issues & Solutions

### Issue: Flyway validation failed
**Solution:** Drop database dan recreate, atau set `spring.flyway.baseline-on-migrate=true`

### Issue: BCrypt password not working
**Solution:** Ensure SecurityConfig has PasswordEncoder bean

### Issue: JWT not validating
**Solution:** Check jwt.secret length (min 256 bits) dan expiration time

### Issue: Thymeleaf template not found
**Solution:** Check template path dan layout:decorate path

### Issue: 403 Forbidden on API
**Solution:** Disable CSRF for /api/mobile/** in SecurityConfig

---

**Generated for Cursor AI Usage**
**Project: Fleet Maintenance System**
**Version: 1.0.0**
