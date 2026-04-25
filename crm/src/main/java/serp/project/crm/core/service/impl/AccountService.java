/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.util.Pair;
import serp.project.crm.core.domain.constant.Constants;
import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.AccountFilterRequest;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.core.service.IAccountService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService implements IAccountService {

    private final IAccountPort accountPort;

    @Transactional
    public AccountEntity createAccount(AccountEntity account, Long tenantId) {
        if (account.getEmail() != null && accountPort.existsByEmail(account.getEmail(), tenantId)) {
            throw new AppException(ErrorMessage.ACCOUNT_ALREADY_EXISTS);
        }

        if (account.getCreditLimit() != null && account.getCreditLimit().compareTo(BigDecimal.ZERO) < 0) {
            throw new AppException(ErrorMessage.CREDIT_LIMIT_NEGATIVE);
        }

        if (account.getParentAccountId() != null) {
            accountPort.findById(account.getParentAccountId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        }

        account.setTenantId(tenantId);
        account.setDefaults();

        AccountEntity saved = accountPort.save(account);

        publishAccountCreatedEvent(saved);

        return saved;
    }

    @Transactional
    public AccountEntity updateAccount(Long id, AccountEntity updates, Long tenantId) {
        AccountEntity existing = accountPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));

        if (updates.getEmail() != null && !updates.getEmail().equals(existing.getEmail())) {
            if (accountPort.existsByEmail(updates.getEmail(), tenantId)) {
                throw new AppException(ErrorMessage.ACCOUNT_ALREADY_EXISTS);
            }
        }

        if (updates.getParentAccountId() != null
                && !updates.getParentAccountId().equals(existing.getParentAccountId())) {
            if (updates.getParentAccountId().equals(id)) {
                throw new AppException(ErrorMessage.ACCOUNT_CANNOT_BE_OWN_PARENT);
            }
            accountPort.findById(updates.getParentAccountId(), tenantId)
                    .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        }

        existing.updateFrom(updates);

        AccountEntity updated = accountPort.save(existing);

        publishAccountUpdatedEvent(updated);

        return updated;
    }

    @Transactional(readOnly = true)
    public Optional<AccountEntity> getAccountById(Long id, Long tenantId) {
        return accountPort.findById(id, tenantId);
    }

    @Transactional(readOnly = true)
    public Optional<AccountEntity> getAccountByEmail(String email, Long tenantId) {
        return accountPort.findByEmail(email, tenantId);
    }

    @Transactional(readOnly = true)
    public Pair<List<AccountEntity>, Long> getAllAccounts(Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return accountPort.findAll(tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Pair<List<AccountEntity>, Long> searchAccounts(String keyword, Long tenantId, PageRequest pageRequest) {
        pageRequest.validate();
        return accountPort.searchByKeyword(keyword, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<AccountEntity> getChildAccounts(Long parentId, Long tenantId) {
        accountPort.findById(parentId, tenantId)
                .orElseThrow(() -> new IllegalArgumentException("Parent Account not found"));

        return accountPort.findByParentAccountId(parentId, tenantId);
    }

    @Transactional(readOnly = true)
    public Pair<List<AccountEntity>, Long> getAccountsByStatus(ActiveStatus status, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return accountPort.findByActiveStatus(status, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public List<AccountEntity> getTopAccountsByRevenue(Long tenantId, int limit) {
        if (limit <= 0 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
        }
        return accountPort.findTopByRevenue(tenantId, limit);
    }

    @Transactional(readOnly = true)
    public Pair<List<AccountEntity>, Long> getAccountsByIndustry(String industry, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return accountPort.findByIndustry(industry, tenantId, pageRequest);
    }

    @Transactional(readOnly = true)
    public Long countAccountsByStatus(ActiveStatus status, Long tenantId) {
        return accountPort.countByActiveStatus(status, tenantId);
    }

    @Override
    @Transactional
    public AccountEntity activateAccount(Long id, Long userId, Long tenantId) {
        AccountEntity account = accountPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        try {
            account.activate(userId);
        } catch (IllegalStateException e) {
            throw new AppException(ErrorMessage.ACCOUNT_ALREADY_ACTIVE);
        }
        AccountEntity updated = accountPort.save(account);
        publishAccountUpdatedEvent(updated);
        return updated;
    }

    @Override
    @Transactional
    public AccountEntity deactivateAccount(Long id, Long userId, Long tenantId) {
        AccountEntity account = accountPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        try {
            account.deactivate(userId);
        } catch (IllegalStateException e) {
            throw new AppException(ErrorMessage.ACCOUNT_ALREADY_INACTIVE);
        }
        AccountEntity updated = accountPort.save(account);
        publishAccountUpdatedEvent(updated);
        return updated;
    }

    @Override
    @Transactional
    public AccountEntity updateCreditLimit(Long id, BigDecimal creditLimit, Long userId, Long tenantId) {
        AccountEntity account = accountPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));
        try {
            account.updateCreditLimit(creditLimit, userId);
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorMessage.CREDIT_LIMIT_NEGATIVE);
        }
        AccountEntity updated = accountPort.save(account);
        publishAccountUpdatedEvent(updated);
        return updated;
    }

    @Transactional
    public void deleteAccount(Long id, Long tenantId) {
        AccountEntity account = accountPort.findById(id, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));

        List<AccountEntity> children = accountPort.findByParentAccountId(id, tenantId);
        if (!children.isEmpty()) {
            throw new AppException(ErrorMessage.CANNOT_DELETE_ACCOUNT_WITH_CHILDREN);
        }

        // TODO: Validation: No active opportunities

        accountPort.deleteById(id, tenantId);

        // Publish event
        publishAccountDeletedEvent(account);

    }

    @Transactional
    public void updateAccountRevenue(Long accountId, Long tenantId, BigDecimal revenue, boolean isWon) {

        AccountEntity account = accountPort.findById(accountId, tenantId)
                .orElseThrow(() -> new AppException(ErrorMessage.ACCOUNT_NOT_FOUND));

        account.recordOpportunityResult(isWon, revenue != null ? revenue : BigDecimal.ZERO, tenantId);
        if (isWon) {
            account.promoteToCustomer(tenantId);
        }

        accountPort.save(account);

    }

    @Override
    @Transactional(readOnly = true)
    public boolean isEmailExists(String email, Long tenantId) {
        return email != null && accountPort.existsByEmail(email, tenantId);
    }

    @Override
    @Transactional(readOnly = true)
    public Pair<List<AccountEntity>, Long> filterAccounts(AccountFilterRequest filter, Long tenantId,
            PageRequest pageRequest) {
        pageRequest.validate();
        return accountPort.filter(filter, pageRequest, tenantId);
    }

    private void publishAccountCreatedEvent(AccountEntity Account) {
        log.debug("Event: Account created - ID: {}, Topic: {}", Account.getId(), Constants.KafkaTopic.ACCOUNT);
    }

    private void publishAccountUpdatedEvent(AccountEntity Account) {
        log.debug("Event: Account updated - ID: {}, Topic: {}", Account.getId(), Constants.KafkaTopic.ACCOUNT);
    }

    private void publishAccountDeletedEvent(AccountEntity Account) {
        log.debug("Event: Account deleted - ID: {}, Topic: {}", Account.getId(), Constants.KafkaTopic.ACCOUNT);
    }
}
