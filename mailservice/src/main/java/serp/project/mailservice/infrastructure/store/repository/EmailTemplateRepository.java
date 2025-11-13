/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.infrastructure.store.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import serp.project.mailservice.core.domain.enums.EmailType;
import serp.project.mailservice.infrastructure.store.model.EmailTemplateModel;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailTemplateRepository extends JpaRepository<EmailTemplateModel, Long> {
    Optional<EmailTemplateModel> findByCode(String code);

    Optional<EmailTemplateModel> findByTenantIdAndCode(Long tenantId, String code);

    List<EmailTemplateModel> findByTenantId(Long tenantId, Pageable pageable);

    List<EmailTemplateModel> findByIsGlobalTrue();

    List<EmailTemplateModel> findByType(EmailType type);

    boolean existsByCode(String code);
}
