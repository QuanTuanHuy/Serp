/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Province/ward options for billing forms
 */

import React from 'react';
import {
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
} from '../../../../api';
import type { Province, Ward } from '../../../../types';
import type { BillingSelectOption } from '../billingPageModels';

const mapProvinceOptions = (
  items: Province[] | undefined
): BillingSelectOption[] => {
  return (items ?? [])
    .filter((province): province is Province => Boolean(province?.provinceCode))
    .map((province) => ({
      value: province.provinceCode,
      label:
        province.shortName?.trim() ||
        province.name?.trim() ||
        province.provinceCode,
    }))
    .sort((a, b) =>
      a.label.localeCompare(b.label, 'en-US', { sensitivity: 'base' })
    );
};

const mapWardOptions = (items: Ward[] | undefined): BillingSelectOption[] => {
  return (items ?? [])
    .filter((ward): ward is Ward => Boolean(ward?.wardCode))
    .map((ward) => ({
      value: ward.wardCode,
      label: `${ward.name?.trim() || ward.wardCode} (${ward.wardCode})`,
    }))
    .sort((a, b) =>
      a.label.localeCompare(b.label, 'en-US', { sensitivity: 'base' })
    );
};

export const useBillingLocationOptions = (params: {
  senderProvinceCode: string;
  receiverProvinceCode: string;
}) => {
  const { data: provincesData, isFetching: isFetchingProvinces } =
    useGetProvincesQuery({
      page: 0,
      size: 200,
    });

  const { data: senderWardsData, isFetching: isFetchingSenderWards } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: params.senderProvinceCode,
        page: 0,
        size: 2000,
      },
      { skip: !params.senderProvinceCode }
    );

  const { data: receiverWardsData, isFetching: isFetchingReceiverWards } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: params.receiverProvinceCode,
        page: 0,
        size: 2000,
      },
      { skip: !params.receiverProvinceCode }
    );

  const provinceOptions = React.useMemo(
    () => mapProvinceOptions(provincesData?.items),
    [provincesData?.items]
  );

  const senderWardOptions = React.useMemo(
    () => mapWardOptions(senderWardsData?.items),
    [senderWardsData?.items]
  );

  const receiverWardOptions = React.useMemo(
    () => mapWardOptions(receiverWardsData?.items),
    [receiverWardsData?.items]
  );

  return {
    provinceOptions,
    senderWardOptions,
    receiverWardOptions,
    isFetchingProvinces,
    isFetchingSenderWards,
    isFetchingReceiverWards,
  };
};
