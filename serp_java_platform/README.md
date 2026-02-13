# SERP Java Platform

Shared Java platform modules for SERP microservices.

## Modules

- `serp-java-bom`: shared dependency alignment.
- `serp-starter-core`: core cross-cutting auto configuration.
- `serp-starter-security-keycloak`: standard JWT + auth context integration.
- `serp-starter-kafka`: producer helper + retry/DLT consumer baseline.
- `serp-starter-redis`: cache + lock helper with key strategy.

## Build

```bash
mvn -f serp_java_platform/pom.xml clean package
```

## Publish to GitHub Packages

```bash
mvn -f serp_java_platform/pom.xml -DskipTests deploy
```

## Minimal usage example in a service

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.serp-project</groupId>
      <artifactId>serp-java-bom</artifactId>
      <version>0.1.0-SNAPSHOT</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>

<dependencies>
  <dependency>
    <groupId>io.github.serp-project</groupId>
    <artifactId>serp-starter-security-keycloak</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.serp-project</groupId>
    <artifactId>serp-starter-kafka</artifactId>
  </dependency>
  <dependency>
    <groupId>io.github.serp-project</groupId>
    <artifactId>serp-starter-redis</artifactId>
  </dependency>
</dependencies>
```

```yaml
serp:
  security:
    jwt:
      jwk-set-uri: ${KEYCLOAK_URL}/realms/serp/protocol/openid-connect/certs
    service:
      required-role: SERP_SERVICES
      allowed-client-ids:
        - serp-account
        - serp-crm
    public-urls:
      - method: POST
        pattern: /api/v1/auth/login
      - method: POST
        pattern: /api/v1/auth/refresh-token
  kafka:
    producer:
      default-topic: serp.events
      correlation-id-header: X-Correlation-Id
    consumer:
      max-attempts: 3
      retry-interval-ms: 1000
      dead-letter-enabled: true
      dlt-suffix: .dlt
  redis:
    cache:
      prefix: serp:cache
      default-ttl-seconds: 300
    lock:
      prefix: serp:lock
      default-ttl-seconds: 30
```
