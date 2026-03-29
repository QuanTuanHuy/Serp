package serp.project.first_mile.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.first_mile.domain.ProductType;
import serp.project.first_mile.repository.projection.CodeNameProjection;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Long> {

	@Query("""
			select p
			from ProductType p
			where p.tenantId = :tenantId
				and (
					:keyword is null
					or :keyword = ''
					or lower(p.code) like lower(concat('%', :keyword, '%'))
					or lower(p.name) like lower(concat('%', :keyword, '%'))
				)
			""")
	Page<ProductType> searchByTenantId(@Param("tenantId") Long tenantId, @Param("keyword") String keyword, Pageable pageable);

	Optional<ProductType> findByIdAndTenantId(Long id, Long tenantId);

	boolean existsByCodeIgnoreCaseAndTenantId(String code, Long tenantId);

	boolean existsByCodeIgnoreCaseAndTenantIdAndIdNot(String code, Long tenantId, Long id);

	@Query("""
			select p.code as code, p.name as name
			from ProductType p
			where p.isActive = true
				and (:tenantId is null or p.tenantId = :tenantId)
			order by p.name asc
			""")
	List<CodeNameProjection> findTemplateCodeNameList(@Param("tenantId") Long tenantId);

	List<ProductType> findByTenantIdAndIsActiveTrueOrderByNameAsc(Long tenantId);
}
