package serp.project.logistics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import serp.project.logistics.entity.InvoiceItemEntity;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItemEntity, String> {



}
