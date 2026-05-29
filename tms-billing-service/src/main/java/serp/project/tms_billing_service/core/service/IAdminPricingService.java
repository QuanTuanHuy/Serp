/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.core.service;

import serp.project.tms_billing_service.dto.request.admin.UpsertSurchargeRuleRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertTariffRequest;
import serp.project.tms_billing_service.dto.request.admin.UpsertVasRuleRequest;
import serp.project.tms_billing_service.dto.response.admin.SurchargeRuleAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.TariffAdminResponse;
import serp.project.tms_billing_service.dto.response.admin.VasRuleAdminResponse;
import serp.project.tms_billing_service.enums.DeliveryService;

import java.util.List;

public interface IAdminPricingService {
    TariffAdminResponse upsertTariff(UpsertTariffRequest request);

    SurchargeRuleAdminResponse upsertSurchargeRule(UpsertSurchargeRuleRequest request);

    VasRuleAdminResponse upsertVasRule(UpsertVasRuleRequest request);

    List<TariffAdminResponse> listTariffs(DeliveryService serviceCode);

    List<SurchargeRuleAdminResponse> listSurchargeRules();

    List<VasRuleAdminResponse> listVasRules();
}
