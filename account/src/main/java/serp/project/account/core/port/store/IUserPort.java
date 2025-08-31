/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import serp.project.account.core.domain.entity.UserEntity;

public interface IUserPort {
    UserEntity save(UserEntity user);
    UserEntity getUserByEmail(String email);
    UserEntity getUserById(Long id);
}
