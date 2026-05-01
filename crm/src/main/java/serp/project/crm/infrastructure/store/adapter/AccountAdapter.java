/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.request.AccountFilterRequest;
import serp.project.crm.core.domain.entity.AccountEntity;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.port.store.IAccountPort;
import serp.project.crm.infrastructure.store.mapper.AccountMapper;
import serp.project.crm.infrastructure.store.model.AccountModel;
import serp.project.crm.infrastructure.store.repository.AccountRepository;
import serp.project.crm.infrastructure.store.specification.AccountSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AccountAdapter implements IAccountPort {

    private final AccountRepository AccountRepository;
    private final AccountMapper AccountMapper;

    @Override
    public AccountEntity save(AccountEntity AccountEntity) {
        var model = AccountMapper.toModel(AccountEntity);
        var savedModel = AccountRepository.save(model);
        return AccountMapper.toEntity(savedModel);
    }

    @Override
    public Optional<AccountEntity> findById(Long id, Long tenantId) {
        return AccountRepository.findByIdAndTenantId(id, tenantId)
                .map(AccountMapper::toEntity);
    }

    @Override
    public Optional<AccountEntity> findByEmail(String email, Long tenantId) {
        return AccountRepository.findByEmailAndTenantId(email, tenantId)
                .map(AccountMapper::toEntity);
    }

    @Override
    public Pair<List<AccountEntity>, Long> findAll(Long tenantId, PageRequest pageRequest) {
        var page = AccountRepository.findByTenantId(tenantId, AccountMapper.toPageable(pageRequest));
        return AccountMapper.pageToPair(page.map(AccountMapper::toEntity));
    }

    @Override
    public Pair<List<AccountEntity>, Long> searchByKeyword(String keyword, Long tenantId, PageRequest pageRequest) {
        var page = AccountRepository.searchByKeyword(tenantId, keyword, AccountMapper.toPageable(pageRequest));
        return AccountMapper.pageToPair(page.map(AccountMapper::toEntity));
    }

    @Override
    public List<AccountEntity> findByParentAccountId(Long parentAccountId, Long tenantId) {
        return AccountRepository.findByParentAccountIdAndTenantId(parentAccountId, tenantId)
                .stream()
                .map(AccountMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Pair<List<AccountEntity>, Long> findByActiveStatus(ActiveStatus activeStatus, Long tenantId,
            PageRequest pageRequest) {
        var page = AccountRepository.findByTenantIdAndActiveStatus(tenantId, activeStatus.name(),
                AccountMapper.toPageable(pageRequest));
        return AccountMapper.pageToPair(page.map(AccountMapper::toEntity));
    }

    @Override
    public Long countByActiveStatus(ActiveStatus activeStatus, Long tenantId) {
        return AccountRepository.countByTenantIdAndActiveStatus(tenantId, activeStatus.name());
    }

    @Override
    public Boolean existsByEmail(String email, Long tenantId) {
        return AccountRepository.existsByEmailAndTenantId(email, tenantId);
    }

    @Override
    public void deleteById(Long id, Long tenantId) {
        AccountRepository.findByIdAndTenantId(id, tenantId)
                .ifPresent(AccountRepository::delete);
    }

    @Override
    public List<AccountEntity> findTopByRevenue(Long tenantId, int limit) {
        var pageable = org.springframework.data.domain.PageRequest.of(0, limit,
                Sort.by(Sort.Direction.DESC, "totalRevenue"));
        return AccountRepository.findByTenantId(tenantId, pageable)
                .stream()
                .map(AccountMapper::toEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Pair<List<AccountEntity>, Long> findByIndustry(String industry, Long tenantId, PageRequest pageRequest) {
        var page = AccountRepository
                .findByTenantIdAndIndustry(tenantId, industry, AccountMapper.toPageable(pageRequest))
                .map(AccountMapper::toEntity);
        return AccountMapper.pageToPair(page);
    }

    @Override
    public Pair<List<AccountEntity>, Long> filter(AccountFilterRequest filter, PageRequest pageRequest, Long tenantId) {
        var pageable = AccountMapper.toPageable(pageRequest);
        Specification<AccountModel> spec = AccountSpecification.build(filter, tenantId);
        var page = AccountRepository.findAll(spec, pageable)
                .map(AccountMapper::toEntity);
        return AccountMapper.pageToPair(page);
    }

    @Override
    public List<AccountEntity> findByIds(List<Long> ids, Long tenantId) {
        return AccountRepository.findByTenantIdAndIdIn(tenantId, ids).stream()
                .map(AccountMapper::toEntity)
                .toList();
    }
}
