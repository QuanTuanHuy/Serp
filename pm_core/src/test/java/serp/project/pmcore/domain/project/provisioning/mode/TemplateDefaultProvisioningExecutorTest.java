/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.project.provisioning.mode;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.project.provisioning.ProvisioningExecutionContext;
import serp.project.pmcore.domain.project.provisioning.SchemeProvisionerRegistry;
import serp.project.pmcore.domain.project.provisioning.provisioner.ISchemeProvisioner;
import serp.project.pmcore.domain.shared.enums.SchemeType;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TemplateDefaultProvisioningExecutorTest {

    private static final Long TENANT_ID = 1L;
    private static final Long USER_ID = 2L;

    @Test
    void provisionShouldClonePermissionSchemeForTemplateDefaults() {
        Map<SchemeType, Long> sourceBindings = new EnumMap<>(SchemeType.class);
        for (SchemeType schemeType : SchemeType.values()) {
            sourceBindings.put(schemeType, 100L + schemeType.ordinal());
        }

        Map<SchemeType, ISchemeProvisioner> provisioners = new EnumMap<>(SchemeType.class);
        for (SchemeType schemeType : SchemeType.values()) {
            ISchemeProvisioner provisioner = mock(ISchemeProvisioner.class);
            when(provisioner.supports()).thenReturn(schemeType);
            when(provisioner.resolveClonedBinding(eq(sourceBindings.get(schemeType)), eq(TENANT_ID), eq(USER_ID), any()))
                    .thenReturn(1_000L + schemeType.ordinal());
            when(provisioner.resolveSharedBinding(eq(sourceBindings.get(schemeType)), eq(TENANT_ID), eq(USER_ID), any()))
                    .thenReturn(2_000L + schemeType.ordinal());
            provisioners.put(schemeType, provisioner);
        }

        TemplateDefaultProvisioningExecutor executor = new TemplateDefaultProvisioningExecutor(
                new SchemeProvisionerRegistry(provisioners.values().stream().toList())
        );

        Map<SchemeType, Long> result = executor.provision(
                sourceBindings,
                TENANT_ID,
                USER_ID,
                ProvisioningExecutionContext.builder()
                        .projectId(10L)
                        .projectKey("SERP")
                        .build()
        );

        assertEquals(1_000L + SchemeType.PERMISSION.ordinal(), result.get(SchemeType.PERMISSION));
        verify(provisioners.get(SchemeType.PERMISSION)).resolveClonedBinding(
                eq(sourceBindings.get(SchemeType.PERMISSION)),
                eq(TENANT_ID),
                eq(USER_ID),
                any()
        );
        verify(provisioners.get(SchemeType.PERMISSION), never()).resolveSharedBinding(
                eq(sourceBindings.get(SchemeType.PERMISSION)),
                eq(TENANT_ID),
                eq(USER_ID),
                any()
        );
    }
}
