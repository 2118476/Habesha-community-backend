# Habesha Community Production Deploy Notes

## Backend (Render) Environment Variables

### Required Database Variables (Neon PostgreSQL)
```
SPRING_DATASOURCE_URL=jdbc:postgresql://[neon-host]/[database]
SPRING_DATASOURCE_USERNAME=[username]
SPRING_DATASOURCE_PASSWORD=[password]
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect
```

### Required JWT Configuration
```
JWT_SECRET=[base64-encoded-secret-key]
JWT_EXPIRATION=86400000
```

### Required CORS Configuration
```
ALLOWED_ORIGIN_PATTERNS=https://habesha-community-frontend.netlify.app,http://localhost:3000,https://*.--habesha-community-frontend.netlify.app
FRONTEND_URL=https://habesha-community-frontend.netlify.app
```

### Optional Twilio (SMS) Configuration
```
TWILIO_ACCOUNT_SID=[your-twilio-account-sid]
TWILIO_AUTH_TOKEN=[your-twilio-auth-token]
TWILIO_PHONE_NUMBER=[your-twilio-phone-number]
```

### Optional Stripe (Payments) Configuration
```
STRIPE_SECRET_KEY=[your-stripe-secret-key]
STRIPE_WEBHOOK_SECRET=[your-stripe-webhook-secret]
```

### Optional Email Configuration (for password reset)
```
MAIL_USERNAME=[gmail-username]
MAIL_PASSWORD=[gmail-app-password]
```

### Optional File Upload Configuration
```
APP_UPLOADS_ROOT=uploads
```

## Frontend (Netlify) Environment Variables

### Required API Configuration
```
REACT_APP_API_URL=https://habesha-community-backend.onrender.com
```

### Optional Feed Configuration
```
REACT_APP_FEED_BACKEND=false
```

## Security Notes

- All secrets have been removed from the codebase and are now environment-driven
- The application.properties file no longer contains hardcoded credentials
- CORS is configured to accept requests from the production Netlify domain and preview deployments
- The backend will automatically use Render's PORT environment variable

## Deployment Process

1. Backend changes have been made to be fully environment-driven
2. Push changes to GitHub backend repository to trigger Render redeploy
3. Ensure all required environment variables are set in Render dashboard
4. Frontend is already configured correctly and should work with the backend once deployed