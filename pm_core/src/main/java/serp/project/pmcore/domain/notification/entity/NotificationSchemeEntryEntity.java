/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.domain.notification.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import serp.project.pmcore.domain.shared.entity.BaseEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class NotificationSchemeEntryEntity extends BaseEntity {
    private Long tenantId;
    private Long schemeId;
    private Long eventId;
    private String recipientType;
    private String recipientRef;
    private Long customFieldId;
    private String channel;
    private Long templateId;
    private Boolean isEnabled;
    private String conditionsJson;
}
