package serp.project.pmcore.domain.project.provisioning;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import serp.project.pmcore.domain.project.provisioning.provisioner.ISchemeProvisioner;
import serp.project.pmcore.domain.shared.enums.SchemeType;
import serp.project.pmcore.domain.shared.exception.DomainErrorCode;
import serp.project.pmcore.domain.shared.exception.DomainException;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SchemeProvisionerRegistry {
    private final Map<SchemeType, ISchemeProvisioner> provisioners;

    public SchemeProvisionerRegistry(List<ISchemeProvisioner> provisioners) {
        this.provisioners = new EnumMap<>(SchemeType.class);

        for (ISchemeProvisioner provisioner : provisioners) {
            SchemeType type = provisioner.supports();
            if (type == null) {
                throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                        "ISchemeProvisioner.supports() must not return null");
            }
            ISchemeProvisioner existing = this.provisioners.put(type, provisioner);
            if (existing != null) {
                throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                        "Duplicate SchemeProvisioner registered for scheme type: " + type);
            }
        }

        log.info("Registered {} scheme provisioners: {}", this.provisioners.size(), this.provisioners.keySet());
    }

    public ISchemeProvisioner get(SchemeType type) {
        if (type == null) {
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "Scheme type must not be null");
        }
        ISchemeProvisioner provisioner = provisioners.get(type);
        if (provisioner == null) {
            throw new DomainException(DomainErrorCode.SCHEME_PROVISIONING_FAILED,
                    "No SchemeProvisioner registered for scheme type: " + type);
        }
        return provisioner;
    }
}
