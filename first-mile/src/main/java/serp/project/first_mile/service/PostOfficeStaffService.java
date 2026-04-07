package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffRequest;
import serp.project.first_mile.dto.response.PostOfficeStaffAssignmentResponse;
import serp.project.first_mile.dto.response.PostOfficeStaffResponse;

public interface PostOfficeStaffService {

	PostOfficeStaffResponse updatePostOfficeStaff(Long id, UpdatePostOfficeStaffRequest request);

	PostOfficeStaffAssignmentResponse assignCourierToPostOffice(Long id, Long postOfficeId);

	PostOfficeStaffAssignmentResponse assignManagerToPostOffice(Long id, Long postOfficeId);

	PostOfficeStaffResponse uploadAvatar(Long id, MultipartFile file);
}
