# Fleet Maintenance System - Quick Reference

## 📋 Quick Start Guide

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Maven 3.8+
- (Optional) Cursor AI for code generation

### Setup in 5 Minutes

1. **Create Database**
   ```sql
   CREATE DATABASE fleet_maintenance_db;
   ```

2. **Clone/Extract Project**
   ```bash
   cd fleet-maintenance-system
   ```

3. **Configure Database**
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/fleet_maintenance_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   ```

4. **Build & Run**
   ```bash
   mvn spring-boot:run
   ```

5. **Access Application**
   - URL: http://localhost:8080
   - Username: `admin`
   - Password: `password123`

---

## 🏗️ Project Structure Overview

```
fleet-maintenance-system/
├── src/main/
│   ├── java/com/fms/
│   │   ├── entity/          # JPA Entities (19 classes)
│   │   ├── repository/      # Data Access Layer
│   │   ├── service/         # Business Logic
│   │   ├── controller/
│   │   │   ├── web/        # Thymeleaf Controllers
│   │   │   └── api/        # REST API for Mobile
│   │   ├── security/        # JWT & Spring Security
│   │   ├── enums/          # 10 Enum types
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── config/         # Spring Configuration
│   │   ├── util/           # Utilities (PDF, Excel)
│   │   └── exception/      # Exception Handlers
│   │
│   └── resources/
│       ├── db/migration/   # Flyway SQL scripts
│       ├── templates/      # Thymeleaf HTML
│       └── static/         # CSS, JS, Images
│
└── pom.xml                 # Maven Dependencies
```

---

## 🗄️ Database Quick Reference

### Main Tables

| Table | Description | Key Fields |
|-------|-------------|------------|
| `users` | System users | username, password, roles |
| `clients` | Corporate clients | code, name, payment_term |
| `vehicles` | Fleet vehicles | license_plate, client_id |
| `workshops` | Service locations | code, type, specialization |
| `mechanics` | Technicians | code, specialization, workshop_id |
| `work_orders` | Service requests | work_order_number, status |
| `spare_parts` | Parts catalog | part_number, unit_cost |
| `inventory` | Stock management | spare_part_id, location_id, quantity |
| `invoices` | Billing | invoice_number, status, total_amount |
| `payments` | Payment records | payment_number, amount |

### Key Relationships

```
clients (1) ─→ (N) vehicles
vehicles (1) ─→ (N) work_orders
work_orders (1) ─→ (N) work_order_items
work_orders (1) ─→ (1) invoices
workshops (1) ─→ (N) mechanics
spare_parts (1) ─→ (N) inventory
```

---

## 🔐 Authentication & Security

### Default Users

| Username | Password | Role | Access |
|----------|----------|------|--------|
| admin | password123 | ADMIN | Full access |
| manager | password123 | MANAGER | Operations |
| finance | password123 | FINANCE | Invoices & Payments |
| client1 | password123 | CLIENT | Own data only |
| mechanic1 | password123 | MECHANIC | Assigned work orders |

### Web Authentication
- Type: **Form Login** (Spring Security)
- Login URL: `/auth/login`
- Session-based

### Mobile API Authentication
- Type: **JWT Bearer Token**
- Login Endpoint: `POST /api/mobile/auth/login`
- Token Expiry: 24 hours (configurable)

**Example Login:**
```bash
curl -X POST http://localhost:8080/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "mechanic1",
    "password": "password123"
  }'

# Response:
{
  "token": "eyJhbGc...",
  "type": "Bearer",
  "username": "mechanic1",
  "roles": ["ROLE_MECHANIC"]
}
```

**Use Token:**
```bash
curl -X GET http://localhost:8080/api/mobile/workorders \
  -H "Authorization: Bearer eyJhbGc..."
```

---

## 📊 Work Order Workflow

### Status Flow

```
REQUESTED ──┐
            ├─→ ESTIMATED ──┐
REJECTED ◄──┘               ├─→ APPROVED ──┐
                            │               ├─→ IN_PROGRESS ──→ COMPLETED ──→ BILLED
         CANCELLED ◄────────┴───────────────┘
```

### Business Rules

1. **REQUESTED**: Client creates work order
2. **ESTIMATED**: Workshop creates cost estimation
3. **APPROVED**: Client approves estimation → stock reserved
4. **IN_PROGRESS**: Mechanic starts work
5. **COMPLETED**: Work done → stock consumed → invoice generated
6. **BILLED**: Invoice created
7. **CANCELLED**: Can cancel before IN_PROGRESS → releases stock
8. **REJECTED**: Client rejects estimation

---

## 🔧 Key Service Methods

### WorkOrderService

```java
// Create new work order
WorkOrder create(WorkOrderCreateRequest request, String username)
// Status: REQUESTED

// Add estimation
WorkOrder createEstimation(Long id, EstimationRequest request)
// Status: ESTIMATED

// Approve (reserves stock)
WorkOrder approve(Long id, String notes, String username)
// Status: APPROVED

// Start work
WorkOrder start(Long id, Long mechanicId)
// Status: IN_PROGRESS

// Complete (consumes stock, generates invoice)
WorkOrder complete(Long id, CompletionRequest request)
// Status: COMPLETED → BILLED
```

### InventoryService

```java
// Check if enough stock available
boolean checkAvailability(Long sparePartId, Long locationId, Integer qty)

// Reserve stock for approved work order
void reserveStock(Long inventoryId, Integer quantity, Long workOrderId)

// Release if work order cancelled
void releaseReservedStock(Long workOrderId)

// Consume when work order completed
void consumeStock(Long inventoryId, Integer quantity, Long workOrderId)

// Transfer between locations
void transferStock(Long sparePartId, Long fromLocationId, Long toLocationId, Integer qty)
```

---

## 🌐 API Endpoints Reference

### Mobile API - Work Orders

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/mobile/auth/login` | Login → get JWT |
| GET | `/api/mobile/workorders` | List work orders |
| GET | `/api/mobile/workorders/{id}` | Work order detail |
| POST | `/api/mobile/workorders/{id}/start` | Start work |
| POST | `/api/mobile/workorders/{id}/complete` | Complete work |
| POST | `/api/mobile/workorders/{id}/upload-photo` | Upload photo |

### Mobile API - Inventory

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/mobile/inventory` | List inventory |
| GET | `/api/mobile/inventory/{id}` | Inventory detail |
| POST | `/api/mobile/inventory/check-availability` | Check stock |

### Web Endpoints

| Route | Description |
|-------|-------------|
| `/` | Dashboard |
| `/workorders` | Work order list |
| `/workorders/create` | Create work order |
| `/workorders/{id}` | Work order detail |
| `/vehicles` | Vehicle list |
| `/workshops` | Workshop list |
| `/inventory` | Inventory list |
| `/invoices` | Invoice list |
| `/clients` | Client list |
| `/mechanics` | Mechanic list |
| `/reports` | Reports menu |

---

## 📝 Sample Data Overview

### Clients (5)
- CLI001: PT Takari Kokoh Sejahtera
- CLI002: PT TSM
- CLI003: Orix
- CLI004: Prima Rent
- CLI005: PT Charoen Pokphand

### Workshops (5)
- WS001: Central Workshop (INTERNAL)
- WS002: Jakarta Branch Workshop (INTERNAL)
- WS003: Auto Prima (PARTNER)
- WS004: Bengkel Jaya (PARTNER)
- WS005: Toyota Auto2000 (CLIENT_RECOMMENDED)

### Vehicles (10)
- B 1234 ABC (Toyota Avanza)
- B 5678 DEF (Honda Jazz)
- B 9012 GHI (Toyota Innova)
- ... and 7 more

### Spare Parts (12)
- Oil, Filters, Brake Pads, Battery, Spark Plugs, etc.

### Work Orders (8 samples)
- 3 COMPLETED
- 1 IN_PROGRESS
- 1 APPROVED
- 1 ESTIMATED
- 1 REQUESTED
- 1 COMPLETED (partner)

---

## 🎨 UI Components

### Status Badges

```html
<!-- Work Order Status -->
<span class="badge bg-success">COMPLETED</span>
<span class="badge bg-info">IN_PROGRESS</span>
<span class="badge bg-primary">APPROVED</span>
<span class="badge bg-warning">ESTIMATED</span>
<span class="badge bg-secondary">REQUESTED</span>
<span class="badge bg-danger">REJECTED</span>

<!-- Invoice Status -->
<span class="badge bg-success">PAID</span>
<span class="badge bg-warning">SENT</span>
<span class="badge bg-danger">OVERDUE</span>
```

### Icons (Font Awesome)

- Work Orders: `<i class="fas fa-tasks"></i>`
- Vehicles: `<i class="fas fa-car"></i>`
- Workshops: `<i class="fas fa-wrench"></i>`
- Inventory: `<i class="fas fa-boxes"></i>`
- Invoices: `<i class="fas fa-file-invoice"></i>`
- Payments: `<i class="fas fa-money-bill"></i>`
- Reports: `<i class="fas fa-chart-bar"></i>`

---

## 🧪 Testing Quick Commands

### Test Database Connection
```bash
psql -U postgres -d fleet_maintenance_db -c "SELECT COUNT(*) FROM users;"
```

### Test Application
```bash
# Run tests
mvn test

# Run specific test
mvn test -Dtest=WorkOrderServiceTest

# Skip tests and build
mvn clean package -DskipTests
```

### Test REST API
```bash
# Get JWT token
TOKEN=$(curl -s -X POST http://localhost:8080/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"mechanic1","password":"password123"}' \
  | jq -r '.token')

# Use token
curl -X GET "http://localhost:8080/api/mobile/workorders" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 📦 Maven Commands

```bash
# Clean and compile
mvn clean compile

# Run application
mvn spring-boot:run

# Build JAR
mvn clean package

# Run JAR
java -jar target/fleet-maintenance-system-1.0.0.jar

# Generate project structure
mvn archetype:generate

# Update dependencies
mvn clean install -U

# Check dependency tree
mvn dependency:tree
```

---

## 🐛 Common Issues & Quick Fixes

### Issue: "Table doesn't exist"
**Fix:** 
```bash
# Check Flyway migrations
mvn flyway:info

# Repair if needed
mvn flyway:repair

# Run migrations manually
mvn flyway:migrate
```

### Issue: "BCrypt password mismatch"
**Fix:** Password hash must be:
```
$2a$10$N9qo8uLOickgx2ZMRZoMye1J6HG.pJI.72vOJSPRmEhPzNjVSLQni
```
(This is hash for "password123")

### Issue: "Port 8080 already in use"
**Fix:** Change in application.properties:
```properties
server.port=8081
```

### Issue: "Thymeleaf template not found"
**Fix:** Check path:
```
src/main/resources/templates/workorder/list.html
                              └─ must match controller return value
```

### Issue: "JWT token invalid"
**Fix:** Check:
1. Secret key length (min 256 bits)
2. Token not expired
3. Token format: `Bearer eyJhbGc...`

---

## 📈 Performance Tips

1. **Use Pagination**: Always use `Pageable` for large datasets
2. **Lazy Loading**: Entities use `FetchType.LAZY` by default
3. **Indexes**: Already created on FK and search columns
4. **Connection Pool**: HikariCP configured by Spring Boot
5. **Cache**: Add `@Cacheable` for frequently accessed data (optional)

---

## 🔄 Deployment Checklist

### Before Production

- [ ] Change default passwords
- [ ] Set strong JWT secret
- [ ] Configure email settings (SMTP)
- [ ] Set up HTTPS
- [ ] Configure proper database credentials
- [ ] Set `spring.jpa.hibernate.ddl-auto=validate`
- [ ] Enable production logging
- [ ] Configure file upload limits
- [ ] Set up backup strategy
- [ ] Configure firewall rules

### Environment Variables
```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://prod-db:5432/fms_db
export SPRING_DATASOURCE_USERNAME=fms_user
export SPRING_DATASOURCE_PASSWORD=secure_password
export JWT_SECRET=production_secret_key_min_256_bits
export MAIL_USERNAME=noreply@yourcompany.com
export MAIL_PASSWORD=mail_password
```

---

## 📚 Additional Resources

### Documentation Files
- `PROJECT_SPECIFICATION.md` - Full project specification
- `CURSOR_INSTRUCTIONS.md` - Cursor AI generation guide
- `README.md` - Main readme file
- `QUICK_REFERENCE.md` - This file

### External Links
- Spring Boot Docs: https://docs.spring.io/spring-boot/
- Thymeleaf Docs: https://www.thymeleaf.org/documentation.html
- PostgreSQL Docs: https://www.postgresql.org/docs/
- Bootstrap 5 Docs: https://getbootstrap.com/docs/5.3/

---

**Last Updated:** May 2026
**Version:** 1.0.0
