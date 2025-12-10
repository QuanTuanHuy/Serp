package serp.project.crm.ui.internal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.crm.kernel.utils.TokenUtils;

@RestController
@RequestMapping("/api/v1/tokens")
@RequiredArgsConstructor
@Slf4j
public class TokenController {
    private final TokenUtils tokenUtils;

    @GetMapping("/my-service-token")
    public String getServiceToken() {
        return tokenUtils.getServiceToken();
    }
}
