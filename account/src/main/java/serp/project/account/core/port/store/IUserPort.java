/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.core.port.store;

import java.util.List;

import org.springframework.data.util.Pair;
import serp.project.account.core.domain.dto.request.GetUserParams;
import serp.project.account.core.domain.entity.UserEntity;

public interface IUserPort {
    UserEntity save(UserEntity user);
    UserEntity getUserByEmail(String email);
    UserEntity getUserById(Long id);
    Pair<Long, List<UserEntity>> getUsers(GetUserParams params);
}
