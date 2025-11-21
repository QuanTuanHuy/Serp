package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.purchase_service.entity.InvoiceItemEntity;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItemEntity, String> {



}
