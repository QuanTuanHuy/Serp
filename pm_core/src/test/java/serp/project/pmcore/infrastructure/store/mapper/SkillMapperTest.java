/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.mapper;

import org.junit.jupiter.api.Test;
import serp.project.pmcore.domain.skill.entity.SkillEntity;
import serp.project.pmcore.domain.skill.entity.UserSkillEntity;
import serp.project.pmcore.domain.skill.entity.WorkItemSkillEntity;
import serp.project.pmcore.domain.skill.enums.SkillProficiency;
import serp.project.pmcore.domain.skill.enums.SkillRequirementType;
import serp.project.pmcore.domain.skill.enums.SkillSource;
import serp.project.pmcore.infrastructure.store.model.SkillModel;
import serp.project.pmcore.infrastructure.store.model.UserSkillModel;
import serp.project.pmcore.infrastructure.store.model.WorkItemSkillModel;

import static org.assertj.core.api.Assertions.assertThat;

class SkillMapperTest {

    @Test
    void skillMapperShouldMapCatalogFields() {
        SkillMapper mapper = new SkillMapper();
        SkillEntity entity = SkillEntity.builder()
                .id(1L)
                .tenantId(2L)
                .code("backend-java")
                .name("Backend Java")
                .description("Java service development")
                .active(true)
                .deletedAt(1715523600000L)
                .build();

        SkillModel model = mapper.toModel(entity);
        SkillEntity mapped = mapper.toEntity(model);

        assertThat(mapped.getTenantId()).isEqualTo(2L);
        assertThat(mapped.getCode()).isEqualTo("backend-java");
        assertThat(mapped.getName()).isEqualTo("Backend Java");
        assertThat(mapped.getActive()).isTrue();
        assertThat(mapped.getDeletedAt()).isEqualTo(1715523600000L);
    }

    @Test
    void workItemSkillMapperShouldMapRequirementFields() {
        WorkItemSkillMapper mapper = new WorkItemSkillMapper();
        WorkItemSkillEntity entity = WorkItemSkillEntity.builder()
                .id(10L)
                .tenantId(2L)
                .projectId(3L)
                .workItemId(4L)
                .skillId(5L)
                .requirementType(SkillRequirementType.REQUIRED)
                .minProficiency(SkillProficiency.PROFICIENT)
                .weight(3)
                .source(SkillSource.MANUAL)
                .build();

        WorkItemSkillModel model = mapper.toModel(entity);
        WorkItemSkillEntity mapped = mapper.toEntity(model);

        assertThat(mapped.getWorkItemId()).isEqualTo(4L);
        assertThat(mapped.getSkillId()).isEqualTo(5L);
        assertThat(mapped.getRequirementType()).isEqualTo(SkillRequirementType.REQUIRED);
        assertThat(mapped.getMinProficiency()).isEqualTo(SkillProficiency.PROFICIENT);
        assertThat(mapped.getWeight()).isEqualTo(3);
        assertThat(mapped.getSource()).isEqualTo(SkillSource.MANUAL);
    }

    @Test
    void userSkillMapperShouldMapProfileFields() {
        UserSkillMapper mapper = new UserSkillMapper();
        UserSkillEntity entity = UserSkillEntity.builder()
                .id(20L)
                .tenantId(2L)
                .userId(6L)
                .skillId(5L)
                .proficiency(SkillProficiency.EXPERT)
                .confidence(90)
                .source(SkillSource.IMPORT)
                .verifiedAt(1715523600000L)
                .build();

        UserSkillModel model = mapper.toModel(entity);
        UserSkillEntity mapped = mapper.toEntity(model);

        assertThat(mapped.getUserId()).isEqualTo(6L);
        assertThat(mapped.getSkillId()).isEqualTo(5L);
        assertThat(mapped.getProficiency()).isEqualTo(SkillProficiency.EXPERT);
        assertThat(mapped.getConfidence()).isEqualTo(90);
        assertThat(mapped.getSource()).isEqualTo(SkillSource.IMPORT);
        assertThat(mapped.getVerifiedAt()).isEqualTo(1715523600000L);
    }
}
