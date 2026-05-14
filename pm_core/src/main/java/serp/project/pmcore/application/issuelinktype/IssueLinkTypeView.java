/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.application.issuelinktype;

import serp.project.pmcore.domain.issuelink.entity.IssueLinkTypeEntity;

public record IssueLinkTypeView(
        Long id,
        Long tenantId,
        String name,
        String outwardDescription,
        String inwardDescription,
        boolean isSystem,
        boolean readOnly,
        Long createdAt,
        Long createdBy,
        Long updatedAt,
        Long updatedBy
) {
    public static IssueLinkTypeView from(IssueLinkTypeEntity entity) {
        return new IssueLinkTypeView(
                entity.getId(),
                entity.getTenantId(),
                entity.getName(),
                entity.getOutwardDescription(),
                entity.getInwardDescription(),
                Boolean.TRUE.equals(entity.getIsSystem()),
                Boolean.TRUE.equals(entity.getIsSystem()),
                entity.getCreatedAt(),
                entity.getCreatedBy(),
                entity.getUpdatedAt(),
                entity.getUpdatedBy()
        );
    }
}
