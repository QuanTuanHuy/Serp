package serp.project.school_bus_service.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.school_bus_service.service.ICodeGeneratorService;
import serp.project.school_bus_service.entity.CodeSequenceEntity;
import serp.project.school_bus_service.repository.CodeSequenceRepository;

@Service
public class CodeGeneratorServiceImpl implements ICodeGeneratorService {

    private static final long INITIAL_SEQUENCE_VALUE = 1L;

    private final CodeSequenceRepository codeSequenceRepository;


    public CodeGeneratorServiceImpl(
    CodeSequenceRepository codeSequenceRepository) {
        this.codeSequenceRepository = codeSequenceRepository;
    }


    @Override
    @Transactional
    public String generate(String sequenceKey, String prefix, Long tenantId, Long actorId) {
        CodeSequenceEntity sequence = codeSequenceRepository
                .findByTenantIdAndSequenceKeyAndIsDeletedFalse(tenantId, sequenceKey)
                .orElseGet(() -> createSequence(sequenceKey, tenantId, actorId));
        Long currentValue = sequence.getNextValue();
        sequence.setNextValue(currentValue + 1);
        sequence.markUpdated(actor(actorId));
        codeSequenceRepository.save(sequence);
        return "%s%06d".formatted(prefix, currentValue);
    }

    private CodeSequenceEntity createSequence(String sequenceKey, Long tenantId, Long actorId) {
        CodeSequenceEntity sequence = new CodeSequenceEntity();
        sequence.markCreated(tenantId, actor(actorId));
        sequence.setSequenceKey(sequenceKey);
        sequence.setNextValue(INITIAL_SEQUENCE_VALUE);
        return sequence;
    }

    private String actor(Long actorId) {
        return actorId == null ? "SYSTEM" : String.valueOf(actorId);
    }
}
