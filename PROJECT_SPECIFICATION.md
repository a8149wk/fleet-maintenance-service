# Fleet Maintenance System - Complete Project Specification

## Project Overview

Aplikasi Fleet Maintenance Service berbasis Spring Boot + Thymeleaf untuk manajemen pemeliharaan kendaraan fleet. Sistem ini menangani work order, inventory spare parts, bengkel rekanan, invoicing, dan mobile API.

---

## Technology Stack

### Backend
- **Java**: 17
- **Spring Boot**: 3.2.5
- **Spring Security**: 6.x (with JWT for API)
- **Spring Data JPA**: dengan Hibernate
- **Database**: PostgreSQL 14+
- **Migration**: Flyway
- **Build Tool**: Maven

### Frontend Web
- **Template Engine**: Thymeleaf
- **CSS Framework**: Bootstrap 5.3
- **Icons**: Font Awesome 6.4
- **JavaScript**: jQuery 3.7

### Mobile API
- **Authentication**: JWT Token
- **Format**: REST API (JSON)

### Additional Libraries
- **Lombok**: Code generation
- **MapStruct**: DTO mapping
- **iText7**: PDF generation
- **Apache POI**: Excel export
- **Commons IO**: File handling

---

## Database Schema

### ERD Summary

**Core Tables:**
- `users` - User accounts
- `roles` - User roles (ADMIN, MANAGER, CLIENT, MECHANIC, FINANCE)
- `user_roles` - Many-to-many relationship
- `clients` - Corporate clients (rental companies, logistics)
- `vehicles` - Fleet vehicles
- `workshops` - Service workshops (INTERNAL, PARTNER, CLIENT_RECOMMENDED)
- `mechanics` - Technicians
- `spare_parts` - Parts catalog
- `inventory_locations` - Warehouse/workshop locations
- `inventory` - Multi-location stock
- `work_orders` - Maintenance work orders
- `work_order_items` - Parts & labor items
- `work_order_documents` - Photos, documents
- `invoices` - Billing
- `payments` - Payment records
- `maintenance_schedules` - Scheduled maintenance
- `notifications` - User notifications
- `audit_logs` - Audit trail
- `system_settings` - App configuration

### Key Relationships

```
clients (1) ---> (*) vehicles
vehicles (1) ---> (*) work_orders
work_orders (1) ---> (*) work_order_items
work_orders (1) ---> (1) invoices
invoices (1) ---> (*) payments
workshops (1) ---> (*) mechanics
spare_parts (1) ---> (*) inventory
inventory_locations (1) ---> (*) inventory
```

---

## DDL - Database Schema

### File: `src/main/resources/db/migration/V1__initial_schema.sql`

**Key Features:**
- All tables dengan proper indexes
- Foreign key constraints
- Triggers untuk `updated_at` columns
- Sequences untuk auto-numbering (WO, Invoice, Payment)
- Generated columns (available_quantity = quantity - reserved_quantity)
- Audit fields (created_at, updated_at, created_by)

**Tables to Create:**

1. **users** - id, username, email, password (BCrypt), full_name, phone, is_active, is_locked, failed_login_attempts, last_login_at, created_at, updated_at

2. **roles** - id, name (ROLE_ADMIN, ROLE_MANAGER, ROLE_CLIENT, ROLE_MECHANIC, ROLE_FINANCE), description, created_at

3. **user_roles** - user_id, role_id (composite PK)

4. **clients** - id, code (unique), name, business_type, npwp, address, city, province, postal_code, phone, email, contact_person, contact_phone, payment_term (days), credit_limit, is_active, notes, created_at, updated_at, created_by

5. **vehicles** - id, client_id, license_plate (unique), brand, model, year, color, vin (unique), engine_number (unique), fuel_type (enum), transmission_type (enum), current_odometer, purchase_date, registration_expiry, insurance_expiry, is_active, notes, created_at, updated_at

6. **workshops** - id, code (unique), name, type (enum: INTERNAL/PARTNER/CLIENT_RECOMMENDED), address, city, province, postal_code, latitude, longitude, phone, email, contact_person, specialization, operating_hours, service_capacity, rating, total_reviews, is_active, notes, created_at, updated_at

7. **mechanics** - id, code (unique), full_name, email, phone, specialization (ENGINE/ELECTRICAL/AC/TRANSMISSION/GENERAL), certification, experience_years, workshop_id, is_active, rating, total_jobs, notes, created_at, updated_at

8. **spare_parts** - id, part_number (unique), name, description, category, brand, unit, unit_cost, selling_price, min_stock_level, reorder_point, is_active, notes, created_at, updated_at

9. **inventory_locations** - id, code (unique), name, type (OWN_WAREHOUSE/PARTNER_WORKSHOP/CENTRAL_WAREHOUSE), workshop_id, address, city, is_active, created_at

10. **inventory** - id, spare_part_id, location_id, quantity, reserved_quantity, available_quantity (computed), min_stock_level, max_stock_level, reorder_point, unit_cost, total_value (computed), batch_number, serial_number, last_restock_date, expiry_date, is_available, notes, created_at, updated_at

11. **inventory_movements** - id, inventory_id, movement_type (IN/OUT/TRANSFER/ADJUSTMENT), quantity, reference_type, reference_id, from_location_id, to_location_id, unit_cost, notes, movement_date, created_by

12. **work_orders** - id, work_order_number (unique), vehicle_id, client_id, workshop_id, assigned_mechanic_id, status (enum), execution_type (enum), service_type (enum), request_description, diagnosis, current_odometer, estimated_cost, actual_cost, labor_cost, parts_cost, discount_amount, tax_amount, total_amount, requested_date, estimated_date, approved_date, started_date, completed_date, is_urgent, is_warranty, warranty_reference, approved_by, approval_notes, rejection_reason, customer_rating, customer_feedback, created_at, updated_at, created_by

13. **work_order_items** - id, work_order_id, item_type (PART/LABOR), spare_part_id, service_name, description, quantity, unit_price, discount_percent, discount_amount, subtotal, notes, created_at

14. **work_order_documents** - id, work_order_id, document_type (PHOTO_BEFORE/PHOTO_AFTER/ESTIMATION/INVOICE/OTHER), file_name, file_path, file_size, mime_type, description, uploaded_at, uploaded_by

15. **invoices** - id, invoice_number (unique), work_order_id, client_id, invoice_date, due_date, subtotal, discount_amount, tax_amount, total_amount, paid_amount, balance, status (enum), payment_method, payment_date, payment_reference, notes, created_at, updated_at, created_by

16. **payments** - id, payment_number (unique), invoice_id, client_id, payment_date, payment_method, amount, reference_number, bank_name, account_number, notes, created_at, created_by

17. **maintenance_schedules** - id, vehicle_id, schedule_type (PERIODIC/MILEAGE_BASED), service_name, description, interval_type (DAYS/MONTHS/KILOMETERS), interval_value, last_service_date, last_service_odometer, next_service_date, next_service_odometer, is_active, reminder_sent, created_at

18. **notifications** - id, user_id, notification_type, title, message, reference_type, reference_id, is_read, read_at, created_at

19. **audit_logs** - id, user_id, action, entity_type, entity_id, old_value (JSONB), new_value (JSONB), ip_address, user_agent, created_at

20. **system_settings** - id, setting_key (unique), setting_value, setting_type, description, is_public, updated_at, updated_by

**Sequences:**
```sql
CREATE SEQUENCE seq_work_order_number START 1;
CREATE SEQUENCE seq_invoice_number START 1;
CREATE SEQUENCE seq_payment_number START 1;
CREATE SEQUENCE seq_client_code START 1;
CREATE SEQUENCE seq_workshop_code START 1;
CREATE SEQUENCE seq_mechanic_code START 1;
```

**Triggers:**
- Auto-update `updated_at` pada semua tabel yang memiliki kolom tersebut

**Indexes:**
- Primary keys
- Foreign keys
- Unique constraints (username, email, license_plate, work_order_number, invoice_number, dll)
- Search indexes (client name, vehicle plate, work order number, status, dates)

---

## DML - Sample Data

### File: `src/main/resources/db/migration/V2__sample_data.sql`

**Sample Data:**

1. **Roles:**
   - ROLE_ADMIN, ROLE_MANAGER, ROLE_CLIENT, ROLE_MECHANIC, ROLE_FINANCE

2. **Users (password: `password123`):**
   - admin/admin@fms.com (ADMIN)
   - manager/manager@fms.com (MANAGER)
   - finance/finance@fms.com (FINANCE)
   - client1/client1@rental.com (CLIENT)
   - mechanic1/mechanic1@fms.com (MECHANIC)

3. **Clients (5 companies):**
   - CLI001: PT Takari Kokoh Sejahtera
   - CLI002: PT Transport Solution Mandiri (TSM)
   - CLI003: Orix Rental Corporation
   - CLI004: Prima Rent
   - CLI005: PT Charoen Pokphand Indonesia

4. **Workshops (5 locations):**
   - WS001: Central Workshop (INTERNAL)
   - WS002: Jakarta Branch Workshop (INTERNAL)
   - WS003: Auto Prima Service (PARTNER)
   - WS004: Bengkel Jaya Motor (PARTNER)
   - WS005: Toyota Auto2000 (CLIENT_RECOMMENDED)

5. **Mechanics (5 technicians):**
   - MEC001-005 dengan berbagai spesialisasi

6. **Vehicles (10 units):**
   - Mix dari Toyota, Honda, Mitsubishi, Daihatsu, Suzuki, Mazda, Isuzu

7. **Spare Parts (12 items):**
   - Oli, filter, brake pad, battery, spark plug, timing belt, AC freon, dll

8. **Inventory Locations (3):**
   - Central Warehouse, Jakarta Workshop, Partner Stock

9. **Inventory:**
   - Stock di 3 lokasi dengan berbagai parts

10. **Work Orders (8 sample):**
    - Mix status: COMPLETED, IN_PROGRESS, APPROVED, ESTIMATED, REQUESTED

11. **Work Order Items:**
    - Parts & labor untuk completed work orders

12. **Invoices (4):**
    - Mix status: PAID, SENT

13. **Payments (2):**
    - Payment untuk invoice yang sudah paid

14. **Maintenance Schedules (3):**
    - Scheduled maintenance untuk beberapa kendaraan

15. **System Settings:**
    - Company info, tax rate, default payment terms

16. **Notifications (4 sample)**

---

## Java Package Structure

```
com.fms/
├── FleetMaintenanceApplication.java          # Main class
│
├── config/
│   ├── SecurityConfig.java                   # Spring Security + JWT
│   ├── WebMvcConfig.java                     # MVC configuration
│   ├── JpaConfig.java                        # JPA auditing
│   └── FileUploadConfig.java                 # File upload settings
│
├── entity/
│   ├── User.java
│   ├── Role.java
│   ├── Client.java
│   ├── Vehicle.java
│   ├── Workshop.java
│   ├── Mechanic.java
│   ├── SparePart.java
│   ├── Inventory.java
│   ├── InventoryLocation.java
│   ├── InventoryMovement.java
│   ├── WorkOrder.java
│   ├── WorkOrderItem.java
│   ├── WorkOrderDocument.java
│   ├── Invoice.java
│   ├── Payment.java
│   ├── MaintenanceSchedule.java
│   ├── Notification.java
│   ├── AuditLog.java
│   └── SystemSetting.java
│
├── enums/
│   ├── WorkOrderStatus.java                  # REQUESTED, ESTIMATED, APPROVED, etc
│   ├── ExecutionType.java                    # INTERNAL, PARTNER_WORKSHOP, CLIENT_RECOMMENDED
│   ├── ServiceType.java                      # ENGINE, ELECTRICAL, AC, TRANSMISSION, etc
│   ├── FuelType.java                         # GASOLINE, DIESEL, ELECTRIC, HYBRID
│   ├── TransmissionType.java                 # MANUAL, AUTOMATIC, CVT
│   ├── WorkshopType.java                     # INTERNAL, PARTNER, CLIENT_RECOMMENDED
│   ├── InvoiceStatus.java                    # DRAFT, SENT, PAID, OVERDUE, etc
│   ├── PaymentMethod.java                    # CASH, TRANSFER, CREDIT_CARD, CHECK
│   ├── MovementType.java                     # IN, OUT, TRANSFER, ADJUSTMENT
│   └── ItemType.java                         # PART, LABOR
│
├── repository/
│   ├── UserRepository.java                   # extends JpaRepository<User, Long>
│   ├── RoleRepository.java
│   ├── ClientRepository.java
│   ├── VehicleRepository.java
│   ├── WorkshopRepository.java
│   ├── MechanicRepository.java
│   ├── SparePartRepository.java
│   ├── InventoryRepository.java
│   ├── InventoryLocationRepository.java
│   ├── InventoryMovementRepository.java
│   ├── WorkOrderRepository.java
│   ├── WorkOrderItemRepository.java
│   ├── InvoiceRepository.java
│   ├── PaymentRepository.java
│   ├── MaintenanceScheduleRepository.java
│   ├── NotificationRepository.java
│   └── SystemSettingRepository.java
│
├── service/
│   ├── UserService.java
│   ├── AuthService.java                      # Login, JWT generation
│   ├── ClientService.java
│   ├── VehicleService.java
│   ├── WorkshopService.java
│   ├── MechanicService.java
│   ├── SparePartService.java
│   ├── InventoryService.java
│   ├── WorkOrderService.java                 # Core business logic
│   ├── InvoiceService.java
│   ├── PaymentService.java
│   ├── NotificationService.java              # Email/WhatsApp (optional)
│   ├── ReportService.java                    # PDF/Excel generation
│   └── DashboardService.java                 # Statistics
│
├── dto/
│   ├── auth/
│   │   ├── LoginRequest.java
│   │   ├── LoginResponse.java
│   │   └── JwtResponse.java
│   ├── workorder/
│   │   ├── WorkOrderDTO.java
│   │   ├── WorkOrderCreateRequest.java
│   │   ├── WorkOrderItemDTO.java
│   │   └── WorkOrderSummary.java
│   ├── vehicle/
│   │   ├── VehicleDTO.java
│   │   └── VehicleCreateRequest.java
│   ├── inventory/
│   │   ├── InventoryDTO.java
│   │   ├── StockSummary.java
│   │   └── LowStockAlert.java
│   └── invoice/
│       ├── InvoiceDTO.java
│       └── PaymentRequest.java
│
├── controller/
│   ├── web/                                  # Thymeleaf Controllers
│   │   ├── DashboardController.java          # @GetMapping("/")
│   │   ├── WorkOrderController.java          # @RequestMapping("/workorders")
│   │   ├── VehicleController.java            # @RequestMapping("/vehicles")
│   │   ├── WorkshopController.java           # @RequestMapping("/workshops")
│   │   ├── InventoryController.java          # @RequestMapping("/inventory")
│   │   ├── InvoiceController.java            # @RequestMapping("/invoices")
│   │   ├── ClientController.java             # @RequestMapping("/clients")
│   │   ├── MechanicController.java           # @RequestMapping("/mechanics")
│   │   ├── ReportController.java             # @RequestMapping("/reports")
│   │   └── AuthController.java               # @RequestMapping("/auth")
│   │
│   └── api/                                  # REST API for Mobile
│       ├── MobileAuthController.java         # @PostMapping("/api/mobile/auth/login")
│       ├── MobileWorkOrderController.java    # @GetMapping("/api/mobile/workorders")
│       ├── MobileInventoryController.java    # @GetMapping("/api/mobile/inventory")
│       └── MobileProfileController.java      # @GetMapping("/api/mobile/profile")
│
├── security/
│   ├── CustomUserDetailsService.java
│   ├── JwtTokenProvider.java                 # Generate/validate JWT
│   ├── JwtAuthenticationFilter.java          # Filter untuk API
│   └── SecurityUtils.java
│
├── exception/
│   ├── GlobalExceptionHandler.java           # @ControllerAdvice
│   ├── ResourceNotFoundException.java
│   ├── BusinessException.java
│   ├── UnauthorizedException.java
│   └── ValidationException.java
│
└── util/
    ├── DateUtils.java
    ├── NumberUtils.java
    ├── FileUploadUtil.java
    ├── PdfGenerator.java                     # iText7
    ├── ExcelExporter.java                    # Apache POI
    └── QRCodeGenerator.java                  # Optional
```

---

## Entity Specifications

### Common Fields Pattern
```java
@CreationTimestamp
@Column(name = "created_at", updatable = false)
private LocalDateTime createdAt;

@UpdateTimestamp
@Column(name = "updated_at")
private LocalDateTime updatedAt;
```

### Sample Entity Structure

#### User.java
```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true, length = 50)
    private String username;
    
    @Column(nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password; // BCrypt
    
    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;
    
    private String phone;
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "is_locked")
    private Boolean isLocked = false;
    
    // ... timestamps
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

#### WorkOrder.java
```java
@Entity
@Table(name = "work_orders")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "work_order_number", nullable = false, unique = true)
    private String workOrderNumber; // WO-2024-001
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workshop_id")
    private Workshop workshop;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_mechanic_id")
    private Mechanic assignedMechanic;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "execution_type", nullable = false)
    private ExecutionType executionType;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false)
    private ServiceType serviceType;
    
    @Column(name = "request_description", nullable = false, columnDefinition = "TEXT")
    private String requestDescription;
    
    @Column(columnDefinition = "TEXT")
    private String diagnosis;
    
    @Column(name = "current_odometer")
    private Integer currentOdometer;
    
    @Column(name = "estimated_cost", precision = 15, scale = 2)
    private BigDecimal estimatedCost;
    
    @Column(name = "actual_cost", precision = 15, scale = 2)
    private BigDecimal actualCost;
    
    @Column(name = "labor_cost", precision = 15, scale = 2)
    private BigDecimal laborCost;
    
    @Column(name = "parts_cost", precision = 15, scale = 2)
    private BigDecimal partsCost;
    
    // ... more fields
    
    @OneToMany(mappedBy = "workOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkOrderItem> items = new ArrayList<>();
}
```

---

## Repository Custom Queries

### WorkOrderRepository.java
```java
public interface WorkOrderRepository extends JpaRepository<WorkOrder, Long> {
    
    Page<WorkOrder> findByStatus(WorkOrderStatus status, Pageable pageable);
    
    @Query("SELECT w FROM WorkOrder w WHERE " +
           "(:status IS NULL OR w.status = :status) AND " +
           "(:search IS NULL OR LOWER(w.workOrderNumber) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(w.vehicle.licensePlate) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(w.client.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<WorkOrder> searchWorkOrders(
        @Param("status") WorkOrderStatus status,
        @Param("search") String search,
        Pageable pageable
    );
    
    List<WorkOrder> findByClientIdAndStatusIn(Long clientId, List<WorkOrderStatus> statuses);
    
    List<WorkOrder> findByAssignedMechanicIdAndStatus(Long mechanicId, WorkOrderStatus status);
    
    @Query("SELECT COUNT(w) FROM WorkOrder w WHERE w.status = :status")
    Long countByStatus(@Param("status") WorkOrderStatus status);
    
    @Query("SELECT w FROM WorkOrder w WHERE w.requestedDate BETWEEN :startDate AND :endDate")
    List<WorkOrder> findByDateRange(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
```

### InventoryRepository.java
```java
public interface InventoryRepository extends JpaRepository<Inventory, Long> {
    
    List<Inventory> findBySparePartId(Long sparePartId);
    
    List<Inventory> findByLocationId(Long locationId);
    
    Optional<Inventory> findBySparePartIdAndLocationId(Long sparePartId, Long locationId);
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.reorderPoint")
    List<Inventory> findLowStockItems();
    
    @Query("SELECT i FROM Inventory i WHERE i.quantity - i.reservedQuantity < :threshold")
    List<Inventory> findLowAvailableStock(@Param("threshold") Integer threshold);
}
```

---

## Service Layer Business Logic

### WorkOrderService.java - Key Methods

```java
@Service
@Transactional
public class WorkOrderService {
    
    // Create new work order
    public WorkOrder create(WorkOrderCreateRequest request, String username) {
        // Generate WO number: WO-2024-001
        // Set status: REQUESTED
        // Assign workshop based on location (optional)
        // Save and return
    }
    
    // Create estimation
    public WorkOrder createEstimation(Long id, EstimationRequest request) {
        // Add estimated cost
        // Add work order items
        // Change status to ESTIMATED
        // Send notification to client
    }
    
    // Approve work order
    public WorkOrder approve(Long id, String notes, String username) {
        // Validate status is ESTIMATED
        // Change status to APPROVED
        // Record approvedBy and approvedDate
        // Reserve inventory if needed
        // Send notification to workshop/mechanic
    }
    
    // Reject work order
    public WorkOrder reject(Long id, String reason) {
        // Change status to REJECTED
        // Record rejection reason
        // Send notification
    }
    
    // Start work
    public WorkOrder start(Long id, Long mechanicId) {
        // Validate status is APPROVED
        // Assign mechanic
        // Change status to IN_PROGRESS
        // Record startedDate
    }
    
    // Complete work order
    public WorkOrder complete(Long id, CompletionRequest request) {
        // Validate status is IN_PROGRESS
        // Update actual costs
        // Update work order items (actual parts used)
        // Deduct inventory
        // Change status to COMPLETED
        // Record completedDate
        // Auto-generate invoice (optional)
        // Update vehicle odometer
    }
    
    // Cancel work order
    public WorkOrder cancel(Long id, String reason) {
        // Release reserved inventory
        // Change status to CANCELLED
        // Send notification
    }
}
```

### InventoryService.java - Key Methods

```java
@Service
@Transactional
public class InventoryService {
    
    // Check stock availability
    public boolean checkAvailability(Long sparePartId, Long locationId, Integer quantity) {
        // Query inventory
        // Check available_quantity >= requested quantity
    }
    
    // Reserve stock for work order
    public void reserveStock(Long inventoryId, Integer quantity) {
        // Increment reserved_quantity
        // Create inventory movement (RESERVED)
    }
    
    // Release reserved stock
    public void releaseReservedStock(Long workOrderId) {
        // Find all reserved items for WO
        // Decrement reserved_quantity
        // Create inventory movement (RELEASED)
    }
    
    // Consume stock (actual usage)
    public void consumeStock(Long inventoryId, Integer quantity, Long workOrderId) {
        // Decrement quantity
        // Decrement reserved_quantity
        // Create inventory movement (OUT, reference: work_order)
    }
    
    // Restock
    public void restock(Long inventoryId, Integer quantity, String notes) {
        // Increment quantity
        // Update lastRestockDate
        // Create inventory movement (IN)
    }
    
    // Transfer stock between locations
    public void transferStock(Long sparePartId, Long fromLocationId, Long toLocationId, Integer quantity) {
        // Deduct from source
        // Add to destination
        // Create inventory movements (OUT + IN with TRANSFER type)
    }
    
    // Get low stock alerts
    public List<LowStockAlert> getLowStockAlerts() {
        // Query items where available_quantity <= reorder_point
    }
}
```

### InvoiceService.java - Key Methods

```java
@Service
@Transactional
public class InvoiceService {
    
    // Generate invoice from work order
    public Invoice generateFromWorkOrder(Long workOrderId) {
        // Get completed work order
        // Calculate subtotal from work_order_items
        // Apply tax (from system settings)
        // Generate invoice_number: INV-2024-001
        // Set due_date = invoice_date + client.payment_term
        // Set status: DRAFT
        // Update work order status to BILLED
    }
    
    // Send invoice to client
    public void sendInvoice(Long invoiceId) {
        // Change status to SENT
        // Send email with PDF attachment
        // Create notification
    }
    
    // Record payment
    public void recordPayment(Long invoiceId, PaymentRequest request) {
        // Create payment record
        // Update invoice.paid_amount
        // Calculate balance
        // Update status (PARTIAL_PAID or PAID)
        // Send receipt email
    }
    
    // Check overdue invoices
    @Scheduled(cron = "0 0 1 * * *") // Daily at 1 AM
    public void checkOverdueInvoices() {
        // Find invoices where due_date < today AND status = SENT
        // Update status to OVERDUE
        // Send reminder notifications
    }
}
```

---

## Security Configuration

### SecurityConfig.java

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/css/**", "/js/**", "/img/**").permitAll()
                .requestMatchers("/api/mobile/**").permitAll() // JWT handles this
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/workorders/**").hasAnyRole("ADMIN", "MANAGER", "CLIENT")
                .requestMatchers("/invoices/**").hasAnyRole("ADMIN", "MANAGER", "FINANCE")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout")
                .permitAll()
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/mobile/**") // Disable for API
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### JWT Configuration for Mobile API

```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(Authentication authentication) {
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(SignatureAlgorithm.HS512, jwtSecret)
            .compact();
    }
    
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
            .setSigningKey(jwtSecret)
            .parseClaimsJws(token)
            .getBody();
        return claims.getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## Thymeleaf Templates Structure

### Layout Pattern

```
templates/
├── layout/
│   ├── main.html                 # Master layout (header, sidebar, footer)
│   ├── header.html               # Top navbar
│   ├── sidebar.html              # Left sidebar menu
│   └── footer.html
│
├── dashboard/
│   └── index.html                # Dashboard with statistics
│
├── workorder/
│   ├── list.html                 # Work order list with filters
│   ├── create.html               # Create new work order
│   ├── detail.html               # Work order detail + approval
│   ├── estimation.html           # Create estimation
│   └── complete.html             # Complete work order form
│
├── vehicle/
│   ├── list.html
│   ├── create.html
│   ├── detail.html
│   └── maintenance-history.html  # Work order history per vehicle
│
├── workshop/
│   ├── list.html
│   ├── create.html
│   ├── detail.html
│   └── performance.html          # Workshop performance metrics
│
├── inventory/
│   ├── list.html                 # Stock list with search & filter
│   ├── create.html
│   ├── stock-movement.html       # Record stock IN/OUT/TRANSFER
│   └── reorder-alert.html        # Low stock alerts
│
├── invoice/
│   ├── list.html
│   ├── detail.html
│   ├── create.html
│   └── payment.html              # Record payment
│
├── client/
│   ├── list.html
│   ├── create.html
│   ├── detail.html
│   └── portal.html               # Client self-service portal
│
├── mechanic/
│   ├── list.html
│   ├── create.html
│   ├── detail.html
│   └── schedule.html             # Mechanic work schedule
│
├── report/
│   ├── index.html                # Report menu
│   ├── workorder-report.html
│   ├── inventory-report.html
│   ├── financial-report.html
│   └── client-report.html
│
├── auth/
│   ├── login.html
│   └── register.html
│
└── error/
    ├── 404.html
    ├── 403.html
    └── 500.html
```

### Sample Template - workorder/list.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:layout="http://www.ultraq.net.nz/thymeleaf/layout"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security"
      layout:decorate="~{layout/main}">
<head>
    <title>Work Orders</title>
</head>
<body>
<div layout:fragment="content" class="container-fluid">
    
    <!-- Page Header -->
    <div class="row mb-3">
        <div class="col-md-6">
            <h2><i class="fas fa-tasks"></i> Work Orders</h2>
        </div>
        <div class="col-md-6 text-end">
            <a th:href="@{/workorders/create}" class="btn btn-primary">
                <i class="fas fa-plus"></i> New Work Order
            </a>
        </div>
    </div>
    
    <!-- Filters -->
    <div class="card mb-3">
        <div class="card-body">
            <form th:action="@{/workorders}" method="get" class="row g-3">
                <div class="col-md-3">
                    <label class="form-label">Status</label>
                    <select name="status" class="form-select">
                        <option value="">All Status</option>
                        <option th:each="st : ${statuses}" 
                                th:value="${st}" 
                                th:text="${st}"
                                th:selected="${st.name() == param.status}">
                        </option>
                    </select>
                </div>
                
                <div class="col-md-6">
                    <label class="form-label">Search</label>
                    <input type="text" name="search" class="form-control" 
                           placeholder="WO Number, Vehicle Plate, Client..."
                           th:value="${param.search}">
                </div>
                
                <div class="col-md-3">
                    <label class="form-label">&nbsp;</label>
                    <button type="submit" class="btn btn-primary w-100">
                        <i class="fas fa-search"></i> Filter
                    </button>
                </div>
            </form>
        </div>
    </div>
    
    <!-- Work Orders Table -->
    <div class="card">
        <div class="card-body">
            <div class="table-responsive">
                <table class="table table-hover">
                    <thead>
                        <tr>
                            <th>WO Number</th>
                            <th>Vehicle</th>
                            <th>Client</th>
                            <th>Service Type</th>
                            <th>Status</th>
                            <th>Estimated Cost</th>
                            <th>Request Date</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr th:each="wo : ${workOrders}">
                            <td>
                                <a th:href="@{/workorders/{id}(id=${wo.id})}" 
                                   th:text="${wo.workOrderNumber}"></a>
                                <span th:if="${wo.isUrgent}" 
                                      class="badge bg-danger ms-1">URGENT</span>
                            </td>
                            <td th:text="${wo.vehicle.licensePlate}"></td>
                            <td th:text="${wo.client.name}"></td>
                            <td th:text="${wo.serviceType}"></td>
                            <td>
                                <span class="badge" 
                                      th:classappend="${wo.status.name() == 'COMPLETED' ? 'bg-success' : 
                                                      wo.status.name() == 'IN_PROGRESS' ? 'bg-info' : 
                                                      wo.status.name() == 'APPROVED' ? 'bg-primary' : 
                                                      wo.status.name() == 'REJECTED' ? 'bg-danger' : 'bg-warning'}"
                                      th:text="${wo.status}">
                                </span>
                            </td>
                            <td th:text="${'Rp ' + #numbers.formatDecimal(wo.estimatedCost, 1, 'COMMA', 0, 'POINT')}"></td>
                            <td th:text="${#temporals.format(wo.requestedDate, 'dd MMM yyyy HH:mm')}"></td>
                            <td>
                                <a th:href="@{/workorders/{id}(id=${wo.id})}" 
                                   class="btn btn-sm btn-info">
                                    <i class="fas fa-eye"></i>
                                </a>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>
            
            <!-- Pagination -->
            <nav th:if="${workOrders.totalPages > 1}">
                <ul class="pagination justify-content-center">
                    <li class="page-item" th:classappend="${workOrders.number == 0} ? 'disabled'">
                        <a class="page-link" 
                           th:href="@{/workorders(page=${workOrders.number - 1}, status=${param.status}, search=${param.search})}">
                            Previous
                        </a>
                    </li>
                    
                    <li class="page-item" 
                        th:each="i : ${#numbers.sequence(0, workOrders.totalPages - 1)}"
                        th:classappend="${i == workOrders.number} ? 'active'">
                        <a class="page-link" 
                           th:href="@{/workorders(page=${i}, status=${param.status}, search=${param.search})}"
                           th:text="${i + 1}">
                        </a>
                    </li>
                    
                    <li class="page-item" th:classappend="${workOrders.number == workOrders.totalPages - 1} ? 'disabled'">
                        <a class="page-link" 
                           th:href="@{/workorders(page=${workOrders.number + 1}, status=${param.status}, search=${param.search})}">
                            Next
                        </a>
                    </li>
                </ul>
            </nav>
        </div>
    </div>
    
</div>
</body>
</html>
```

---

## REST API for Mobile

### Base URL: `/api/mobile`

### Authentication

**POST** `/api/mobile/auth/login`
```json
Request:
{
  "username": "mechanic1",
  "password": "password123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "mechanic1",
  "fullName": "Mechanic Ahmad",
  "roles": ["ROLE_MECHANIC"]
}
```

### Work Orders

**GET** `/api/mobile/workorders`
- Headers: `Authorization: Bearer {token}`
- Query params: `status`, `page`, `size`
- Response: Paginated list of work orders

**GET** `/api/mobile/workorders/{id}`
- Headers: `Authorization: Bearer {token}`
- Response: Work order detail with items

**POST** `/api/mobile/workorders/{id}/start`
- Headers: `Authorization: Bearer {token}`
- Body: `{ "notes": "Starting work" }`
- Response: Updated work order

**POST** `/api/mobile/workorders/{id}/complete`
- Headers: `Authorization: Bearer {token}`
- Body: 
```json
{
  "actualCost": 1500000,
  "laborCost": 300000,
  "partsCost": 1200000,
  "notes": "Work completed successfully",
  "items": [
    {
      "sparePartId": 1,
      "quantity": 5,
      "unitPrice": 25000
    }
  ]
}
```

**POST** `/api/mobile/workorders/{id}/upload-photo`
- Headers: `Authorization: Bearer {token}`
- Content-Type: `multipart/form-data`
- Body: `photo` (file), `documentType` (PHOTO_BEFORE/PHOTO_AFTER)

### Inventory

**GET** `/api/mobile/inventory`
- Headers: `Authorization: Bearer {token}`
- Query params: `search`, `locationId`
- Response: List of inventory items with availability

**GET** `/api/mobile/inventory/{id}`
- Headers: `Authorization: Bearer {token}`
- Response: Inventory detail

**POST** `/api/mobile/inventory/check-availability`
- Headers: `Authorization: Bearer {token}`
- Body: 
```json
{
  "sparePartId": 1,
  "locationId": 1,
  "quantity": 5
}
```
- Response: `{ "available": true, "availableQuantity": 50 }`

### Profile

**GET** `/api/mobile/profile`
- Headers: `Authorization: Bearer {token}`
- Response: User profile + mechanic info

**GET** `/api/mobile/notifications`
- Headers: `Authorization: Bearer {token}`
- Response: List of notifications

---

## Configuration Files

### pom.xml Dependencies

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-thymeleaf</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-mail</artifactId>
    </dependency>
    
    <!-- Thymeleaf Extras -->
    <dependency>
        <groupId>org.thymeleaf.extras</groupId>
        <artifactId>thymeleaf-extras-springsecurity6</artifactId>
    </dependency>
    
    <dependency>
        <groupId>nz.net.ultraq.thymeleaf</groupId>
        <artifactId>thymeleaf-layout-dialect</artifactId>
    </dependency>
    
    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-database-postgresql</artifactId>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.11.5</version>
    </dependency>
    
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.11.5</version>
        <scope>runtime</scope>
    </dependency>
    
    <!-- PDF Generation (iText7) -->
    <dependency>
        <groupId>com.itextpdf</groupId>
        <artifactId>itext7-core</artifactId>
        <version>7.2.5</version>
        <type>pom</type>
    </dependency>
    
    <!-- Excel (Apache POI) -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.3</version>
    </dependency>
    
    <!-- File Upload -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.13.0</version>
    </dependency>
    
    <!-- DevTools -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
        <scope>runtime</scope>
        <optional>true</optional>
    </dependency>
    
    <!-- Testing -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.security</groupId>
        <artifactId>spring-security-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### application.properties

```properties
# Application
spring.application.name=Fleet Maintenance System
server.port=8080

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/fleet_maintenance_db
spring.datasource.username=postgres
spring.datasource.password=postgres
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.locations=classpath:db/migration

# Thymeleaf
spring.thymeleaf.cache=false
spring.thymeleaf.prefix=classpath:/templates/
spring.thymeleaf.suffix=.html

# File Upload
spring.servlet.multipart.enabled=true
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
file.upload-dir=./uploads

# JWT
jwt.secret=MySecretKeyForJWTTokenGenerationFleetMaintenanceSystem2024
jwt.expiration=86400000

# Logging
logging.level.root=INFO
logging.level.com.fms=DEBUG

# Date Format
spring.mvc.format.date=yyyy-MM-dd
spring.jackson.time-zone=Asia/Jakarta
```

---

## Development Workflow

### 1. Setup Database
```sql
CREATE DATABASE fleet_maintenance_db;
```

### 2. Run Application
```bash
mvn spring-boot:run
```

### 3. Access
- Web: http://localhost:8080
- Login: username `admin`, password `password123`

### 4. API Testing
```bash
# Login
curl -X POST http://localhost:8080/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"mechanic1","password":"password123"}'

# Get work orders (use token from login)
curl -X GET http://localhost:8080/api/mobile/workorders \
  -H "Authorization: Bearer {your-jwt-token}"
```

---

## Key Features Implementation Checklist

### Phase 1: Core Features (MVP)
- [ ] User authentication & authorization
- [ ] Client management (CRUD)
- [ ] Vehicle management (CRUD)
- [ ] Workshop management (CRUD)
- [ ] Work order creation & listing
- [ ] Work order estimation
- [ ] Work order approval workflow
- [ ] Basic inventory (view stock)
- [ ] Simple dashboard (counts)

### Phase 2: Business Logic
- [ ] Work order execution (start, complete)
- [ ] Inventory consumption from work orders
- [ ] Invoice auto-generation
- [ ] Payment recording
- [ ] Low stock alerts
- [ ] Work order assignment to mechanics
- [ ] File upload (photos, documents)

### Phase 3: Advanced Features
- [ ] Inventory movements (IN, OUT, TRANSFER)
- [ ] Multi-location stock management
- [ ] Stock reservation
- [ ] Scheduled maintenance reminders
- [ ] Client portal (view own vehicles & work orders)
- [ ] PDF report generation (work order, invoice)
- [ ] Excel export (inventory, financial reports)

### Phase 4: Mobile API
- [ ] JWT authentication
- [ ] Mobile work order APIs
- [ ] Mobile inventory check APIs
- [ ] Photo upload from mobile
- [ ] Push notifications

### Phase 5: Analytics & Reporting
- [ ] Dashboard with charts (revenue, work orders by status)
- [ ] Work order reports (by date range, client, workshop)
- [ ] Inventory reports (stock levels, movements)
- [ ] Financial reports (revenue, outstanding invoices)
- [ ] Mechanic performance reports

---

## Best Practices & Conventions

### Naming Conventions
- **Tables**: snake_case (plural) - `work_orders`, `spare_parts`
- **Columns**: snake_case - `created_at`, `work_order_number`
- **Java Classes**: PascalCase - `WorkOrder`, `SparePartService`
- **Java Methods**: camelCase - `findById()`, `createWorkOrder()`
- **REST Endpoints**: kebab-case - `/api/mobile/work-orders`

### Code Standards
- Use Lombok for boilerplate reduction
- Use DTOs for API responses (don't expose entities directly)
- Validate all inputs with `@Valid` and custom validators
- Use `@Transactional` on service methods
- Log all important business operations
- Handle exceptions with `@ControllerAdvice`

### Database
- Always use migrations (Flyway) - never change existing migration files
- Add indexes on foreign keys and frequently queried columns
- Use enums for fixed value sets
- Add audit fields (created_at, updated_at, created_by) to all main tables

### Security
- Never log passwords
- Use BCrypt for password hashing
- Validate JWT tokens on every API request
- Use HTTPS in production
- Sanitize all user inputs
- Implement CSRF protection for web forms

---

## Deployment

### Build for Production
```bash
mvn clean package -DskipTests
```

### Run in Production
```bash
java -jar target/fleet-maintenance-system-1.0.0.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:postgresql://prod-server:5432/fleet_db \
  --spring.datasource.username=prod_user \
  --spring.datasource.password=secure_password
```

### Docker (Optional)
```dockerfile
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
COPY target/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

---

## License

Proprietary - All Rights Reserved

---

**Generated: May 2026**
**Version: 1.0.0**
