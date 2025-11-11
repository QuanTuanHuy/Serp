package serp.project.account.core.port.store;

import serp.project.account.core.domain.dto.request.GetMenuDisplayParams;
import serp.project.account.core.domain.entity.MenuDisplayEntity;

import java.util.List;

import org.springframework.data.util.Pair;

public interface IMenuDisplayPort {
    MenuDisplayEntity save(MenuDisplayEntity menuDisplay);

    MenuDisplayEntity getById(Long id);

    List<MenuDisplayEntity> getByIds(List<Long> ids);

    List<MenuDisplayEntity> getByModuleId(Long moduleId);

    MenuDisplayEntity getByModuleIdAndName(Long moduleId, String name);

    void deleteMenuDisplay(Long id);

    Pair<List<MenuDisplayEntity>, Long> getAllMenuDisplays(GetMenuDisplayParams params);
}
