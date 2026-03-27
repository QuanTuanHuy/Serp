/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.Ward;
import serp.project.first_mile.repository.projection.CodeNameProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    Page<Ward> findAllByProvinceCodeOrderByNameAsc(String provinceCode, Pageable pageable);

    Optional<Ward> findByWardCode(String wardCode);

    @Query("""
            select w.wardCode as code, w.name as name
            from Ward w
            order by w.name asc
            """)
    List<CodeNameProjection> findTemplateCodeNameList();
}
