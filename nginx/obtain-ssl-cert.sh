#!/bin/bash

# Author: QuanTuanHuy
# Description: Script to obtain SSL certificates for Serp Project

set -e

echo "================================================"
echo "SERP SSL Certificate Setup"
echo "================================================"

# Configuration
DOMAIN_MAIN="serp.texkis.com"
DOMAIN_API="api.serp.texkis.com"
DOMAIN_AUTH="auth.serp.texkis.com"
EMAIL="your-email@example.com"  # Change this!
SSL_DIR="/home/openerp/serp/nginx/ssl"
WWW_DIR="/home/openerp/serp/nginx/www"

# Validate email
if [ "$EMAIL" = "your-email@example.com" ]; then
    echo "ERROR: Please update the EMAIL variable in this script!"
    exit 1
fi

# Create directories
echo "Creating directories..."
mkdir -p "$SSL_DIR"
mkdir -p "$WWW_DIR"

# Check if certificates already exist
if [ -d "$SSL_DIR/live/$DOMAIN_MAIN" ]; then
    echo "WARNING: Certificates already exist at $SSL_DIR/live/$DOMAIN_MAIN"
    read -p "Do you want to renew them? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Aborted."
        exit 0
    fi
    RENEW_FLAG="--force-renewal"
else
    RENEW_FLAG=""
fi

# Stop Nginx if running (to free port 80)
echo "Stopping Nginx if running..."
docker stop serp-nginx 2>/dev/null || true

# Obtain certificates using Certbot standalone mode
echo "Obtaining SSL certificates from Let's Encrypt..."
docker run -it --rm \
    -v "$SSL_DIR:/etc/letsencrypt" \
    -v "$WWW_DIR:/var/www/certbot" \
    -p 80:80 \
    -p 443:443 \
    certbot/certbot certonly \
    --standalone \
    --preferred-challenges http \
    -d "$DOMAIN_MAIN" \
    -d "$DOMAIN_API" \
    -d "$DOMAIN_AUTH" \
    --email "$EMAIL" \
    --agree-tos \
    --non-interactive \
    $RENEW_FLAG

# Check if successful
if [ $? -eq 0 ]; then
    echo "================================================"
    echo "SUCCESS! SSL certificates obtained."
    echo "================================================"
    echo "Certificates location: $SSL_DIR/live/$DOMAIN_MAIN/"
    echo ""
    echo "Next steps:"
    echo "1. Start the production environment:"
    echo "   cd /home/openerp/serp"
    echo "   docker-compose -f docker-compose.prod.yml up -d"
    echo ""
    echo "2. Setup auto-renewal cron job:"
    echo "   ./setup-ssl-renewal.sh"
    echo ""
else
    echo "ERROR: Failed to obtain SSL certificates"
    echo "Please check:"
    echo "- DNS records are properly configured"
    echo "- Ports 80 and 443 are open"
    echo "- Domain names are correct"
    exit 1
fi
