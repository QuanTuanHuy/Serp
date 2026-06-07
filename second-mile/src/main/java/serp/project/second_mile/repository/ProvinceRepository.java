/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import serp.project.second_mile.domain.Province;
import serp.project.second_mile.repository.projection.CodeNameProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {
    Optional<Province> findByProvinceCode(String provinceCode);

    @Query("""
            select p.provinceCode as code, p.name as name
            from Province p
            order by p.name asc
            """)
    List<CodeNameProjection> findTemplateCodeNameList();
}
