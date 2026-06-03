/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_order.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import serp.project.tms_order.dto.response.ProductTypeTemplateDTO;
import serp.project.tms_order.dto.response.ProvinceExcelTemplateDTO;
import serp.project.tms_order.dto.response.WardExcelTemplateDTO;
import serp.project.tms_order.repository.ProductTypeRepository;
import serp.project.tms_order.repository.ProvinceRepository;
import serp.project.tms_order.repository.WardRepository;
import serp.project.tms_order.service.OrderExcelService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderExcelServiceImpl implements OrderExcelService {

    private final ProductTypeRepository productTypeRepository;
    private final ProvinceRepository provinceRepository;
    private final WardRepository wardRepository;

    @Override
    public List<ProductTypeTemplateDTO> getProductTypeTemplate(Long tenantId) {
        return productTypeRepository.findTemplateCodeNameList(tenantId)
                .stream()
                .map(item -> new ProductTypeTemplateDTO(item.getName(), item.getCode()))
                .toList();
    }

    @Override
    public List<ProvinceExcelTemplateDTO> getProvinceExcelTemplate() {
        return provinceRepository.findTemplateCodeNameList()
                .stream()
                .map(item -> new ProvinceExcelTemplateDTO(item.getName(), item.getCode()))
                .toList();
    }

    @Override
    public List<WardExcelTemplateDTO> getWardExcelTemplate() {
        return wardRepository.findTemplateCodeNameList()
                .stream()
                .map(item -> new WardExcelTemplateDTO(item.getCode(), item.getName()))
                .toList();
    }
}
