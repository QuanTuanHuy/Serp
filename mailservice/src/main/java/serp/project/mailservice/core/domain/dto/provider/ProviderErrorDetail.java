/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.domain.dto.provider;

public record ProviderErrorDetail(
        String message,
        String field,
        Object help,
        String id) {
}
