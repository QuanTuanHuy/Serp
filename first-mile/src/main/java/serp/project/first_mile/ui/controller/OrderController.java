package serp.project.first_mile.ui.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.OrderImportDTO;
import serp.project.first_mile.dto.response.ValidateImportFileDTO;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.kernel.utils.AuthUtils;
import serp.project.first_mile.service.OrderService;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final AuthUtils authUtils;
    private final OrderService orderService;

    @GetMapping("/template")
    public ResponseEntity<byte[]> exportTemplate() {
        Long tenantId = authUtils.getCurrentTenantId().orElseThrow(
                () -> new AppException(ErrorCode.UNAUTHORIZED)
        );
        log.info("REST request to export Order Template Excel for tenant {}", tenantId);

        byte[] excelData = orderService.exportTemplate(tenantId);

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=order_template.xlsx")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelData);
    }

    @PostMapping("/validate")
    public ValidateImportFileDTO<OrderImportDTO> validateFile(
            @RequestParam("file") MultipartFile file
    ) {
        // TODO: impl validate file
        return null;
    }
}
