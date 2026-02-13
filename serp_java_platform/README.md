# SERP Java Platform

Shared Java platform modules for SERP microservices.

## Modules

- `serp-java-bom`: shared dependency alignment.
- `serp-starter-core`: core cross-cutting auto configuration.
- `serp-starter-security-keycloak`: standard JWT + auth context integration.

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
```
