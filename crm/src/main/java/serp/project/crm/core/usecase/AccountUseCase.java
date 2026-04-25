/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateAccountRequest;
import serp.project.crm.core.domain.dto.request.AccountFilterRequest;
import serp.project.crm.core.domain.dto.request.UpdateAccountRequest;
import serp.project.crm.core.domain.dto.request.UpdateCreditLimitRequest;
import serp.project.crm.core.domain.dto.response.AccountResponse;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.AccountDtoMapper;
import serp.project.crm.core.service.IAccountService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountUseCase {

    private final IAccountService accountService;
    private final AccountDtoMapper accountDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createAccount(CreateAccountRequest request, Long tenantId) {
        try {
            AccountEntity accountEntity = accountDtoMapper.toEntity(request);
            AccountEntity createdAccount = accountService.createAccount(accountEntity, tenantId);
            AccountResponse response = accountDtoMapper.toResponse(createdAccount);

            log.info("[AccountUseCase] Account created successfully with ID: {}", createdAccount.getId());
            return responseUtils.success(response, "Account created successfully");

        } catch (AppException e) {
            log.error("[AccountUseCase] Error creating Account: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[AccountUseCase] Unexpected error creating Account: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateAccount(Long id, UpdateAccountRequest request, Long tenantId) {
        try {
            AccountEntity updates = accountDtoMapper.toEntity(request);
            AccountEntity updatedAccount = accountService.updateAccount(id, updates, tenantId);
            AccountResponse response = accountDtoMapper.toResponse(updatedAccount);

            log.info("[AccountUseCase] Account updated successfully: {}", id);
            return responseUtils.success(response, "Account updated successfully");

        } catch (AppException e) {
            log.error("[AccountUseCase] Error updating Account: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[AccountUseCase] Unexpected error updating Account: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAccountById(Long id, Long tenantId) {
        try {
            AccountEntity account = accountService.getAccountById(id, tenantId)
                    .orElse(null);

            if (account == null) {
                return responseUtils.notFound(ErrorMessage.ACCOUNT_NOT_FOUND);
            }

            AccountResponse response = accountDtoMapper.toResponse(account);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("[AccountUseCase] Error fetching Account: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch Account");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllAccounts(Long tenantId, PageRequest pageRequest) {
        try {
            var result = accountService.getAllAccounts(tenantId, pageRequest);

            List<AccountResponse> accountResponses = result.getFirst().stream()
                    .map(accountDtoMapper::toResponse)
                    .toList();

            PageResponse<AccountResponse> pageResponse = PageResponse.of(
                    accountResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[AccountUseCase] Error fetching Accounts: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch Accounts");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> filterAccounts(AccountFilterRequest request, Long tenantId) {
        try {

            PageRequest pageRequest = request.toPageRequest();
            var result = accountService.filterAccounts(request, tenantId, pageRequest);

            List<AccountResponse> accountResponses = result.getFirst().stream()
                    .map(accountDtoMapper::toResponse)
                    .toList();
            PageResponse<AccountResponse> pageResponse = PageResponse.of(
                    accountResponses, pageRequest, result.getSecond());
            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[AccountUseCase] Error filtering Accounts: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to filter Accounts");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> activateAccount(Long id, Long userId, Long tenantId) {
        AccountEntity updatedAccount = accountService.activateAccount(id, userId, tenantId);
        AccountResponse response = accountDtoMapper.toResponse(updatedAccount);
        return responseUtils.success(response, "Account activated successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deactivateAccount(Long id, Long userId, Long tenantId) {
        AccountEntity updatedAccount = accountService.deactivateAccount(id, userId, tenantId);
        AccountResponse response = accountDtoMapper.toResponse(updatedAccount);
        return responseUtils.success(response, "Account deactivated successfully");
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateCreditLimit(Long id, UpdateCreditLimitRequest request, Long userId, Long tenantId) {
        AccountEntity updatedAccount = accountService.updateCreditLimit(id, request.getCreditLimit(), userId, tenantId);
        AccountResponse response = accountDtoMapper.toResponse(updatedAccount);
        return responseUtils.success(response, "Credit limit updated successfully");
    }

    @Transactional
    public GeneralResponse<?> deleteAccount(Long id, Long tenantId) {
        try {
            accountService.deleteAccount(id, tenantId);

            log.info("[AccountUseCase] Account deleted successfully: {}", id);
            return responseUtils.status("Account deleted successfully");

        } catch (AppException e) {
            log.error("[AccountUseCase] Validation error deleting Account: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("[AccountUseCase] Unexpected error deleting Account: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete Account");
        }
    }
}
