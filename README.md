# Fleet Maintenance System 🚗🔧

Sistem manajemen pemeliharaan kendaraan fleet berbasis Spring Boot + Thymeleaf untuk perusahaan transportasi/logistik dengan multi-workshop, multi-client, dan integrasi mobile API.

## 🎯 Features

- ✅ **Work Order Management** - Manajemen perintah kerja perbaikan dengan workflow approval
- ✅ **Fleet & Vehicle Management** - Database kendaraan client dengan history maintenance
- ✅ **Multi-Workshop Support** - Internal, Partner, dan Client-recommended workshops
- ✅ **Inventory Management** - Stok spare parts multi-lokasi dengan reservasi
- ✅ **Invoicing & Payments** - Auto-generate invoice dan track pembayaran
- ✅ **Mechanic Management** - Assignment & performance tracking teknisi
- ✅ **Client Portal** - Portal untuk client monitor kendaraan
- ✅ **REST API** - API untuk mobile app (JWT authentication)
- ✅ **Reporting** - PDF & Excel reports untuk bisnis

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Spring Boot 3.2.5, Spring Security, Spring Data JPA |
| Frontend Web | Thymeleaf, Bootstrap 5, jQuery |
| Mobile API | REST API with JWT |
| Database | PostgreSQL 14+ |
| Migration | Flyway |
| Build | Maven |
| Java | 17 |

## 📋 Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Maven 3.8+

### Installation

1. **Create Database**
   ```sql
   CREATE DATABASE fleet_maintenance_db;
   ```

2. **Configure Database**
   
   Edit `src/main/resources/application.properties`:
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/fleet_maintenance_db
   spring.datasource.username=postgres
   spring.datasource.password=your_password
   ```

3. **Build & Run**
   ```bash
   mvn spring-boot:run
   ```

4. **Access Application**
   - URL: http://localhost:8080
   - Username: `admin`
   - Password: `password123`

## 📖 Documentation

Dokumentasi lengkap tersedia dalam format Markdown:

| File | Description |
|------|-------------|
| **[PROJECT_SPECIFICATION.md](PROJECT_SPECIFICATION.md)** | 📘 Spesifikasi lengkap project (DDL, DML, Architecture) |
| **[CURSOR_INSTRUCTIONS.md](CURSOR_INSTRUCTIONS.md)** | 🤖 Instruksi untuk generate code dengan Cursor AI |
| **[QUICK_REFERENCE.md](QUICK_REFERENCE.md)** | ⚡ Quick reference untuk development |

## 🗄️ Database Schema

**Core Tables:**
- `users`, `roles`, `user_roles` - Authentication & Authorization
- `clients`, `vehicles` - Fleet management
- `workshops`, `mechanics` - Service providers
- `spare_parts`, `inventory`, `inventory_locations` - Inventory management
- `work_orders`, `work_order_items` - Work order processing
- `invoices`, `payments` - Financial transactions

**Total:** 20 tables dengan proper relationships dan indexes

## 👥 Default Users

| Username | Password | Role | Access Level |
|----------|----------|------|--------------|
| admin | password123 | ADMIN | Full access |
| manager | password123 | MANAGER | Operations management |
| finance | password123 | FINANCE | Invoices & payments |
| client1 | password123 | CLIENT | Own data only |
| mechanic1 | password123 | MECHANIC | Assigned work orders |

## 🔄 Work Order Workflow

```
REQUESTED → ESTIMATED → APPROVED → IN_PROGRESS → COMPLETED → BILLED
     ↓           ↓           ↓
 CANCELLED   REJECTED   CANCELLED
```

## 🌐 API Endpoints

### Web Application
- `/` - Dashboard
- `/workorders` - Work order management
- `/vehicles` - Vehicle management
- `/inventory` - Inventory management
- `/invoices` - Invoice & payment

### Mobile REST API

**Authentication:**
```bash
POST /api/mobile/auth/login
```

**Work Orders:**
```bash
GET  /api/mobile/workorders
GET  /api/mobile/workorders/{id}
POST /api/mobile/workorders/{id}/start
POST /api/mobile/workorders/{id}/complete
```

**Inventory:**
```bash
GET  /api/mobile/inventory
POST /api/mobile/inventory/check-availability
```

## 🚀 Using Cursor AI

Proyek ini dilengkapi dengan instruksi lengkap untuk Cursor AI:

1. Buka `CURSOR_INSTRUCTIONS.md`
2. Copy instruksi per section ke Cursor Chat
3. Cursor akan generate code sesuai spesifikasi
4. Review dan adjust sesuai kebutuhan

**Example:**
```
@Cursor: Generate WorkOrderService.java dengan methods:
- create, approve, start, complete, cancel
- Include stock reservation logic
```

## 📦 Build Commands

```bash
# Development
mvn spring-boot:run

# Production build
mvn clean package -DskipTests

# Run JAR
java -jar target/fleet-maintenance-system-1.0.0.jar

# Run tests
mvn test
```

## 🧪 Testing API

```bash
# Login and get JWT token
curl -X POST http://localhost:8080/api/mobile/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"mechanic1","password":"password123"}'

# Use token to get work orders
curl -X GET http://localhost:8080/api/mobile/workorders \
  -H "Authorization: Bearer {your-jwt-token}"
```

## 📂 Project Structure

```
fleet-maintenance-system/
├── src/main/
│   ├── java/com/fms/
│   │   ├── entity/          # JPA Entities
│   │   ├── repository/      # Spring Data JPA
│   │   ├── service/         # Business Logic
│   │   ├── controller/
│   │   │   ├── web/        # Thymeleaf Controllers
│   │   │   └── api/        # REST API
│   │   ├── security/        # JWT & Security
│   │   ├── dto/            # Data Transfer Objects
│   │   └── util/           # Utilities
│   └── resources/
│       ├── db/migration/   # Flyway SQL
│       ├── templates/      # Thymeleaf HTML
│       └── static/         # CSS, JS
├── PROJECT_SPECIFICATION.md    # Full spec
├── CURSOR_INSTRUCTIONS.md      # AI generation guide
├── QUICK_REFERENCE.md          # Quick ref
└── pom.xml
```

## 🔐 Security

- **Web:** Form-based authentication (Spring Security)
- **API:** JWT Bearer Token authentication
- **Password:** BCrypt hashing
- **CSRF:** Enabled for web, disabled for API
- **Roles:** ADMIN, MANAGER, CLIENT, MECHANIC, FINANCE

## 📊 Sample Data

Project includes sample data:
- 5 Clients (rental companies & logistics)
- 5 Workshops (internal & partners)
- 5 Mechanics
- 10 Vehicles
- 12 Spare Parts
- 8 Work Orders (various statuses)
- 4 Invoices

## 🐛 Troubleshooting

**Database connection failed:**
```bash
# Check PostgreSQL is running
sudo systemctl status postgresql

# Test connection
psql -U postgres -d fleet_maintenance_db
```

**Port 8080 in use:**
```properties
# Change in application.properties
server.port=8081
```

**Flyway migration failed:**
```bash
mvn flyway:repair
mvn flyway:migrate
```

## 📝 License

Proprietary - All Rights Reserved

## 🙏 Acknowledgments

- Spring Boot Team
- Thymeleaf Team
- PostgreSQL Community
- Bootstrap Team

---

**Version:** 1.0.0  
**Last Updated:** May 2026  
**Status:** Ready for Cursor AI Generation 🤖

For detailed implementation instructions, see [CURSOR_INSTRUCTIONS.md](CURSOR_INSTRUCTIONS.md)
