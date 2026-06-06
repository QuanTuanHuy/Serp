package serp.project.tms_order.service;

import serp.project.tms_order.dto.response.ProductTypeTemplateDTO;
import serp.project.tms_order.dto.response.ProvinceExcelTemplateDTO;
import serp.project.tms_order.dto.response.WardExcelTemplateDTO;

import java.util.List;

public interface OrderExcelService {
    List<ProductTypeTemplateDTO> getProductTypeTemplate(Long tenantId);
    List<ProvinceExcelTemplateDTO>  getProvinceExcelTemplate();
    List<WardExcelTemplateDTO> getWardExcelTemplate();
}

