# Habesha Community Backend - Deployment Guide

## Environment Variables

### Required Environment Variables

#### Database Configuration
```bash
# PostgreSQL connection (Neon pooler compatible)
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple&sslmode=require
SPRING_DATASOURCE_USERNAME=your_db_username
SPRING_DATASOURCE_PASSWORD=your_db_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# Hibernate configuration (recommended for production)
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

#### Security & JWT
```bash
JWT_SECRET=your_very_long_and_secure_jwt_secret_key_here
JWT_EXPIRATION=86400000
```

#### CORS Configuration
```bash
# Comma-separated list of allowed origin patterns
ALLOWED_ORIGIN_PATTERNS=https://your-frontend.netlify.app,https://*.netlify.app,http://localhost:3000
FRONTEND_URL=https://your-frontend.netlify.app
```

#### External Services (Optional)
```bash
# Twilio SMS (optional)
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_PHONE_NUMBER=your_twilio_phone_number

# Stripe Payments (optional)
STRIPE_SECRET_KEY=sk_test_or_live_your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# Email (optional - disabled in health checks)
MAIL_USERNAME=your_gmail_username
MAIL_PASSWORD=your_gmail_app_password
```

#### Application Configuration
```bash
# File uploads
APP_UPLOADS_ROOT=uploads

# Flyway migrations
FLYWAY_ENABLED=true

# Logging
SPRING_JPA_SHOW_SQL=false
```

## Neon Database Configuration

### Connection URL Parameters
For Neon pooler compatibility, always include these parameters in your `SPRING_DATASOURCE_URL`:

- `prepareThreshold=0` - Disables server-side prepared statements
- `preferQueryMode=simple` - Uses simple query mode instead of extended protocol
- `sslmode=require` - Ensures SSL connection

**Example:**
```
jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple&sslmode=require
```

### Why These Parameters?
These parameters prevent the "cached plan must not change result type" error that occurs with Neon's connection pooler when using prepared statements after schema changes.

## Deployment Steps

### 1. Render Deployment

1. **Create a new Web Service** on Render
2. **Connect your GitHub repository**
3. **Configure build settings:**
   - Build Command: `mvn clean package -DskipTests`
   - Start Command: `java -jar target/habesha-community-backend-0.0.1-SNAPSHOT.jar`

4. **Set environment variables** (see above)

5. **Configure health check:**
   - Health Check Path: `/actuator/health`

### 2. Database Setup

1. **Create Neon database** with pooler enabled
2. **Run migrations** - Flyway will automatically run on startup
3. **Verify schema** - Check that all tables are created properly

### 3. Frontend CORS Configuration

Ensure your frontend origin is included in `ALLOWED_ORIGIN_PATTERNS`:
```bash
ALLOWED_ORIGIN_PATTERNS=https://your-app.netlify.app,https://*.netlify.app,http://localhost:3000
```

## Health Checks

The application provides health endpoints:

- `/actuator/health` - Spring Boot Actuator health endpoint (recommended)
- Includes database connectivity check
- Mail health indicator is disabled to prevent timeouts

## Security Configuration

### CORS
- Configured for Netlify deployment patterns
- Supports wildcard subdomains (`*.netlify.app`)
- Allows credentials for authenticated requests
- Proper preflight (OPTIONS) handling

### Authentication
- JWT-based authentication
- Secure password encoding with BCrypt
- Role-based access control (USER, ADMIN, MODERATOR)

## Troubleshooting

### Common Issues

1. **"cached plan must not change result type"**
   - Ensure Neon URL includes `prepareThreshold=0&preferQueryMode=simple`
   - Check HikariCP configuration in application.properties

2. **CORS preflight failures**
   - Verify `ALLOWED_ORIGIN_PATTERNS` includes your frontend domain
   - Check that OPTIONS requests are permitted in security config

3. **Health check failures**
   - Mail health indicator is disabled by default
   - Database connectivity issues will show in `/actuator/health`

4. **Schema mismatch errors**
   - Run Flyway migrations: `mvn flyway:migrate`
   - Set `SPRING_JPA_HIBERNATE_DDL_AUTO=validate` for production

### Logs to Check
- Application startup logs for configuration issues
- Database connection logs for Neon pooler issues
- CORS logs for frontend connectivity issues

## Production Recommendations

1. **Use `validate` for DDL auto** - Prevents accidental schema changes
2. **Enable Flyway migrations** - Ensures consistent schema across environments
3. **Disable mail health checks** - Prevents SMTP timeouts from marking app unhealthy
4. **Use proper connection pooling** - HikariCP configured for Neon compatibility
5. **Monitor health endpoints** - Set up monitoring on `/actuator/health`

## Local Development

For local development, you can use these minimal environment variables:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/habesha_local
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
JWT_SECRET=local_development_secret_key_change_in_production
ALLOWED_ORIGIN_PATTERNS=http://localhost:3000,http://localhost:3001
FRONTEND_URL=http://localhost:3000
```