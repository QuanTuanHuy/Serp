/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.account.infrastructure.store.specification;

import org.springframework.data.jpa.domain.Specification;
import serp.project.account.infrastructure.store.model.RoleModel;
import java.util.List;

public class RoleSpecification extends BaseSpecification<RoleModel> {
    
    public static Specification<RoleModel> hasExactName(String name) {
        return equal("name", name);
    }
    
    public static Specification<RoleModel> hasId(Long id) {
        return equal("id", id);
    }
    
    public static Specification<RoleModel> hasIds(List<Long> ids) {
        return in("id", ids);
    }
    
    public static Specification<RoleModel> findSystemRoles() {
        return or(
            hasExactName("ADMIN"),
            hasExactName("USER"),
            hasExactName("SUPER_ADMIN")
        );
    }

}
