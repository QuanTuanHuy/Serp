/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.service;

import serp.project.second_mile.dto.response.HubStaffAssignmentResponse;
import serp.project.second_mile.dto.response.HubStaffResponse;
import serp.project.second_mile.enums.HubStaffRole;

import java.util.List;

public interface HubStaffAssignmentService {
    HubStaffAssignmentResponse assignStaffToHub(Long staffId, Long hubId);

    List<HubStaffAssignmentResponse> listActiveAssignmentsByHub(Long hubId, HubStaffRole role);

    HubStaffAssignmentResponse unassignStaffFromHub(Long assignmentId);

    List<HubStaffResponse> getAssignableStaffByRole(HubStaffRole role, String keyword);
}
