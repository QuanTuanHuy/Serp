# HTTPS Configuration for SERP System

## Overview
This guide sets up HTTPS for SERP microservices using Nginx reverse proxy with Let's Encrypt SSL certificates.

## Architecture
```
Internet (HTTPS:443)
    ↓
Nginx Reverse Proxy (SSL Termination)
    ↓
Internal Services (HTTP)
    ├── Keycloak (8180)
    ├── API Gateway (8080)
    ├── Frontend (3000)
    └── Other services
```

## Domain Mapping
- `serp.texkis.com` → Frontend (serp_web:3000)
- `api.serp.texkis.com` → API Gateway (8080)
- `auth.serp.texkis.com` → Keycloak (8180)

## Prerequisites
1. Domain DNS pointing to server IP:
   - A record: `serp.texkis.com` → Your_Server_IP
   - A record: `api.serp.texkis.com` → Your_Server_IP
   - A record: `auth.serp.texkis.com` → Your_Server_IP

2. Ports 80 and 443 open in firewall:
   ```bash
   sudo ufw allow 80/tcp
   sudo ufw allow 443/tcp
   ```

## Files Structure
```
serp/
├── docker-compose.prod.yml       # Production compose with Nginx
├── nginx/
│   ├── nginx.conf               # Main Nginx config
│   ├── conf.d/
│   │   ├── serp-web.conf       # Frontend config
│   │   ├── api-gateway.conf    # API Gateway config
│   │   └── keycloak.conf       # Keycloak config
│   └── ssl/                     # SSL certificates (auto-generated)
```

## Setup Steps

### 1. Initial HTTP Setup (for Let's Encrypt validation)
First, use HTTP-only config to obtain SSL certificates.

### 2. Obtain SSL Certificates
```bash
# Using Certbot Docker
docker run -it --rm \
  -v /home/openerp/serp/nginx/ssl:/etc/letsencrypt \
  -v /home/openerp/serp/nginx/www:/var/www/certbot \
  -p 80:80 \
  certbot/certbot certonly --standalone \
  -d serp.texkis.com \
  -d api.serp.texkis.com \
  -d auth.serp.texkis.com \
  --email your-email@example.com \
  --agree-tos \
  --non-interactive
```

### 3. Start Services with HTTPS
```bash
cd /home/openerp/serp
docker-compose -f docker-compose.prod.yml up -d
```

### 4. Auto-Renewal Setup
Add to crontab for automatic renewal:
```bash
crontab -e

# Add this line (runs every day at 3 AM)
0 3 * * * docker run --rm -v /home/openerp/serp/nginx/ssl:/etc/letsencrypt -v /home/openerp/serp/nginx/www:/var/www/certbot certbot/certbot renew --webroot -w /var/www/certbot && docker exec serp-nginx nginx -s reload
```

## Configuration Updates

### Keycloak Environment Variables
Update `KC_HOSTNAME` to use HTTPS:
```yaml
KC_HOSTNAME: auth.serp.texkis.com
KC_PROXY_HEADERS: xforwarded
KC_HTTP_ENABLED: true
KC_HOSTNAME_STRICT: false
```

### Frontend Environment
Update `.env.production`:
```env
NEXT_PUBLIC_API_URL=https://api.serp.texkis.com
NEXT_PUBLIC_KEYCLOAK_URL=https://auth.serp.texkis.com
```

### Backend Services
Update Keycloak URLs in application configs:
```yaml
app:
  keycloak:
    server-url: https://auth.serp.texkis.com
```

## Testing
```bash
# Test SSL certificate
openssl s_client -connect serp.texkis.com:443

# Check Nginx config
docker exec serp-nginx nginx -t

# View Nginx logs
docker logs serp-nginx -f
```

## Security Headers
Nginx is configured with security headers:
- HSTS (HTTP Strict Transport Security)
- X-Frame-Options
- X-Content-Type-Options
- X-XSS-Protection
- Referrer-Policy

## Troubleshooting

### Certificate Not Found
```bash
# Check certificate files
ls -la /home/openerp/serp/nginx/ssl/live/serp.texkis.com/
```

### 502 Bad Gateway
- Check if backend services are running
- Verify network connectivity: `docker network inspect serp_net`

### WebSocket Issues (Keycloak Admin Console)
Ensure `proxy_set_header Upgrade $http_upgrade;` is set in Nginx config.
