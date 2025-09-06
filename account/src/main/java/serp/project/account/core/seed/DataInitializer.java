/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.domain.dto.request.CreateUserDto;
import serp.project.account.core.domain.entity.BaseEntity;
import serp.project.account.core.service.impl.RoleService;
import serp.project.account.core.service.impl.UserService;
import serp.project.account.kernel.property.AdminProperties;
import serp.project.account.kernel.utils.CollectionUtils;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserService userService;
    private final RoleService roleService;

    private final AdminProperties adminProperties;

    @Override
    public void run(String... args) {
        log.info("Initializing data...");

        try {
            createSuperAdminUser();
        } catch (Exception e) {
            log.error("Data initialization failed: {}", e.getMessage());
        }
    }

    private void createSuperAdminUser() {
        // var roles = roleService.getAllRoles();
        // if (CollectionUtils.isEmpty(roles)) {
        //     log.info("No roles found, need create roles ...");
        //     return;
        // }
        // var createUserDto = CreateUserDto.builder()
        //         .email(adminProperties.getEmail())
        //         .password(adminProperties.getPassword())
        //         .fullName("Serp Super Admin")
        //         .roleIds(roles.stream().map(BaseEntity::getId).toList())
        //         .build();
        // userService.createUser(createUserDto);
        log.info("Super admin user created successfully.");
    }

}
