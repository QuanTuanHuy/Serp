/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import serp.project.account.infrastructure.store.model.ModuleModel;

@Repository
public interface IModuleRepository extends IBaseRepository<ModuleModel> {
    Optional<ModuleModel> findByModuleName(String moduleName);
    boolean existsByModuleName(String moduleName);
}
