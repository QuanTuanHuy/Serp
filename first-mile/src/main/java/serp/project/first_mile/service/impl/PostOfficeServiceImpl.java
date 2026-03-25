/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.first_mile.domain.PostOffice;
import serp.project.first_mile.domain.Ward;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreatePostOfficeRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeRequest;
import serp.project.first_mile.dto.response.PostOfficeResponse;
import serp.project.first_mile.exception.AppException;
import serp.project.first_mile.exception.ErrorCode;
import serp.project.first_mile.mapper.PostOfficeMapper;
import serp.project.first_mile.repository.PostOfficeRepository;
import serp.project.first_mile.repository.ProvinceRepository;
import serp.project.first_mile.repository.WardRepository;
import serp.project.first_mile.service.PostOfficeService;

@Service
@RequiredArgsConstructor
public class PostOfficeServiceImpl implements PostOfficeService {
    private final PostOfficeRepository postOfficeRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;

    @Override
    public PageResponse<PostOfficeResponse> getPostOffices(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<PostOffice> postOfficePage;
        if (keyword == null || keyword.isBlank()) {
            postOfficePage = postOfficeRepository.findAll(pageable);
        } else {
            postOfficePage = postOfficeRepository.findByCodeContainingIgnoreCaseOrNameContainingIgnoreCase(
                    keyword,
                    keyword,
                    pageable
            );
        }

        Page<PostOfficeResponse> mappedPage = postOfficePage.map(PostOfficeMapper::toResponse);

        return PageResponse.<PostOfficeResponse>builder()
                .items(mappedPage.getContent())
                .page(mappedPage.getNumber())
                .size(mappedPage.getSize())
                .totalElements(mappedPage.getTotalElements())
                .totalPages(mappedPage.getTotalPages())
                .hasNext(mappedPage.hasNext())
                .hasPrevious(mappedPage.hasPrevious())
                .build();
    }

    @Override
    public PostOfficeResponse getPostOfficeById(Long id) {
        PostOffice postOffice = getPostOfficeOrThrow(id);
        return PostOfficeMapper.toResponse(postOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeResponse createPostOffice(CreatePostOfficeRequest request) {
        validateGeoCoordinates(request.latitude(), request.longitude());
        validateAddress(request.provinceCode(), request.wardCode());

        if (postOfficeRepository.existsByCode(request.code())) {
            throw new AppException(ErrorCode.POST_OFFICE_CODE_EXISTED);
        }

        PostOffice postOffice = PostOfficeMapper.toEntity(request);
        PostOffice savedPostOffice = postOfficeRepository.save(postOffice);
        return PostOfficeMapper.toResponse(savedPostOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostOfficeResponse updatePostOffice(Long id, UpdatePostOfficeRequest request) {
        validateGeoCoordinates(request.latitude(), request.longitude());
        validateAddress(request.provinceCode(), request.wardCode());

        PostOffice postOffice = getPostOfficeOrThrow(id);

        if (!postOffice.getCode().equals(request.code()) && postOfficeRepository.existsByCode(request.code())) {
            throw new AppException(ErrorCode.POST_OFFICE_CODE_EXISTED);
        }

        PostOfficeMapper.mapForUpdate(request, postOffice);
        PostOffice updatedPostOffice = postOfficeRepository.save(postOffice);
        return PostOfficeMapper.toResponse(updatedPostOffice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePostOffice(Long id) {
        PostOffice postOffice = getPostOfficeOrThrow(id);
        postOfficeRepository.delete(postOffice);
    }

    private PostOffice getPostOfficeOrThrow(Long id) {
        return postOfficeRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.POST_OFFICE_NOT_FOUND));
    }

    private void validateAddress(String provinceCode, String wardCode) {
        provinceRepository.findByProvinceCode(provinceCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROVINCE_NOT_FOUND));

        Ward ward = wardRepository.findByWardCode(wardCode)
                .orElseThrow(() -> new AppException(ErrorCode.WARD_NOT_FOUND));

        if (!provinceCode.equals(ward.getProvinceCode())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private void validateGeoCoordinates(Double latitude, Double longitude) {
        if ((latitude == null && longitude != null) || (latitude != null && longitude == null)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }
}
