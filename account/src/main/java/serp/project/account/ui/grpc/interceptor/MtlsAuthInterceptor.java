/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Interceptor that validates client certificates for mTLS authentication.
 * Extracts the Common Name (CN) from the client certificate and verifies
 * that it belongs to an allowed service.
 */
@GrpcGlobalServerInterceptor
@Order(1)
@Slf4j
public class MtlsAuthInterceptor implements ServerInterceptor {

    /**
     * Set of allowed service names that can connect via gRPC.
     * The CN in client certificate must match one of these.
     */
    private static final Set<String> ALLOWED_SERVICES = Set.of(
            "crm-service",
            "discuss-service",
            "notification-service",
            "ptm-task-service",
            "ptm-schedule-service",
            "logistics-service",
            "purchase-service",
            "sales-service",
            "api-gateway"
    );

    /**
     * Pattern to extract CN from certificate subject.
     */
    private static final Pattern CN_PATTERN = Pattern.compile("CN=([^,]+)");

    /**
     * Context key to store the authenticated service name.
     */
    public static final Context.Key<String> SERVICE_NAME_KEY = Context.key("serviceName");

    @Value("${grpc.server.security.enabled:true}")
    private boolean securityEnabled;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        // Skip auth for health checks
        String fullMethodName = call.getMethodDescriptor().getFullMethodName();
        if (fullMethodName.startsWith("grpc.health.") || fullMethodName.startsWith("grpc.reflection.")) {
            return next.startCall(call, headers);
        }

        // If security is disabled (dev mode), allow all
        if (!securityEnabled) {
            log.debug("mTLS security disabled, allowing request to {}", fullMethodName);
            Context ctx = Context.current().withValue(SERVICE_NAME_KEY, "dev-client");
            return Contexts.interceptCall(ctx, call, headers, next);
        }

        // Get SSL session from transport attributes
        SSLSession sslSession = call.getAttributes().get(Grpc.TRANSPORT_ATTR_SSL_SESSION);
        if (sslSession == null) {
            log.warn("No SSL session found for gRPC call to {}", fullMethodName);
            call.close(Status.UNAUTHENTICATED.withDescription("No TLS session"), new Metadata());
            return new ServerCall.Listener<>() {};
        }

        try {
            // Get peer certificates (java.security.cert.Certificate[])
            Certificate[] peerCertificates = sslSession.getPeerCertificates();
            if (peerCertificates == null || peerCertificates.length == 0) {
                log.warn("No peer certificates found for gRPC call to {}", fullMethodName);
                call.close(Status.UNAUTHENTICATED.withDescription("No client certificate"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            // Extract CN from the first certificate (client cert)
            if (!(peerCertificates[0] instanceof X509Certificate)) {
                log.warn("Client certificate is not X509 for gRPC call to {}", fullMethodName);
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid certificate type"), new Metadata());
                return new ServerCall.Listener<>() {};
            }
            
            X509Certificate clientCert = (X509Certificate) peerCertificates[0];
            String subjectDN = clientCert.getSubjectX500Principal().getName();
            String serviceName = extractCN(subjectDN);

            if (serviceName == null) {
                log.warn("Could not extract CN from certificate: {}", subjectDN);
                call.close(Status.UNAUTHENTICATED.withDescription("Invalid certificate CN"), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            // Verify service is allowed
            if (!ALLOWED_SERVICES.contains(serviceName)) {
                log.warn("Service '{}' is not authorized to call {}", serviceName, fullMethodName);
                call.close(Status.PERMISSION_DENIED
                        .withDescription("Service not authorized: " + serviceName), new Metadata());
                return new ServerCall.Listener<>() {};
            }

            log.debug("Authenticated gRPC call from service '{}' to {}", serviceName, fullMethodName);

            // Add service name to context for downstream use
            Context ctx = Context.current().withValue(SERVICE_NAME_KEY, serviceName);
            return Contexts.interceptCall(ctx, call, headers, next);

        } catch (SSLPeerUnverifiedException e) {
            log.error("Failed to verify peer certificate: {}", e.getMessage());
            call.close(Status.UNAUTHENTICATED.withDescription("Certificate verification failed"), new Metadata());
            return new ServerCall.Listener<>() {};
        }
    }

    /**
     * Extracts the Common Name (CN) from a certificate subject DN.
     *
     * @param subjectDN The subject DN string (e.g., "CN=crm-service,O=SERP")
     * @return The CN value, or null if not found
     */
    private String extractCN(String subjectDN) {
        Matcher matcher = CN_PATTERN.matcher(subjectDN);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
