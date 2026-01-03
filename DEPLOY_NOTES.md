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
ALLOWED_ORIGIN_PATTERNS=https://habesha-community-frontend.netlify.app,http://localhost:3000

# Optional Services (leave empty if not configured)
TWILIO_ACCOUNT_SID=
TWILIO_AUTH_TOKEN=
TWILIO_PHONE_NUMBER=
STRIPE_SECRET_KEY=
STRIPE_WEBHOOK_SECRET=
MAIL_USERNAME=
MAIL_PASSWORD=
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

### Database Schema

The application uses Hibernate's `ddl-auto=update` mode to automatically manage schema changes. Key fixes for Postgres compatibility:

- Removed `LONGBLOB` columnDefinition from User.profileImage (now uses `@Lob` with `byte[]`)
- All other `TEXT` columnDefinitions are Postgres-compatible

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
- Check that `SPRING_JPA_HIBERNATE_DDL_AUTO=update` is set
- Verify database user has CREATE/ALTER permissions
- Consider manual schema creation if needed

**CORS errors:**
- Verify `ALLOWED_ORIGIN_PATTERNS` includes your frontend domain
- Check that frontend uses correct `REACT_APP_API_URL`