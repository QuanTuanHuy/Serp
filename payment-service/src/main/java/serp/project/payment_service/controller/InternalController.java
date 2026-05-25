package serp.project.payment_service.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/internal/payments")
@RequiredArgsConstructor
@Tag(name = "Internal endpoints", description = "APIs nội bộ liên quan đến thanh toán")
public class InternalController {

}
