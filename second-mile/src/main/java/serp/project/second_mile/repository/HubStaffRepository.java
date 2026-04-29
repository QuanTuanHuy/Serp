/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.HubStaff;

import java.util.Optional;

@Repository
public interface HubStaffRepository extends JpaRepository<HubStaff, Long>, JpaSpecificationExecutor<HubStaff> {
    Optional<HubStaff> findByCode(String code);
}
