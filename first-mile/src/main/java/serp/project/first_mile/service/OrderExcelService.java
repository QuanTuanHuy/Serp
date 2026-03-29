package serp.project.first_mile.service;

import serp.project.first_mile.dto.response.ProductTypeTemplateDTO;
import serp.project.first_mile.dto.response.ProvinceExcelTemplateDTO;
import serp.project.first_mile.dto.response.WardExcelTemplateDTO;

import java.util.List;

public interface OrderExcelService {
    List<ProductTypeTemplateDTO> getProductTypeTemplate(Long tenantId);
    List<ProvinceExcelTemplateDTO>  getProvinceExcelTemplate();
    List<WardExcelTemplateDTO> getWardExcelTemplate();
}
