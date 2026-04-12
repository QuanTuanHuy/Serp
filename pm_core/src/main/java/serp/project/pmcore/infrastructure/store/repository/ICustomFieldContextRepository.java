/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldContextModel;

import java.util.List;

@Repository
public interface ICustomFieldContextRepository extends JpaRepository<CustomFieldContextModel, Long> {

    @Query(value = """
            SELECT c.*
            FROM custom_field_contexts c
            WHERE c.deleted_at IS NULL
              AND c.custom_field_id = :customFieldId
              AND (c.issue_type_key = :issueTypeKey OR c.issue_type_key IS NULL)
            ORDER BY CASE WHEN c.issue_type_key = :issueTypeKey THEN 0 ELSE 1 END, c.id ASC
            """, nativeQuery = true)
    List<CustomFieldContextModel> findApplicableContexts(@Param("customFieldId") Long customFieldId,
                                                         @Param("issueTypeKey") String issueTypeKey);
}
