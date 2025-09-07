package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.OrganizationEntity;

import java.util.List;

public interface IOrganizationPort {
    OrganizationEntity save(OrganizationEntity organization);
    OrganizationEntity getById(Long id);
    boolean existsByCode(String code);
    List<OrganizationEntity> getByIds(List<Long> ids);
}
