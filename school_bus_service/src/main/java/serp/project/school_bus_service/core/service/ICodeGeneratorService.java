package serp.project.school_bus_service.core.service;

public interface ICodeGeneratorService {

    String generate(String sequenceKey, String prefix, Long tenantId, Long actorId);
}
