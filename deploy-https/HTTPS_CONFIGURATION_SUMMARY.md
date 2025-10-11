# HTTPS Configuration Summary for SERP

## What Has Been Created

### 1. Nginx Reverse Proxy Configuration
- **Main config**: `nginx/nginx.conf` - Base Nginx configuration with SSL settings
- **Virtual hosts**:
  - `nginx/conf.d/serp-web.conf` - Frontend (serp.texkis.com)
  - `nginx/conf.d/api-gateway.conf` - API Gateway (api.serp.texkis.com)
  - `nginx/conf.d/keycloak.conf` - Keycloak (auth.serp.texkis.com)

### 2. Docker Compose Production
- **File**: `docker-compose.prod.yml`
- Includes Nginx service with SSL certificate volumes
- All services configured for production with HTTPS

### 3. SSL Certificate Management Scripts
- `nginx/obtain-ssl-cert.sh` - Get initial SSL certificates from Let's Encrypt
- `nginx/setup-ssl-renewal.sh` - Setup automatic renewal cron job
- `nginx/deploy-prod.sh` - Complete production deployment script

### 4. Documentation
- `docs/deploy/nginx-https-setup.md` - Technical setup guide (English)
- `docs/deploy/HTTPS_QUICK_START.md` - Quick reference guide (English)
- `docs/deploy/HTTPS_SETUP_VI.md` - Complete guide in Vietnamese

### 5. Frontend Configuration
- `serp_web/Dockerfile` - Multi-stage Docker build for Next.js
- `serp_web/.env.production.example` - Production environment variables template
- `serp_web/next.config.ts` - Updated with standalone output mode

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     Internet (HTTPS)                        │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│              Nginx Reverse Proxy (Port 443)                 │
│  - SSL Termination (Let's Encrypt certificates)             │
│  - Security headers (HSTS, X-Frame-Options, etc.)           │
│  - Request routing based on domain                          │
└──────────────┬──────────────────┬──────────────────┬────────┘
               │                  │                  │
               ▼                  ▼                  ▼
   ┌─────────────────┐ ┌──────────────────┐ ┌────────────────┐
   │   serp-web      │ │ serp-api-gateway │ │ serp-keycloak  │
   │   (HTTP:3000)   │ │   (HTTP:8080)    │ │  (HTTP:8080)   │
   │   Next.js       │ │      Go          │ │   Keycloak     │
   └─────────────────┘ └──────────────────┘ └────────────────┘
```

## Domain Mapping

| Domain | Backend Service | Purpose |
|--------|----------------|---------|
| `serp.texkis.com` | serp-web:3000 | Main web application (React/Next.js) |
| `api.serp.texkis.com` | serp-api-gateway:8080 | REST API endpoints |
| `auth.serp.texkis.com` | serp-keycloak:8080 | Authentication & authorization |

## Security Features

### SSL/TLS
- **TLS 1.2 and 1.3** only
- Modern cipher suites
- **HSTS** (HTTP Strict Transport Security) with 1-year max-age
- **OCSP stapling** for certificate validation

### Security Headers
- `Strict-Transport-Security`: Force HTTPS for 1 year
- `X-Frame-Options`: Prevent clickjacking
- `X-Content-Type-Options`: Prevent MIME sniffing
- `X-XSS-Protection`: XSS filter
- `Referrer-Policy`: Control referrer information

### CORS Configuration
API Gateway configured with:
- Allow origin: `https://serp.texkis.com`
- Allow credentials: true
- Allowed methods: GET, POST, PUT, DELETE, OPTIONS, PATCH

## Deployment Workflow

### Prerequisites
1. DNS records pointing to server IP
2. Ports 80 and 443 open
3. Docker and Docker Compose installed
4. Valid email for Let's Encrypt

### Step-by-Step Deployment

```bash
# 1. Update email in SSL script
nano nginx/obtain-ssl-cert.sh
# Change: EMAIL="your-email@example.com"

# 2. Obtain SSL certificates
chmod +x nginx/obtain-ssl-cert.sh
./nginx/obtain-ssl-cert.sh

# 3. Deploy production
chmod +x nginx/deploy-prod.sh
./nginx/deploy-prod.sh

# 4. Setup auto-renewal
chmod +x nginx/setup-ssl-renewal.sh
./nginx/setup-ssl-renewal.sh
```

### Verification

```bash
# Test HTTPS endpoints
curl -I https://serp.texkis.com
curl -I https://api.serp.texkis.com/health
curl -I https://auth.serp.texkis.com

# Check SSL certificate
openssl s_client -connect serp.texkis.com:443 -servername serp.texkis.com < /dev/null
```

## SSL Certificate Auto-Renewal

### How It Works
1. Cron job runs daily at 3:00 AM
2. Certbot checks if certificates need renewal (within 30 days of expiration)
3. If renewal needed, obtains new certificates
4. Nginx automatically reloads with new certificates

### Manual Renewal
```bash
/home/openerp/serp/nginx/renew-ssl.sh
```

### Check Expiration
```bash
echo | openssl s_client -servername serp.texkis.com -connect serp.texkis.com:443 2>/dev/null | openssl x509 -noout -dates
```

## Service Configuration Updates

### Keycloak Environment Variables
```yaml
KC_HOSTNAME: auth.serp.texkis.com
KC_PROXY_HEADERS: xforwarded
KC_HTTP_ENABLED: true
KC_HOSTNAME_STRICT: false
```

### Backend Services (Java/Go)
Update Keycloak URL in all service configs:
```yaml
app:
  keycloak:
    server-url: https://auth.serp.texkis.com
```

### Frontend (Next.js)
Create `.env.production`:
```env
NEXT_PUBLIC_API_URL=https://api.serp.texkis.com
NEXT_PUBLIC_KEYCLOAK_URL=https://auth.serp.texkis.com
NEXT_PUBLIC_KEYCLOAK_REALM=serp
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=serp-web
```

## Maintenance Commands

### View Logs
```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker logs serp-nginx -f
docker logs serp-keycloak -f
docker logs serp-api-gateway -f
```

### Restart Services
```bash
docker-compose -f docker-compose.prod.yml restart [service-name]
```

### Update Application
```bash
cd /home/openerp/serp
git pull
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
```

## Troubleshooting

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| Certificate not found | SSL cert not obtained | Run `./nginx/obtain-ssl-cert.sh` |
| 502 Bad Gateway | Backend service down | Check logs, restart service |
| CORS errors | Incorrect CORS headers | Check `api-gateway.conf`, reload Nginx |
| Keycloak admin not loading | WebSocket issue | Check proxy headers in `keycloak.conf` |

### Debug Commands
```bash
# Test Nginx config
docker exec serp-nginx nginx -t

# Reload Nginx
docker exec serp-nginx nginx -s reload

# Check container network
docker network inspect serp_net

# View Nginx error log
docker exec serp-nginx cat /var/log/nginx/error.log
```

## Performance Optimization

### Nginx
- Worker processes: `auto` (uses all CPU cores)
- Gzip compression enabled
- Static file caching for Next.js assets

### SSL Session Caching
- Session cache: 10MB (can serve ~40,000 sessions)
- Session timeout: 10 minutes

### Connection Keep-Alive
- Keep-alive timeout: 65 seconds
- Reduces SSL handshake overhead

## Security Best Practices

1. **Change default passwords** immediately
   - Keycloak admin password
   - PostgreSQL password
   
2. **Regular updates**
   - Update Docker images weekly
   - Monitor security advisories
   
3. **Backup regularly**
   - Database (PostgreSQL)
   - Redis data
   - Keycloak configuration
   - SSL certificates
   
4. **Monitor logs**
   - Set up log aggregation
   - Watch for suspicious patterns
   
5. **Firewall rules**
   - Only expose ports 80, 443
   - Use IP whitelisting for admin interfaces

## Backup Strategy

### What to Backup
- PostgreSQL database
- Redis data
- Keycloak data
- SSL certificates
- Nginx configurations

### Backup Script
Located at: `nginx/deploy-prod.sh` (includes backup example in comments)

### Automated Backups
Setup daily backups via cron:
```bash
0 2 * * * /home/openerp/backup-serp.sh
```

## Monitoring

### Basic Monitoring
```bash
# Container resource usage
docker stats

# Disk usage
df -h /home/openerp/serp/data

# SSL certificate expiration
echo | openssl s_client -servername serp.texkis.com -connect serp.texkis.com:443 2>/dev/null | openssl x509 -noout -dates
```

### Advanced Monitoring (Optional)
Consider adding:
- Prometheus for metrics
- Grafana for dashboards
- ELK Stack for log aggregation
- Uptime monitoring (UptimeRobot, StatusCake)

## Next Steps

1. **Deploy to production server**
   ```bash
   # On server
   cd /home/openerp/serp
   ./nginx/obtain-ssl-cert.sh
   ./nginx/deploy-prod.sh
   ./nginx/setup-ssl-renewal.sh
   ```

2. **Configure Keycloak**
   - Login to https://auth.serp.texkis.com/admin
   - Change admin password
   - Create realm `serp`
   - Setup clients for services

3. **Update service configurations**
   - Update all Keycloak URLs to HTTPS
   - Update frontend environment variables
   - Update CORS origins

4. **Test thoroughly**
   - Test all user flows
   - Verify HTTPS on all pages
   - Check API endpoints
   - Test authentication

5. **Setup monitoring**
   - Configure log aggregation
   - Setup alerts for certificate expiration
   - Monitor service health

## Support & Documentation

- **Technical Guide**: `docs/deploy/nginx-https-setup.md`
- **Quick Start**: `docs/deploy/HTTPS_QUICK_START.md`
- **Vietnamese Guide**: `docs/deploy/HTTPS_SETUP_VI.md`
- **Main Architecture**: `docs/MICROSERVICES_ERP_ARCHITECTURE.md`

## Files Created/Modified

```
serp/
├── docker-compose.prod.yml (NEW)
├── nginx/ (NEW)
│   ├── nginx.conf
│   ├── conf.d/
│   │   ├── serp-web.conf
│   │   ├── api-gateway.conf
│   │   └── keycloak.conf
│   ├── obtain-ssl-cert.sh
│   ├── setup-ssl-renewal.sh
│   └── deploy-prod.sh
├── serp_web/
│   ├── Dockerfile (NEW)
│   ├── .env.production.example (NEW)
│   └── next.config.ts (MODIFIED)
└── docs/deploy/
    ├── nginx-https-setup.md (NEW)
    ├── HTTPS_QUICK_START.md (NEW)
    └── HTTPS_SETUP_VI.md (NEW)
```

---

**Author**: QuanTuanHuy  
**Project**: SERP - Smart ERP System  
**Last Updated**: $(date)
