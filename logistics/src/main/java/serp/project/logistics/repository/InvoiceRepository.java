package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics.entity.InvoiceEntity;

public interface InvoiceRepository extends JpaRepository<InvoiceEntity, String> {
}
