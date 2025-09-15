/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */


package serp.project.account.core.seed;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import serp.project.account.core.service.IRoleService;
import serp.project.account.core.service.IUserService;
import serp.project.account.kernel.property.AdminProperties;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final IUserService userService;
    private final IRoleService roleService;

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
        // log.info("Super admin user created successfully.");
    }

}
