# HTTPS Deployment - Quick Start Guide

## Prerequisites Checklist

- [ ] Server with public IP address
- [ ] Domain: `serp.texkis.com` pointing to server IP
- [ ] Subdomain: `api.serp.texkis.com` pointing to server IP
- [ ] Subdomain: `auth.serp.texkis.com` pointing to server IP
- [ ] Ports 80, 443 open in firewall
- [ ] Docker and Docker Compose installed
- [ ] Valid email address for Let's Encrypt

## DNS Configuration

Add these A records to your DNS:
```
serp.texkis.com      A    <YOUR_SERVER_IP>
api.serp.texkis.com  A    <YOUR_SERVER_IP>
auth.serp.texkis.com A    <YOUR_SERVER_IP>
```

Verify DNS propagation:
```bash
nslookup serp.texkis.com
nslookup api.serp.texkis.com
nslookup auth.serp.texkis.com
```

## Deployment Steps

### 1. Prepare Server

```bash
# Connect to server
ssh openerp@your-server

# Create project directory
sudo mkdir -p /home/openerp/serp
sudo chown -R openerp:openerp /home/openerp/serp

# Clone repository
cd /home/openerp
git clone <your-repo-url> serp
cd serp
```

### 2. Update Configuration

Edit email in SSL certificate script:
```bash
nano nginx/obtain-ssl-cert.sh
# Change: EMAIL="your-email@example.com"
# To:     EMAIL="your-actual-email@example.com"
```

### 3. Obtain SSL Certificates

```bash
cd /home/openerp/serp
chmod +x nginx/obtain-ssl-cert.sh
./nginx/obtain-ssl-cert.sh
```

**Expected output:**
```
Obtaining SSL certificates from Let's Encrypt...
SUCCESS! SSL certificates obtained.
```

### 4. Deploy Production Environment

```bash
chmod +x nginx/deploy-prod.sh
./nginx/deploy-prod.sh
```

### 5. Setup Auto-Renewal

```bash
chmod +x nginx/setup-ssl-renewal.sh
./nginx/setup-ssl-renewal.sh
```

### 6. Verify Deployment

```bash
# Check all containers are running
docker-compose -f docker-compose.prod.yml ps

# Test HTTPS endpoints
curl -I https://serp.texkis.com
curl -I https://api.serp.texkis.com/health
curl -I https://auth.serp.texkis.com

# Check SSL certificate
openssl s_client -connect serp.texkis.com:443 -servername serp.texkis.com < /dev/null
```

### 7. Access Services

- **Frontend**: https://serp.texkis.com
- **API Gateway**: https://api.serp.texkis.com
- **Keycloak Admin**: https://auth.serp.texkis.com/admin
  - Username: `serp-admin`
  - Password: `serp-admin`

## Troubleshooting

### SSL Certificate Errors

**Problem**: Certificate not found
```bash
# Check if certificates exist
ls -la /home/openerp/serp/nginx/ssl/live/serp.texkis.com/

# If missing, re-run
./nginx/obtain-ssl-cert.sh
```

**Problem**: DNS validation failed
```bash
# Verify DNS is properly configured
dig serp.texkis.com +short
dig api.serp.texkis.com +short
dig auth.serp.texkis.com +short

# Wait for DNS propagation (can take up to 24 hours)
```

### Service Not Starting

```bash
# Check logs
docker-compose -f docker-compose.prod.yml logs -f [service-name]

# Common services to check:
docker logs serp-nginx
docker logs serp-keycloak
docker logs serp-api-gateway
docker logs serp-web
```

### Nginx Errors

```bash
# Test Nginx configuration
docker exec serp-nginx nginx -t

# Reload Nginx
docker exec serp-nginx nginx -s reload

# View error logs
docker exec serp-nginx cat /var/log/nginx/error.log
```

### 502 Bad Gateway

**Cause**: Backend service not running or unreachable

```bash
# Check if backend service is running
docker ps | grep serp

# Check network connectivity
docker exec serp-nginx ping serp-api-gateway
docker exec serp-nginx ping serp-keycloak
docker exec serp-nginx ping serp-web

# Restart specific service
docker-compose -f docker-compose.prod.yml restart serp-api-gateway
```

### Keycloak Admin Console Issues

**Problem**: Can't access admin console

```bash
# Check Keycloak logs
docker logs serp-keycloak -f

# Verify environment variables
docker exec serp-keycloak env | grep KC_

# Ensure proxy headers are working
docker exec serp-nginx cat /etc/nginx/conf.d/keycloak.conf | grep proxy_set_header
```

## Maintenance Commands

### View Logs
```bash
# All services
docker-compose -f docker-compose.prod.yml logs -f

# Specific service
docker-compose -f docker-compose.prod.yml logs -f serp-nginx
docker-compose -f docker-compose.prod.yml logs -f serp-keycloak
docker-compose -f docker-compose.prod.yml logs -f serp-api-gateway
```

### Restart Services
```bash
# All services
docker-compose -f docker-compose.prod.yml restart

# Specific service
docker-compose -f docker-compose.prod.yml restart serp-nginx
```

### Update Application
```bash
cd /home/openerp/serp
git pull
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
```

### Renew SSL Manually
```bash
/home/openerp/serp/nginx/renew-ssl.sh
```

### Backup Data
```bash
# Backup script
cat > /home/openerp/backup-serp.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/home/openerp/backups/$(date +%Y%m%d)"
mkdir -p "$BACKUP_DIR"

# Backup PostgreSQL
docker exec serp-postgres pg_dumpall -U serpuser > "$BACKUP_DIR/postgres.sql"

# Backup Redis
docker exec serp-redis redis-cli BGSAVE
cp /home/openerp/serp/data/redis/dump.rdb "$BACKUP_DIR/"

# Backup Keycloak data
cp -r /home/openerp/serp/data/keycloak "$BACKUP_DIR/"

echo "Backup completed: $BACKUP_DIR"
EOF

chmod +x /home/openerp/backup-serp.sh
```

## Monitoring

### Check SSL Certificate Expiration
```bash
echo | openssl s_client -servername serp.texkis.com -connect serp.texkis.com:443 2>/dev/null | openssl x509 -noout -dates
```

### Monitor Docker Resources
```bash
docker stats
```

### Check Disk Usage
```bash
df -h /home/openerp/serp/data
```

## Security Best Practices

1. **Change default passwords** for Keycloak admin
2. **Enable firewall** rules to only allow necessary ports
3. **Regular updates** of Docker images
4. **Monitor logs** for suspicious activity
5. **Backup regularly** (database, Redis, Keycloak)
6. **Use strong passwords** for all services
7. **Keep SSL certificates** up to date (auto-renewal handles this)

## Performance Tuning

### Nginx Worker Processes
Edit `nginx/nginx.conf`:
```nginx
worker_processes auto;  # Use all CPU cores
```

### Database Connection Pooling
Check service configurations for optimal pool sizes based on traffic.

### Redis Memory Limit
```bash
docker exec serp-redis redis-cli CONFIG SET maxmemory 512mb
docker exec serp-redis redis-cli CONFIG SET maxmemory-policy allkeys-lru
```

## Support

For issues not covered here:
1. Check service logs: `docker-compose -f docker-compose.prod.yml logs -f`
2. Review documentation in `docs/` folder
3. Verify all environment variables are correctly set
4. Ensure all dependencies are running: PostgreSQL, Redis, Kafka, Keycloak
