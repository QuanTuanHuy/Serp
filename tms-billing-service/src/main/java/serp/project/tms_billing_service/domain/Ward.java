/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.tms_billing_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import serp.project.tms_billing_service.enums.LoaiTuyen;
import serp.project.tms_billing_service.enums.PhanLoai;

@Setter
@Getter
@Entity
@Table(name = "wards")
public class Ward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ward_code", nullable = false, unique = true, length = 6)
    private String wardCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "province_code", nullable = false, length = 2)
    private String provinceCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "province_code",
            referencedColumnName = "province_code",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Province province;

    @Column(name = "phan_loai")
    @Enumerated(EnumType.STRING)
    private PhanLoai phanLoai;

    @Column(name = "loai_tuyen")
    @Enumerated(EnumType.STRING)
    private LoaiTuyen loaiTuyen;
}

