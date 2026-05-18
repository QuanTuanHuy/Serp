package serp.project.tms_billing_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Entity
@Table(name = "provinces")
public class Province {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "province_code", nullable = false, unique = true, length = 2)
    private String provinceCode;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "code", nullable = false, length = 5)
    private String code;

    @Column(name = "place_type", nullable = false)
    private String placeType;

    @Column(name = "country_code", nullable = false, length = 10)
    private String countryCode;

    @OneToMany(mappedBy = "province", fetch = FetchType.LAZY)
    private List<Ward> wards = new ArrayList<>();

    @Column(name = "mien")
    private Long mien;
}

