# Storage Configuration Guide

This guide explains how to configure MinIO/S3 storage for the Discuss Service in different environments.

## The Signature Mismatch Problem

**Error:** `The request signature we calculated does not match the signature you provided. (Status Code: 403)`

**Root Causes:**
1. Missing or incorrect access credentials (`STORAGE_ACCESS_KEY`, `STORAGE_SECRET_KEY`)
2. Endpoint mismatch between internal server communication and client-accessible URLs
3. Time synchronization issues (server time differs > 15 minutes from actual time)

## Solution: Dual Endpoint Configuration

The service now supports **two separate endpoints**:

- **`STORAGE_ENDPOINT`**: Internal endpoint for server-to-MinIO communication
- **`STORAGE_PUBLIC_ENDPOINT`**: Public endpoint for client access (browsers, mobile apps)

This solves the Docker network hostname issue where services use `http://serp-minio:9000` internally but clients need `http://your-domain.com:9000`.

---

## Configuration Examples

### 1. Local Development (Without Docker)

```bash
# .env
STORAGE_ENDPOINT=http://localhost:9000
STORAGE_PUBLIC_ENDPOINT=http://localhost:9000
STORAGE_ACCESS_KEY=minioadmin
STORAGE_SECRET_KEY=minioadmin123
STORAGE_BUCKET=discuss-attachments
STORAGE_REGION=us-east-1
```

**Setup:**
```bash
# Start MinIO
docker run -d \
  -p 9000:9000 \
  -p 9001:9001 \
  -e MINIO_ROOT_USER=minioadmin \
  -e MINIO_ROOT_PASSWORD=minioadmin123 \
  --name minio \
  minio/minio server /data --console-address ":9001"

# Start service
cd discuss_service
./run-dev.sh
```

---

### 2. Docker Compose (Development)

```bash
# .env
STORAGE_ENDPOINT=http://serp-minio:9000
STORAGE_PUBLIC_ENDPOINT=http://localhost:9000
STORAGE_ACCESS_KEY=minioadmin
STORAGE_SECRET_KEY=minioadmin123
STORAGE_BUCKET=discuss-attachments
STORAGE_REGION=us-east-1
```

**Why two endpoints?**
- `STORAGE_ENDPOINT=http://serp-minio:9000` → Server uses Docker network hostname
- `STORAGE_PUBLIC_ENDPOINT=http://localhost:9000` → Clients access via host machine

**Setup:**
```bash
# Start infrastructure
docker-compose -f docker-compose.dev.yml up -d

# Start service (with Docker network access)
docker-compose -f docker-compose.dev.yml up discuss-service
```

---

### 3. Production (Docker + Reverse Proxy)

#### 3.1 Using Nginx/Traefik as Reverse Proxy

```bash
# .env.production
STORAGE_ENDPOINT=http://serp-minio:9000
STORAGE_PUBLIC_ENDPOINT=https://s3.yourdomain.com
STORAGE_ACCESS_KEY=<STRONG_ACCESS_KEY>
STORAGE_SECRET_KEY=<STRONG_SECRET_KEY>
STORAGE_BUCKET=serp-discuss-prod
STORAGE_REGION=us-east-1
```

**Nginx Configuration Example:**
```nginx
# /etc/nginx/sites-available/minio
server {
    listen 80;
    server_name s3.yourdomain.com;
    
    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name s3.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/s3.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/s3.yourdomain.com/privkey.pem;

    # Increase file upload size
    client_max_body_size 100M;

    location / {
        proxy_pass http://serp-minio:9000;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # For chunked upload
        proxy_buffering off;
        proxy_request_buffering off;
    }
}
```

#### 3.2 Using Host Machine IP (No Reverse Proxy)

```bash
# .env.production
STORAGE_ENDPOINT=http://serp-minio:9000
STORAGE_PUBLIC_ENDPOINT=http://192.168.1.100:9000  # Your server's IP
STORAGE_ACCESS_KEY=<STRONG_ACCESS_KEY>
STORAGE_SECRET_KEY=<STRONG_SECRET_KEY>
STORAGE_BUCKET=serp-discuss-prod
STORAGE_REGION=us-east-1
```

**Note:** This approach exposes MinIO directly. Not recommended for production.

---

### 4. Production (AWS S3)

```bash
# .env.production
# Leave STORAGE_ENDPOINT empty for AWS S3
STORAGE_ENDPOINT=
STORAGE_PUBLIC_ENDPOINT=
STORAGE_ACCESS_KEY=AKIAIOSFODNN7EXAMPLE
STORAGE_SECRET_KEY=wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
STORAGE_BUCKET=serp-discuss-production
STORAGE_REGION=ap-southeast-1
```

**Setup:**
1. Create IAM user with S3 permissions
2. Create S3 bucket: `serp-discuss-production`
3. Configure bucket policy for public read (if needed)
4. Enable CORS for client-side uploads

---

## MinIO Production Hardening

### 1. Change Default Credentials

```bash
# Generate strong credentials
ACCESS_KEY=$(openssl rand -hex 16)
SECRET_KEY=$(openssl rand -hex 32)

# Update docker-compose
docker-compose down
# Edit docker-compose.yml with new credentials
docker-compose up -d
```

### 2. Enable HTTPS

```yaml
# docker-compose.yml
services:
  serp-minio:
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER}
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD}
    command: server /data --console-address ":9001" --certs-dir /certs
    volumes:
      - minio_data:/data
      - ./certs:/certs  # Mount SSL certificates
```

**Generate self-signed certificate (for testing):**
```bash
mkdir -p certs
openssl req -x509 -nodes -days 365 -newkey rsa:2048 \
  -keyout certs/private.key \
  -out certs/public.crt \
  -subj "/CN=minio.local"
```

### 3. Create Access Key via MinIO Console

Instead of using root credentials:

1. Access MinIO Console: `http://localhost:9001`
2. Login with root credentials
3. Navigate to **Access Keys** → **Create Access Key**
4. Save the generated key pair
5. Use these credentials in `.env` instead of root user

---

## Troubleshooting

### Error: "Signature mismatch" (403)

**Check:**
1. Credentials are correct:
   ```bash
   docker exec serp-minio mc alias set local http://localhost:9000 $ACCESS_KEY $SECRET_KEY
   docker exec serp-minio mc ls local
   ```

2. Time synchronization:
   ```bash
   # Check server time
   date
   
   # Sync time (if needed)
   sudo ntpdate -s time.nist.gov
   ```

3. Endpoint configuration:
   ```bash
   # Verify endpoints in logs
   docker logs discuss-service | grep "S3 client"
   ```

### Error: "Connection refused"

**Check:**
1. MinIO is running:
   ```bash
   docker ps | grep minio
   curl http://localhost:9000/minio/health/live
   ```

2. Network connectivity:
   ```bash
   docker exec discuss-service ping serp-minio
   ```

### Error: "Bucket does not exist"

The service auto-creates buckets on startup. If it fails:

```bash
# Create bucket manually
docker exec serp-minio mc mb local/discuss-attachments

# Set public policy (if needed)
docker exec serp-minio mc anonymous set download local/discuss-attachments
```

---

## Testing Storage Configuration

```bash
# 1. Start services
./run-dev.sh

# 2. Upload a file via API
curl -X POST http://localhost:8092/discuss/api/v1/channels/1/messages \
  -H "Authorization: Bearer $JWT_TOKEN" \
  -F "content=Test message" \
  -F "attachments=@/path/to/file.jpg"

# 3. Check MinIO
docker exec serp-minio mc ls local/discuss-attachments --recursive

# 4. Access file via returned URL
# Should work from browser without authentication
```

---

## Environment Variables Reference

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `STORAGE_ENDPOINT` | Yes | - | Internal endpoint for server communication |
| `STORAGE_PUBLIC_ENDPOINT` | No | `${STORAGE_ENDPOINT}` | Public endpoint for client access |
| `STORAGE_ACCESS_KEY` | Yes | - | MinIO/S3 access key |
| `STORAGE_SECRET_KEY` | Yes | - | MinIO/S3 secret key |
| `STORAGE_BUCKET` | No | `discuss-attachments` | Bucket name |
| `STORAGE_REGION` | No | `us-east-1` | AWS region (required by SDK) |
| `PRESIGNED_URL_EXPIRY_MINUTES` | No | `60` | Expiry for on-demand URLs |
| `DOWNLOAD_URL_EXPIRY_DAYS` | No | `7` | Expiry for message attachment URLs |
| `MAX_FILE_SIZE` | No | `52428800` (50MB) | Max file size in bytes |
| `MAX_FILES_PER_MESSAGE` | No | `10` | Max attachments per message |

---

## Security Best Practices

1. **Never commit credentials** - Use `.env.example` for templates
2. **Use strong credentials** - Generate with `openssl rand -hex 32`
3. **Enable HTTPS** - Required for production
4. **Limit bucket access** - Use bucket policies
5. **Enable versioning** - Protect against accidental deletion
6. **Set up backup** - Use MinIO replication or AWS S3 versioning
7. **Monitor access logs** - Enable audit logging
8. **Rotate credentials** - Regularly update access keys

---

## Migration from Old Configuration

If upgrading from a version without `publicEndpoint`:

1. Add `STORAGE_PUBLIC_ENDPOINT` to `.env`:
   ```bash
   # For Docker Compose
   STORAGE_PUBLIC_ENDPOINT=http://localhost:9000
   
   # For production
   STORAGE_PUBLIC_ENDPOINT=https://s3.yourdomain.com
   ```

2. Restart service:
   ```bash
   ./run-dev.sh
   ```

3. Old configuration (without `STORAGE_PUBLIC_ENDPOINT`) still works by falling back to `STORAGE_ENDPOINT`.

---

For more details, see `application-prod.yml` and `.env.example`.
