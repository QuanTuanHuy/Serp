# Cấu hình HTTPS cho hệ thống SERP

## Tổng quan

Hệ thống SERP sử dụng **Nginx reverse proxy** với **Let's Encrypt SSL certificates** để bảo mật tất cả các kết nối HTTPS.

### Kiến trúc

```
Internet (HTTPS:443)
    ↓
Nginx (SSL Termination)
    ↓
┌─────────────────────────────────────┐
│  Internal Services (HTTP)           │
│  ├── Frontend (serp_web:3000)       │
│  ├── API Gateway (8080)             │
│  ├── Keycloak (8080)                │
│  ├── Account Service (8081)         │
│  ├── CRM Service (8086)             │
│  └── Other Services...              │
└─────────────────────────────────────┘
```

### Domain Mapping

| Domain | Service | Internal Port |
|--------|---------|---------------|
| `serp.texkis.com` | Frontend | 3000 |
| `api.serp.texkis.com` | API Gateway | 8080 |
| `auth.serp.texkis.com` | Keycloak | 8080 |

## Yêu cầu trước khi triển khai

### 1. Cấu hình DNS

Tạo các A records trên nhà cung cấp DNS của bạn:

```
Type  Name                   Value
────────────────────────────────────────
A     serp.texkis.com       <IP_SERVER_CUA_BAN>
A     api.serp.texkis.com   <IP_SERVER_CUA_BAN>
A     auth.serp.texkis.com  <IP_SERVER_CUA_BAN>
```

**Kiểm tra DNS propagation:**
```bash
nslookup serp.texkis.com
nslookup api.serp.texkis.com
nslookup auth.serp.texkis.com
```

### 2. Mở ports trên Firewall

```bash
# Ubuntu/Debian
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw status

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=443/tcp
sudo firewall-cmd --reload
```

### 3. Cài đặt Docker và Docker Compose

```bash
# Kiểm tra phiên bản
docker --version
docker-compose --version

# Nếu chưa có, cài đặt Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Cài đặt Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

## Hướng dẫn triển khai từng bước

### Bước 1: Chuẩn bị môi trường

```bash
# SSH vào server
ssh openerp@your-server-ip

# Tạo thư mục dự án
sudo mkdir -p /home/openerp/serp
sudo chown -R openerp:openerp /home/openerp/serp

# Clone repository (hoặc copy files)
cd /home/openerp
git clone <repository-url> serp
cd serp
```

### Bước 2: Cấu hình email cho Let's Encrypt

Mở file và thay đổi email:
```bash
cd /home/openerp/serp
nano nginx/obtain-ssl-cert.sh
```

Tìm và sửa dòng:
```bash
EMAIL="your-email@example.com"  # Đổi thành email thật của bạn
```

### Bước 3: Lấy SSL Certificate

```bash
# Cấp quyền thực thi
chmod +x nginx/obtain-ssl-cert.sh

# Chạy script
./nginx/obtain-ssl-cert.sh
```

**Kết quả mong đợi:**
```
================================================
Obtaining SSL certificates from Let's Encrypt...
Successfully received certificate.
Certificate is saved at: /home/openerp/serp/nginx/ssl/live/serp.texkis.com/fullchain.pem
Key is saved at:         /home/openerp/serp/nginx/ssl/live/serp.texkis.com/privkey.pem
================================================
SUCCESS! SSL certificates obtained.
================================================
```

**Nếu gặp lỗi:**
- Kiểm tra DNS đã trỏ đúng IP chưa
- Kiểm tra port 80 và 443 đã mở chưa
- Đợi DNS propagation (có thể mất vài giờ)

### Bước 4: Triển khai Production

```bash
# Cấp quyền thực thi
chmod +x nginx/deploy-prod.sh

# Deploy
./nginx/deploy-prod.sh
```

Script này sẽ:
1. ✓ Kiểm tra SSL certificates
2. ✓ Tạo các thư mục cần thiết
3. ✓ Pull Docker images
4. ✓ Build application images
5. ✓ Start tất cả services
6. ✓ Kiểm tra trạng thái

### Bước 5: Cấu hình Auto-renewal SSL

```bash
# Cấp quyền thực thi
chmod +x nginx/setup-ssl-renewal.sh

# Setup cron job
./nginx/setup-ssl-renewal.sh
```

Cron job sẽ tự động gia hạn certificate mỗi ngày lúc 3h sáng.

**Kiểm tra cron job:**
```bash
crontab -l
```

### Bước 6: Xác nhận triển khai thành công

```bash
# Kiểm tra containers đang chạy
docker-compose -f docker-compose.prod.yml ps

# Test HTTPS endpoints
curl -I https://serp.texkis.com
curl -I https://api.serp.texkis.com/health
curl -I https://auth.serp.texkis.com

# Kiểm tra SSL certificate
openssl s_client -connect serp.texkis.com:443 -servername serp.texkis.com < /dev/null | grep -A 2 "Verify return code"
```

**Kết quả mong đợi:**
```
Verify return code: 0 (ok)
```

## Truy cập các dịch vụ

Sau khi triển khai thành công:

| Dịch vụ | URL | Thông tin đăng nhập |
|---------|-----|---------------------|
| **Frontend** | https://serp.texkis.com | (User account) |
| **API Gateway** | https://api.serp.texkis.com | (Token based) |
| **Keycloak Admin** | https://auth.serp.texkis.com/admin | Username: `serp-admin`<br>Password: `serp-admin` |

**⚠️ Quan trọng:** Đổi mật khẩu admin Keycloak ngay sau khi đăng nhập lần đầu!

## Cấu trúc thư mục

```
/home/openerp/serp/
├── docker-compose.prod.yml     # Production compose file
├── nginx/
│   ├── nginx.conf              # Main Nginx config
│   ├── conf.d/
│   │   ├── serp-web.conf      # Frontend HTTPS config
│   │   ├── api-gateway.conf   # API Gateway HTTPS config
│   │   └── keycloak.conf      # Keycloak HTTPS config
│   ├── ssl/                    # SSL certificates (auto-generated)
│   │   └── live/
│   │       └── serp.texkis.com/
│   │           ├── fullchain.pem
│   │           ├── privkey.pem
│   │           └── chain.pem
│   ├── www/                    # Webroot for Let's Encrypt
│   ├── logs/                   # Nginx access & error logs
│   ├── obtain-ssl-cert.sh      # Script lấy SSL certificate
│   ├── setup-ssl-renewal.sh    # Script setup auto-renewal
│   ├── renew-ssl.sh           # Script gia hạn SSL (auto-generated)
│   └── deploy-prod.sh         # Script deploy production
└── data/                       # Persistent data
    ├── postgres/
    ├── redis/
    ├── kafka/
    └── keycloak/
```

## Quản lý và Bảo trì

### Xem logs

```bash
# Tất cả services
docker-compose -f docker-compose.prod.yml logs -f

# Service cụ thể
docker-compose -f docker-compose.prod.yml logs -f serp-nginx
docker-compose -f docker-compose.prod.yml logs -f serp-keycloak
docker-compose -f docker-compose.prod.yml logs -f serp-api-gateway
docker-compose -f docker-compose.prod.yml logs -f serp-web

# Nginx access logs
docker exec serp-nginx tail -f /var/log/nginx/access.log

# Nginx error logs
docker exec serp-nginx tail -f /var/log/nginx/error.log
```

### Restart services

```bash
# Restart tất cả
docker-compose -f docker-compose.prod.yml restart

# Restart service cụ thể
docker-compose -f docker-compose.prod.yml restart serp-nginx
docker-compose -f docker-compose.prod.yml restart serp-keycloak
```

### Cập nhật ứng dụng

```bash
cd /home/openerp/serp

# Pull code mới
git pull

# Rebuild và restart
docker-compose -f docker-compose.prod.yml build
docker-compose -f docker-compose.prod.yml up -d
```

### Gia hạn SSL thủ công

```bash
# Chạy script renewal
/home/openerp/serp/nginx/renew-ssl.sh

# Hoặc chạy trực tiếp
docker run --rm \
  -v /home/openerp/serp/nginx/ssl:/etc/letsencrypt \
  -v /home/openerp/serp/nginx/www:/var/www/certbot \
  certbot/certbot renew --webroot -w /var/www/certbot

# Reload Nginx
docker exec serp-nginx nginx -s reload
```

### Kiểm tra SSL certificate expiration

```bash
echo | openssl s_client -servername serp.texkis.com -connect serp.texkis.com:443 2>/dev/null | openssl x509 -noout -dates
```

## Xử lý sự cố

### Lỗi 1: Certificate not found

**Triệu chứng:** Nginx không start, lỗi certificate not found

**Giải pháp:**
```bash
# Kiểm tra file certificate
ls -la /home/openerp/serp/nginx/ssl/live/serp.texkis.com/

# Nếu không tồn tại, lấy lại
./nginx/obtain-ssl-cert.sh
```

### Lỗi 2: 502 Bad Gateway

**Triệu chứng:** Truy cập web hiện lỗi 502

**Giải pháp:**
```bash
# Kiểm tra backend service có chạy không
docker ps | grep serp

# Kiểm tra logs của service bị lỗi
docker-compose -f docker-compose.prod.yml logs serp-api-gateway
docker-compose -f docker-compose.prod.yml logs serp-web

# Restart service
docker-compose -f docker-compose.prod.yml restart serp-api-gateway
```

### Lỗi 3: Keycloak admin console không load

**Triệu chứng:** Không truy cập được admin console

**Giải pháp:**
```bash
# Kiểm tra Keycloak logs
docker logs serp-keycloak -f

# Kiểm tra environment variables
docker exec serp-keycloak env | grep KC_

# Verify Nginx config
docker exec serp-nginx nginx -t

# Restart Keycloak
docker-compose -f docker-compose.prod.yml restart serp-keycloak
```

### Lỗi 4: CORS errors trên frontend

**Triệu chứng:** Browser console hiện CORS errors

**Giải pháp:**
Kiểm tra file `nginx/conf.d/api-gateway.conf` có đầy đủ CORS headers:
```nginx
add_header Access-Control-Allow-Origin "https://serp.texkis.com" always;
add_header Access-Control-Allow-Credentials "true" always;
```

Reload Nginx:
```bash
docker exec serp-nginx nginx -s reload
```

### Lỗi 5: Let's Encrypt rate limit

**Triệu chứng:** Không lấy được certificate, lỗi "too many certificates"

**Giải pháp:**
Let's Encrypt có giới hạn:
- 50 certificates per domain per week
- 5 duplicate certificates per week

Đợi 1 tuần hoặc dùng staging environment để test:
```bash
# Test với staging (không bị rate limit)
docker run -it --rm \
  -v /home/openerp/serp/nginx/ssl:/etc/letsencrypt \
  -v /home/openerp/serp/nginx/www:/var/www/certbot \
  -p 80:80 \
  certbot/certbot certonly --standalone \
  --staging \
  -d serp.texkis.com \
  -d api.serp.texkis.com \
  -d auth.serp.texkis.com \
  --email your-email@example.com \
  --agree-tos
```

## Backup và Restore

### Backup

```bash
#!/bin/bash
# Tạo file /home/openerp/backup-serp.sh

BACKUP_DIR="/home/openerp/backups/$(date +%Y%m%d_%H%M%S)"
mkdir -p "$BACKUP_DIR"

# Backup PostgreSQL
docker exec serp-postgres pg_dumpall -U serpuser > "$BACKUP_DIR/postgres.sql"

# Backup Redis
docker exec serp-redis redis-cli BGSAVE
sleep 5
docker cp serp-redis:/data/dump.rdb "$BACKUP_DIR/"

# Backup Keycloak data
cp -r /home/openerp/serp/data/keycloak "$BACKUP_DIR/"

# Backup SSL certificates
cp -r /home/openerp/serp/nginx/ssl "$BACKUP_DIR/"

# Backup configurations
cp -r /home/openerp/serp/nginx/conf.d "$BACKUP_DIR/"
cp /home/openerp/serp/docker-compose.prod.yml "$BACKUP_DIR/"

echo "Backup completed: $BACKUP_DIR"
```

**Chạy backup:**
```bash
chmod +x /home/openerp/backup-serp.sh
./backup-serp.sh
```

**Setup backup tự động (mỗi ngày lúc 2h sáng):**
```bash
crontab -e
# Thêm dòng:
0 2 * * * /home/openerp/backup-serp.sh
```

### Restore

```bash
# Restore PostgreSQL
cat /home/openerp/backups/YYYYMMDD_HHMMSS/postgres.sql | docker exec -i serp-postgres psql -U serpuser

# Restore Redis
docker cp /home/openerp/backups/YYYYMMDD_HHMMSS/dump.rdb serp-redis:/data/
docker restart serp-redis

# Restore Keycloak
docker-compose -f docker-compose.prod.yml down
cp -r /home/openerp/backups/YYYYMMDD_HHMMSS/keycloak/* /home/openerp/serp/data/keycloak/
docker-compose -f docker-compose.prod.yml up -d
```

## Bảo mật nâng cao

### 1. Đổi mật khẩu mặc định

```bash
# Keycloak admin
# Login vào https://auth.serp.texkis.com/admin
# Navigate to: Users → serp-admin → Credentials → Reset Password

# PostgreSQL
docker exec -it serp-postgres psql -U serpuser
ALTER USER serpuser WITH PASSWORD 'new-strong-password';
\q

# Update environment variables trong docker-compose.prod.yml
```

### 2. Giới hạn truy cập admin

Chỉnh sửa `nginx/conf.d/keycloak.conf`:
```nginx
location /admin/ {
    # Chỉ cho phép từ IP cụ thể
    allow 123.456.789.0/24;  # Your office IP
    deny all;
    
    proxy_pass http://serp-keycloak:8080/admin/;
    # ... rest of config
}
```

### 3. Enable fail2ban

```bash
sudo apt install fail2ban

# Tạo jail cho Nginx
sudo nano /etc/fail2ban/jail.local
```

Thêm:
```ini
[nginx-http-auth]
enabled = true
filter = nginx-http-auth
logpath = /home/openerp/serp/nginx/logs/*-error.log
maxretry = 5
bantime = 3600
```

### 4. Regular security updates

```bash
# Cập nhật Docker images hàng tuần
docker-compose -f docker-compose.prod.yml pull
docker-compose -f docker-compose.prod.yml up -d
```

## Monitoring

### Setup Prometheus + Grafana (Optional)

Xem file `docs/deploy/monitoring-setup.md` để cài đặt monitoring đầy đủ.

### Basic monitoring

```bash
# CPU, Memory usage
docker stats

# Disk usage
df -h /home/openerp/serp/data

# Nginx access patterns
docker exec serp-nginx tail -100 /var/log/nginx/access.log | awk '{print $1}' | sort | uniq -c | sort -rn
```

## Tài liệu tham khảo

- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)
- [Nginx SSL Configuration](https://nginx.org/en/docs/http/configuring_https_servers.html)
- [Keycloak Reverse Proxy](https://www.keycloak.org/server/reverseproxy)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

## Hỗ trợ

Nếu gặp vấn đề:
1. Kiểm tra logs: `docker-compose -f docker-compose.prod.yml logs -f`
2. Xem mục "Xử lý sự cố" ở trên
3. Kiểm tra [GitHub Issues](your-repo/issues)
4. Liên hệ team DevOps
