/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.ptm_optimization.infrastructure.store.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import serp.project.ptm_optimization.infrastructure.store.model.TaskRegistrationModel;

@Repository
public interface ITaskRegistrationRepository extends IBaseRepository<TaskRegistrationModel> {
    Optional<TaskRegistrationModel> findByUserId(Long userId);
}
