package serp.project.first_mile.service;

import org.springframework.web.multipart.MultipartFile;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffAssignmentRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeStaffRequest;
import serp.project.first_mile.dto.response.PostOfficeStaffAssignmentResponse;
import serp.project.first_mile.dto.response.PostOfficeStaffResponse;
import serp.project.first_mile.enums.PostOfficeStaffRole;

import java.util.List;

public interface PostOfficeStaffService {

	PostOfficeStaffResponse getPostOfficeStaffById(Long id);

	List<PostOfficeStaffResponse> getAssignableStaffByRole(PostOfficeStaffRole role, String keyword);

	List<PostOfficeStaffResponse> getActiveCouriersByPostOffice(Long postOfficeId);

	PostOfficeStaffResponse updatePostOfficeStaff(Long id, UpdatePostOfficeStaffRequest request);

	PostOfficeStaffAssignmentResponse assignCourierToPostOffice(Long id, Long postOfficeId);

	PostOfficeStaffAssignmentResponse assignManagerToPostOffice(Long id, Long postOfficeId);

	PostOfficeStaffAssignmentResponse updateCourierAssignmentDetails(
			Long assignmentId,
			UpdatePostOfficeStaffAssignmentRequest request
	);

	List<PostOfficeStaffAssignmentResponse> getActiveAssignmentsByPostOffice(Long postOfficeId, PostOfficeStaffRole role);

	PostOfficeStaffAssignmentResponse unassignStaffFromPostOffice(Long assignmentId);

	PostOfficeStaffResponse uploadAvatar(Long id, MultipartFile file);
}
