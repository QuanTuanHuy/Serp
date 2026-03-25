/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.first_mile.ui.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import serp.project.first_mile.dto.ApiResponse;
import serp.project.first_mile.dto.PageResponse;
import serp.project.first_mile.dto.request.CreatePostOfficeRequest;
import serp.project.first_mile.dto.request.UpdatePostOfficeRequest;
import serp.project.first_mile.dto.response.PostOfficeResponse;
import serp.project.first_mile.exception.MessageService;
import serp.project.first_mile.service.PostOfficeService;

@RestController
@RequestMapping("/api/v1/post-offices")
@RequiredArgsConstructor
public class PostOfficeController {
    private final PostOfficeService postOfficeService;
    private final MessageService messageService;

    @GetMapping
    public ApiResponse<PageResponse<PostOfficeResponse>> getPostOffices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword
    ) {
        return ApiResponse.<PageResponse<PostOfficeResponse>>builder()
                .message(messageService.getMessage("success.post_offices.list"))
                .result(postOfficeService.getPostOffices(page, size, keyword))
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<PostOfficeResponse> getPostOfficeById(@PathVariable Long id) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.detail"))
                .result(postOfficeService.getPostOfficeById(id))
                .build();
    }

    @PostMapping
    public ApiResponse<PostOfficeResponse> createPostOffice(@Valid @RequestBody CreatePostOfficeRequest request) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.create"))
                .result(postOfficeService.createPostOffice(request))
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<PostOfficeResponse> updatePostOffice(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePostOfficeRequest request
    ) {
        return ApiResponse.<PostOfficeResponse>builder()
                .message(messageService.getMessage("success.post_offices.update"))
                .result(postOfficeService.updatePostOffice(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePostOffice(@PathVariable Long id) {
        postOfficeService.deletePostOffice(id);
        return ApiResponse.<Void>builder()
                .message(messageService.getMessage("success.post_offices.delete"))
                .build();
    }
}
