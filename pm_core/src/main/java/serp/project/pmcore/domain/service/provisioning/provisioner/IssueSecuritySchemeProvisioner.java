package serp.project.pmcore.domain.service.provisioning.provisioner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.entity.IssueSecuritySchemeEntity;
import serp.project.pmcore.domain.enums.CloneMode;
import serp.project.pmcore.domain.enums.SchemeType;
import serp.project.pmcore.domain.port.store.IIssueSecuritySchemePort;
import serp.project.pmcore.domain.port.store.ITenantSchemeMappingPort;
import serp.project.pmcore.domain.service.provisioning.cloner.IssueSecuritySchemeCloner;
import serp.project.pmcore.domain.service.provisioning.provisioner.base.AbstractMappedSharedProvisioner;

import java.util.Optional;

@Component
@Slf4j
public class IssueSecuritySchemeProvisioner extends AbstractMappedSharedProvisioner<IssueSecuritySchemeEntity> {

    private final IIssueSecuritySchemePort issueSecuritySchemePort;
    private final IssueSecuritySchemeCloner issueSecuritySchemeCloner;

    public IssueSecuritySchemeProvisioner(ITenantSchemeMappingPort tenantSchemeMappingPort,
                                          IIssueSecuritySchemePort issueSecuritySchemePort,
                                          IssueSecuritySchemeCloner issueSecuritySchemeCloner) {
        super(tenantSchemeMappingPort);
        this.issueSecuritySchemePort = issueSecuritySchemePort;
        this.issueSecuritySchemeCloner = issueSecuritySchemeCloner;
    }

    @Override
    public SchemeType supports() {
        return SchemeType.ISSUE_SECURITY;
    }

    @Override
    protected Optional<IssueSecuritySchemeEntity> loadSourceByIdIncludingSystem(Long sourceSchemeId, Long tenantId) {
        return issueSecuritySchemePort.getIssueSecuritySchemeByIdIncludingSystem(sourceSchemeId, tenantId);
    }

    @Override
    protected Long getSourceId(IssueSecuritySchemeEntity source) {
        return source.getId();
    }

    @Override
    protected Long getSourceTenantId(IssueSecuritySchemeEntity source) {
        return source.getTenantId();
    }

    @Override
    protected boolean tenantSchemeExists(Long tenantSchemeId, Long tenantId) {
        return issueSecuritySchemePort.getIssueSecuritySchemeById(tenantSchemeId, tenantId).isPresent();
    }

    @Override
    protected String sourceEntityLabel() {
        return "issue security scheme";
    }

    @Override
    protected Long cloneForTenant(IssueSecuritySchemeEntity source,
                                  Long tenantId,
                                  Long userId,
                                  CloneMode cloneMode) {
        return issueSecuritySchemeCloner.cloneIssueSecurityScheme(source, tenantId, userId, cloneMode);
    }
}