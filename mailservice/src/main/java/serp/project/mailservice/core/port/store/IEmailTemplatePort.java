/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

package serp.project.mailservice.core.port.store;

import serp.project.mailservice.core.domain.entity.EmailTemplateEntity;
import serp.project.mailservice.core.domain.enums.EmailType;

import java.util.List;
import java.util.Optional;

public interface IEmailTemplatePort {
    EmailTemplateEntity save(EmailTemplateEntity template);

    Optional<EmailTemplateEntity> findById(Long id);

    Optional<EmailTemplateEntity> findByCode(String code);

    Optional<EmailTemplateEntity> findByTenantIdAndCode(Long tenantId, String code);

    List<EmailTemplateEntity> findByTenantId(Long tenantId, int page, int size);

    List<EmailTemplateEntity> findGlobalTemplates();

    List<EmailTemplateEntity> findByType(EmailType type);

    void delete(Long id);

    boolean existsByCode(String code);

    boolean existsByCodeAndTenantId(String code, Long tenantId);
}
