# Deployment Notes - Habesha Community Backend

## Render + Neon Postgres Configuration

### Environment Variables Required

```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple
SPRING_DATASOURCE_USERNAME=your_neon_username
SPRING_DATASOURCE_PASSWORD=your_neon_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# JPA Configuration
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION=86400000

# CORS Configuration
FRONTEND_URL=https://habesha-community-frontend.netlify.app
ALLOWED_ORIGIN_PATTERNS=https://habesha-community-frontend.netlify.app,https://*.netlify.app,http://localhost:3000

# Optional Services (leave empty if not configured)
TWILIO_ACCOUNT_SID=
TWILIO_AUTH_TOKEN=
TWILIO_PHONE_NUMBER=
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=
MAIL_USERNAME=
MAIL_PASSWORD=

# Flyway Configuration (optional)
FLYWAY_ENABLED=true
```

### Critical Neon Pooler Configuration

When using Neon's pooler (recommended for production), add these JDBC parameters to avoid "cached plan must not change result type" errors:

- `prepareThreshold=0` - Disables prepared statement caching
- `preferQueryMode=simple` - Uses simple query mode instead of extended protocol

**Example JDBC URL:**
```
jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple
```

### Health Endpoints

The application provides two health check endpoints:

1. **Simple Health Check** (no DB dependency):
   - `GET /health` - Returns plain text "OK"
   - Used by Render for health checks

2. **Actuator Health Check** (includes DB status):
   - `GET /actuator/health` - Returns JSON with application status
   - Shows database connectivity and other components

### Database Schema & Migrations

The application uses both Hibernate and Flyway for database management:

**Hibernate Configuration:**
- Uses `ddl-auto=update` mode for automatic schema updates
- All entities use PostgreSQL-compatible JPA mappings
- Removed MySQL-specific `columnDefinition` attributes

**Flyway Migrations:**
- Enabled by default (`FLYWAY_ENABLED=true`)
- Baseline-on-migrate enabled for existing databases
- Migration files located in `src/main/resources/db/migration/`
- Current migrations:
  - `V2__Add_User_Profile_Image_Columns.sql` - Ensures profile image columns exist

**Key PostgreSQL Compatibility Fixes:**
- Removed `LONGBLOB` columnDefinition from User.profileImage (now uses `@Lob` with `byte[]`)
- Replaced MySQL-specific `columnDefinition = "TEXT"` with portable `@Column(length = 4000)` in all entities
- All binary data uses PostgreSQL `BYTEA` type via Hibernate mapping

### Health Checks Configuration

Mail health indicator is disabled to prevent SMTP timeouts from breaking health endpoints:
- `management.health.mail.enabled=false` in application.properties
- Mail sending functionality remains available, only health check is disabled

### Deployment Checklist

1. ✅ Set all required environment variables in Render
2. ✅ Use Neon pooler URL with `prepareThreshold=0&preferQueryMode=simple`
3. ✅ Verify `/health` endpoint returns 200 OK
4. ✅ Verify `/actuator/health` shows database connectivity
5. ✅ Test login and basic API endpoints

### Troubleshooting

**"cached plan must not change result type" error:**
- Ensure JDBC URL includes `prepareThreshold=0&preferQueryMode=simple`
- Verify you're using the pooler endpoint, not direct connection

**"column does not exist" errors:**
- Flyway migrations will create missing columns automatically
- Check that `SPRING_JPA_HIBERNATE_DDL_AUTO=update` is set
- Verify database user has CREATE/ALTER permissions
- If Flyway is disabled, consider manual schema creation

**Flyway migration issues:**
- Set `FLYWAY_ENABLED=false` to disable migrations if needed
- Baseline-on-migrate is enabled for existing databases
- Check migration files in `src/main/resources/db/migration/`

**CORS errors:**
- Verify `ALLOWED_ORIGIN_PATTERNS` includes your frontend domain and `https://*.netlify.app` for deploy previews
- Check that frontend uses correct `REACT_APP_API_URL`
- Ensure OPTIONS requests to `/auth/**` endpoints return 200/204 with proper CORS headers

**Mail health check timeouts:**
- Mail health indicator is disabled by default (`management.health.mail.enabled=false`)
- This prevents SMTP connection timeouts from breaking `/actuator/health`
- Mail sending functionality is not affected