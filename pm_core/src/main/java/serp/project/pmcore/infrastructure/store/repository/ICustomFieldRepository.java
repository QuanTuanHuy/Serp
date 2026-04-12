/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import serp.project.pmcore.infrastructure.store.model.CustomFieldModel;

import java.util.List;

@Repository
public interface ICustomFieldRepository extends JpaRepository<CustomFieldModel, Long> {

    @Query("SELECT f FROM CustomFieldModel f WHERE f.fieldKey IN :fieldKeys ORDER BY f.id ASC")
    List<CustomFieldModel> findAllByFieldKeyIn(@Param("fieldKeys") List<String> fieldKeys);
}
