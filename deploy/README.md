# Fleet Maintenance System – Deployment Package

This directory contains everything needed to ship a runnable, single-JAR
build of the application **with embedded Tomcat**. Database credentials
and log file path are kept **outside** the JAR so an operator can change
them without rebuilding.

## Contents

```
deploy/
├── config/
│   ├── database.properties   # DB connection + JPA/Flyway/JWT
│   └── logging.properties    # Log file path, rotation, verbosity
├── nginx/
│   └── fleetmaintsvc.conf    # Sample Nginx vhost (reverse proxy + HTTPS)
├── run.bat                   # Windows launcher (foreground)
├── run.sh                    # Linux/macOS launcher (foreground)
├── ecosystem.config.js       # PM2 process descriptor (background)
├── package.ps1               # Build & assemble the distribution
├── package.sh                # Build & assemble the distribution (bash)
└── README.md                 # This file
```

## Building the distribution

The assembly scripts run `mvn package`, then copy the resulting JAR
together with the templates above into a self-contained `dist/` folder
at the repo root.

### Windows (PowerShell)

```powershell
.\deploy\package.ps1
```

### Linux / macOS

```bash
./deploy/package.sh
```

After it finishes you will have:

```
dist/fleet-maintenance-1.0.0/
├── fleet-maintenance-system-1.0.0.jar
├── config/
│   ├── database.properties
│   └── logging.properties
├── nginx-sample/
│   └── fleetmaintsvc.conf   # copy to /etc/nginx/conf.d/
├── run.bat                  # foreground launcher
├── run.sh                   # foreground launcher
├── ecosystem.config.js      # PM2 descriptor (background)
├── logs/                    # (created on first run)
└── README.md
```

You can copy / zip this whole folder to the target server.

## Running

1.  Make sure Java 17+ is installed (`java -version`).
2.  Edit `config/database.properties` with the real DB host, user, and
    password, and change `jwt.secret` to a strong random value.
3.  Edit `config/logging.properties` if you want logs somewhere other
    than `./logs/fleet-maintenance.log`.
4.  Launch:
    -   Windows: double-click `run.bat` or run it from a shell
    -   Linux/macOS: `./run.sh`

The app listens on **http://localhost:8080** by default. Override with
`server.port=...` in `database.properties` or by editing the launcher.

## Running with PM2 (background / daemonized)

`run.bat` / `run.sh` run the JVM in the foreground. For a long-running
deployment use [PM2](https://pm2.keymetrics.io/), which keeps the
process up, restarts it on crash, and (optionally) auto-starts on boot.

### Install once on the target host

```bash
# Linux / macOS — needs Node.js 18+
npm install -g pm2
```

```powershell
# Windows — needs Node.js 18+ from https://nodejs.org/
npm install -g pm2
npm install -g pm2-windows-startup   # only if you want boot persistence
```

### Start the service

```bash
cd /path/to/fleet-maintenance-1.0.0
pm2 start ecosystem.config.js     # auto-detects the JAR sibling file
pm2 save                          # persist current process list
```

`ecosystem.config.js` already:

-   names the app **fleet-maintenance**
-   sets `cwd` to the folder it lives in, so Spring Boot finds
    `./config/database.properties` and `./config/logging.properties`
-   launches the JVM with `-Xms256m -Xmx1024m` (override by exporting
    `JAVA_OPTS` before `pm2 start`)
-   captures JVM stdout/stderr into `./logs/pm2-stdout.log` and
    `./logs/pm2-stderr.log` (the structured application log still goes
    to wherever `logging.file.name` points)
-   auto-restarts on crash and recycles the JVM if RSS exceeds 1.5 GB

### Day-to-day commands

```bash
pm2 status                        # is it running?
pm2 logs fleet-maintenance        # tail JVM stdout/stderr
pm2 logs fleet-maintenance --lines 200
pm2 monit                         # interactive dashboard
pm2 reload  fleet-maintenance     # zero-downtime restart
pm2 restart fleet-maintenance     # hard restart
pm2 stop    fleet-maintenance
pm2 delete  fleet-maintenance     # remove from PM2 list
```

### Auto-start on system boot

```bash
# Linux (run as the user that owns the PM2 daemon)
pm2 startup systemd               # PM2 prints a sudo command — run it
pm2 save
```

```powershell
# Windows
pm2-startup install
pm2 save
```

### Custom JVM options

```bash
JAVA_OPTS="-Xms512m -Xmx2048m -Dfile.encoding=UTF-8" pm2 start ecosystem.config.js
```

```powershell
$env:JAVA_OPTS = "-Xms512m -Xmx2048m -Dfile.encoding=UTF-8"
pm2 start ecosystem.config.js
```

## Exposing the service on a public domain (Nginx + HTTPS)

The application binds to `127.0.0.1:8080`. To put it behind a real
domain like `fleetmaintsvc.my.id` with HTTPS, terminate TLS in Nginx
and reverse-proxy to localhost. A working vhost template ships in
`nginx-sample/fleetmaintsvc.conf`.

### 1. Point DNS

At your domain registrar add two A records (apex + `www`) targeting
the server's public IP. Verify with:

```bash
dig +short fleetmaintsvc.my.id
```

### 2. Open the firewall

```bash
# Server-side (firewalld)
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https
firewall-cmd --reload
```

Plus the cloud provider's security group: allow inbound TCP 80 and
443 from `0.0.0.0/0`. Leave 8080 **closed** to the public — clients
should only reach the app through Nginx.

### 3. Install Nginx and drop in the vhost

```bash
dnf install -y nginx
systemctl enable --now nginx

# Edit `server_name` inside the file if your domain differs.
cp ./nginx-sample/fleetmaintsvc.conf /etc/nginx/conf.d/

nginx -t && systemctl reload nginx
curl -I http://fleetmaintsvc.my.id/auth/login    # expect 200 OK
```

The vhost already forwards `Host`, `X-Real-IP`, `X-Forwarded-For`,
`X-Forwarded-Proto`, `X-Forwarded-Host`, and `X-Forwarded-Port`.
Spring Boot honors them via `server.forward-headers-strategy=framework`
(default in the bundled `application.properties`), so login redirects,
CSRF same-origin checks, and the cookie `Secure` flag will see the
public scheme/host instead of the internal `:8080`.

### 4. Issue a TLS certificate with Let's Encrypt

```bash
dnf install -y certbot python3-certbot-nginx

certbot --nginx \
    -d fleetmaintsvc.my.id -d www.fleetmaintsvc.my.id \
    --agree-tos --redirect -m you@example.com --non-interactive
```

certbot rewrites the vhost in place to add the `listen 443 ssl` block
and an HTTP→HTTPS redirect, then reloads Nginx. Auto-renewal runs from
the systemd timer:

```bash
systemctl status certbot-renew.timer
certbot renew --dry-run
```

### 5. Smoke-test the public URL

```bash
curl -I http://fleetmaintsvc.my.id              # should redirect to https
curl -I https://fleetmaintsvc.my.id/auth/login  # 200 OK on the login page
```

Open `https://fleetmaintsvc.my.id` in a browser and sign in.

## How external configuration works

Inside `application.properties` (bundled in the JAR) there is exactly
one line that enables the override:

```properties
spring.config.import=optional:file:./config/database.properties,optional:file:./config/logging.properties
```

`optional:` means the JAR still boots cleanly even when these files are
missing; the bundled defaults are used instead. When the files **are**
present next to the JAR, every property they declare wins over the
in-JAR default. This is plain Spring Boot — no custom code involved.

## Hardening checklist

-   [ ] Change `jwt.secret` in `database.properties` to at least 32
        random bytes.
-   [ ] Set strong `spring.datasource.password`.
-   [ ] Keep `spring.jpa.hibernate.ddl-auto=validate` (it is the default).
-   [ ] Confirm `logging.file.name` points to a writable absolute path
        (e.g. `/var/log/fleet-maintenance/app.log`).
-   [ ] Rotate the seeded user passwords (`password123`) before going
        live.
