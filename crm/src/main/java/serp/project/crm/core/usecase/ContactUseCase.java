/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import serp.project.crm.core.domain.constant.ErrorMessage;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateContactRequest;
import serp.project.crm.core.domain.dto.request.UpdateContactRequest;
import serp.project.crm.core.domain.dto.response.ContactResponse;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.exception.AppException;
import serp.project.crm.core.mapper.ContactDtoMapper;
import serp.project.crm.core.service.IContactService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ContactUseCase {

    private final IContactService contactService;
    private final ContactDtoMapper contactDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> createContact(CreateContactRequest request, Long userId, Long tenantId) {
        try {
            ContactEntity contactEntity = contactDtoMapper.toEntity(request);
            ContactEntity createdContact = contactService.createContact(contactEntity, userId, tenantId);
            ContactResponse response = contactDtoMapper.toResponse(createdContact);

            log.info("[ContactUseCase] Contact created successfully with ID: {}", createdContact.getId());
            return responseUtils.success(response, "Contact created successfully");

        } catch (AppException e) {
            log.error("[ContactUseCase] Error creating contact: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ContactUseCase] Unexpected error creating contact: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> updateContact(Long id, UpdateContactRequest request, Long userId, Long tenantId) {
        try {
            ContactEntity updates = contactDtoMapper.toEntity(request);
            ContactEntity updatedContact = contactService.updateContact(id, updates, userId, tenantId);
            ContactResponse response = contactDtoMapper.toResponse(updatedContact);

            log.info("[ContactUseCase] Contact updated successfully: {}", id);
            return responseUtils.success(response, "Contact updated successfully");

        } catch (AppException e) {
            log.error("[ContactUseCase] Error updating contact: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ContactUseCase] Unexpected error updating contact: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getContactById(Long id, Long tenantId) {
        try {
            ContactEntity contact = contactService.getContactById(id, tenantId)
                    .orElse(null);

            if (contact == null) {
                return responseUtils.notFound(ErrorMessage.CONTACT_NOT_FOUND);
            }

            ContactResponse response = contactDtoMapper.toResponse(contact);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("[ContactUseCase] Error fetching contact: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch contact");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getContactsByCustomerId(Long customerId, Long tenantId) {
        try {
            List<ContactEntity> contacts = contactService.getContactsByCustomerId(customerId, tenantId);

            List<ContactResponse> contactResponses = contacts.stream()
                    .map(contactDtoMapper::toResponse)
                    .toList();

            return responseUtils.success(contactResponses);

        } catch (Exception e) {
            log.error("[ContactUseCase] Error fetching contacts by customer ID: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch contacts");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllContacts(Long tenantId, PageRequest pageRequest) {
        try {
            var result = contactService.getAllContacts(tenantId, pageRequest);

            List<ContactResponse> contactResponses = result.getFirst().stream()
                    .map(contactDtoMapper::toResponse)
                    .toList();

            PageResponse<ContactResponse> pageResponse = PageResponse.of(
                    contactResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("[ContactUseCase] Error fetching contacts: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch contacts");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public GeneralResponse<?> deleteContact(Long id, Long tenantId) {
        try {
            contactService.deleteContact(id, tenantId);

            log.info("[ContactUseCase] Contact deleted successfully: {}", id);
            return responseUtils.status("Contact deleted successfully");

        } catch (AppException e) {
            log.error("[ContactUseCase] Error deleting contact: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[ContactUseCase] Unexpected error deleting contact: {}", e.getMessage(), e);
            throw e;
        }
    }
}
