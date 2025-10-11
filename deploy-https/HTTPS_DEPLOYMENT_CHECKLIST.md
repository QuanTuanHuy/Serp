# HTTPS Deployment Checklist for SERP

## Pre-Deployment Checklist

### DNS Configuration
- [ ] A record for `serp.texkis.com` → Server IP
- [ ] A record for `api.serp.texkis.com` → Server IP  
- [ ] A record for `auth.serp.texkis.com` → Server IP
- [ ] DNS propagation verified (use `nslookup` or `dig`)

### Server Preparation
- [ ] Server accessible via SSH
- [ ] Docker installed (`docker --version`)
- [ ] Docker Compose installed (`docker-compose --version`)
- [ ] User has sudo privileges
- [ ] Git installed (if deploying from repository)

### Firewall Configuration
- [ ] Port 80 (HTTP) open
- [ ] Port 443 (HTTPS) open
- [ ] Port 5432 (PostgreSQL) **NOT** exposed to internet
- [ ] Port 6379 (Redis) **NOT** exposed to internet
- [ ] Firewall rules verified

### Email for Let's Encrypt
- [ ] Valid email address available
- [ ] Email updated in `nginx/obtain-ssl-cert.sh`

## Deployment Steps

### Step 1: Initial Setup
- [ ] SSH into server: `ssh openerp@your-server`
- [ ] Create directory: `sudo mkdir -p /home/openerp/serp`
- [ ] Set ownership: `sudo chown -R openerp:openerp /home/openerp/serp`
- [ ] Clone/copy project files to `/home/openerp/serp`
- [ ] Navigate to project: `cd /home/openerp/serp`

### Step 2: SSL Certificate Setup
- [ ] Update email in `nginx/obtain-ssl-cert.sh`
- [ ] Make script executable: `chmod +x nginx/obtain-ssl-cert.sh`
- [ ] Run certificate script: `./nginx/obtain-ssl-cert.sh`
- [ ] Verify certificates created in `nginx/ssl/live/serp.texkis.com/`
- [ ] Check certificate expiration date

### Step 3: Production Deployment
- [ ] Make deploy script executable: `chmod +x nginx/deploy-prod.sh`
- [ ] Review `docker-compose.prod.yml` configuration
- [ ] Update environment variables if needed
- [ ] Run deployment: `./nginx/deploy-prod.sh`
- [ ] Wait for all containers to start

### Step 4: Auto-Renewal Setup
- [ ] Make renewal script executable: `chmod +x nginx/setup-ssl-renewal.sh`
- [ ] Run renewal setup: `./nginx/setup-ssl-renewal.sh`
- [ ] Verify cron job created: `crontab -l`
- [ ] Check renewal log path exists

## Verification Checklist

### Container Health
- [ ] All containers running: `docker-compose -f docker-compose.prod.yml ps`
- [ ] Nginx container healthy
- [ ] Keycloak container healthy
- [ ] API Gateway container healthy
- [ ] Frontend container healthy
- [ ] PostgreSQL container healthy
- [ ] Redis container healthy
- [ ] Kafka container healthy

### SSL Certificate Verification
- [ ] Frontend HTTPS works: `curl -I https://serp.texkis.com`
- [ ] API Gateway HTTPS works: `curl -I https://api.serp.texkis.com/health`
- [ ] Keycloak HTTPS works: `curl -I https://auth.serp.texkis.com`
- [ ] No SSL warnings in browser
- [ ] Certificate chain valid
- [ ] Certificate not expired

### Service Accessibility
- [ ] Frontend loads in browser: https://serp.texkis.com
- [ ] API responds: https://api.serp.texkis.com/health
- [ ] Keycloak admin accessible: https://auth.serp.texkis.com/admin
- [ ] Can login to Keycloak admin console
- [ ] No CORS errors in browser console

### Security Headers
- [ ] HSTS header present
- [ ] X-Frame-Options header present
- [ ] X-Content-Type-Options header present
- [ ] Security headers verify: `curl -I https://serp.texkis.com | grep -i "strict-transport"`

### Logs Review
- [ ] Nginx access logs clean: `docker logs serp-nginx`
- [ ] Nginx error logs empty/minimal
- [ ] Keycloak started successfully
- [ ] API Gateway healthy
- [ ] No critical errors in any service

## Post-Deployment Configuration

### Keycloak Setup
- [ ] Login to admin console: https://auth.serp.texkis.com/admin
- [ ] Change admin password (IMPORTANT!)
- [ ] Create realm: `serp`
- [ ] Create client for frontend: `serp-web`
- [ ] Create client for API Gateway
- [ ] Configure client settings (redirect URIs, etc.)
- [ ] Test user login flow

### Frontend Configuration
- [ ] Create `.env.production` with correct URLs
- [ ] Verify environment variables loaded
- [ ] Test frontend → API communication
- [ ] Test frontend → Keycloak authentication

### Backend Services
- [ ] Verify services can connect to Keycloak
- [ ] Test JWT validation
- [ ] Verify database connections
- [ ] Test Redis cache
- [ ] Test Kafka messaging

## Security Hardening

### Passwords
- [ ] Keycloak admin password changed
- [ ] PostgreSQL password changed (if using default)
- [ ] Document new passwords in secure location

### Access Control
- [ ] SSH key-based authentication enabled
- [ ] Password SSH login disabled (optional)
- [ ] sudo requires password
- [ ] Keycloak admin console IP restricted (optional)

### Firewall Rules
- [ ] Only necessary ports exposed
- [ ] Database ports not accessible from internet
- [ ] Redis port not accessible from internet
- [ ] Kafka port not accessible from internet (if not needed)

### Monitoring Setup
- [ ] Log rotation configured
- [ ] Disk space monitoring setup
- [ ] SSL certificate expiration monitoring
- [ ] Service health check alerts

## Backup Configuration

### Backup Script
- [ ] Backup script created
- [ ] Script executable: `chmod +x /home/openerp/backup-serp.sh`
- [ ] Test backup manually
- [ ] Verify backup files created
- [ ] Check backup size reasonable

### Automated Backups
- [ ] Backup cron job created
- [ ] Backup schedule appropriate (daily at 2 AM)
- [ ] Backup retention policy defined
- [ ] Backup storage location has enough space

### Backup Contents
- [ ] PostgreSQL database backed up
- [ ] Redis data backed up
- [ ] Keycloak data backed up
- [ ] SSL certificates backed up
- [ ] Nginx configs backed up

## Maintenance Checklist

### Daily
- [ ] Check service health: `docker-compose -f docker-compose.prod.yml ps`
- [ ] Review error logs for issues
- [ ] Monitor disk usage

### Weekly
- [ ] Review access logs for anomalies
- [ ] Check SSL certificate expiration
- [ ] Update Docker images: `docker-compose -f docker-compose.prod.yml pull`
- [ ] Review backup logs

### Monthly
- [ ] Test backup restoration
- [ ] Review and rotate logs
- [ ] Security audit
- [ ] Performance review

## Troubleshooting Reference

### If SSL Certificate Fails
1. Check DNS: `nslookup serp.texkis.com`
2. Check ports: `sudo netstat -tulpn | grep -E ':(80|443)'`
3. Check Let's Encrypt logs
4. Try staging first: Add `--staging` flag

### If Container Won't Start
1. Check logs: `docker logs [container-name]`
2. Check environment variables
3. Check ports not in use
4. Check disk space: `df -h`

### If 502 Bad Gateway
1. Check backend service running
2. Check network: `docker network inspect serp_net`
3. Check Nginx config: `docker exec serp-nginx nginx -t`
4. Restart service: `docker-compose -f docker-compose.prod.yml restart [service]`

### If CORS Errors
1. Check `nginx/conf.d/api-gateway.conf` CORS headers
2. Verify frontend origin matches
3. Reload Nginx: `docker exec serp-nginx nginx -s reload`

## Rollback Plan

### If Deployment Fails
1. Stop new deployment: `docker-compose -f docker-compose.prod.yml down`
2. Switch to previous version
3. Start previous version
4. Investigate logs
5. Fix issues before retry

### If SSL Issues
1. Remove problematic certificates
2. Re-run `obtain-ssl-cert.sh`
3. Restart Nginx

## Documentation

### Update Documentation
- [ ] Document any configuration changes
- [ ] Update passwords in secure vault
- [ ] Note any deviations from standard setup
- [ ] Document custom modifications

### Knowledge Transfer
- [ ] Team briefed on deployment
- [ ] Access credentials shared (securely)
- [ ] Monitoring dashboards shared
- [ ] Escalation procedures documented

## Final Sign-Off

- [ ] All critical services running
- [ ] HTTPS working on all domains
- [ ] Authentication working
- [ ] Backups configured
- [ ] Monitoring in place
- [ ] Documentation complete
- [ ] Team trained

**Deployed by:** ___________________  
**Date:** ___________________  
**Sign-off:** ___________________

---

## Quick Commands Reference

```bash
# View all services
docker-compose -f docker-compose.prod.yml ps

# View logs
docker-compose -f docker-compose.prod.yml logs -f [service]

# Restart service
docker-compose -f docker-compose.prod.yml restart [service]

# Stop all
docker-compose -f docker-compose.prod.yml down

# Start all
docker-compose -f docker-compose.prod.yml up -d

# Check SSL cert
echo | openssl s_client -servername serp.texkis.com -connect serp.texkis.com:443 2>/dev/null | openssl x509 -noout -dates

# Renew SSL manually
/home/openerp/serp/nginx/renew-ssl.sh

# Backup
/home/openerp/backup-serp.sh

# Check disk space
df -h

# Check container resources
docker stats
```
