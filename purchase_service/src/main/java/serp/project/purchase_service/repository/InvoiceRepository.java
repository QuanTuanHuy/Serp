package serp.project.purchase_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.purchase_service.entity.InvoiceEntity;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, String> {
}
