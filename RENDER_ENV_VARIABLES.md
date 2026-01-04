# Render Environment Variables Configuration

## Required Environment Variables for Production

### Database Configuration
```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple&sslmode=require
SPRING_DATASOURCE_USERNAME=your_neon_username
SPRING_DATASOURCE_PASSWORD=your_neon_password
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
```

### Security & JWT
```bash
JWT_SECRET=your_very_secure_jwt_secret_key_minimum_32_characters
JWT_EXPIRATION=86400000
```

### CORS Configuration (CRITICAL for Netlify Frontend)
```bash
ALLOWED_ORIGIN_PATTERNS=http://localhost:3000,https://*.netlify.app,https://habesha-community-frontend.netlify.app
FRONTEND_URL=https://habesha-community-frontend.netlify.app
```

### Production Settings
```bash
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
FLYWAY_ENABLED=true
```

### Optional Services (Won't break deployment if missing)
```bash
# Twilio SMS
TWILIO_ACCOUNT_SID=your_twilio_account_sid
TWILIO_AUTH_TOKEN=your_twilio_auth_token
TWILIO_PHONE_NUMBER=your_twilio_phone_number

# Stripe Payments
STRIPE_SECRET_KEY=sk_test_or_live_your_stripe_secret_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# Email
MAIL_USERNAME=your_gmail_username
MAIL_PASSWORD=your_gmail_app_password
```

### Application Configuration
```bash
APP_UPLOADS_ROOT=uploads
SPRING_JPA_SHOW_SQL=false
```

## CORS Configuration Details

The backend is configured to accept requests from:
1. **Local development:** `http://localhost:3000`, `http://localhost:3001`
2. **Netlify wildcard:** `https://*.netlify.app` (covers all Netlify subdomains)
3. **Specific Netlify URL:** `https://habesha-community-frontend.netlify.app`

### Important CORS Notes:
- `allowCredentials(true)` is enabled for authentication
- All standard HTTP methods are allowed: GET, POST, PUT, DELETE, PATCH, OPTIONS
- Preflight requests are cached for 1 hour for performance
- All headers are allowed (`*`) for maximum compatibility

## Security Considerations

### Secrets Management
⚠️ **IMPORTANT:** Never commit secrets to the repository!

After any secret exposure:
1. **Rotate all exposed secrets:**
   - JWT_SECRET
   - Database passwords
   - Email app passwords
   - Twilio tokens
   - Stripe keys

2. **Update Render environment variables**
3. **Redeploy the application**

### Environment Variable Security
- Use Render's encrypted environment variable storage
- Avoid logging sensitive values
- Use strong, unique secrets for each environment

## Deployment Verification

After setting environment variables:

1. **Check Health Endpoint:**
   ```bash
   curl https://your-app.onrender.com/actuator/health
   ```
   Expected: `{"status":"UP","components":{"db":{"status":"UP"}}}`

2. **Test CORS from Frontend:**
   ```javascript
   fetch('https://your-app.onrender.com/auth/register', {
     method: 'OPTIONS',
     headers: {
       'Origin': 'https://habesha-community-frontend.netlify.app'
     }
   })
   ```
   Expected: 200/204 response with CORS headers

3. **Monitor Logs:**
   Look for successful startup messages:
   ```
   ✅ CORS Configuration - Allowed Origin Patterns: [...]
   ✅ Started HabeshaCommunityBackendApplication in X.XXX seconds
   ✅ Tomcat started on port(s): 8080 (http)
   ```

## Troubleshooting

### Common Issues:

1. **CORS Errors:**
   - Verify `ALLOWED_ORIGIN_PATTERNS` includes exact Netlify URL
   - Check for trailing slashes or typos in URLs
   - Ensure no extra spaces in comma-separated values

2. **Database Connection Issues:**
   - Verify Neon URL includes required parameters
   - Check database credentials are correct
   - Ensure database is not sleeping (Neon free tier)

3. **Health Check Failures:**
   - Mail health indicator is disabled by default
   - Database connectivity issues will show in health response
   - Check application logs for specific errors

### Environment Variable Format:
```bash
# Correct format (no spaces around equals, proper escaping)
ALLOWED_ORIGIN_PATTERNS=http://localhost:3000,https://*.netlify.app,https://habesha-community-frontend.netlify.app

# Incorrect format (spaces, quotes)
ALLOWED_ORIGIN_PATTERNS = "http://localhost:3000, https://*.netlify.app"
```

## Production Checklist

- [ ] All required environment variables set
- [ ] Database connection working
- [ ] CORS origins include Netlify URL
- [ ] JWT secret is secure (32+ characters)
- [ ] Health endpoint returns 200 OK
- [ ] Flyway migrations complete successfully
- [ ] No sensitive data in logs
- [ ] Frontend can make API calls without CORS errors

**Backend is ready for production deployment! 🚀**