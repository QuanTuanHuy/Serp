/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

package serp.project.pmcore.infrastructure.store.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "notification_scheme_entries")
@SQLRestriction("deleted_at IS NULL")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
public class NotificationSchemeEntryModel extends BaseModel {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "scheme_id", nullable = false)
    private Long schemeId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "recipient_type", nullable = false)
    private String recipientType;

    @Column(name = "recipient_ref")
    private String recipientRef;

    @Column(name = "custom_field_id")
    private Long customFieldId;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "template_id")
    private Long templateId;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "conditions_json")
    private String conditionsJson;
}
