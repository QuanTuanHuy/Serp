/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOfficeStaffAssignment;

@Repository
public interface PostOfficeStaffAssignmentRepository extends JpaRepository<PostOfficeStaffAssignment, Long> {
    boolean existsByStaffIdAndAssignedToIsNull(Long staffId);
}
