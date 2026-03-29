/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.screen.port;

import java.util.List;

import serp.project.pmcore.domain.screen.entity.ScreenSchemeItemEntity;

public interface IScreenSchemeItemPort {
    List<ScreenSchemeItemEntity> createScreenSchemeItems(List<ScreenSchemeItemEntity> items);

    List<ScreenSchemeItemEntity> getScreenSchemeItemsByScreenSchemeIdIncludingSystem(Long screenSchemeId, Long tenantId);

    List<ScreenSchemeItemEntity> getScreenSchemeItemsByScreenSchemeId(Long screenSchemeId, Long tenantId);
}
