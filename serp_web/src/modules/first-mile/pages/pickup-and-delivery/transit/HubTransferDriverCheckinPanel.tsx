/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub transfer driver check-in panel
 */

'use client';

import * as React from 'react';

import { getErrorMessage, useAppSelector } from '@/lib/store';
import { useNotification } from '@/shared/hooks';

import {
  useDriverCheckinBagDistributionEndMutation,
  useDriverCheckinBagDistributionStartMutation,
  useGetBagDistributionManifestsQuery,
} from '../../../api/firstMileApi';
import type { BagDistributionManifest } from '../../../types';
import { CheckinDialog, DriverTab } from './components';
import {
  canDriverCheckin,
  type CheckinMode,
  type CheckinState,
} from '../../dispatchers/second-mile/secondMileDispatchModels';

export function HubTransferDriverCheckinPanel() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canCheckin = canDriverCheckin(roles);

  const [checkinTarget, setCheckinTarget] = React.useState<{
    manifest: BagDistributionManifest;
    mode: CheckinMode;
  } | null>(null);
  const [checkinState, setCheckinState] = React.useState<CheckinState>({
    latitude: '',
    longitude: '',
    locationLabel: '',
  });

  const {
    data: manifestsData,
    isFetching,
    refetch,
  } = useGetBagDistributionManifestsQuery({
    page: 0,
    size: 50,
  });

  const [driverCheckinStart, { isLoading: isCheckingInStart }] =
    useDriverCheckinBagDistributionStartMutation();
  const [driverCheckinEnd, { isLoading: isCheckingInEnd }] =
    useDriverCheckinBagDistributionEndMutation();

  const manifests = (manifestsData?.items ?? []).filter(
    (manifest) =>
      manifest.status === 'CREATED' || manifest.status === 'OUTBOUND_CONFIRMED'
  );

  const openCheckin = (
    manifest: BagDistributionManifest,
    mode: CheckinMode
  ) => {
    if (!canCheckin) {
      notification.error('Bạn cần quyền tài xế hoặc vận hành hub để check-in.');
      return;
    }
    setCheckinTarget({ manifest, mode });
    setCheckinState({ latitude: '', longitude: '', locationLabel: '' });
  };

  const handleSubmitCheckin = async () => {
    if (!checkinTarget) return;
    const latitude = Number(checkinState.latitude);
    const longitude = Number(checkinState.longitude);

    if (!Number.isFinite(latitude) || !Number.isFinite(longitude)) {
      notification.error('Nhập đầy đủ vĩ độ và kinh độ.');
      return;
    }
    if (!checkinState.photo) {
      notification.error('Tải lên ảnh check-in.');
      return;
    }

    const formData = new FormData();
    formData.append('latitude', String(latitude));
    formData.append('longitude', String(longitude));
    if (checkinState.locationLabel.trim()) {
      formData.append('location_label', checkinState.locationLabel.trim());
    }
    formData.append('photo', checkinState.photo);

    try {
      if (checkinTarget.mode === 'start') {
        await driverCheckinStart({
          manifestId: checkinTarget.manifest.id,
          formData,
        }).unwrap();
        notification.success('Đã gửi check-in lúc xuất phát.');
      } else {
        await driverCheckinEnd({
          manifestId: checkinTarget.manifest.id,
          formData,
        }).unwrap();
        notification.success('Đã gửi check-in lúc đến nơi.');
      }
      setCheckinTarget(null);
      void refetch();
    } catch (err) {
      notification.error('Không thể gửi check-in.', {
        description: getErrorMessage(err),
      });
    }
  };

  return (
    <>
      <DriverTab
        manifests={manifests}
        isFetching={isFetching}
        canCheckin={canCheckin}
        onOpenCheckin={openCheckin}
      />

      <CheckinDialog
        target={checkinTarget}
        state={checkinState}
        isLoading={isCheckingInStart || isCheckingInEnd}
        onStateChange={setCheckinState}
        onOpenChange={(open) => {
          if (!open) setCheckinTarget(null);
        }}
        onSubmit={handleSubmitCheckin}
      />
    </>
  );
}
