/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.dto.request.GetOrganizationParams;
import serp.project.account.core.domain.entity.OrganizationEntity;

import java.util.List;

import org.springframework.data.util.Pair;

public interface IOrganizationPort {
    OrganizationEntity save(OrganizationEntity organization);

    OrganizationEntity getById(Long id);

    boolean existsByCode(String code);

    OrganizationEntity getOrganizationByName(String name);

    List<OrganizationEntity> getByIds(List<Long> ids);

    Pair<List<OrganizationEntity>, Long> getOrganizations(GetOrganizationParams params);
}
