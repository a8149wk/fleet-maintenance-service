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
├── run.bat                   # Windows launcher
├── run.sh                    # Linux/macOS launcher
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
├── run.bat
├── run.sh
├── logs/                     # (created on first run)
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
