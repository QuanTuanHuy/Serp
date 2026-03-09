/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import serp.project.pmcore.infrastructure.store.model.OutboxEventModel;

@Repository
public interface IOutboxEventRepository extends JpaRepository<OutboxEventModel, Long> {
}
