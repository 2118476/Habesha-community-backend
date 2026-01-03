# Habesha Community Backend

Spring Boot REST API backend for the Habesha Community platform, serving the Ethiopian diaspora in the UK.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL database (Neon recommended for production)

### Local Development
```bash
# Clone and navigate to backend
cd habesha_community_backend

# Set environment variables (see .env.example)
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/habesha"
export SPRING_DATASOURCE_USERNAME="your_username"
export SPRING_DATASOURCE_PASSWORD="your_password"
export JWT_SECRET="your_jwt_secret_key_here"

# Run the application
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`

## 🌐 Production Deployment (Render + Neon)

### Required Environment Variables

```bash
# Database Configuration (Neon PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple
SPRING_DATASOURCE_USERNAME=your_neon_username
SPRING_DATASOURCE_PASSWORD=your_neon_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRATION=86400000

# CORS Configuration (Critical for Netlify frontend)
ALLOWED_ORIGIN_PATTERNS=https://habesha-community-frontend.netlify.app,https://*.netlify.app,http://localhost:3000

# Optional Services
MAIL_USERNAME=your_gmail_username
MAIL_PASSWORD=your_gmail_app_password
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
STRIPE_SECRET_KEY=your_stripe_secret
```

### Critical Neon Pooler Configuration

**IMPORTANT**: When using Neon's pooler, include these JDBC parameters to avoid "cached plan must not change result type" errors:

- `prepareThreshold=0` - Disables prepared statement caching
- `preferQueryMode=simple` - Uses simple query mode

**Example JDBC URL:**
```
jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple
```

## 🏗️ Architecture

### Database Management
- **Hibernate**: Automatic schema updates with `ddl-auto=update`
- **Flyway**: Database migrations for production safety
- **PostgreSQL**: Primary database with full compatibility

### Security
- **JWT Authentication**: Stateless authentication with configurable expiration
- **CORS**: Environment-driven configuration supporting Netlify deploy previews
- **Role-based Access**: Admin, Moderator, and User roles

### Key Features
- **File Uploads**: Local storage with configurable limits
- **Email Integration**: Gmail SMTP for notifications
- **Payment Processing**: Stripe integration for premium features
- **SMS Notifications**: Twilio integration for messaging
- **Health Checks**: Lightweight endpoints for monitoring

## 🔧 Configuration

### Database Schema
All entities use PostgreSQL-compatible JPA mappings:
- Binary data: `@Lob` with `byte[]` → PostgreSQL `BYTEA`
- Text fields: `@Column(length = 4000)` → PostgreSQL `TEXT`
- No MySQL-specific `columnDefinition` attributes

### CORS Configuration
Supports multiple origin patterns for development and production:
```properties
# Development + Production + Deploy Previews
ALLOWED_ORIGIN_PATTERNS=http://localhost:3000,https://*.netlify.app,https://your-domain.com
```

### Health Endpoints
1. **Simple Health Check**: `GET /health` - Returns "OK" (no dependencies)
2. **Actuator Health**: `GET /actuator/health` - Detailed status with DB connectivity

## 🐛 Troubleshooting

### Common Issues

**"cached plan must not change result type"**
- Ensure JDBC URL includes `prepareThreshold=0&preferQueryMode=simple`
- Verify you're using Neon pooler endpoint

**CORS preflight failures (OPTIONS 403)**
- Check `ALLOWED_ORIGIN_PATTERNS` includes your frontend domain
- Verify patterns include `https://*.netlify.app` for deploy previews
- Confirm OPTIONS requests are permitted in SecurityConfig

**"column does not exist" errors**
- Flyway migrations will create missing columns automatically
- Ensure database user has CREATE/ALTER permissions
- Check `SPRING_JPA_HIBERNATE_DDL_AUTO=update` is set

**Mail health check timeouts**
- Mail health indicator is disabled by default
- SMTP timeouts won't break `/actuator/health`
- Mail sending functionality remains available

### Deployment Checklist
- ✅ Set all required environment variables
- ✅ Use Neon pooler URL with correct parameters
- ✅ Verify `/health` returns 200 OK
- ✅ Test CORS with frontend domain
- ✅ Confirm database connectivity via `/actuator/health`

## 📚 API Documentation

When running, visit:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI Spec**: `http://localhost:8080/v3/api-docs`

## 🔒 Security Notes

- JWT secrets should be strong and environment-specific
- Database credentials should use environment variables
- CORS patterns should be restrictive in production
- File upload limits are enforced (15MB default)
- Mail health checks are disabled to prevent timeout issues

## 📝 Development Notes

### Adding New Entities
1. Create entity with PostgreSQL-compatible annotations
2. Add Flyway migration if needed (`src/main/resources/db/migration/`)
3. Update SecurityConfig for new endpoints
4. Add appropriate tests

### Database Migrations
- Flyway migrations run automatically on startup
- Use `V{version}__{description}.sql` naming convention
- Include conditional DDL for existing databases
- Test migrations on staging before production

For detailed deployment instructions, see [DEPLOY_NOTES.md](./DEPLOY_NOTES.md).