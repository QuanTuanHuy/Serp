/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.List;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.MenuDisplayModel;

@Repository
public interface IMenuDisplayRepository extends IBaseRepository<MenuDisplayModel> {
    List<MenuDisplayModel> findByIdIn(List<Long> ids);

    List<MenuDisplayModel> findByModuleId(Long moduleId);

    MenuDisplayModel findByModuleIdAndName(Long moduleId, String name);
}
