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
- ✅ **Swagger / OpenAPI Sandbox** - Try-it-out playground untuk seluruh `/api/**`
- ✅ **User & Role Administration** - CRUD pengguna, role, dan mapping role ↔ menu lewat UI
- ✅ **Custom Branding** - Logo aplikasi bisa diganti admin tanpa redeploy
- ✅ **SumoPod-style Sidebar** - Layout side-navigation kiri, bisa di-collapse di desktop
- ✅ **Partner & supplier portals** - Bengkel rekanan dan penyedia spare part melihat WO yang di-assign, mengisi estimasi, dan notifikasi assignment
- ✅ **External estimate approval** - Admin/manager/finance (sesuai pengaturan role) menyetujui estimasi eksternal; badge antrian di sidebar

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
   - The login page lists demo usernames (internal accounts plus **partner1** / **supplier1** for the partner workshop and parts-supplier portals). Password for all demo accounts: **password123**.

## 🗄️ Database Schema

**Core Tables:**
- `users`, `roles`, `user_roles` - Authentication & Authorization
- `clients`, `vehicles` - Fleet management
- `workshops`, `mechanics` - Service providers
- `spare_parts`, `inventory`, `inventory_locations` - Inventory management
- `work_orders`, `work_order_items` - Work order processing
- `user_workshops` - Links partner-portal users to a partner workshop
- `work_order_supplier_users` - Assigns parts-supplier users to a work order
- `app_settings` - Key/value settings (e.g. `ESTIMATE_APPROVER_ROLES` for external estimate approvers)
- `invoices`, `payments` - Financial transactions

**Schema evolution:** See `src/main/resources/db/migration/` for the full Flyway history (V9 adds partner/supplier estimate approval workflow, roles, menus, and seed users).

## 👥 Default Users

| Username | Password | Role | Access Level |
|----------|----------|------|--------------|
| admin | password123 | ADMIN | Full access |
| manager | password123 | MANAGER | Operations management |
| finance | password123 | FINANCE | Invoices & payments |
| client1 | password123 | CLIENT | Own data only |
| mechanic1 | password123 | MECHANIC | Assigned work orders |
| partner1 | password123 | PARTNER_WORKSHOP | Partner portal (WO di workshop partner yang di-link ke user) |
| supplier1 | password123 | PARTS_SUPPLIER | Supplier portal (WO yang di-assign sebagai penyedia spare part) |

## 🔄 Work Order Workflow

```
REQUESTED → ESTIMATED → APPROVED → IN_PROGRESS → COMPLETED → BILLED
     ↓           ↓           ↓
 CANCELLED   REJECTED   CANCELLED
```

## 🌐 URL Map

### Web Application
- `/` — Dashboard
- `/workorders` — Work order management
- `/vehicles` — Vehicle management
- `/inventory` — Inventory management
- `/invoices`, `/payments` — Invoicing & cashflow
- `/clients`, `/workshops`, `/mechanics` — Master data (ADMIN / MANAGER)
- `/reports` — Reporting (ADMIN / MANAGER / FINANCE)
- `/partner/workorders` — Portal bengkel rekanan (PARTNER_WORKSHOP)
- `/supplier/workorders` — Portal penyedia spare part (PARTS_SUPPLIER)
- `/approvals/estimates` — Antrian persetujuan estimasi eksternal (ADMIN / MANAGER / FINANCE; siapa boleh approve juga diatur di Approval setup)

### Administration (ADMIN only)
- `/admin/users` — User CRUD + role assignment + enable/disable
- `/admin/roles` — Role CRUD + role ↔ menu mapping
- `/admin/branding` — Upload logo aplikasi (PNG/JPG/SVG/WEBP/GIF/ICO, ≤2 MB)
- `/admin/approval-settings` — CSV role Spring Security yang boleh approve estimasi eksternal (`ESTIMATE_APPROVER_ROLES`)

### Public utility
- `/auth/login`, `/auth/logout` — Form-based login
- `/branding/logo` — Stream logo aktif (anonymous-readable, dipakai login page)

### Swagger / OpenAPI
- `/swagger-ui.html` — Interactive playground (redirects ke `/swagger-ui/index.html`)
- `/v3/api-docs` — OpenAPI 3 JSON

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

Daftar lengkap & try-it-out tersedia langsung di Swagger UI.

## 📦 Build Commands

```bash
# Development (H2 in-memory, no Flyway, schema dibuat Hibernate)
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production build (executable JAR, embedded Tomcat)
mvn clean package -DskipTests

# Run JAR (pakai application.properties default - PostgreSQL)
java -jar target/fleet-maintenance-system-1.0.0.jar

# Override DB via env (atau pakai config/database.properties — lihat di bawah)
DB_USERNAME=postgres DB_PASSWORD=secret \
  java -jar target/fleet-maintenance-system-1.0.0.jar

# Run tests
mvn test
```

### Externalized configuration

`application.properties` mengimpor 2 file opsional dari working directory:

```
./config/database.properties
./config/logging.properties
```

Untuk membuat config production yang persisten, salin template dari
`deploy/config/*.properties` ke `./config/` dan isi nilainya.
File `config/` di-gitignore — kredensial tidak akan ikut commit.

### Deployment package

```powershell
# Windows
.\deploy\package.ps1
```
```bash
# Linux / macOS
./deploy/package.sh
```

Menghasilkan:
- `dist/fleet-maintenance-1.0.0/` — folder berisi JAR + config template + scripts
- `dist/fleet-maintenance-1.0.0.zip` — siap di-`scp` ke server

Detail deploy via PM2 + Nginx + Let's Encrypt: lihat **[deploy/README.md](deploy/README.md)**.

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
├── deploy/                 # Deployment kit (config templates, run scripts, PM2, Nginx)
└── pom.xml
```

## 🔐 Security

- **Web:** Form-based authentication (Spring Security)
- **API:** JWT Bearer Token authentication
- **Password:** BCrypt hashing
- **CSRF:** Enabled for web, disabled for API
- **Roles:** ADMIN, MANAGER, CLIENT, MECHANIC, FINANCE, PARTNER_WORKSHOP (partner portal), PARTS_SUPPLIER (supplier portal)
- **Dynamic menu visibility:** Sidebar items per user dihitung dari
  tabel `menus` × `role_menus`; admin bisa ubah mapping di
  `/admin/roles/{id}/edit` tanpa redeploy.

## 📊 Sample Data

Project includes sample data:
- 5 Clients (rental companies & logistics)
- 5 Workshops (internal & partners)
- 5 Mechanics
- 10 Vehicles
- 12 Spare Parts
- 8 Work Orders (various statuses)
- 4 Invoices
- Flyway **V9** seeds `partner1` / `supplier1` (see **Default Users** above) and navigation entries for partner/supplier portals and estimate approvals

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
