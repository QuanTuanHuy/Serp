package serp.project.account.ui.controller.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/internal/api/v1/test")
public class TestInternalController {

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test success");
    }
}
