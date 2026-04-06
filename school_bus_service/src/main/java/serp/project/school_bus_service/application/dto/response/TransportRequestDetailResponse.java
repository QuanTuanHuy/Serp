package serp.project.school_bus_service.application.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TransportRequestDetailResponse {

    private TransportRequestResponse request;
    private List<RequestStudentResponse> students;
}
