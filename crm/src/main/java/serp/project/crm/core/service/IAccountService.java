/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service;

import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.AccountFilterRequest;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface IAccountService {

    AccountEntity createAccount(AccountEntity account, Long tenantId);
    AccountEntity updateAccount(Long id, AccountEntity updates, Long tenantId);
    AccountEntity activateAccount(Long id, Long userId, Long tenantId);
    AccountEntity deactivateAccount(Long id, Long userId, Long tenantId);
    AccountEntity updateCreditLimit(Long id, BigDecimal creditLimit, Long userId, Long tenantId);
    void deleteAccount(Long id, Long tenantId);
    void updateAccountRevenue(Long accountId, Long tenantId, BigDecimal revenue, boolean isWon, Long updatedBy);

    Optional<AccountEntity> getAccountById(Long id, Long tenantId);
    Optional<AccountEntity> getAccountByEmail(String email, Long tenantId);
    Pair<List<AccountEntity>, Long> getAllAccounts(Long tenantId, PageRequest pageRequest);
    Pair<List<AccountEntity>, Long> searchAccounts(String keyword, Long tenantId, PageRequest pageRequest);
    List<AccountEntity> getChildAccounts(Long parentId, Long tenantId);
    Pair<List<AccountEntity>, Long> getAccountsByStatus(ActiveStatus status, Long tenantId, PageRequest pageRequest);
    List<AccountEntity> getTopAccountsByRevenue(Long tenantId, int limit);
    Pair<List<AccountEntity>, Long> getAccountsByIndustry(String industry, Long tenantId, PageRequest pageRequest);
    Long countAccountsByStatus(ActiveStatus status, Long tenantId);
    Pair<List<AccountEntity>, Long> filterAccounts(AccountFilterRequest filter, Long tenantId,
            PageRequest pageRequest);

    boolean isEmailExists(String email, Long tenantId);
}
