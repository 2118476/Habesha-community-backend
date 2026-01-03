# Render Deployment Checklist

## ✅ Pre-Deployment Verification

- [x] **Code committed and pushed to GitHub**
- [x] **Build successful** (`mvn clean package -DskipTests`)
- [x] **PostgreSQL driver updated** (42.7.2 - security fix)
- [x] **Flyway migrations created** (V1, V2, V3, V4)
- [x] **CORS configuration enhanced** for Netlify
- [x] **Health endpoint configured** (`/actuator/health`)
- [x] **Neon pooler compatibility** added

## 🚀 Render Deployment Steps

### 1. Create New Web Service
1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Click "New" → "Web Service"
3. Connect your GitHub repository: `Habesha-community-backend`
4. Select the `main` branch

### 2. Configure Build Settings
```
Build Command: mvn clean package -DskipTests
Start Command: java -jar target/habesha-community-backend-0.0.1-SNAPSHOT.jar
```

### 3. Set Environment Variables

#### Required Variables
```bash
# Database (Neon PostgreSQL)
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxx-pooler.us-east-1.aws.neon.tech/habesha?prepareThreshold=0&preferQueryMode=simple&sslmode=require
SPRING_DATASOURCE_USERNAME=your_neon_username
SPRING_DATASOURCE_PASSWORD=your_neon_password

# Security
JWT_SECRET=your_very_long_secure_jwt_secret_key_minimum_32_characters

# CORS (Update with your actual Netlify URL)
ALLOWED_ORIGIN_PATTERNS=https://your-app.netlify.app,https://*.netlify.app,http://localhost:3000
FRONTEND_URL=https://your-app.netlify.app

# Production Settings
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
FLYWAY_ENABLED=true
```

#### Optional Variables (won't break deployment if missing)
```bash
# Twilio SMS
TWILIO_ACCOUNT_SID=your_twilio_sid
TWILIO_AUTH_TOKEN=your_twilio_token
TWILIO_PHONE_NUMBER=your_twilio_phone

# Stripe Payments
STRIPE_SECRET_KEY=sk_test_or_live_your_stripe_key
STRIPE_WEBHOOK_SECRET=whsec_your_webhook_secret

# Email
MAIL_USERNAME=your_gmail_username
MAIL_PASSWORD=your_gmail_app_password
```

### 4. Configure Health Check
- **Health Check Path**: `/actuator/health`
- **Expected Response**: `{"status":"UP"}`

### 5. Deploy
1. Click "Create Web Service"
2. Wait for initial deployment (5-10 minutes)
3. Monitor logs for any issues

## 🔍 Post-Deployment Verification

### Test Health Endpoint
```bash
curl https://your-app.onrender.com/actuator/health
# Expected: {"status":"UP","components":{"db":{"status":"UP"}}}
```

### Test CORS from Frontend
```javascript
// Should work without CORS errors
fetch('https://your-app.onrender.com/auth/login', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    email: 'test@example.com',
    password: 'password'
  })
});
```

### Verify Database Schema
Check Render logs for:
```
Flyway Community Edition x.x.x by Redgate
Successfully applied 4 migrations to schema "public"
```

## 🐛 Troubleshooting

### Common Issues & Solutions

1. **"cached plan must not change result type"**
   - ✅ Fixed: Neon URL includes `prepareThreshold=0&preferQueryMode=simple`

2. **CORS preflight failures**
   - ✅ Fixed: OPTIONS requests permitted first in security chain
   - Verify `ALLOWED_ORIGIN_PATTERNS` includes your Netlify URL

3. **Health check failures**
   - ✅ Fixed: Mail health indicator disabled
   - Check database connectivity in logs

4. **Build failures**
   - ✅ Fixed: PostgreSQL driver updated, no conflicting dependencies
   - Check Java version (should be 17)

### Log Monitoring
Watch for these success indicators:
```
✅ Started HabeshaCommunityBackendApplication in X.XXX seconds
✅ Flyway Community Edition - Successfully applied X migrations
✅ HikariPool-1 - Start completed
✅ Tomcat started on port(s): 8080 (http)
```

## 📋 Environment Variables Summary

Copy this to Render's environment variables section:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://YOUR_NEON_URL?prepareThreshold=0&preferQueryMode=simple&sslmode=require
SPRING_DATASOURCE_USERNAME=YOUR_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD

# Security
JWT_SECRET=YOUR_SECURE_JWT_SECRET_32_CHARS_MINIMUM

# CORS
ALLOWED_ORIGIN_PATTERNS=https://YOUR_APP.netlify.app,https://*.netlify.app
FRONTEND_URL=https://YOUR_APP.netlify.app

# Production
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
FLYWAY_ENABLED=true
```

## ✅ Deployment Complete

Once deployed successfully:
1. Update frontend API base URL to your Render URL
2. Test authentication flow end-to-end
3. Verify file uploads work
4. Check all API endpoints respond correctly

**Your backend is now production-ready! 🎉**