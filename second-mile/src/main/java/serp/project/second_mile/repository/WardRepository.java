/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Ward;
import serp.project.second_mile.repository.projection.CodeNameProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {
    Optional<Ward> findByWardCode(String wardCode);

    @Query("""
            select w.wardCode as code, w.name as name
            from Ward w
            order by w.name asc
            """)
    List<CodeNameProjection> findTemplateCodeNameList();
}
