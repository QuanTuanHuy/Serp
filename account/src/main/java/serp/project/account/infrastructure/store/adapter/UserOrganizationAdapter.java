/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.UserOrganizationEntity;
import serp.project.account.core.port.store.IUserOrganizationPort;
import serp.project.account.infrastructure.store.mapper.UserOrganizationMapper;
import serp.project.account.infrastructure.store.model.UserOrganizationModel;
import serp.project.account.infrastructure.store.repository.IUserOrganizationRepository;

@Component
@RequiredArgsConstructor
public class UserOrganizationAdapter implements IUserOrganizationPort {
    private final IUserOrganizationRepository userOrganizationRepository;
    private final UserOrganizationMapper userOrganizationMapper;
    
    @Override
    public UserOrganizationEntity save(UserOrganizationEntity userOrganization) {
        UserOrganizationModel userOrganizationModel = userOrganizationMapper.toModel(userOrganization);
        return userOrganizationMapper.toEntity(userOrganizationRepository.save(userOrganizationModel));
    }

    @Override
    public List<UserOrganizationEntity> getByUserId(Long userId) {
        return userOrganizationMapper.toEntityList(userOrganizationRepository.findByUserId(userId));
    }

    @Override
    public UserOrganizationEntity getByUserIdAndOrganizationIdAndRoleId(Long userId, Long organizationId, Long roleId) {
        return userOrganizationMapper.toEntity(
                userOrganizationRepository.findByUserIdAndOrganizationIdAndRoleId(userId, organizationId, roleId));
    }
}
