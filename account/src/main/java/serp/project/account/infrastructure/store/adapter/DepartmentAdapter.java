/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import serp.project.account.core.domain.dto.request.GetDepartmentParams;
import serp.project.account.core.domain.entity.DepartmentEntity;
import serp.project.account.core.port.store.IDepartmentPort;
import serp.project.account.infrastructure.store.mapper.DepartmentMapper;
import serp.project.account.infrastructure.store.repository.IDepartmentRepository;
import serp.project.account.infrastructure.store.specification.DepartmentSpecification;
import serp.project.account.kernel.utils.PaginationUtils;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepartmentAdapter implements IDepartmentPort {
    private final IDepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;
    private final PaginationUtils paginationUtils;

    @Override
    public DepartmentEntity save(DepartmentEntity department) {
        var model = departmentMapper.toModel(department);
        return departmentMapper.toEntity(departmentRepository.save(model));
    }

    @Override
    public Optional<DepartmentEntity> getById(Long id) {
        return departmentRepository.findById(id)
                .map(departmentMapper::toEntity);
    }

    @Override
    public Optional<DepartmentEntity> getByOrganizationIdAndCode(Long organizationId, String code) {
        return departmentRepository.findByOrganizationIdAndCode(organizationId, code)
                .map(departmentMapper::toEntity);
    }

    @Override
    public List<DepartmentEntity> getByOrganizationId(Long organizationId) {
        return departmentMapper.toEntityList(departmentRepository.findByOrganizationId(organizationId));
    }

    @Override
    public List<DepartmentEntity> getActiveByOrganizationId(Long organizationId) {
        return departmentMapper
                .toEntityList(departmentRepository.findByOrganizationIdAndIsActive(organizationId, true));
    }

    @Override
    public List<DepartmentEntity> getByParentDepartmentId(Long parentDepartmentId) {
        return departmentMapper.toEntityList(departmentRepository.findByParentDepartmentId(parentDepartmentId));
    }

    @Override
    public List<DepartmentEntity> getActiveByParentDepartmentId(Long parentDepartmentId) {
        return departmentMapper
                .toEntityList(departmentRepository.findByParentDepartmentIdAndIsActive(parentDepartmentId, true));
    }

    @Override
    public List<DepartmentEntity> getByManagerId(Long managerId) {
        return departmentMapper.toEntityList(departmentRepository.findByManagerId(managerId));
    }

    @Override
    public Pair<List<DepartmentEntity>, Long> getDepartments(GetDepartmentParams params) {
        var pageable = paginationUtils.getPageable(params);
        var specification = DepartmentSpecification.buildSpec(params);
        var page = departmentRepository.findAll(specification, pageable);

        var departments = departmentMapper.toEntityList(page.getContent());
        return Pair.of(departments, page.getTotalElements());
    }

    @Override
    public Optional<DepartmentEntity> getLatestByOrganizationId(Long organizationId) {
        return departmentRepository.findTopByOrganizationIdOrderByIdDesc(organizationId)
                .map(departmentMapper::toEntity);
    }

    @Override
    public Long countByOrganizationId(Long organizationId) {
        return departmentRepository.countByOrganizationId(organizationId);
    }

    @Override
    public Long countActiveByOrganizationId(Long organizationId) {
        return departmentRepository.countByOrganizationIdAndIsActive(organizationId, true);
    }

    @Override
    public void delete(Long id) {
        departmentRepository.findById(id).ifPresent(model -> {
            model.setIsActive(false);
            departmentRepository.save(model);
            log.info("Soft deleted department with id: {}", id);
        });
    }
}
