/*
 * PM2 ecosystem file for the Fleet Maintenance System.
 *
 * Usage (from inside this directory, where the JAR lives):
 *
 *   pm2 start ecosystem.config.js          # start in background
 *   pm2 logs fleet-maintenance             # tail logs
 *   pm2 status                             # show running apps
 *   pm2 reload fleet-maintenance           # zero-downtime reload
 *   pm2 stop fleet-maintenance             # stop
 *   pm2 delete fleet-maintenance           # remove from PM2 list
 *   pm2 save                               # persist for boot
 *
 * The script auto-detects fleet-maintenance-system-*.jar next to this
 * file so no path edits are needed.
 *
 * JVM tuning: set JAVA_OPTS in the environment before running pm2, e.g.
 *   set JAVA_OPTS=-Xms512m -Xmx2048m -Dfile.encoding=UTF-8   (Windows)
 *   export JAVA_OPTS="-Xms512m -Xmx2048m -Dfile.encoding=UTF-8"  (Linux)
 */

const fs = require('fs');
const path = require('path');

const baseDir = __dirname;

const jarFile = fs.readdirSync(baseDir)
    .find((name) => /^fleet-maintenance-system-.*\.jar$/.test(name)
                    && !name.endsWith('.original'));

if (!jarFile) {
    throw new Error(
        'Could not find fleet-maintenance-system-*.jar in ' + baseDir +
        '. Did you copy the JAR next to ecosystem.config.js?'
    );
}

const defaultJavaOpts = '-Xms256m -Xmx1024m -Dfile.encoding=UTF-8';
const javaOpts = (process.env.JAVA_OPTS || defaultJavaOpts)
    .split(/\s+/)
    .filter(Boolean);

const logsDir = path.join(baseDir, 'logs');
if (!fs.existsSync(logsDir)) {
    fs.mkdirSync(logsDir, { recursive: true });
}

module.exports = {
    apps: [
        {
            name: 'fleet-maintenance',
            cwd: baseDir,
            script: 'java',
            interpreter: 'none',
            args: [...javaOpts, '-jar', jarFile],

            autorestart: true,
            watch: false,
            max_memory_restart: '1500M',

            // PM2-managed wrapper logs (stdout/stderr from the JVM).
            // The application's structured log file is still managed
            // by Logback via config/logging.properties.
            out_file: path.join(logsDir, 'pm2-stdout.log'),
            error_file: path.join(logsDir, 'pm2-stderr.log'),
            merge_logs: true,
            log_date_format: 'YYYY-MM-DD HH:mm:ss',

            // Give the JVM time to come up before PM2 declares it
            // unhealthy and respawns it.
            kill_timeout: 30000,
            listen_timeout: 60000,

            env: {
                // Spring Boot picks up config/database.properties and
                // config/logging.properties via spring.config.import
                // declared in the bundled application.properties — no
                // env vars required for normal operation.
            },
        },
    ],
};
