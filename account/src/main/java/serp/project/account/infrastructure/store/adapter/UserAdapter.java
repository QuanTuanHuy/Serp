/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.adapter;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import serp.project.account.core.domain.entity.UserEntity;
import serp.project.account.core.port.store.IUserPort;
import serp.project.account.infrastructure.store.mapper.UserMapper;
import serp.project.account.infrastructure.store.model.UserModel;
import serp.project.account.infrastructure.store.repository.IUserRepository;

@Component
@RequiredArgsConstructor
public class UserAdapter implements IUserPort {
    private final IUserRepository userRepository;
    private final UserMapper userMapper;
    
    @Override
    public UserEntity save(UserEntity user) {
        UserModel userModel = userMapper.toModel(user);
        return userMapper.toEntity(userRepository.save(userModel));
    }

    @Override
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toEntity)
                .orElse(null);
    }

    @Override
    public UserEntity getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toEntity)
                .orElse(null);
    }
}
