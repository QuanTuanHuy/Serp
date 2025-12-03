package serp.project.logistics.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_invoice")
public class InvoiceEntity {

    @Id
    private String id;

    @CreationTimestamp
    @Column(name = "created_stamp")
    private String createdStamp;

    @Column(name = "from_supplier_id")
    private String fromSupplierId;

    @Column(name = "to_customer_id")
    private String toCustomerId;

    @Column(name = "invoice_type_id")
    private String invoiceTypeId;

    @Column(name = "invoice_name")
    private String invoiceName;

    @Column(name = "status_id")
    private String statusId;

    @UpdateTimestamp
    @Column(name = "last_updated_stamp")
    private LocalDateTime lastUpdatedStamp;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    private Long amount;

    @Column(name = "tenant_id")
    private Long tenantId;

}
