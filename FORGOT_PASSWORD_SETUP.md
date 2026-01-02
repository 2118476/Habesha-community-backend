# Forgot Password Implementation - Setup Guide

## Overview
This implementation provides a complete forgot password flow with email functionality using Gmail SMTP.

## Backend Changes Made

### 1. Dependencies
- ✅ `spring-boot-starter-mail` already exists in pom.xml

### 2. Configuration (application.properties)
```properties
# Mail configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${MAIL_USERNAME}
spring.mail.password=${MAIL_PASSWORD}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Frontend URL for reset links
app.frontend-url=${FRONTEND_URL}
```

### 3. New Files Created
- `PasswordResetToken.java` - Entity for managing reset tokens with expiration
- `PasswordResetTokenRepository.java` - Repository for token operations
- `MailService.java` - Service for sending emails
- `ForgotPasswordRequest.java` - DTO for forgot password requests

### 4. Updated Files
- `PasswordController.java` - Enhanced with proper email functionality and security
- `DebugController.java` - Added mail testing endpoint
- `.env.example` - Added mail configuration examples

### 5. Database Changes
A new table `password_reset_tokens` will be created automatically with:
- `id` (Primary Key)
- `token` (Unique reset token)
- `expires_at` (Token expiration timestamp)
- `used` (Boolean flag)
- `user_id` (Foreign key to users table)
- `created_at` (Creation timestamp)

## Frontend Changes Made

### 1. New Pages
- `ForgotPassword.js` - Page for requesting password reset
- `ResetPassword.js` - Page for setting new password with token

### 2. Updated Files
- `authReset.js` - Simplified API calls to match backend endpoints
- `Account.jsx` - Updated forgot password link
- `App.js` - Added routes for new pages
- `Account.css` - Added styles for new components

### 3. New Routes
- `/forgot-password` - Forgot password form
- `/reset-password?token=TOKEN` - Reset password form

## Setup Instructions

### 1. Environment Variables
Create a `.env` file in the backend directory with:
```env
MAIL_USERNAME=your_gmail@gmail.com
MAIL_PASSWORD=your_app_password
FRONTEND_URL=http://localhost:3000
```

### 2. Gmail App Password Setup
1. Enable 2-Factor Authentication on your Gmail account
2. Go to Google Account settings > Security > App passwords
3. Generate an app password for "Mail"
4. Use this app password (not your regular password) in MAIL_PASSWORD

### 3. Test the Implementation

#### Backend Testing
1. Start the backend server
2. Test email functionality:
   ```
   GET http://localhost:8080/debug/mail-test?to=your-email@example.com
   ```
3. Test forgot password:
   ```
   POST http://localhost:8080/auth/forgot-password
   Content-Type: application/json
   
   {
     "email": "existing-user@example.com"
   }
   ```

#### Frontend Testing
1. Start the frontend server
2. Navigate to `/login`
3. Click "Forgot your password?"
4. Enter email and submit
5. Check email for reset link
6. Click reset link to set new password

## API Endpoints

### POST /auth/forgot-password
Request password reset for an email address.

**Request:**
```json
{
  "email": "user@example.com"
}
```

**Response:**
```json
{
  "message": "If an account with that email exists, a password reset link has been sent."
}
```

### POST /auth/reset-password
Reset password using a valid token.

**Request:**
```json
{
  "token": "reset-token-here",
  "newPassword": "newSecurePassword123"
}
```

**Response:**
```json
{
  "message": "Password reset successfully"
}
```

### GET /debug/mail-test (Development Only)
Test email functionality.

**Parameters:**
- `to` - Email address to send test email

**Response:**
```json
{
  "message": "Test email sent successfully to email@example.com"
}
```

## Security Features

1. **Token Expiration**: Tokens expire after 30 minutes
2. **Single Use**: Tokens can only be used once
3. **User Enumeration Protection**: Always returns success message regardless of email existence
4. **Secure Token Generation**: Uses UUID for unpredictable tokens
5. **Token Cleanup**: Old tokens are automatically cleaned up

## Troubleshooting

### Email Not Sending
1. Check Gmail app password is correct
2. Verify 2FA is enabled on Gmail account
3. Check backend logs for mail errors
4. Test with debug endpoint first

### Frontend Issues
1. Ensure routes are properly configured
2. Check browser console for JavaScript errors
3. Verify API calls are reaching backend

### Database Issues
1. Ensure database is running
2. Check if PasswordResetToken table was created
3. Verify foreign key constraints

## Production Considerations

1. **Remove Debug Endpoint**: Delete `/debug/mail-test` endpoint before production
2. **Environment Variables**: Use proper secret management for production
3. **Rate Limiting**: Consider adding rate limiting to prevent abuse
4. **Email Templates**: Consider using HTML email templates for better UX
5. **Monitoring**: Add monitoring for email delivery failures
6. **Cleanup Job**: Consider adding a scheduled job to clean up expired tokens

## Next Steps

1. Test the complete flow end-to-end
2. Remove the debug endpoint when satisfied
3. Consider adding email templates for better user experience
4. Add rate limiting if needed
5. Monitor email delivery in production