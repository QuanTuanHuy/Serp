/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.PostOffice;

@Repository
public interface PostOfficeRepository extends JpaRepository<PostOffice, Long> {
    boolean existsByCode(String code);

    Page<PostOffice> findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(String code, String name, Pageable pageable);

    Page<PostOffice> findByLocationIsNull(Pageable pageable);
}
