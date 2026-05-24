-- Tự động sinh mã UPDATE phân loại và loại tuyến địa danh từ file MD
-- Cấu trúc đã được gỡ bỏ tiền tố (Xã, Phường...) để tối ưu map dữ liệu (LIKE).

UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Bình Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Châu Đốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Chi Lăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Long Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Long Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Mỹ Thới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tân Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Thới Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tịnh Biên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Tế%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%An Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%An Cư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%An Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Ba Chúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Bình Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Bình Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Bình Thạnh Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Cần Đăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Châu Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Châu Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Chợ Mới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Chợ Vàm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Cô Tô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Cù Lao Giêng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Định Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hòa Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hội An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Khánh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Long Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Long Kiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Mỹ Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Mỹ Hòa Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Nhơn Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Nhơn Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Núi Cấm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Ô Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Óc Eo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Phú An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Phú Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Phú Hữu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Phú Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Phú Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tây Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Thạnh Mỹ Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Thoại Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tri Tôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Hanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Hậu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Thạnh Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Xương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Kiên Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Phú Quốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Thổ Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hà Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Rạch Giá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tô Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Thông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%An Biên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%An Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Bình An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Bình Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Bình Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Châu Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Định Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Đông Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Đông Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Đông Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Giang Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Giồng Riềng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Gò Quao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hòa Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hòa Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hòa Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hòn Đất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Hòn Nghệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Kiên Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Long Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Mỹ Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Ngọc Chúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Sơn Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Sơn Kiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tân Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tân Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tân Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tây Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Thạnh Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Thạnh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Thạnh Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Tiên Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%U Minh Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vân Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Điều%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Hòa Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%An Giang%'
  AND w.name LIKE '%Vĩnh Tuy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Bắc Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Cảnh Thụy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Chũ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đa Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Nếnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Phượng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tiền Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tự Lạn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Vân Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Việt Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Yên Dũng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%An Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Bắc Lũng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Bảo Đài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Biển Động%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Biên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Bố Hạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Cẩm Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đại Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đèo Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đồng Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đông Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đồng Việt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Dương Hưu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Hiệp Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Hoàng Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Hợp Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Kép%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Kiên Lao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Lạng Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Lục Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Lục Ngạn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Lục Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Mỹ Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Nam Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Nghĩa Phương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Ngọc Thiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Nhã Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Phúc Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Quang Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Sa Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Sơn Động%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Sơn Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tam Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tân Dĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tân Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tây Yên Tử%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tiên Lục%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Trường Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tuấn Đạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Vân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Xuân Cẩm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Xuân Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Yên Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Yên Thế%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Bồng Lai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đào Viên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đồng Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Hạp Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Kinh Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Mão Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Nam Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Nhân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Ninh Xá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Phù Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Phương Liễu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Quế Võ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Song Liễu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tam Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Thuận Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Trạm Lộ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Trí Quả%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Từ Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Võ Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Vũ Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Cao Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Chi Lăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đại Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đại Lai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Đông Cứu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Gia Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Lâm Thao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Liên Bão%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Lương Tài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Nhân Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Phật Tích%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Phù Lãng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tam Đa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tam Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tân Chi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Tiên Du%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Trung Chính%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Trung Kênh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Văn Môn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Yên Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Bắc Ninh%'
  AND w.name LIKE '%Yên Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Bạc Liêu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Giá Rai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Hiệp Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Láng Tròn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Vĩnh Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%An Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Châu Thới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Định Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Đông Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Gành Hào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Hòa Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Hồng Dân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Hưng Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Long Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Ninh Quới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Ninh Thạnh Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Phong Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Phong Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Phước Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Vĩnh Hậu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Vĩnh Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Vĩnh Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Vĩnh Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Vĩnh Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Vĩnh Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%An Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Hòa Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Lý Văn Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Biển Bạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Cái Đôi Vàm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Cái Nước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Đá Bạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Đầm Dơi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Đất Mới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Đất Mũi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Hồ Thị Kỷ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Hưng Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Khánh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Khánh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Khánh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Khánh Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Lương Thế Trân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Năm Căn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Nguyễn Phích%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Nguyễn Việt Khái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Phan Ngọc Hiển%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Phú Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Phú Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Quách Phẩm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Sông Đốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tạ An Khương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tam Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tân Ân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tân Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tân Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tân Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Thanh Tùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Thới Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Trần Phán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Trần Văn Thời%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%Trí Phải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cà Mau%'
  AND w.name LIKE '%U Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%An Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Bình Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Cái Khế%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Cái Răng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Hưng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Long Tuyền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Ninh Kiều%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Ô Môn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phước Thới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tân Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thới An Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thới Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thốt Nốt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thuận Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trung Nhứt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Cờ Đỏ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Đông Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Đông Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Nhơn Ái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phong Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thạnh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thạnh Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thạnh Quới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thới Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thới Lai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trung Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trường Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trường Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trường Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Trinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Đại Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Long Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Long Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Long Phú 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Ngã Bảy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vị Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vị Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Châu Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Đông Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Hiệp Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Hòa An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Hỏa Lựu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Lương Tâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phú Hữu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phụng Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phương Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tân Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tân Phước Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thạnh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thạnh Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trường Long Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vị Thanh 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vị Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Thuận Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Viễn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Xà Phiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Khánh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Mỹ Quới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Mỹ Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Ngã Năm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phú Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Sóc Trăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%An Lạc Thôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%An Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%An Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Cù Lao Dung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Đại Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Đại Ngãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Gia Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Hồ Đắc Kiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Hòa Tú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Kế Sách%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Lai Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Lâm Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Lịch Hội Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Liêu Tú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Long Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Long Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Mỹ Hương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Mỹ Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Mỹ Tú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Ngọc Tố%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Nhơn Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Nhu Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phong Nẫm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phú Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Phú Tâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tài Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tân Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Tân Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thạnh Thới An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thới An Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Thuận Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trần Đề%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Trường Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cần Thơ%'
  AND w.name LIKE '%Vĩnh Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Nùng Trí Cao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Tân Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Thục Phán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Bạch Đằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Bảo Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Bảo Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Bế Văn Đàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Ca Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Cần Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Canh Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Cô Ba%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Cốc Pàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Đàm Thuỷ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Đình Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Đoài Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Độc Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Đông Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Đức Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Hạ Lang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Hà Quảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Hạnh Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Hòa An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Hưng Đạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Huy Giáp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Khánh Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Kim Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Lũng Nặm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Lý Bôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Lý Quốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Minh Khai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Minh Tâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Nam Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Nam Tuấn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Nguyên Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Nguyễn Huệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Phan Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Phục Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Quang Hán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Quảng Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Quang Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Quang Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Quảng Uyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Sơn Lộ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Tam Kim%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Thạch An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Thành Công%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Thanh Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Thông Nông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Tĩnh Túc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Tổng Cọt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Trà Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Trùng Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Trường Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Vinh Quý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Xuân Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Cao Bằng%'
  AND w.name LIKE '%Yên Thổ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hoàng Sa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%An Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%An Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Cẩm Lệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hải Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hải Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hòa Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hòa Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hòa Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Liên Chiểu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Ngũ Hành Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Sơn Trà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thanh Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Bà Nà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hòa Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hòa Vang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%An Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Bàn Thạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Điện Bàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Điện Bàn Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Điện Bàn Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hội An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hội An Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hội An Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hương Trà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Quảng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tam Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Avương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Bến Giằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Bến Hiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Chiên Đàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Đắc Pring%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Đại Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Điện Bàn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Đồng Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Đông Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Đức Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Duy Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Duy Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Gò Nổi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hà Nha%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hiệp Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Hùng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Khâm Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%La Dêê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%La Êê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Lãnh Ngọc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Nam Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Nam Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Nam Trà My%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Nông Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Núi Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Phú Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Phú Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Phước Chánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Phước Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Phước Năng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Phước Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Phước Trà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Quế Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Quế Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Quế Sơn Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Sơn Cẩm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Sông Kôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Sông Vàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tam Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tam Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tam Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tam Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tân Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tây Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tây Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thăng An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thăng Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thăng Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thăng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thăng Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thạnh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thạnh Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thu Bồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Thượng Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Tiên Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Đốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Giáp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Leng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Liên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà My%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Tập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Trà Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Việt An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Vu Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đà Nẵng%'
  AND w.name LIKE '%Xuân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Buôn Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Buôn Ma Thuột%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cư Bao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Kao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tân Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Thành Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Buôn Đôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cư M’gar%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cư M’ta%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cư Pơng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cư Prao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cư Pui%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cư Yang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Cuôr Đăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Đắk Liêng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Đắk Phơi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Dang Kang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Dliê Ya%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Dray Bhăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Dur Kmăl%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Bung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Drăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Drông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea H’leo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Hiao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Kar%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Khăl%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Kiết%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Kly%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Knốp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Knuếc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Ktur%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea M’Droh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Na%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Ning%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Nuôl%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Ô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Păl%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Phê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Riêng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Rốk%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Súp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Trang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Tul%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Wer%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Wy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Hòa Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Hòa Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ia Lốp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ia Rvê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Krông Á%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Krông Ana%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Krông Bông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Krông Búk%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Krông Năng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Krông Nô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Krông Pắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Liên Sơn Lắk%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%M’Drắk%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Nam Ka%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Phú Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Pơng Drang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Quảng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tam Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Vụ Bổn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Yang Mao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Bình Kiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Đông Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Hòa Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Phú Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Sông Cầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tuy Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Xuân Đài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Đồng Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Đức Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Bá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ea Ly%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Hòa Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Hòa Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Hòa Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Ô Loan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Phú Hòa 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Phú Hòa 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Phú Mỡ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Sơn Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Sơn Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Sông Hinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Suối Trai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tây Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tây Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tuy An Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tuy An Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tuy An Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Tuy An Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Vân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Xuân Cảnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Xuân Lãnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Xuân Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Xuân Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đắk Lắk%'
  AND w.name LIKE '%Xuân Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Điện Biên Phủ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Lay%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Búng Lao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Chà Tở%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Chiềng Sinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Ảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Chà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Lạn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Luân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Mùn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Nhà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Nhé%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Phăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Pồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Toong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Mường Tùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Nà Bủng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Nà Hỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Na Sang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Na Son%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Nà Tấu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Nậm Kè%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Nậm Nèn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Núa Ngam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Pa Ham%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Phình Giàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Pu Nhi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Pú Nhung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Quài Tở%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Quảng Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Sam Mứn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Sáng Nhè%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Si Pa Phìn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Sín Chải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Sín Thầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Sính Phình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Thanh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Thanh Nưa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Thanh Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Tìa Dình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Tủa Chùa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Tủa Thàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Tuần Giáo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Điện Biên%'
  AND w.name LIKE '%Xa Dung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%An Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bình Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bình Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Chơn Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đồng Xoài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Minh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phước Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phước Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bình Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bom Bo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bù Đăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bù Gia Mập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đa Kia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đak Nhau%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đăk Ơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đồng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đồng Tâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Hưng Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Lộc Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Lộc Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Lộc Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Lộc Tấn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Lộc Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Lộc Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Long Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Minh Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Nghĩa Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Nha Bích%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phú Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phú Riềng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phú Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phước Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân Khai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Thiện Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Thọ Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Thuận Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bảo Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Biên Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bình Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Hàng Gòn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Hố Nai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Long Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Long Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Long Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phước Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tam Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tam Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân Triều%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Trấn Biên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Trảng Dài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%An Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%An Viễn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bàu Hàm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bình An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Bình Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Cẩm Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đại Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Đak Lua%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Dầu Giây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Định Quán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Gia Kiệm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Hưng Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%La Ngà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Long Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Long Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Nam Cát Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Nhơn Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phú Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phú Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phú Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phú Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phước An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Phước Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Sông Ray%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tà Lài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Tân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Thanh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Thống Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Trảng Bom%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Trị An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Đường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Quế%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Nai%'
  AND w.name LIKE '%Xuân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%An Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Cao Lãnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Hồng Ngự%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Ngãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Trà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Sa Đéc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thường Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%An Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%An Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%An Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Ba Sao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Bình Hàng Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Bình Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Đốc Binh Kiều%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Hòa Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Lai Vung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Lấp Vò%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Long Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Long Phú Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ An Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Quí%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Phong Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Phong Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Phú Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Phú Hựu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Phú Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Phương Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tam Nông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Hộ Cơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Khánh Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Nhuận Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Phú Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thanh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thanh Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tháp Mười%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thường Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tràm Chim%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Trường Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Bình Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Cai Lậy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Đạo Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Gò Công%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Long Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Phước Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Tho%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Nhị Quý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Sơn Qui%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thanh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thới Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Trung An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%An Hữu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%An Thạnh Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Bình Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Bình Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Bình Trưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Cái Bè%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Châu Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Chợ Gạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Đồng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Gia Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Gò Công Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Hậu Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Hiệp Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Hội Cư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Hưng Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Kim Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Long Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Long Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Long Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Long Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Lương Hòa Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Đức Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Thiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Mỹ Tịnh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Ngũ Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Phú Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Hương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Phú Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Phước 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Phước 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Phước 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Thới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Tân Thuận Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thanh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Thạnh Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Vĩnh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Vĩnh Hựu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Đồng Tháp%'
  AND w.name LIKE '%Vĩnh Kim%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Nhơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Nhơn Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Nhơn Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Nhơn Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bình Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bồng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hoài Nhơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hoài Nhơn Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hoài Nhơn Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hoài Nhơn Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hoài Nhơn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Quy Nhơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Quy Nhơn Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Quy Nhơn Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Quy Nhơn Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Quy Nhơn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Tam Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ân Hảo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Lão%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Nhơn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Toàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ân Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bình An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bình Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bình Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bình Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bình Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Canh Liên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Canh Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Cát Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Đề Gi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hòa Hội%';
UPDATE public.wards w

SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hoài Ân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hội Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Kim Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ngô Mây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Nhơn Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phù Cát%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phù Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phù Mỹ Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phù Mỹ Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phù Mỹ Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phù Mỹ Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Tây Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Tuy Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Tuy Phước Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Tuy Phước Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Tuy Phước Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Vân Canh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Vạn Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Vĩnh Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Vĩnh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Vĩnh Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Vĩnh Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Xuân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%An Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ayun Pa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Diên Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hội Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Pleiku%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Thống Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Albá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ayun%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bàu Cạn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Biển Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Bờ Ngoong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Chơ Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Chư A Thai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Chư Krey%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Chư Păh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Chư Prông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Chư Pưh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Chư Sê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Cửu An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Đak Đoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Đak Pơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Đak Rong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Đak Sơmei%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Đăk Song%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Đức Cơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Gào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Hra%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Băng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Boòng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Chia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Dơk%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Dom%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Dreh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Grai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Hiao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Hrú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Hrung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Khươl%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Ko%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Krái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Krêl%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Lâu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Le%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Ly%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Mơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Nan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia O%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Pa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Phí%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Pia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Pnôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Púch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Rbol%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Rsai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Sao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Tôr%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ia Tul%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Kbang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Kdang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Kon Chiêng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Kon Gang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Kông Bơ La%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Kông Chro%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Krong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Lơ Pang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Mang Yang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phú Thiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Phú Túc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Pờ Tó%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Sơn Lang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Sró%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Tơ Tung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Uar%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ya Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Gia Lai%'
  AND w.name LIKE '%Ya Ma%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ba Đình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Bạch Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Bồ Đề%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Cầu Giấy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Chương Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Cửa Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đại Mỗ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Định Công%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đống Đa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đông Ngạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Dương Nội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Giảng Võ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hà Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hai Bà Trưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hoàn Kiếm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hoàng Liệt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hoàng Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hồng Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Khương Đình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Kiến Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Kim Liên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Láng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Lĩnh Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Long Biên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Nghĩa Đô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ngọc Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ô Chợ Dừa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phú Diễn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phú Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phú Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phúc Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phương Liệt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Sơn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tây Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tây Mỗ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tây Tựu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thanh Liệt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thanh Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thượng Cát%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Từ Liêm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tùng Thiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tương Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Văn Miếu \- Quốc Tử Giám%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Việt Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Vĩnh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Vĩnh Tuy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Xuân Đỉnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Xuân Phương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Yên Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Yên Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Yên Sở%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%An Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ba Vì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Bất Bạt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Bát Tràng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Bình Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Chương Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Chuyên Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Cổ Đô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đa Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đại Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đại Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Dân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đan Phượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đoài Phương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Đông Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Dương Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Gia Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hạ Bằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hát Môn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hòa Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hòa Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hòa Xá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hoài Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hồng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hồng Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hưng Đạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Hương Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Kiều Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Kim Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Liên Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Mê Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Minh Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Mỹ Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Nam Phù%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ngọc Hồi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Nội Bài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ô Diên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phú Cát%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phù Đổng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phú Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phú Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phúc Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phúc Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phúc Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phúc Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Phượng Dực%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Quảng Bị%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Quang Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Quảng Oai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Quốc Oai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Sóc Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Sơn Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Suối Hai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tam Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tây Phương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thạch Thất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thanh Oai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thanh Trì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thiên Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thư Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thuận An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thượng Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Thường Tín%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Tiến Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Trần Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Trung Giã%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ứng Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Ứng Thiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Vân Đình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Vật Lại%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Vĩnh Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Xuân Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Yên Bài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Yên Lãng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Nội%'
  AND w.name LIKE '%Yên Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Bắc Hồng Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hà Huy Tập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hải Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hoành Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Nam Hồng Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Sông Trí%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Thành Sen%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Trần Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Vũng Áng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Cẩm Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Cẩm Duệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Cẩm Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Cẩm Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Cẩm Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Cẩm Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Can Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Cổ Đạm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đan Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đông Kinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đồng Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đồng Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đức Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đức Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đức Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đức Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Đức Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Gia Hanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hà Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hồng Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hương Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hương Đô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hương Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hương Phố%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hương Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Hương Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kim Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kỳ Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kỳ Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kỳ Khang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kỳ Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kỳ Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kỳ Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Kỳ Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Lộc Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Mai Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Mai Phụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Nghi Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Phúc Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Sơn Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Sơn Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Sơn Kim 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Sơn Kim 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Sơn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Sơn Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Thạch Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Thạch Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Thạch Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Thạch Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Thiên Cầm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Thượng Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Tiên Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Toàn Lưu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Trường Lưu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Tứ Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Tùng Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Việt Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Vũ Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Xuân Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hà Tĩnh%'
  AND w.name LIKE '%Yên Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Ái Quốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Bắc An Phụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Chí Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Chu Văn An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hải Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kinh Môn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Lê Đại Hành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Lê Thanh Nghị%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nam Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nguyễn Đại Năng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nguyễn Trãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nhị Chiểu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Phạm Sư Mạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tân Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thạch Khôi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thành Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Trần Hưng Đạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Trần Liễu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Trần Nhân Tông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tứ Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Việt Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Bắc Thanh Miện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Bình Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Cẩm Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Cẩm Giàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Chí Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Đại Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Đường An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Gia Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Gia Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hà Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hà Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hà Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hà Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hải Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hồng Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hợp Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kẻ Sặt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Khúc Thừa Dụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kim Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Lạc Phượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Lai Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Mao Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nam An Phụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nam Sách%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nam Thanh Miện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nguyên Giáp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nguyễn Lương Bằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Ninh Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Phú Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tân Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thái Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thanh Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thanh Miện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thượng Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Trần Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Trường Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tứ Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tuệ Tĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Vĩnh Lại%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Yết Kiêu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Dừng phục vụ'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Bạch Long Vĩ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Cát Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Biên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Bạch Đằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Đồ Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Đông Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Dương Kinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Gia Viên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hải An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hoà Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hồng An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hồng Bàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hưng Đạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kiến An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Lê Chân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Lê Ích Mộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Lưu Kiếm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nam Đồ Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nam Triệu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Ngô Quyền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Phù Liễn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thiên Hương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Thuỷ Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Lão%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%An Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Chấn Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Hùng Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kiến Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kiến Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kiến Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Kiến Thụy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nghi Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Nguyễn Bỉnh Khiêm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Quyết Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tân Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tiên Lãng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Tiên Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Việt Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Vĩnh Am%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Vĩnh Bảo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Vĩnh Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Vĩnh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Vĩnh Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hải Phòng%'
  AND w.name LIKE '%Vĩnh Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bến Cát%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Cơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Chánh Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Chánh Phú Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Dĩ An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Đông Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hòa Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Lái Thiêu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Đông Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Uyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tây Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thới Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thủ Dầu Một%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thuận An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thuận Giao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Vĩnh Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bắc Tân Uyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bàu Bàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Dầu Tiếng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Minh Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Giáo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phước Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phước Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thanh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thường Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Trừ Văn Thố%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Hội Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Hội Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Nhơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Phú Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bàn Cờ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bảy Hiền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bến Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Hưng Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Lợi Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Quới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Thới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Trị Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Trưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Cát Lái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Cầu Kiệu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Cầu Ông Lãnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Chánh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Chợ Lớn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Chợ Quán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Diên Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Đông Hưng Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Đức Nhuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Gia Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Gò Vấp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hạnh Thông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hiệp Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hòa Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hòa Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Khánh Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Linh Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Minh Phụng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Nhiêu Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Nhuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Thọ Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phước Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Sài Gòn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tam Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Sơn Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Sơn Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Sơn Nhì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Tạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Thới Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tăng Nhơn Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tây Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thạnh Mỹ Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thới An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thông Tây Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thủ Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Trung Mỹ Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Vĩnh Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Vườn Lài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Xóm Chiếu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Xuân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Nhơn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%An Thới Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bà Điểm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Chánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Cần Giờ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Củ Chi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Đông Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hiệp Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hóc Môn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hưng Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Nhà Bè%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Nhuận Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Hòa Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân An Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Nhựt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Vĩnh Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thái Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Thạnh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Vĩnh Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Xuân Thới Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Côn Đảo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bà Rịa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Hương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phú Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phước Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Rạch Dừa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tam Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tam Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Vũng Tàu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bàu Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Bình Giã%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Châu Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Châu Pha%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Đất Đỏ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hồ Tràm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hòa Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Hoà Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Kim Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Long Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Ngãi Giao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Nghĩa Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Phước Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Xuân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hồ Chí Minh%'
  AND w.name LIKE '%Xuyên Mộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đường Hào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hồng Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Mỹ Hào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Phố Hiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Sơn Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thượng Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Ân Thi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Châu Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Chí Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đại Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đoàn Đào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đức Hợp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hiệp Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hoàn Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hoàng Hoa Thám%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hồng Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Khoái Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Lạc Đạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Lương Bằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Mễ Sở%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nghĩa Dân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nghĩa Trụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nguyễn Trãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nguyễn Văn Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Như Quỳnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Phạm Ngũ Lão%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Phụng Công%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Quang Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tân Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tiên Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tiên Lữ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tiên Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tống Trân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Triệu Việt Vương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Văn Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Việt Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Việt Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Xuân Trúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Yên Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thái Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Trà Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Trần Hưng Đạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Trần Lãm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Vũ Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%A Sào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Ái Quốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bắc Đông Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bắc Đông Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bắc Thái Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bắc Thụy Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bắc Tiên Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bình Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bình Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Bình Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Diên Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đồng Bằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đồng Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đông Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đông Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đông Thái Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đông Thụy Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đông Tiền Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Đông Tiên Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hồng Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hồng Vũ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hưng Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Hưng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Kiến Xương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Lê Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Lê Quý Đôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Long Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Minh Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nam Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nam Đông Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nam Thái Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nam Thụy Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nam Tiền Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nam Tiên Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Ngọc Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Ngự Thiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Nguyễn Du%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Phụ Dực%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Quang Lịch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Quỳnh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Quỳnh Phụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tân Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tây Thái Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tây Thụy Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tây Tiền Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thái Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thái Thụy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thần Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thư Trì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thư Vũ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Thụy Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tiền Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tiên Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Tiên La%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Trà Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Vạn Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Vũ Quý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Vũ Thư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Hưng Yên%'
  AND w.name LIKE '%Vũ Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bảo An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Đô Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Đông Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Ninh Chử%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Phan Rang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Anh Dũng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bác Ái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bác Ái Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bác Ái Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Cà Ná%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Công Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Lâm Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Mỹ Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Ninh Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Ninh Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Ninh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Phước Dinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Phước Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Phước Hậu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Phước Hữu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Thuận Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Thuận Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Vĩnh Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Xuân Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Trường Sa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Ba Ngòi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bắc Cam Ranh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bắc Nha Trang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Cam Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Cam Ranh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Đông Ninh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Hòa Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Nam Nha Trang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Nha Trang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Ninh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Tây Nha Trang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bắc Khánh Vĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Bắc Ninh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Cam An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Cam Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Cam Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Đại Lãnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Diên Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Diên Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Diên Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Diên Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Diên Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Đông Khánh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Hòa Trí%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Khánh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Khánh Vĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Nam Cam Ranh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Nam Khánh Vĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Nam Ninh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Suối Dầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Suối Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Tân Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Tây Khánh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Tây Khánh Vĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Tây Ninh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Trung Khánh Vĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Tu Bông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Vạn Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Vạn Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Khánh Hòa%'
  AND w.name LIKE '%Vạn Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Đoàn Kết%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Tân Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Bản Bo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Bình Lư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Bum Nưa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Bum Tở%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Dào San%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Hồng Thu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Hua Bum%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Khoen On%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Khổng Lào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Khun Há%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Lê Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Mù Cả%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Mường Khoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Mường Kim%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Mường Mô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Mường Tè%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Mường Than%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Nậm Cuổi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Nậm Hàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Nậm Mạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Nậm Sỏ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Nậm Tăm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Pa Tần%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Pa Ủ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Pắc Ta%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Phong Thổ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Pu Sam Cáp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Sì Lở Lầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Sìn Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Sin Suối Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Tả Lèng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Tà Tổng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Tân Uyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Than Uyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Thu Lũm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lai Châu%'
  AND w.name LIKE '%Tủa Sín Chải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phú Quý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bình Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%La Gi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Mũi Né%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phan Thiết%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phú Thuỷ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phước Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tiến Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bắc Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bắc Ruộng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đông Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đồng Kho%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đức Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hải Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Kiệm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Liêm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Thuận Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hàm Thuận Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hòa Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hoài Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hồng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hồng Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%La Dạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Liên Hương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Lương Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nam Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nghị Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phan Rí Cửa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phan Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Sơn Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Sông Lũy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Suối Kiết%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tân Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tân Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tân Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tánh Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Trà Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tuy Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tuyên Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Vĩnh Hảo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bắc Gia Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đông Gia Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nam Gia Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Cư Jút%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đắk Mil%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đắk Sắk%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đắk Song%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đắk Wil%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đức An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đức Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Kiến Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Krông Nô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nam Đà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nam Dong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nâm Nung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nhân Cơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Tín%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Trực%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tà Đùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Thuận An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Thuận Hạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Trường Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tuy Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%1 Bảo Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%2 Bảo Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%3 Bảo Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%B''Lao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Cam Ly \- Đà Lạt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Lâm Viên \- Đà Lạt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Lang Biang \- Đà Lạt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Xuân Hương \- Đà Lạt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Xuân Trường \- Đà Lạt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bảo Lâm 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bảo Lâm 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bảo Lâm 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bảo Lâm 4%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bảo Lâm 5%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Bảo Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Cát Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Cát Tiên 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Cát Tiên 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đạ Huoai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đạ Huoai 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đạ Huoai 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đạ Tẻh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đạ Tẻh 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đạ Tẻh 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đam Rông 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đam Rông 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đam Rông 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đam Rông 4%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Di Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đinh Trang Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đinh Văn Lâm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đơn Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%D''Ran%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Đức Trọng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Gia Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hiệp Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hòa Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Hòa Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Ka Đô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Lạc Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nam Ban \- Lâm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Nam Hà \- Lâm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Ninh Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phú Sơn Lâm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Phúc Thọ Lâm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Quảng Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Sơn Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tà Hine%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tà Năng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tân Hà \- Lâm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lâm Đồng%'
  AND w.name LIKE '%Tân Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Đông Kinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Hoàng Văn Thụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Lương Văn Tri%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tam Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Ba Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Bắc Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Bằng Mạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Bình Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Cai Kinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Cao Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Châu Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Chi Lăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Chiến Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Công Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Điềm He%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Đình Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Đoàn Kết%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Đồng Đăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Hoa Thám%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Hội Hoan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Hồng Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Hưng Vũ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Hữu Liên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Hữu Lũng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Kháng Chiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Khánh Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Khuất Xá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Kiên Mộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Lộc Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Lợi Bác%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Mẫu Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Na Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Na Sầm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Nhân Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Nhất Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Quan Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Quốc Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Quốc Việt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Quý Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tân Đoàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tân Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tân Tri%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tân Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thái Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thất Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thiện Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thiện Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thiện Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thiện Thuật%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thống Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Thụy Hùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tràng Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tri Lễ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Tuấn Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Văn Lãng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Vạn Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Vân Nham%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Văn Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Vũ Lăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Vũ Lễ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Xuân Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Yên Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lạng Sơn%'
  AND w.name LIKE '%Yên Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Cam Đường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Lào Cai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Sa Pa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%A Mú Sung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bắc Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bản Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bản Lầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bản Liền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bản Xèo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bảo Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bảo Nhai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bảo Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bảo Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bát Xát%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Cao Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Chiềng Ken%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Cốc Lầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Cốc San%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Dền Sáng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Dương Quỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Gia Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Hợp Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Khánh Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Lùng Phình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Minh Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Mường Bo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Mường Hum%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Mường Khương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Nậm Chày%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Nậm Xé%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Nghĩa Đô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Ngũ Chỉ Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Pha Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Phong Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Phúc Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Si Ma Cai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Sín Chéng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tả Củ Tỷ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tả Phìn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tả Van%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tằng LỏOng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Thượng Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Trịnh Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Văn Bàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Võ Lao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Xuân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Xuân Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Y Tý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Âu Lâu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Cầu Thia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Nam Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Nghĩa Lộ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Trung Tâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Văn Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Yên Bái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Bảo Ái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Cảm Nhân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Cát Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Chấn Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Châu Quế%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Chế Tạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Đông Cuông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Gia Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Hạnh Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Hưng Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Khánh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Khao Mang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Lâm Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Lâm Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Lao Chải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Liên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Lục Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Lương Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Mậu A%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Mỏ Vàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Mù Cang Chải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Mường Lai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Nậm Có%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Nghĩa Tâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Phình Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Phong Dụ Hạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Phong Dụ Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Phúc Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Púng Luông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Quy Mông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Sơn Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tà Xi Láng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tân Hợp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tân Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Thác Bà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Thượng Bằng La%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Trạm Tấu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Trấn Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Tú Lệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Văn Chấn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Việt Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Xuân Ái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Yên Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Lào Cai%'
  AND w.name LIKE '%Yên Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Cửa Lò%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hoàng Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tân Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tây Hiếu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Thái Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Thành Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Trường Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Vinh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Vinh Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Vinh Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%An Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Anh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Anh Sơn Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Bắc Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Bạch Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Bạch Ngọc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Bích Hào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Bình Chuẩn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Bình Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Cam Phục%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Cát Ngạn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Châu Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Châu Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Châu Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Châu Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Châu Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Chiêu Lưu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Con Cuông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Đại Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Đại Huệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Diễn Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Đô Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Đông Hiếu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Đông Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Đông Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Đức Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Giai Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Giai Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hải Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hải Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hạnh Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hoa Quân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hợp Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hùng Chân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hùng Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hưng Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hưng Nguyên Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Huồi Tụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hữu Khuông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Hữu Kiệm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Keng Đu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Kim Bảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Kim Liên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Lam Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Lượng Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Lương Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mậu Thạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Minh Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Minh Hợp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Môn Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mường Chọng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mường Ham%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mường Lống%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mường Quàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mường Típ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mường Xén%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Mỹ Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Na Loi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Na Ngoi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nậm Cắn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nam Đàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nga My%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghi Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Đàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Hành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nghĩa Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nhân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Nhôn Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Phúc Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quan Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quảng Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quang Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quế Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳ Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳ Hợp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Lưu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Tam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Quỳnh Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Sơn Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tam Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tam Hợp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tam Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tam Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tân Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tân Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Thần Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Thành Bình Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Thiên Nhẫn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Thông Thụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Thuần Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tiên Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tiền Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tri Lễ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Trung Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Tương Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Vạn An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Vân Du%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Văn Hiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Văn Kiều%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Vân Tụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Vĩnh Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Xuân Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Yên Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Yên Na%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Yên Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Yên Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Nghệ An%'
  AND w.name LIKE '%Yên Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Châu Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Đồng Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Duy Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Duy Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Duy Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hà Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Kim Bảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Kim Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Lê Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Liêm Tuyền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Lý Thường Kiệt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nguyễn Úy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Phủ Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Phù Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Tam Chúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Tiên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Bắc Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Bình An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Bình Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Bình Lục%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Bình Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Bình Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Liêm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Lý Nhân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Xang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nhân Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Tân Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Thanh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Thanh Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Thanh Liêm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Trần Thương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Vĩnh Trụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Đông Hoa Lư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hoa Lư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Hoa Lư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Tam Điệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Tây Hoa Lư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Trung Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Bình Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Chất Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Cúc Phương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Đại Hoàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Định Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Đồng Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Gia Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Gia Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Gia Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Gia Trấn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Gia Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Gia Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Gia Viễn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Khánh Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Khánh Nhạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Khánh Thiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Khánh Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Kim Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Kim Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Lai Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nho Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Phát Diệm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Phú Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Phú Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Quang Thiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Quỳnh Lưu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Thanh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Mạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Mô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Từ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Đông A%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hồng Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Mỹ Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Thành Nam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Thiên Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Trường Thi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Vị Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Cát Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Cổ Lễ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Đồng Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Giao Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Giao Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Giao Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Giao Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Giao Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Giao Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Giao Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải Hậu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hải Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hiển Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Hồng Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Liên Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Minh Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Minh Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nam Trực%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nghĩa Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nghĩa Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Nghĩa Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Ninh Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Ninh Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Phong Doanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Quang Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Quỹ Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Rạng Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Tân Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Trực Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Vạn Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Vụ Bản%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Vũ Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Xuân Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Xuân Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Xuân Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Xuân Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Ý Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Ninh Bình%'
  AND w.name LIKE '%Yên Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hòa Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Kỳ Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thống Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%An Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%An Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Bao La%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Cao Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Cao Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Cao Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đà Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đại Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đức Nhàn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Dũng Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hợp Kim%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Kim Bôi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Lạc Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Lạc Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Lạc Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Liên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Lương Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Mai Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Mai Hạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Mường Bi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Mường Động%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Mường Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Mường Thàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Mường Vang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Nật Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Ngọc Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Nhân Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Pà Cò%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Quy Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Quyết Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tân Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tân Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tân Pheo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thịnh Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thung Nai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thượng Cốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tiền Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Toàn Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Trị%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Âu Cơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Nông Trang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Phong Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Phú Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thanh Miếu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Việt Trì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Bản Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Bằng Luân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Bình Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Cẩm Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Chân Mộng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Chí Đám%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Chí Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Cự Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Dân Chủ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đan Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đào Xá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đoan Hùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đồng Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đông Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hạ Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hiền Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hiền Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hoàng Cương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hùng Việt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hương Cần%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hy Cương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Khả Cửu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Lai Đồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Lâm Thao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Liên Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Long Cốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Minh Đài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Minh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Phú Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Phú Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Phù Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Phùng Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Quảng Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Sơn Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tam Nông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tây Cốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thanh Ba%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thanh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thanh Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thọ Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thu Cúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thượng Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tiên Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Trạm Thản%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Trung Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tu Vũ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vân Bán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Văn Lang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Văn Miếu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vạn Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh Chân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Võ Miếu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Xuân Đài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Xuân Lũng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Xuân Viên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Phúc Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Xuân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Bình Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Bình Tuyền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Bình Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đại Đình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Đạo Trù%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hải Lựu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hoàng An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hội Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Hợp Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Lập Thạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Liên Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Liên Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Nguyệt Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Sơn Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Sông Lô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tam Đảo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tam Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tam Dương Bắc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tam Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tam Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tề Lỗ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thái Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Thổ Tang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Tiên Lữ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Vĩnh Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Xuân Lãng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Phú Thọ%'
  AND w.name LIKE '%Yên Lãng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk BLa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Cấm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Kon Tum%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Bờ Y%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Kôi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Mar%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Môn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Pék%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Plô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Pxi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Rơ Wa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Rve%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Sao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Tô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Tờ Kan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đăk Ui%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Dục Nông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ia Chim%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ia Đal%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ia Tơi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Kon Braih%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Kon Đào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Kon Plông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Măng Bút%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Măng Đen%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Măng Ri%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Mô Rai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ngọc Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ngọk Bay%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ngọk Réo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ngọk Tụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Rờ Kơi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sa Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sa Loong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sa Thầy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Tu Mơ Rông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Xốp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ya Ly%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Lý Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Cẩm Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đức Phổ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Nghĩa Lộ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sa Huỳnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Trà Câu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Trương Quang Trọng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%An Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Dinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Động%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Tô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Tơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Vì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Ba Xa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Bình Chương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Bình Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Bình Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Cà Đam%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đặng Thùy Trâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đình Cương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đông Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Đông Trà Bồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Khánh Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Lân Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Long Phụng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Minh Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Mỏ Cày%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Mộ Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Nghĩa Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Nghĩa Hành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Nguyễn Nghiêm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Phước Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Hạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Tây Hạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Tây Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Sơn Tịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Tây Trà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Tây Trà Bồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Thanh Bồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Thiện Tín%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Thọ Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Tịnh Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Trà Bồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Trà Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Trường Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Tư Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Vạn Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ngãi%'
  AND w.name LIKE '%Vệ Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Cô Tô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Vân Đồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%An Sinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Bãi Cháy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Bình Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Cẩm Phả%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Cao Xanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Cửa Ông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Đông Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Đông Triều%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hà An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hà Lầm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hạ Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hà Tu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hiệp Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hoàng Quế%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hoành Bồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hồng Gai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Liên Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Mạo Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Móng Cái 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Móng Cái 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Móng Cái 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Mông Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Phong Cốc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Quang Hanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Quảng Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Tuần Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Uông Bí%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Vàng Danh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Việt Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Yên Tử%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Ba Chẽ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Bình Liêu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Cái Chiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Đầm Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Điền Xá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Đông Ngũ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Đường Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hải Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hải Lạng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hải Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hải Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Hoành Mô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Kỳ Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Lục Hồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Lương Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Quảng Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Quảng Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Quảng La%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Quảng Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Thống Nhất%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Tiên Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Ninh%'
  AND w.name LIKE '%Vĩnh Thực%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Ba Đồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Bắc Gianh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Đồng Hới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Đồng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Đồng Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Bắc Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Bố Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Cam Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Dân Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Đồng Lê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Đông Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Hòa Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Hoàn Lão%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Kim Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Kim Ngân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Kim Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Lệ Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Lệ Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Minh Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Nam Ba Đồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Nam Gianh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Nam Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Ninh Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Phong Nha%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Phú Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Quảng Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Quảng Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Sen Ngư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tân Gianh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tân Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Thượng Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Trung Thuần%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Trường Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Trường Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Trường Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tuyên Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tuyên Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tuyên Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tuyên Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tuyên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Cồn Cỏ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Đông Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Nam Đông Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Quảng Trị%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%A Dơi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Ái Tử%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Ba Lòng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Bến Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Bến Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Cam Lộ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Cồn Tiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Cửa Tùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Cửa Việt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Đakrông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Diên Sanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Gio Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Hải Lăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Hiếu Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Hướng Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Hướng Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Hướng Phùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Khe Sanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%La Lay%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Lao Bảo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Lìa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Mỹ Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Nam Cửa Việt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Nam Hải Lăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tà Rụt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Tân Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Triệu Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Triệu Cơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Triệu Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Vĩnh Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Vĩnh Hoàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Vĩnh Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Quảng Trị%'
  AND w.name LIKE '%Vĩnh Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Cơi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Sinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mộc Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mộc Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Thảo Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tô Hiệu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Vân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Bắc Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Bình Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Bó Sinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Hặc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Khoong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Khương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng La%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Lao%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Mung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Sại%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Sơ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Chiềng Sung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Co Mạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Đoàn Kết%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Gia Phù%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Huổi Một%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Kim Bon%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Long Hẹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Lóng Phiêng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Lóng Sập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mai Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Muổi Nọi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Bám%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Bang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Bú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Chanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Chiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Cơi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường É%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Giôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Hung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Khiêng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường La%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Lầm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Lạn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Lèo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Mường Sại%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Nậm Lầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Nậm Ty%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Ngọc Chiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Pắc Ngà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Phiêng Cằm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Phiêng Khoài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Phiêng Pằn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Phù Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Púng Bánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Quỳnh Nhai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Song Khủa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Sông Mã%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Sốp Cộp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Suối Tọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tà Hộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tạ Khoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tà Xùa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tân Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tân Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Thuận Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tô Múa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Tường Hạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Vân Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Xím Vàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Xuân Nha%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Yên Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Sơn La%'
  AND w.name LIKE '%Yên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Khánh Hậu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Kiến Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Long An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%An Lục Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%An Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Bến Lức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Bình Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Bình Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Bình Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Bình Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Cần Đước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Cần Giuộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Đông Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Đức Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Đức Huệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Đức Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hậu Nghĩa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hậu Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hiệp Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hòa Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hưng Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Khánh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Long Cang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Long Hựu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Lương Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mộc Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mỹ An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mỹ Hạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mỹ Lệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mỹ Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mỹ Quý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mỹ Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Mỹ Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Nhơn Hòa Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Nhơn Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Nhựt Tảo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Phước Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Phước Vĩnh Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Rạch Kiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tầm Vu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Lân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Tập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Tây%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Trụ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thạnh Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thạnh Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thạnh Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thủ Thừa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thuận Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tuyên Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tuyên Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Vàm Cỏ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Vĩnh Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Vĩnh Công%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Vĩnh Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Vĩnh Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%An Tịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Bình Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Gia Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Gò Dầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hòa Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Long Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Ninh Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thanh Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Trảng Bàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Bến Cầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Cầu Khởi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Châu Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Dương Minh Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hảo Đước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hoà Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Hưng Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Lộc Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Long Chữ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Long Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Ninh Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Phước Chỉ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Phước Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Phước Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Biên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Hoà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thạnh Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Thạnh Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Trà Vong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tây Ninh%'
  AND w.name LIKE '%Truông Mít%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bắc Kạn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Đức Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Ba Bể%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bạch Thông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bằng Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bằng Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Cẩm Giàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Cao Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Chợ Đồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Chợ Mới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Chợ Rã%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Côn Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Cường Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Đồng Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Hiệp Lực%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Nà Phặc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Na Rì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Nam Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Ngân Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Nghĩa Tá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Nghiên Loan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phong Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phủ Thông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phúc Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Quảng Bạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Tân Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Thanh Mai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Thanh Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Thượng Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Thượng Quan%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Trần Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Văn Lang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Vĩnh Thông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Xuân Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Yên Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Yên Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Yên Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bá Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bách Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Gia Sàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Linh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phan Đình Phùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phổ Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phúc Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Quan Triều%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Quyết Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Sông Công%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Tích Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Trung Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Vạn Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%An Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bình Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Bình Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Đại Phúc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Đại Từ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Dân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Điềm Thụy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Định Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Đồng Hỷ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Đức Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Hợp Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Kha Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Kim Phượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%La Bằng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%La Hiên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Lam Vỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Nam Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Nghinh Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phú Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phú Đình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phú Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phú Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phú Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phú Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Phượng Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Quân Chu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Quang Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Sảng Mộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Tân Cương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Tân Khánh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Thần Sa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Thành Công%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Trại Cau%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Tràng Xá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Trung Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Văn Hán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Văn Lăng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Vạn Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Võ Nhai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Vô Tranh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thái Nguyên%'
  AND w.name LIKE '%Yên Trạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Bỉm Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Đào Duy Tư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Đông Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Đông Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Đông Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hạc Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hải Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hải Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hàm Rồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nam Sầm Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nghi Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Ngọc Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nguyệt Viên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quảng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quang Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Sầm Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tân Dân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tĩnh Gia%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trúc Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%An Nông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Ba Đình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Bá Thước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Bát Mọt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Biện Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Các Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Cẩm Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Cẩm Thạch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Cẩm Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Cẩm Tú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Cẩm Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Cổ Lũng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Công Chính%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Điền Lư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Điền Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Định Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Định Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Đồng Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Đông Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Đồng Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Giao An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hà Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hà Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hậu Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hiền Kiệt%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hồ Vương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoa Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hóa Quỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoằng Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hoạt Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hồi Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Hợp Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Kiên Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Kim Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Lam Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Linh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Lĩnh Toại%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Luận Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Lương Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Lưu Vệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Mậu Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Minh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Mường Chanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Mường Lát%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Mường Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Mường Mìn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Na Mèo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nam Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nga An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nga Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nga Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Ngọc Lặc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Ngọc Liên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Ngọc Trạo%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nguyệt Ấn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nhi Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Như Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Như Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Nông Cống%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Phú Lệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Phú Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Pù Luông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Pù Nhi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quan Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quảng Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quang Chiểu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quảng Chính%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quảng Ngọc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quảng Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quảng Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quý Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Quý Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Sao Vàng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Sơn Điện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Sơn Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tam Chung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tam Lư%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tam Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tân Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tân Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tây Đô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thạch Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thạch Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thạch Quảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thăng Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thắng Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thắng Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thanh Kỳ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thanh Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thanh Quân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thành Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thiên Phủ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thiết Ống%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thiệu Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thiệu Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thiệu Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thiệu Toán%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thiệu Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thọ Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thọ Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thọ Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thọ Ngọc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thọ Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thọ Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thượng Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Thường Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tiên Trang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tống Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Triệu Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Triệu Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trung Chính%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trung Hạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trung Lý%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trung Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trung Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trường Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Trường Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Tượng Lĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Vân Du%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Vạn Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Văn Nho%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Văn Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Vạn Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Vĩnh Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Xuân Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Xuân Chinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Xuân Du%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Xuân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Xuân Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Xuân Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Xuân Tín%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Khương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Nhân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Ninh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Thắng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Thanh Hóa%'
  AND w.name LIKE '%Yên Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%An Cựu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Dương Nỗ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Hóa Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Hương An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Hương Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Hương Trà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Kim Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Kim Trà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Mỹ Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phong Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phong Dinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phong Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phong Quảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phong Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phú Bài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phú Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Thanh Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Thuận An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Thuận Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Thủy Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Vỹ Dạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%A Lưới 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%A Lưới 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%A Lưới 3%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%A Lưới 4%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%A Lưới 5%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Bình Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Chân Mây – Lăng Cô%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Đan Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Hưng Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Khe Tre%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Lộc An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Long Quảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Nam Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phú Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phú Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phú Vang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Phú Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Quảng Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Huế%'
  AND w.name LIKE '%Vinh Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hà Giang 1%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hà Giang 2%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bắc Mê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bắc Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bạch Đích%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bạch Ngọc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bản Máy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bằng Hành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bằng Lang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Cán Tỷ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Cao Bồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Đồng Tâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Đồng Văn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Đồng Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Du Già%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Đường Hồng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Đường Thượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Giáp Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hồ Thầu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hoàng Su Phì%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hùng An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Khâu Vai%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Khuôn Lùng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Lao Chải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Liên Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Linh Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Lũng Cú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Lũng Phìn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Lùng Tám%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Mậu Duệ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Mèo Vạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Minh Ngọc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Minh Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Minh Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Nấm Dẩn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Nậm Dịch%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Nghĩa Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Ngọc Đường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Ngọc Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Niêm Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Pà Vầy Sủ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Phố Bảng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Phú Linh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Pờ Ly Ngài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Quản Bạ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Quang Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Quảng Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Sà Phìn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Sơn Vĩ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Sủng Máng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân Trịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tát Ngà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thắng Mố%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thàng Tín%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thanh Thuỷ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thông Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thuận Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thượng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tiên Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tiên Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Trung Thịnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tùng Bá%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tùng Vài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Vị Xuyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Việt Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Vĩnh Tuy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Xín Mần%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Xuân Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Cường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%An Tường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bình Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Minh Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Mỹ Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Nông Tiến%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bạch Xa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bình An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bình Ca%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Bình Xa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Chiêm Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Côn Lôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Đông Thọ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hàm Yên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hòa An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hồng Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hồng Thái%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hùng Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Hùng Lợi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Kiên Đài%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Kiến Thiết%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Kim Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Lâm Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Lực Hành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Minh Quang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Minh Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Nà Hang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Nhữ Khê%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Phú Lương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Phù Lưu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Sơn Dương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Sơn Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân Thanh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tân Trào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thái Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thái Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thái Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thượng Lâm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Thượng Nông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Tri Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Trung Hà%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Trung Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Trường Sinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Xuân Vân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Hoa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Lập%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Nguyên%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Tuyên Quang%'
  AND w.name LIKE '%Yên Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Bến Tre%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phú Khương%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phú Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Sơn Đông%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Định%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Ngãi Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Qui%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Ba Tri%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Bảo Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Bình Đại%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Châu Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Châu Hưng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Chợ Lách%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Đại Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Đồng Khởi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Giao Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Giồng Trôm%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hưng Khánh Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hưng Nhượng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hương Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Lộc Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Lương Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Lương Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Mỏ Cày%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Mỹ Chánh Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Nhuận Phú Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phú Phụng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phú Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phú Túc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phước Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phước Mỹ Trung%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Quới Điền%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Hào%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Thành Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Xuân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thạnh Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thạnh Phong%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thạnh Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thạnh Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thành Thới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thạnh Trị%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thới Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tiên Thủy%';
UPDATE public.wards w
SET phan_loai = 'Cụm 2', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Vĩnh Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Duyên Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hòa Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Nguyệt Hóa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trà Vinh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trường Long Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Phú Tân%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Bình Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Càng Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Cầu Kè%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Cầu Ngang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Châu Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Đại An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Đôn Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Đông Hải%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hàm Giang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hiệp Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hòa Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hùng Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hưng Mỹ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Hữu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Vĩnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Lưu Nghiệp Anh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Mỹ Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Ngũ Lạc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Nhị Long%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Nhị Trường%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phong Thạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Song Lộc%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tam Ngãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Hòa%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tập Ngãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tập Sơn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tiểu Cần%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trà Cú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 3', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Vinh Kim%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Bình Minh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Cái Vồn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Đông Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Châu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phước Hậu%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Hạnh%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Ngãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Thanh Đức%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%An Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Bình Phước%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Cái Ngang%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Cái Nhum%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hiếu Phụng%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hiếu Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hòa Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Hòa Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Long Hồ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Lục Sỹ Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Mỹ Thuận%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Ngãi Tứ%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Nhơn Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = '0'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Phú Quới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Quới An%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Quới Thiện%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Song Phú%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tam Bình%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Long Hội%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Lược%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Tân Quới%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trà Côn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trà Ôn%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trung Hiệp%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trung Ngãi%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Trung Thành%';
UPDATE public.wards w
SET phan_loai = 'Cụm 1', loai_tuyen = 'Vùng xa'
    FROM public.provinces p
WHERE w.province_code = p.province_code
  AND p.name LIKE '%Vĩnh Long%'
  AND w.name LIKE '%Vĩnh Xuân%';
