/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - Post office location resolver hook
 */

import React from 'react';
import { useAppDispatch } from '@/lib/store';
import {
  firstMileApi,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
} from '../../api';
import type {
  PostOffice,
  PostOfficeImportItem,
  Province,
  Ward,
} from '../../types';
import { normalizeLocationCode } from './postOfficeForm';

interface UsePostOfficeLocationsParams {
  postOffices?: PostOffice[];
  previewItems?: PostOfficeImportItem[];
  formProvinceCode: string;
  formWardCode: string;
}

interface UsePostOfficeLocationsResult {
  selectedProvinceCode: string;
  selectedWardCode: string;
  provinceSelectOptions: Province[];
  wardSelectOptions: Ward[];
  isFetchingWardsForForm: boolean;
  getProvinceLabel: (provinceCode?: string) => string;
  getWardLabel: (provinceCode?: string, wardCode?: string) => string;
}

export const usePostOfficeLocations = ({
  postOffices,
  previewItems,
  formProvinceCode,
  formWardCode,
}: UsePostOfficeLocationsParams): UsePostOfficeLocationsResult => {
  const dispatch = useAppDispatch();
  const [wardNamesByProvinceCode, setWardNamesByProvinceCode] = React.useState<
    Record<string, Record<string, string>>
  >({});

  const selectedProvinceCode = React.useMemo(
    () => normalizeLocationCode(formProvinceCode),
    [formProvinceCode]
  );
  const selectedWardCode = React.useMemo(
    () => normalizeLocationCode(formWardCode),
    [formWardCode]
  );

  const { data: provincesData } = useGetProvincesQuery({
    page: 0,
    size: 200,
  });

  const { data: wardsForFormData, isFetching: isFetchingWardsForForm } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: selectedProvinceCode,
        page: 0,
        size: 1000,
      },
      {
        skip: !selectedProvinceCode,
      }
    );

  const provinceNameByCode = React.useMemo(() => {
    return (provincesData?.items ?? []).reduce<Record<string, string>>(
      (accumulator, province) => {
        const provinceCode = normalizeLocationCode(province.provinceCode);

        if (provinceCode) {
          accumulator[provinceCode] = province.name;
        }

        return accumulator;
      },
      {}
    );
  }, [provincesData]);

  const provinceSelectOptions = React.useMemo(() => {
    const options = [...(provincesData?.items ?? [])];

    if (
      selectedProvinceCode &&
      !options.some(
        (province) =>
          normalizeLocationCode(province.provinceCode) === selectedProvinceCode
      )
    ) {
      options.unshift({
        provinceCode: selectedProvinceCode,
        name: selectedProvinceCode,
      });
    }

    return options;
  }, [provincesData, selectedProvinceCode]);

  const wardSelectOptions = React.useMemo(() => {
    const options = [...(wardsForFormData?.items ?? [])];

    if (
      selectedWardCode &&
      !options.some(
        (ward) => normalizeLocationCode(ward.wardCode) === selectedWardCode
      )
    ) {
      options.unshift({
        wardCode: selectedWardCode,
        name: selectedWardCode,
        provinceCode: selectedProvinceCode,
      });
    }

    return options;
  }, [selectedProvinceCode, selectedWardCode, wardsForFormData]);

  React.useEffect(() => {
    const provinceCodesFromPostOffices = (postOffices ?? [])
      .map((postOffice) => normalizeLocationCode(postOffice.provinceCode))
      .filter(Boolean);
    const provinceCodesFromImportPreview = (previewItems ?? [])
      .map((item) => normalizeLocationCode(item.province_code))
      .filter(Boolean);
    const provinceCodes = Array.from(
      new Set([
        ...provinceCodesFromPostOffices,
        ...provinceCodesFromImportPreview,
      ])
    );
    const missingProvinceCodes = provinceCodes.filter(
      (provinceCode) => !(provinceCode in wardNamesByProvinceCode)
    );

    if (missingProvinceCodes.length === 0) {
      return;
    }

    let isCancelled = false;

    const fetchWards = async () => {
      await Promise.all(
        missingProvinceCodes.map(async (provinceCode) => {
          try {
            const wardPage = await dispatch(
              firstMileApi.endpoints.getWardsByProvinceCode.initiate(
                {
                  provinceCode,
                  page: 0,
                  size: 1000,
                },
                {
                  subscribe: false,
                }
              )
            ).unwrap();

            if (isCancelled) {
              return;
            }

            const wardNameByCode = wardPage.items.reduce<
              Record<string, string>
            >((accumulator, ward) => {
              const wardCode = normalizeLocationCode(ward.wardCode);

              if (wardCode) {
                accumulator[wardCode] = ward.name;
              }

              return accumulator;
            }, {});

            setWardNamesByProvinceCode((prev) => ({
              ...prev,
              [provinceCode]: wardNameByCode,
            }));
          } catch {
            if (isCancelled) {
              return;
            }

            // Mark as resolved to avoid infinite retries during render.
            setWardNamesByProvinceCode((prev) => ({
              ...prev,
              [provinceCode]: {},
            }));
          }
        })
      );
    };

    void fetchWards();

    return () => {
      isCancelled = true;
    };
  }, [dispatch, postOffices, previewItems, wardNamesByProvinceCode]);

  const getProvinceLabel = React.useCallback(
    (provinceCode?: string) => {
      const normalizedProvinceCode = normalizeLocationCode(provinceCode);

      if (!normalizedProvinceCode) {
        return '-';
      }

      return (
        provinceNameByCode[normalizedProvinceCode] || normalizedProvinceCode
      );
    },
    [provinceNameByCode]
  );

  const getWardLabel = React.useCallback(
    (provinceCode?: string, wardCode?: string) => {
      const normalizedWardCode = normalizeLocationCode(wardCode);

      if (!normalizedWardCode) {
        return '-';
      }

      const normalizedProvinceCode = normalizeLocationCode(provinceCode);

      if (!normalizedProvinceCode) {
        return normalizedWardCode;
      }

      const wardNameByCode = wardNamesByProvinceCode[normalizedProvinceCode];

      return wardNameByCode?.[normalizedWardCode] || normalizedWardCode;
    },
    [wardNamesByProvinceCode]
  );

  return {
    selectedProvinceCode,
    selectedWardCode,
    provinceSelectOptions,
    wardSelectOptions,
    isFetchingWardsForForm,
    getProvinceLabel,
    getWardLabel,
  };
};
