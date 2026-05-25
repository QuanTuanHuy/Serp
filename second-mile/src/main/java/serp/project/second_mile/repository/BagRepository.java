/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Bag;

@Repository
public interface BagRepository extends JpaRepository<Bag, Long>, JpaSpecificationExecutor<Bag> {
    boolean existsByTenantIdAndBagCodeIgnoreCase(Long tenantId, String bagCode);
}
