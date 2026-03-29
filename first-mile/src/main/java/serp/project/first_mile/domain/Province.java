package serp.project.first_mile.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
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

}
