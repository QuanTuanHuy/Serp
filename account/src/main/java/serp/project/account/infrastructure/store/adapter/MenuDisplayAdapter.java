/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.MenuDisplayEntity;
import serp.project.account.core.port.store.IMenuDisplayPort;
import serp.project.account.infrastructure.store.mapper.MenuDisplayMapper;
import serp.project.account.infrastructure.store.model.MenuDisplayModel;
import serp.project.account.infrastructure.store.repository.IMenuDisplayRepository;
import serp.project.account.kernel.utils.CollectionUtils;

@Component
@RequiredArgsConstructor
public class MenuDisplayAdapter implements IMenuDisplayPort {
    private final IMenuDisplayRepository menuDisplayRepository;
    private final MenuDisplayMapper menuDisplayMapper;
    
    @Override
    public MenuDisplayEntity save(MenuDisplayEntity menuDisplay) {
        MenuDisplayModel menuDisplayModel = menuDisplayMapper.toModel(menuDisplay);
        return menuDisplayMapper.toEntity(menuDisplayRepository.save(menuDisplayModel));
    }

    @Override
    public List<MenuDisplayEntity> getByIds(List<Long> ids) {
        if (!CollectionUtils.isEmpty(ids) && ids.size() == 1) {
            return List.of(getById(ids.getFirst()));
        }
        return menuDisplayMapper.toEntityList(menuDisplayRepository.findByIdIn(ids));
    }

    private MenuDisplayEntity getById(Long id) {
        return menuDisplayMapper.toEntity(menuDisplayRepository.findById(id).orElse(null));
    }
}
