# SERP Deployment Documentation

This directory contains comprehensive documentation for deploying the SERP system with HTTPS.

## 📚 Documentation Files

### Quick Start
- **[HTTPS_QUICK_START.md](./HTTPS_QUICK_START.md)** - Quick reference guide for HTTPS setup (English)
- **[HTTPS_DEPLOYMENT_CHECKLIST.md](./HTTPS_DEPLOYMENT_CHECKLIST.md)** - Step-by-step deployment checklist

### Detailed Guides
- **[HTTPS_SETUP_VI.md](./HTTPS_SETUP_VI.md)** - Complete HTTPS setup guide in Vietnamese (Hướng dẫn tiếng Việt)
- **[nginx-https-setup.md](./nginx-https-setup.md)** - Technical Nginx & SSL configuration guide
- **[HTTPS_CONFIGURATION_SUMMARY.md](./HTTPS_CONFIGURATION_SUMMARY.md)** - Overview of HTTPS configuration

### Other Resources
- **[SERVER_DOCKER_COMPOSE_SETUP.md](./SERVER_DOCKER_COMPOSE_SETUP.md)** - Original server setup guide

## 🚀 Quick Deployment

For a quick deployment, follow these steps:

### 1. Prerequisites
```bash
# Ensure DNS is configured
nslookup serp.texkis.com
nslookup api.serp.texkis.com
nslookup auth.serp.texkis.com

# Ensure ports are open
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
```

### 2. Obtain SSL Certificates
```bash
cd /home/openerp/serp
chmod +x nginx/obtain-ssl-cert.sh
./nginx/obtain-ssl-cert.sh
```

### 3. Deploy Production
```bash
chmod +x nginx/deploy-prod.sh
./nginx/deploy-prod.sh
```

### 4. Setup Auto-Renewal
```bash
chmod +x nginx/setup-ssl-renewal.sh
./nginx/setup-ssl-renewal.sh
```

## 📖 Documentation Guide

### For First-Time Deployment
1. Start with **[HTTPS_DEPLOYMENT_CHECKLIST.md](./HTTPS_DEPLOYMENT_CHECKLIST.md)** - Follow this step-by-step
2. Reference **[HTTPS_QUICK_START.md](./HTTPS_QUICK_START.md)** for quick commands
3. Read **[HTTPS_SETUP_VI.md](./HTTPS_SETUP_VI.md)** for detailed Vietnamese guide

### For Understanding the Architecture
1. **[HTTPS_CONFIGURATION_SUMMARY.md](./HTTPS_CONFIGURATION_SUMMARY.md)** - High-level overview
2. **[nginx-https-setup.md](./nginx-https-setup.md)** - Technical details

### For Troubleshooting
1. Check **[HTTPS_SETUP_VI.md](./HTTPS_SETUP_VI.md)** section "Xử lý sự cố"
2. Check **[HTTPS_QUICK_START.md](./HTTPS_QUICK_START.md)** section "Troubleshooting"

## 🏗️ Architecture Overview

```
Internet (HTTPS:443)
    ↓
Nginx Reverse Proxy
    ├── serp.texkis.com → Frontend (Next.js)
    ├── api.serp.texkis.com → API Gateway (Go)
    └── auth.serp.texkis.com → Keycloak (Auth)
```

## 🔧 Configuration Files

### Nginx Configuration
```
nginx/
├── nginx.conf                  # Main Nginx config
├── conf.d/
│   ├── serp-web.conf          # Frontend HTTPS config
│   ├── api-gateway.conf       # API Gateway HTTPS config
│   └── keycloak.conf          # Keycloak HTTPS config
└── ssl/                        # SSL certificates (auto-generated)
```

### Docker Compose
- **docker-compose.prod.yml** - Production environment with Nginx

### Deployment Scripts
```
nginx/
├── obtain-ssl-cert.sh         # Get SSL certificates
├── setup-ssl-renewal.sh       # Setup auto-renewal
├── deploy-prod.sh             # Deploy production
└── renew-ssl.sh              # Renew SSL (auto-generated)
```

## 📝 Services Configuration

### Domain Mapping
| Domain | Service | Port |
|--------|---------|------|
| serp.texkis.com | Frontend (Next.js) | 3000 |
| api.serp.texkis.com | API Gateway (Go) | 8080 |
| auth.serp.texkis.com | Keycloak | 8080 |

### SSL Certificate
- **Provider**: Let's Encrypt
- **Validity**: 90 days
- **Auto-renewal**: Daily check at 3:00 AM
- **Domains**: 
  - serp.texkis.com
  - api.serp.texkis.com
  - auth.serp.texkis.com

## 🛡️ Security Features

- ✅ TLS 1.2 and 1.3 only
- ✅ Modern cipher suites
- ✅ HSTS with 1-year max-age
- ✅ Security headers (X-Frame-Options, CSP, etc.)
- ✅ OCSP stapling
- ✅ CORS configuration
- ✅ Automatic SSL renewal

## 📊 Monitoring & Maintenance

### Health Checks
```bash
# Check all services
docker-compose -f docker-compose.prod.yml ps

# Test HTTPS
curl -I https://serp.texkis.com
curl -I https://api.serp.texkis.com/health
curl -I https://auth.serp.texkis.com
```

### View Logs
```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker logs serp-nginx -f
docker logs serp-keycloak -f
```

### SSL Certificate Status
```bash
# Check expiration
echo | openssl s_client -servername serp.texkis.com \
  -connect serp.texkis.com:443 2>/dev/null | \
  openssl x509 -noout -dates
```

## 🔄 Update & Maintenance

### Update Application
```bash
cd /home/openerp/serp
git pull
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
```

### Renew SSL Certificate (Manual)
```bash
/home/openerp/serp/nginx/renew-ssl.sh
```

### Backup
```bash
# Create backup script from documentation
# See HTTPS_SETUP_VI.md section "Backup và Restore"
/home/openerp/backup-serp.sh
```

## 🆘 Troubleshooting

### Common Issues

| Issue | Quick Fix |
|-------|-----------|
| 502 Bad Gateway | Check backend service: `docker logs [service]` |
| Certificate not found | Run: `./nginx/obtain-ssl-cert.sh` |
| CORS errors | Check `nginx/conf.d/api-gateway.conf` |
| Keycloak admin not loading | Check WebSocket headers in Nginx config |

### Emergency Commands
```bash
# Restart all services
docker-compose -f docker-compose.prod.yml restart

# Stop all services
docker-compose -f docker-compose.prod.yml down

# View Nginx error log
docker exec serp-nginx cat /var/log/nginx/error.log

# Test Nginx config
docker exec serp-nginx nginx -t
```

## 📚 Additional Resources

### External Documentation
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [Nginx SSL Configuration](https://nginx.org/en/docs/http/configuring_https_servers.html)
- [Keycloak Reverse Proxy](https://www.keycloak.org/server/reverseproxy)
- [Docker Compose](https://docs.docker.com/compose/)

### Project Documentation
- [Main Architecture](../MICROSERVICES_ERP_ARCHITECTURE.md)
- [Keycloak Integration](../keycloak/)
- [Service Communication](../be_communication/)

## 📞 Support

For issues or questions:
1. Check the troubleshooting section in relevant documentation
2. Review service logs: `docker-compose -f docker-compose.prod.yml logs -f`
3. Consult the [deployment checklist](./HTTPS_DEPLOYMENT_CHECKLIST.md)
4. Contact DevOps team

## 🎯 Next Steps After Deployment

1. ✅ Verify all services running with HTTPS
2. ✅ Change Keycloak admin password
3. ✅ Configure Keycloak realm and clients
4. ✅ Setup monitoring and alerts
5. ✅ Configure backups
6. ✅ Test user flows
7. ✅ Document any custom changes

---

**Author**: QuanTuanHuy  
**Project**: SERP - Smart ERP System  
**Last Updated**: 2025-01-11
