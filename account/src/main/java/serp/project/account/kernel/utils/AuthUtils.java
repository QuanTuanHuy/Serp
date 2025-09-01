/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.kernel.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
public class AuthUtils {

    public Optional<Jwt> getCurrentJwt() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                return Optional.of(jwt);
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Error getting current JWT", e);
            return Optional.empty();
        }
    }

    public Optional<Long> getCurrentUserId() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("sub"))
                .filter(sub -> !sub.isEmpty())
                .map(Long::valueOf);
    }

    public Optional<String> getCurrentUserEmail() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("email"));
    }

    public Optional<String> getCurrentUserFullName() {
        return getCurrentJwt()
                .map(jwt -> jwt.getClaimAsString("full_name"));
    }
}
