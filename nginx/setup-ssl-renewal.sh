#!/bin/bash

# Author: QuanTuanHuy
# Description: Setup automatic SSL certificate renewal for Serp Project

set -e

echo "================================================"
echo "SERP SSL Auto-Renewal Setup"
echo "================================================"

SSL_DIR="/home/openerp/serp/nginx/ssl"
WWW_DIR="/home/openerp/serp/nginx/www"

# Create renewal script
RENEWAL_SCRIPT="/home/openerp/serp/nginx/renew-ssl.sh"

cat > "$RENEWAL_SCRIPT" << 'EOF'
#!/bin/bash
# Auto-generated SSL renewal script for Serp Project

SSL_DIR="/home/openerp/serp/nginx/ssl"
WWW_DIR="/home/openerp/serp/nginx/www"

# Renew certificates
docker run --rm \
    -v "$SSL_DIR:/etc/letsencrypt" \
    -v "$WWW_DIR:/var/www/certbot" \
    certbot/certbot renew \
    --webroot \
    -w /var/www/certbot \
    --quiet

# Reload Nginx if renewal was successful
if [ $? -eq 0 ]; then
    docker exec serp-nginx nginx -s reload
    echo "$(date): SSL certificates renewed and Nginx reloaded" >> /home/openerp/serp/nginx/renewal.log
fi
EOF

chmod +x "$RENEWAL_SCRIPT"

# Create cron job
CRON_JOB="0 3 * * * $RENEWAL_SCRIPT"

# Check if cron job already exists
if crontab -l 2>/dev/null | grep -q "$RENEWAL_SCRIPT"; then
    echo "Cron job already exists for SSL renewal"
else
    # Add to crontab
    (crontab -l 2>/dev/null; echo "$CRON_JOB") | crontab -
    echo "Cron job added successfully!"
fi

echo "================================================"
echo "Auto-renewal setup completed!"
echo "================================================"
echo "Renewal script: $RENEWAL_SCRIPT"
echo "Cron schedule: Every day at 3:00 AM"
echo "Log file: /home/openerp/serp/nginx/renewal.log"
echo ""
echo "Current crontab:"
crontab -l | grep "$RENEWAL_SCRIPT"
echo ""
echo "To manually test renewal:"
echo "  $RENEWAL_SCRIPT"
