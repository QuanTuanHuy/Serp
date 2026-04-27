/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.fieldconfig.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.fieldconfig.entity.FieldConfigSchemeItemEntity;

public interface IFieldConfigSchemeItemPort {
    List<FieldConfigSchemeItemEntity> createFieldConfigSchemeItems(List<FieldConfigSchemeItemEntity> items);

    List<FieldConfigSchemeItemEntity> getFieldConfigSchemeItemsBySchemeIdIncludingSystem(Long schemeId, Long tenantId);

    List<FieldConfigSchemeItemEntity> getFieldConfigSchemeItemsBySchemeId(Long schemeId, Long tenantId);

    Optional<FieldConfigSchemeItemEntity> getItemBySchemeIdAndIssueTypeId(Long schemeId, Long issueTypeId, Long tenantId);

    boolean existsByIssueTypeId(Long issueTypeId, Long tenantId);
}
