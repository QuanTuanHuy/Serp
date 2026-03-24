/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.response.ProvinceResponse;
import serp.project.first_mile.dto.response.WardResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.mapper.LocationMapper;
import serp.project.first_mile.repository.ProvinceRepository;
import serp.project.first_mile.repository.WardRepository;
import serp.project.first_mile.service.LocationService;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;

    @Override
    public PageResponse<ProvinceResponse> getProvinces(int page, int size) {
    Pageable pageable = PageRequest.of(page, size);
    var provincePage = provinceRepository.findAllByOrderByNameAsc(pageable)
        .map(LocationMapper::toProvinceResponse);

    return PageResponse.<ProvinceResponse>builder()
        .items(provincePage.getContent())
        .page(provincePage.getNumber())
        .size(provincePage.getSize())
        .totalElements(provincePage.getTotalElements())
        .totalPages(provincePage.getTotalPages())
        .hasNext(provincePage.hasNext())
        .hasPrevious(provincePage.hasPrevious())
        .build();
    }

    @Override
    public PageResponse<WardResponse> getWardsByProvinceCode(String provinceCode, int page, int size) {
        provinceRepository.findByProvinceCode(provinceCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROVINCE_NOT_FOUND));

    Pageable pageable = PageRequest.of(page, size);
    var wardPage = wardRepository.findAllByProvinceCodeOrderByNameAsc(provinceCode, pageable)
        .map(LocationMapper::toWardResponse);

    return PageResponse.<WardResponse>builder()
        .items(wardPage.getContent())
        .page(wardPage.getNumber())
        .size(wardPage.getSize())
        .totalElements(wardPage.getTotalElements())
        .totalPages(wardPage.getTotalPages())
        .hasNext(wardPage.hasNext())
        .hasPrevious(wardPage.hasPrevious())
        .build();
    }
}
