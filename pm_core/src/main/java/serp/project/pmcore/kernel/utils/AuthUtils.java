/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.kernel.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AuthUtils {

    public Optional<Jwt> getCurrentJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return Optional.of(jwt);
        }
        return Optional.empty();
    }

    public Optional<Long> getCurrentUserId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("uid"))
                .filter(sub -> !sub.isEmpty())
                .map(Long::valueOf);
    }

    public Optional<Long> getCurrentTenantId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("tid"))
                .filter(tenant -> !tenant.isEmpty())
                .map(Long::valueOf);
    }
}
