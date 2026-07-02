package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.entity.SchoolBusUserEntity;
import serp.project.school_bus_service.entity.SchoolBusUserRoleEntity;
import serp.project.school_bus_service.repository.SchoolBusUserRoleRepository;
import serp.project.school_bus_service.service.ISchoolBusUserRoleService;
import serp.project.school_bus_service.shared.base.AbstractBaseService;
import serp.project.school_bus_service.shared.base.BaseRepository;

import java.util.Collection;
import java.util.Map;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SchoolBusUserRoleServiceImpl extends AbstractBaseService<SchoolBusUserRoleEntity, Long>
        implements ISchoolBusUserRoleService {

    private static final String SCHOOL_BUS_ROLE_PREFIX = "SCHOOL_BUS_";

    private final SchoolBusUserRoleRepository schoolBusUserRoleRepository;

    public SchoolBusUserRoleServiceImpl(SchoolBusUserRoleRepository schoolBusUserRoleRepository) {
        this.schoolBusUserRoleRepository = schoolBusUserRoleRepository;
    }

    @Override
    protected BaseRepository<SchoolBusUserRoleEntity, Long> getRepository() {
        return schoolBusUserRoleRepository;
    }

    @Override
    @Transactional
    public void replaceRoles(SchoolBusUserEntity user, Collection<String> roleNames) {
        if (user == null || user.getId() == null) {
            return;
        }

        Set<String> normalizedRoles = normalizeRoles(roleNames);
        List<SchoolBusUserRoleEntity> existingRoles =
                schoolBusUserRoleRepository.findAllBySchoolBusUserIdAndTenantId(user.getId(), user.getTenantId());
        Map<String, SchoolBusUserRoleEntity> existingByRole = existingRoles.stream()
                .collect(Collectors.toMap(
                        SchoolBusUserRoleEntity::getRoleName,
                        Function.identity(),
                        (first, ignored) -> first));

        List<SchoolBusUserRoleEntity> rolesToSave = new java.util.ArrayList<>();

        for (SchoolBusUserRoleEntity existingRole : existingRoles) {
            if (!normalizedRoles.contains(existingRole.getRoleName())
                    && !Boolean.TRUE.equals(existingRole.getIsDeleted())) {
                existingRole.markSoftDeleted("SYSTEM");
                existingRole.setIsActive(false);
                rolesToSave.add(existingRole);
            }
        }

        for (String roleName : normalizedRoles) {
            SchoolBusUserRoleEntity existingRole = existingByRole.get(roleName);
            if (existingRole == null) {
                rolesToSave.add(createRole(user, roleName));
                continue;
            }
            if (Boolean.TRUE.equals(existingRole.getIsDeleted()) || Boolean.FALSE.equals(existingRole.getIsActive())) {
                existingRole.setIsDeleted(false);
                existingRole.setIsActive(true);
                existingRole.markUpdated("SYSTEM");
                rolesToSave.add(existingRole);
            }
        }

        if (!rolesToSave.isEmpty()) {
            schoolBusUserRoleRepository.saveAll(rolesToSave);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findActiveUserIdsByTenantAndRoleNames(Long tenantId, Collection<String> roleNames) {
        Set<String> normalizedRoles = normalizeRoles(roleNames);
        if (tenantId == null || normalizedRoles.isEmpty()) {
            return List.of();
        }
        return schoolBusUserRoleRepository.findActiveUserIdsByTenantAndRoleNames(tenantId, normalizedRoles);
    }

    private Set<String> normalizeRoles(Collection<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            return Set.of();
        }

        Set<String> normalizedRoles = new LinkedHashSet<>();
        for (String roleName : roleNames) {
            if (roleName == null || roleName.isBlank()) {
                continue;
            }
            String normalized = roleName.trim().toUpperCase(Locale.ROOT);
            if (normalized.startsWith(SCHOOL_BUS_ROLE_PREFIX)) {
                normalizedRoles.add(normalized);
            }
        }
        return normalizedRoles;
    }

    private SchoolBusUserRoleEntity createRole(SchoolBusUserEntity user, String roleName) {
        SchoolBusUserRoleEntity role = new SchoolBusUserRoleEntity();
        role.markCreated(user.getTenantId(), "SYSTEM");
        role.setSchoolBusUserId(user.getId());
        role.setRoleName(roleName);
        return role;
    }
}
