/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.tms_billing_service.domain.Ward;

import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    Optional<Ward> findByWardCode(String wardCode);
}
