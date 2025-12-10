/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import serp.project.account.kernel.utils.DataUtils;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.time.Duration;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .requestFactory(this::buildProxyAwareRequestFactory)
                .build();
    }

    private SimpleClientHttpRequestFactory buildProxyAwareRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(5));
        Proxy proxy = resolveProxy();

        if (proxy != null) {
            factory.setProxy(proxy);
        }

        return factory;
    }

    private Proxy resolveProxy() {
        String proxyEnv = System.getenv("HTTPS_PROXY");
        if (DataUtils.isNullOrEmpty(proxyEnv)) {
            proxyEnv = System.getenv("HTTP_PROXY");
        }

        if (DataUtils.isNullOrEmpty(proxyEnv)) {
            return null;
        }

        try {
            URI proxyUri = URI.create(proxyEnv);
            String host = proxyUri.getHost();
            int port = proxyUri.getPort();

            if (DataUtils.isNullOrEmpty(host) || port == -1) {
                return null;
            }

            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
