SET client_encoding = 'UTF8';

DROP TABLE IF EXISTS provinces CASCADE;

CREATE TABLE provinces (
                           id BIGSERIAL PRIMARY KEY,
                           province_code VARCHAR(2) NOT NULL,
                           name VARCHAR(255) NOT NULL,
                           short_name VARCHAR(255) NOT NULL,
                           code VARCHAR(5) NOT NULL,
                           place_type VARCHAR(255) NOT NULL,
                           country_code VARCHAR(10) NOT NULL
);

CREATE UNIQUE INDEX provinces_province_code_unique ON provinces (province_code);

CREATE UNIQUE INDEX provinces_country_code_unique ON provinces (country_code);


INSERT INTO provinces (id, province_code, name, short_name, code, place_type, country_code) VALUES
                                                                                                (1, '01', 'Thành phố Hà Nội', 'Thành phố Hà Nội', 'HNI', 'Thành phố Trung Ương', 'VN'),
                                                                                                (2, '04', 'Cao Bằng', 'Cao Bằng', 'CBG', 'Tỉnh', 'VN'),
                                                                                                (3, '08', 'Tuyên Quang', 'Tuyên Quang', 'TGQ', 'Tỉnh', 'VN'),
                                                                                                (4, '11', 'Điện Biên', 'Điện Biên', 'DBN', 'Tỉnh', 'VN'),
                                                                                                (5, '12', 'Lai Châu', 'Lai Châu', 'LCU', 'Tỉnh', 'VN'),
                                                                                                (6, '14', 'Sơn La', 'Sơn La', 'SLA', 'Tỉnh', 'VN'),
                                                                                                (7, '15', 'Lào Cai', 'Lào Cai', 'LCI', 'Tỉnh', 'VN'),
                                                                                                (8, '19', 'Thái Nguyên', 'Thái Nguyên', 'TNN', 'Tỉnh', 'VN'),
                                                                                                (9, '20', 'Lạng Sơn', 'Lạng Sơn', 'LSN', 'Tỉnh', 'VN'),
                                                                                                (10, '22', 'Quảng Ninh', 'Quảng Ninh', 'QNH', 'Tỉnh', 'VN'),
                                                                                                (11, '24', 'Bắc Ninh', 'Bắc Ninh', 'BNH', 'Tỉnh', 'VN'),
                                                                                                (12, '25', 'Phú Thọ', 'Phú Thọ', 'PTO', 'Tỉnh', 'VN'),
                                                                                                (13, '31', 'Thành phố Hải Phòng', 'Thành phố Hải Phòng', 'HPG', 'Thành phố Trung Ương', 'VN'),
                                                                                                (14, '33', 'Hưng Yên', 'Hưng Yên', 'HYN', 'Tỉnh', 'VN'),
                                                                                                (15, '37', 'Ninh Bình', 'Ninh Bình', 'NBH', 'Tỉnh', 'VN'),
                                                                                                (16, '38', 'Thanh Hóa', 'Thanh Hóa', 'THA', 'Tỉnh', 'VN'),
                                                                                                (17, '40', 'Nghệ An', 'Nghệ An', 'NAN', 'Tỉnh', 'VN'),
                                                                                                (18, '42', 'Hà Tĩnh', 'Hà Tĩnh', 'HTH', 'Tỉnh', 'VN'),
                                                                                                (19, '44', 'Quảng Trị', 'Quảng Trị', 'QTI', 'Tỉnh', 'VN'),
                                                                                                (20, '46', 'Thành phố Huế', 'Thành phố Huế', 'TTH', 'Thành phố Trung Ương', 'VN'),
                                                                                                (21, '48', 'Thành phố Đà Nẵng', 'Thành phố Đà Nẵng', 'DNG', 'Thành phố Trung Ương', 'VN'),
                                                                                                (22, '51', 'Quảng Ngãi', 'Quảng Ngãi', 'QNI', 'Tỉnh', 'VN'),
                                                                                                (23, '52', 'Gia Lai', 'Gia Lai', 'GLI', 'Tỉnh', 'VN'),
                                                                                                (24, '56', 'Khánh Hòa', 'Khánh Hòa', 'KHA', 'Tỉnh', 'VN'),
                                                                                                (25, '66', 'Đắk Lắk', 'Đắk Lắk', 'DLK', 'Tỉnh', 'VN'),
                                                                                                (26, '68', 'Lâm Đồng', 'Lâm Đồng', 'LDG', 'Tỉnh', 'VN'),
                                                                                                (27, '75', 'Đồng Nai', 'Đồng Nai', 'DNI', 'Tỉnh', 'VN'),
                                                                                                (28, '79', 'Thành phố Hồ Chí Minh', 'Thành phố Hồ Chí Minh', 'HCM', 'Thành phố Trung Ương', 'VN'),
                                                                                                (29, '80', 'Tây Ninh', 'Tây Ninh', 'TNH', 'Tỉnh', 'VN'),
                                                                                                (30, '82', 'Đồng Tháp', 'Đồng Tháp', 'DTP', 'Tỉnh', 'VN'),
                                                                                                (31, '86', 'Vĩnh Long', 'Vĩnh Long', 'VLG', 'Tỉnh', 'VN'),
                                                                                                (32, '91', 'An Giang', 'An Giang', 'AGG', 'Tỉnh', 'VN'),
                                                                                                (33, '92', 'Thành phố Cần Thơ', 'Thành phố Cần Thơ', 'CTO', 'Thành phố Trung Ương', 'VN'),
                                                                                                (34, '96', 'Cà Mau', 'Cà Mau', 'CMU', 'Tỉnh', 'VN');

SELECT setval('provinces_id_seq', (SELECT MAX(id) FROM provinces));
