#!/bin/bash
#
# Author: QuanTuanHuy
# Description: Generate mTLS certificates for SERP internal gRPC services
#
# Usage: ./scripts/generate-grpc-certs.sh
#

set -e

CERTS_DIR="./certs"
VALIDITY_DAYS=365
KEY_SIZE=2048
CA_KEY_SIZE=4096

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== SERP gRPC mTLS Certificate Generator ===${NC}"
echo ""

# Create certs directory if not exists
mkdir -p "$CERTS_DIR"

# Services that need certificates
SERVICES=("account" "crm" "discuss" "notification" "ptm-task" "ptm-schedule" "logistics" "purchase" "sales")

# Check if CA already exists
if [ -f "$CERTS_DIR/ca.crt" ] && [ -f "$CERTS_DIR/ca.key" ]; then
    echo -e "${YELLOW}CA certificate already exists. Skipping CA generation.${NC}"
    echo "Delete $CERTS_DIR/ca.crt and $CERTS_DIR/ca.key to regenerate."
else
    echo -e "${GREEN}Generating Root CA...${NC}"
    
    # Generate CA private key
    openssl genrsa -out "$CERTS_DIR/ca.key" $CA_KEY_SIZE 2>/dev/null
    
    # Generate CA certificate
    openssl req -new -x509 -days $VALIDITY_DAYS \
        -key "$CERTS_DIR/ca.key" \
        -out "$CERTS_DIR/ca.crt" \
        -subj "/C=VN/ST=Hanoi/L=Hanoi/O=SERP/OU=Internal/CN=SERP Internal CA" \
        2>/dev/null
    
    echo -e "${GREEN}Root CA generated${NC}"
fi

echo ""
echo -e "${GREEN}Generating service certificates...${NC}"

for service in "${SERVICES[@]}"; do
    echo -n "  Generating cert for $service-service... "
    
    # Generate service private key
    openssl genrsa -out "$CERTS_DIR/${service}.key" $KEY_SIZE 2>/dev/null
    
    # Create certificate signing request
    openssl req -new \
        -key "$CERTS_DIR/${service}.key" \
        -out "$CERTS_DIR/${service}.csr" \
        -subj "/C=VN/ST=Hanoi/L=Hanoi/O=SERP/OU=Internal/CN=${service}-service" \
        2>/dev/null
    
    # Create extension file for SAN (Subject Alternative Names)
    cat > "$CERTS_DIR/${service}.ext" <<EOF
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = ${service}-service
DNS.2 = ${service}-service.serp.svc.cluster.local
DNS.3 = localhost
IP.1 = 127.0.0.1
EOF

    # Sign certificate with CA
    openssl x509 -req -days $VALIDITY_DAYS \
        -in "$CERTS_DIR/${service}.csr" \
        -CA "$CERTS_DIR/ca.crt" \
        -CAkey "$CERTS_DIR/ca.key" \
        -CAcreateserial \
        -out "$CERTS_DIR/${service}.crt" \
        -extfile "$CERTS_DIR/${service}.ext" \
        2>/dev/null
    
    # Clean up CSR and extension file
    rm -f "$CERTS_DIR/${service}.csr" "$CERTS_DIR/${service}.ext"
    
    echo -e "${GREEN}Done${NC}"
done

# Clean up CA serial file
rm -f "$CERTS_DIR/ca.srl"

echo ""
echo -e "${GREEN}=== Certificate Generation Complete ===${NC}"
echo ""
echo "Generated files in $CERTS_DIR/:"
ls -la "$CERTS_DIR/"
echo ""
echo -e "${YELLOW}Important:${NC}"
echo "  - Keep ca.key secure! It can sign new certificates."
echo "  - Distribute ca.crt to all services for trust verification."
echo "  - Each service needs: ca.crt, <service>.crt, <service>.key"
echo ""
echo -e "${GREEN}To verify a certificate:${NC}"
echo "  openssl verify -CAfile $CERTS_DIR/ca.crt $CERTS_DIR/account.crt"
echo ""
echo -e "${GREEN}To view certificate details:${NC}"
echo "  openssl x509 -in $CERTS_DIR/account.crt -text -noout"
