/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "product_types")
@EntityListeners(AuditingEntityListener.class)
public class ProductType extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "is_active")
    private Boolean isActive;
}
