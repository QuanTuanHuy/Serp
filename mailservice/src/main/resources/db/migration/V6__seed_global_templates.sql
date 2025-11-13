/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

-- ======================================
-- V6: Seed global email templates
-- ======================================

-- ==================== Verification Email Template ====================
INSERT INTO email_templates (
    tenant_id, name, code, description, subject, body_template, is_html, 
    variables_schema, default_values, type, language, category, 
    is_global, version, is_active, created_at, updated_at
) VALUES (
    NULL,
    'Email Verification Template',
    'VERIFICATION_EMAIL',
    'Default template for email verification',
    'Verify Your Email Address',
    '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #4F46E5; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
        .button { display: inline-block; background: #4F46E5; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Welcome to SERP!</h1>
        </div>
        <div class="content">
            <p>Hello <strong th:text="${userName}">User</strong>,</p>
            <p>Thank you for registering with SERP. Please verify your email address to activate your account.</p>
            <div style="text-align: center;">
                <a th:href="${verificationLink}" class="button">Verify Email Address</a>
            </div>
            <p>Or copy and paste this link into your browser:</p>
            <p style="word-break: break-all; color: #4F46E5;" th:text="${verificationLink}">verification-link</p>
            <p>This link will expire in <span th:text="${expirationMinutes}">30</span> minutes.</p>
            <p>If you did not create an account, please ignore this email.</p>
        </div>
        <div class="footer">
            <p>&copy; 2025 SERP. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
    true,
    '{"userName": "string", "verificationLink": "string", "expirationMinutes": "number"}',
    '{"expirationMinutes": 30}',
    'VERIFICATION',
    'en',
    'Authentication',
    true,
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ==================== Password Reset Template ====================
INSERT INTO email_templates (
    tenant_id, name, code, description, subject, body_template, is_html, 
    variables_schema, default_values, type, language, category, 
    is_global, version, is_active, created_at, updated_at
) VALUES (
    NULL,
    'Password Reset Template',
    'PASSWORD_RESET',
    'Default template for password reset',
    'Reset Your Password',
    '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #DC2626; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
        .button { display: inline-block; background: #DC2626; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .warning { background: #FEF3C7; border-left: 4px solid #F59E0B; padding: 10px; margin: 20px 0; }
        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Password Reset Request</h1>
        </div>
        <div class="content">
            <p>Hello <strong th:text="${userName}">User</strong>,</p>
            <p>We received a request to reset your password. Click the button below to create a new password:</p>
            <div style="text-align: center;">
                <a th:href="${resetLink}" class="button">Reset Password</a>
            </div>
            <p>Or copy and paste this link into your browser:</p>
            <p style="word-break: break-all; color: #DC2626;" th:text="${resetLink}">reset-link</p>
            <div class="warning">
                <strong>Security Notice:</strong> This link will expire in <span th:text="${expirationMinutes}">15</span> minutes.
            </div>
            <p><strong>If you did not request a password reset, please ignore this email and your password will remain unchanged.</strong></p>
        </div>
        <div class="footer">
            <p>&copy; 2025 SERP. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
    true,
    '{"userName": "string", "resetLink": "string", "expirationMinutes": "number"}',
    '{"expirationMinutes": 15}',
    'PASSWORD_RESET',
    'en',
    'Authentication',
    true,
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ==================== Welcome Email Template ====================
INSERT INTO email_templates (
    tenant_id, name, code, description, subject, body_template, is_html, 
    variables_schema, default_values, type, language, category, 
    is_global, version, is_active, created_at, updated_at
) VALUES (
    NULL,
    'Welcome Email Template',
    'WELCOME_EMAIL',
    'Welcome email for new users',
    'Welcome to SERP - Your Smart ERP Solution!',
    '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; text-align: center; border-radius: 5px 5px 0 0; }
        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
        .feature { background: white; padding: 15px; margin: 10px 0; border-left: 4px solid #667eea; }
        .button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🎉 Welcome to SERP!</h1>
        </div>
        <div class="content">
            <p>Hello <strong th:text="${userName}">User</strong>,</p>
            <p>Welcome to <strong th:text="${organizationName}">Organization</strong>! We''re excited to have you on board.</p>
            <h3>What''s Next?</h3>
            <div class="feature">
                <strong>📊 Explore Your Dashboard</strong>
                <p>Get insights into your tasks, projects, and team activities.</p>
            </div>
            <div class="feature">
                <strong>👥 Connect with Your Team</strong>
                <p>Collaborate seamlessly with your colleagues.</p>
            </div>
            <div class="feature">
                <strong>⚡ Boost Productivity</strong>
                <p>Leverage powerful tools to streamline your workflow.</p>
            </div>
            <div style="text-align: center;">
                <a th:href="${dashboardLink}" class="button">Go to Dashboard</a>
            </div>
            <p>If you have any questions, feel free to reach out to our support team.</p>
        </div>
        <div class="footer">
            <p>&copy; 2025 SERP. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
    true,
    '{"userName": "string", "organizationName": "string", "dashboardLink": "string"}',
    '{}',
    'WELCOME',
    'en',
    'Onboarding',
    true,
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ==================== Notification Template ====================
INSERT INTO email_templates (
    tenant_id, name, code, description, subject, body_template, is_html, 
    variables_schema, default_values, type, language, category, 
    is_global, version, is_active, created_at, updated_at
) VALUES (
    NULL,
    'Generic Notification Template',
    'NOTIFICATION_EMAIL',
    'Generic notification template',
    'New Notification from SERP',
    '<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <style>
        body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; }
        .header { background: #10B981; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
        .content { background: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
        .message { background: white; padding: 20px; border-radius: 5px; margin: 20px 0; }
        .button { display: inline-block; background: #10B981; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
        .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1 th:text="${notificationTitle}">Notification</h1>
        </div>
        <div class="content">
            <p>Hello <strong th:text="${userName}">User</strong>,</p>
            <div class="message">
                <p th:utext="${notificationMessage}">Notification message here</p>
            </div>
            <div th:if="${actionLink}" style="text-align: center;">
                <a th:href="${actionLink}" class="button">View Details</a>
            </div>
        </div>
        <div class="footer">
            <p>&copy; 2025 SERP. All rights reserved.</p>
        </div>
    </div>
</body>
</html>',
    true,
    '{"userName": "string", "notificationTitle": "string", "notificationMessage": "string", "actionLink": "string"}',
    '{"notificationTitle": "Notification"}',
    'NOTIFICATION',
    'en',
    'General',
    true,
    1,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- ==================== Comments ====================
COMMENT ON COLUMN email_templates.body_template IS 'Thymeleaf template with HTML markup';
COMMENT ON COLUMN email_templates.variables_schema IS 'JSON schema defining required template variables';
COMMENT ON COLUMN email_templates.is_global IS 'Global templates available to all tenants';
