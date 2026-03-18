package serp.project.pmcore.application.command.project.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import serp.project.pmcore.domain.dto.request.CreateProjectRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateProjectValidator {

    public void validate(CreateProjectRequest request, Long tenantId) {

    }
}
