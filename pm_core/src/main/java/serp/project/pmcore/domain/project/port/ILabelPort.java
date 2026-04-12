/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.port;

import java.util.List;
import java.util.Optional;

import serp.project.pmcore.domain.project.entity.LabelEntity;

public interface ILabelPort {
    LabelEntity createLabel(LabelEntity label);

    Optional<LabelEntity> getLabelById(Long id, Long tenantId);

    List<LabelEntity> getLabelsByProjectId(Long projectId, Long tenantId);

    void updateLabel(LabelEntity label);

    void deleteLabel(Long labelId, Long tenantId);
}
