/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.ui.grpc.interceptor;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;
import org.springframework.core.annotation.Order;

import java.time.Instant;

/**
 * Interceptor that logs gRPC request and response details for debugging
 * and monitoring purposes.
 */
@GrpcGlobalServerInterceptor
@Order(10)
@Slf4j
public class GrpcLoggingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
            ServerCall<ReqT, RespT> call,
            Metadata headers,
            ServerCallHandler<ReqT, RespT> next) {

        String methodName = call.getMethodDescriptor().getFullMethodName();
        long startTime = Instant.now().toEpochMilli();
        String serviceName = MtlsAuthInterceptor.SERVICE_NAME_KEY.get();

        log.info("gRPC Request: method={}, caller={}", methodName, serviceName);

        // Wrap the call to intercept response
        ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                long duration = Instant.now().toEpochMilli() - startTime;
                if (status.isOk()) {
                    log.info("gRPC Response: method={}, status=OK, duration={}ms", methodName, duration);
                } else {
                    log.warn("gRPC Response: method={}, status={}, description={}, duration={}ms",
                            methodName, status.getCode(), status.getDescription(), duration);
                }
                super.close(status, trailers);
            }
        };

        // Wrap the listener to log request details
        ServerCall.Listener<ReqT> listener = next.startCall(wrappedCall, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {
            @Override
            public void onMessage(ReqT message) {
                log.debug("gRPC Request body: method={}, message={}", methodName, message);
                super.onMessage(message);
            }

            @Override
            public void onCancel() {
                log.warn("gRPC Request cancelled: method={}", methodName);
                super.onCancel();
            }
        };
    }
}
