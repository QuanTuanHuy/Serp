/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.crm.core.domain.entity;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.crm.core.domain.enums.ActiveStatus;
import serp.project.crm.core.domain.enums.CustomerType;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class CustomerEntity extends BaseEntity {
    private String customerCode;
    private String name;
    private String email;
    private String phone;
    private String website;
    private String industry;
    private String companySize;
    private AddressEntity billingAddress;
    private AddressEntity shippingAddress;
    private CustomerType customerType;
    private BigDecimal creditLimit;
    private String paymentTerms;
    private String taxId;
    private ActiveStatus activeStatus;
}
