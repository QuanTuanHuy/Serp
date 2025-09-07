package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.MenuDisplayEntity;

import java.util.List;

public interface IMenuDisplayPort {
    MenuDisplayEntity save(MenuDisplayEntity menuDisplay);
    List<MenuDisplayEntity> getByIds(List<Long> ids);
}
