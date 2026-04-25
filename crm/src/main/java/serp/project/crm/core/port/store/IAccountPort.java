/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.port.store;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.AccountFilterRequest;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;

import java.util.List;
import java.util.Optional;

public interface IAccountPort {
    AccountEntity save(AccountEntity AccountEntity);

    Optional<AccountEntity> findById(Long id, Long tenantId);

    Optional<AccountEntity> findByEmail(String email, Long tenantId);

    Pair<List<AccountEntity>, Long> findAll(Long tenantId, PageRequest pageRequest);

    Pair<List<AccountEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest);

    List<AccountEntity> findByParentAccountId(Long parentAccountId, Long tenantId);

    Pair<List<AccountEntity>, Long> findByActiveStatus(ActiveStatus activeStatus, Long tenantId,
            PageRequest pageRequest);

    Long countByActiveStatus(ActiveStatus activeStatus, Long tenantId);

    Boolean existsByEmail(String email, Long tenantId);

    void deleteById(Long id, Long tenantId);

    List<AccountEntity> findTopByRevenue(Long tenantId, int limit);

    Pair<List<AccountEntity>, Long> findByIndustry(String industry, Long tenantId, PageRequest pageRequest);

    Pair<List<AccountEntity>, Long> filter(AccountFilterRequest filter, PageRequest pageRequest, Long tenantId);
}
