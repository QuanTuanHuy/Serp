#!/bin/bash

# Author: QuanTuanHuy
# Description: Deploy Serp Project with HTTPS

set -e

echo "================================================"
echo "SERP Production Deployment with HTTPS"
echo "================================================"

SERP_DIR="/home/openerp/serp"
cd "$SERP_DIR"

# Step 1: Verify SSL certificates
echo "Step 1: Verifying SSL certificates..."
if [ ! -f "$SERP_DIR/nginx/ssl/live/serp.texkis.com/fullchain.pem" ]; then
    echo "ERROR: SSL certificates not found!"
    echo "Please run: ./nginx/obtain-ssl-cert.sh"
    exit 1
fi
echo "✓ SSL certificates found"

# Step 2: Create necessary directories
echo "Step 2: Creating directories..."
mkdir -p "$SERP_DIR/nginx/logs"
mkdir -p "$SERP_DIR/data/redis"
mkdir -p "$SERP_DIR/data/postgres"
mkdir -p "$SERP_DIR/data/kafka"
mkdir -p "$SERP_DIR/data/keycloak/import"
echo "✓ Directories created"

# Step 3: Pull latest images
echo "Step 3: Pulling Docker images..."
docker-compose -f docker-compose.prod.yml pull

# Step 4: Build custom images
echo "Step 4: Building application images..."
docker-compose -f docker-compose.prod.yml build

# Step 5: Stop existing services
echo "Step 5: Stopping existing services..."
docker-compose -f docker-compose.prod.yml down

# Step 6: Start services
echo "Step 6: Starting services..."
docker-compose -f docker-compose.prod.yml up -d

# Step 7: Wait for services to be healthy
echo "Step 7: Waiting for services to start..."
sleep 10

# Step 8: Check service status
echo "Step 8: Checking service status..."
docker-compose -f docker-compose.prod.yml ps

# Step 9: Test SSL certificates
echo "Step 9: Testing SSL configuration..."
sleep 5

echo ""
echo "Testing HTTPS endpoints:"
echo "- Frontend: https://serp.texkis.com"
curl -k -I https://serp.texkis.com || echo "  (Service may still be starting...)"

echo "- API Gateway: https://api.serp.texkis.com/health"
curl -k -I https://api.serp.texkis.com/health || echo "  (Service may still be starting...)"

echo "- Keycloak: https://auth.serp.texkis.com"
curl -k -I https://auth.serp.texkis.com || echo "  (Service may still be starting...)"

echo ""
echo "================================================"
echo "Deployment completed!"
echo "================================================"
echo ""
echo "Services accessible at:"
echo "  Frontend:    https://serp.texkis.com"
echo "  API Gateway: https://api.serp.texkis.com"
echo "  Keycloak:    https://auth.serp.texkis.com"
echo ""
echo "Keycloak Admin Console:"
echo "  URL:      https://auth.serp.texkis.com/admin"
echo "  Username: serp-admin"
echo "  Password: serp-admin"
echo ""
echo "Useful commands:"
echo "  View logs:    docker-compose -f docker-compose.prod.yml logs -f [service]"
echo "  Restart:      docker-compose -f docker-compose.prod.yml restart [service]"
echo "  Stop all:     docker-compose -f docker-compose.prod.yml down"
echo "  Update:       ./nginx/deploy-prod.sh"
echo ""
