package serp.project.purchase_service.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
@Table(name = "wms2_invoice_item")
public class InvoiceItemEntity {

    @Id
    private String id;

    @Column(name = "invoice_id")
    private String invoiceId;

    @Column(name = "invoice_item_seq_id")
    private int invoiceItemSeqId;

    @Column(name = "invoice_item_name")
    private String invoiceItemName;

    @Column(name = "tenant_id")
    private Long tenantId;

}
