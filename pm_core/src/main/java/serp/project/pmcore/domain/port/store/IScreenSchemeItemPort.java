/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.port.store;

import serp.project.pmcore.domain.entity.ScreenSchemeItemEntity;

import java.util.List;

public interface IScreenSchemeItemPort {
    List<ScreenSchemeItemEntity> createScreenSchemeItems(List<ScreenSchemeItemEntity> items);

    List<ScreenSchemeItemEntity> getScreenSchemeItemsByScreenSchemeIdIncludingSystem(Long screenSchemeId, Long tenantId);
}
