/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service;

import serp.project.first_mile.dto.request.ReserveOriginPostOfficeRequest;
import serp.project.first_mile.dto.response.OriginPostOfficeReservationResponse;

public interface PostOfficeReservationService {
    OriginPostOfficeReservationResponse reserveBestOriginPostOffice(ReserveOriginPostOfficeRequest request);

    OriginPostOfficeReservationResponse reserveDropOffOriginPostOffice(
            Long postOfficeId,
            ReserveOriginPostOfficeRequest request
    );

    OriginPostOfficeReservationResponse validateManagedPostOffice(Long postOfficeId);
}
