/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.second_mile.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import serp.project.second_mile.enums.HubStaffRole;
import serp.project.second_mile.enums.HubStaffStatus;

import java.time.LocalDate;

@Setter
@Getter
@SuperBuilder
@Entity
@Table(name = "hub_staffs")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@AllArgsConstructor
public class HubStaff extends AbstractAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "role")
    @Enumerated(EnumType.STRING)
    private HubStaffRole role;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private HubStaffStatus status;

    @Column(name = "hire_date")
    private LocalDate hireDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "note")
    private String note;
}
