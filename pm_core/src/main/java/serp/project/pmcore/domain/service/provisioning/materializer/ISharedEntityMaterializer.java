package serp.project.pmcore.domain.service.provisioning.materializer;

public interface ISharedEntityMaterializer<S> {
    Long materialize(Long sourceId, Long tenantId, Long userId);
}
