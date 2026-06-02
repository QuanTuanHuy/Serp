/*
Author: Nguyen The Anh
Description: Part of Serp Project
*/

package serp.project.last_mile.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
    @JoinColumn(name = "province_code", referencedColumnName = "province_code", nullable = false, insertable = false, updatable = false)
    private Province province;

}