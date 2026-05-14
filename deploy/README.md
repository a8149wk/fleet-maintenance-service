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
