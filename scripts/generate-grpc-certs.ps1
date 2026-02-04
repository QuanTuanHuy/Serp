# Author: QuanTuanHuy
# Description: Generate mTLS certificates for SERP internal gRPC services (Windows PowerShell)
# Usage: .\scripts\generate-grpc-certs.ps1

$CERTS_DIR = ".\certs"
$VALIDITY_DAYS = 365
$KEY_SIZE = 2048
$CA_KEY_SIZE = 4096

Write-Host "=== SERP gRPC mTLS Certificate Generator ===" -ForegroundColor Green
Write-Host ""

# Create certs directory if not exists
if (!(Test-Path $CERTS_DIR)) {
    New-Item -ItemType Directory -Path $CERTS_DIR | Out-Null
}

# Services that need certificates
$SERVICES = @("account", "crm", "discuss", "notification", "ptm-task", "ptm-schedule", "logistics", "purchase", "sales")

# Check if CA already exists
if ((Test-Path "$CERTS_DIR\ca.crt") -and (Test-Path "$CERTS_DIR\ca.key")) {
    Write-Host "CA certificate already exists. Skipping CA generation." -ForegroundColor Yellow
    Write-Host "Delete $CERTS_DIR\ca.crt and $CERTS_DIR\ca.key to regenerate."
} else {
    Write-Host "Generating Root CA..." -ForegroundColor Green
    
    # Generate CA private key
    & openssl genrsa -out "$CERTS_DIR\ca.key" $CA_KEY_SIZE 2>&1 | Out-Null
    
    # Generate CA certificate
    & openssl req -new -x509 -days $VALIDITY_DAYS `
        -key "$CERTS_DIR\ca.key" `
        -out "$CERTS_DIR\ca.crt" `
        -subj "/C=VN/ST=Hanoi/L=Hanoi/O=SERP/OU=Internal/CN=SERP Internal CA" 2>&1 | Out-Null
    
    Write-Host "Root CA generated" -ForegroundColor Green
}

Write-Host ""
Write-Host "Generating service certificates..." -ForegroundColor Green

foreach ($service in $SERVICES) {
    Write-Host "  Generating cert for $service-service... " -NoNewline
    
    # Generate service private key
    & openssl genrsa -out "$CERTS_DIR\$service.key" $KEY_SIZE 2>&1 | Out-Null
    
    # Create certificate signing request
    & openssl req -new `
        -key "$CERTS_DIR\$service.key" `
        -out "$CERTS_DIR\$service.csr" `
        -subj "/C=VN/ST=Hanoi/L=Hanoi/O=SERP/OU=Internal/CN=$service-service" 2>&1 | Out-Null
    
    # Create extension file for SAN (Subject Alternative Names)
    $extContent = @"
authorityKeyIdentifier=keyid,issuer
basicConstraints=CA:FALSE
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth, clientAuth
subjectAltName = @alt_names

[alt_names]
DNS.1 = $service-service
DNS.2 = $service-service.serp.svc.cluster.local
DNS.3 = localhost
IP.1 = 127.0.0.1
"@
    $extContent | Out-File -FilePath "$CERTS_DIR\$service.ext" -Encoding ASCII -NoNewline
    
    # Sign certificate with CA
    & openssl x509 -req -days $VALIDITY_DAYS `
        -in "$CERTS_DIR\$service.csr" `
        -CA "$CERTS_DIR\ca.crt" `
        -CAkey "$CERTS_DIR\ca.key" `
        -CAcreateserial `
        -out "$CERTS_DIR\$service.crt" `
        -extfile "$CERTS_DIR\$service.ext" 2>&1 | Out-Null
    
    # Clean up CSR and extension file
    Remove-Item "$CERTS_DIR\$service.csr" -ErrorAction SilentlyContinue
    Remove-Item "$CERTS_DIR\$service.ext" -ErrorAction SilentlyContinue
    
    Write-Host "Done" -ForegroundColor Green
}

# Clean up CA serial file
Remove-Item "$CERTS_DIR\ca.srl" -ErrorAction SilentlyContinue

Write-Host ""
Write-Host "=== Certificate Generation Complete ===" -ForegroundColor Green
Write-Host ""
Write-Host "Generated files in $CERTS_DIR`:"
Get-ChildItem $CERTS_DIR | Format-Table Name, Length -AutoSize
Write-Host ""
Write-Host "Important:" -ForegroundColor Yellow
Write-Host "  - Keep ca.key secure! It can sign new certificates."
Write-Host "  - Distribute ca.crt to all services for trust verification."
Write-Host "  - Each service needs: ca.crt, <service>.crt, <service>.key"
Write-Host ""
Write-Host "To verify a certificate:" -ForegroundColor Green
Write-Host "  openssl verify -CAfile $CERTS_DIR\ca.crt $CERTS_DIR\account.crt"
Write-Host ""
Write-Host "To view certificate details:" -ForegroundColor Green
Write-Host "  openssl x509 -in $CERTS_DIR\account.crt -text -noout"
