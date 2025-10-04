/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.crm.core.domain.dto.GeneralResponse;
import serp.project.crm.core.domain.dto.PageRequest;
import serp.project.crm.core.domain.dto.PageResponse;
import serp.project.crm.core.domain.dto.request.CreateContactRequest;
import serp.project.crm.core.domain.dto.request.UpdateContactRequest;
import serp.project.crm.core.domain.dto.response.ContactResponse;
import serp.project.crm.core.domain.entity.ContactEntity;
import serp.project.crm.core.mapper.ContactDtoMapper;
import serp.project.crm.core.service.IContactService;
import serp.project.crm.kernel.utils.ResponseUtils;

import java.util.List;

/**
 * Contact Use Case - Orchestrates contact management workflows
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ContactUseCase {

    private final IContactService contactService;
    private final ContactDtoMapper contactDtoMapper;
    private final ResponseUtils responseUtils;

    @Transactional
    public GeneralResponse<?> createContact(CreateContactRequest request, Long tenantId) {
        try {
            log.info("Creating contact: {}, tenant: {}", request.getName(), tenantId);

            ContactEntity contactEntity = contactDtoMapper.toEntity(request);
            ContactEntity createdContact = contactService.createContact(contactEntity, tenantId);
            ContactResponse response = contactDtoMapper.toResponse(createdContact);

            log.info("Contact created successfully with ID: {}", createdContact.getId());
            return responseUtils.success(response, "Contact created successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error creating contact: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating contact: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to create contact");
        }
    }

    @Transactional
    public GeneralResponse<?> updateContact(Long id, UpdateContactRequest request, Long tenantId) {
        try {
            log.info("Updating contact ID: {}, tenant: {}", id, tenantId);

            ContactEntity updates = contactDtoMapper.toEntity(request);
            ContactEntity updatedContact = contactService.updateContact(id, updates, tenantId);
            ContactResponse response = contactDtoMapper.toResponse(updatedContact);

            log.info("Contact updated successfully: {}", id);
            return responseUtils.success(response, "Contact updated successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error updating contact: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error updating contact: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to update contact");
        }
    }

    @Transactional
    public GeneralResponse<?> setPrimaryContact(Long contactId, Long tenantId) {
        try {
            log.info("Setting primary contact ID: {}, tenant: {}", contactId, tenantId);

            ContactEntity contact = contactService.setPrimaryContact(contactId, tenantId);
            ContactResponse response = contactDtoMapper.toResponse(contact);

            log.info("Primary contact set successfully: {}", contactId);
            return responseUtils.success(response, "Primary contact set successfully");

        } catch (IllegalArgumentException e) {
            log.error("Error setting primary contact: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error setting primary contact: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to set primary contact");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getContactById(Long id, Long tenantId) {
        try {
            log.info("Fetching contact ID: {}, tenant: {}", id, tenantId);

            ContactEntity contact = contactService.getContactById(id, tenantId)
                    .orElse(null);

            if (contact == null) {
                return responseUtils.notFound("Contact not found");
            }

            ContactResponse response = contactDtoMapper.toResponse(contact);
            return responseUtils.success(response);

        } catch (Exception e) {
            log.error("Error fetching contact: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch contact");
        }
    }

    @Transactional(readOnly = true)
    public GeneralResponse<?> getAllContacts(Long tenantId, PageRequest pageRequest) {
        try {
            log.info("Fetching all contacts for tenant: {}", tenantId);

            var result = contactService.getAllContacts(tenantId, pageRequest);

            List<ContactResponse> contactResponses = result.getFirst().stream()
                    .map(contactDtoMapper::toResponse)
                    .toList();

            PageResponse<ContactResponse> pageResponse = PageResponse.of(
                    contactResponses, pageRequest, result.getSecond());

            return responseUtils.success(pageResponse);

        } catch (Exception e) {
            log.error("Error fetching contacts: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to fetch contacts");
        }
    }

    @Transactional
    public GeneralResponse<?> deleteContact(Long id, Long tenantId) {
        try {
            log.info("Deleting contact ID: {}, tenant: {}", id, tenantId);

            contactService.deleteContact(id, tenantId);

            log.info("Contact deleted successfully: {}", id);
            return responseUtils.status("Contact deleted successfully");

        } catch (IllegalArgumentException e) {
            log.error("Validation error deleting contact: {}", e.getMessage());
            return responseUtils.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error deleting contact: {}", e.getMessage(), e);
            return responseUtils.internalServerError("Failed to delete contact");
        }
    }
}
