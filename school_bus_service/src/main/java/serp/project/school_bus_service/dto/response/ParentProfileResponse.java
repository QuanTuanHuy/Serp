package serp.project.school_bus_service.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ParentProfileResponse extends BaseResponse {

    private Long userId;
    private Long schoolBusUserId;
    private Long accountUserId;
    private String fullName;
    private String phone;
    private String email;
    private String address;
    private SchoolBusUserResponse user;

    public ParentProfileResponse() {
    }

    public ParentProfileResponse(Long id,
                                 Long tenantId,
                                 Boolean isActive,
                                 Boolean isDeleted,
                                 LocalDateTime createdAt,
                                 String createdBy,
                                 LocalDateTime updatedAt,
                                 String updatedBy,
                                 Long schoolBusUserId,
                                 Long accountUserId,
                                 String fullName,
                                 String phone,
                                 String email,
                                 String address) {
        setId(id);
        setTenantId(tenantId);
        setIsActive(isActive);
        setIsDeleted(isDeleted);
        setCreatedAt(createdAt);
        setCreatedBy(createdBy);
        setUpdatedAt(updatedAt);
        setUpdatedBy(updatedBy);
        this.schoolBusUserId = schoolBusUserId;
        this.accountUserId = accountUserId;
        this.userId = accountUserId;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }
}
