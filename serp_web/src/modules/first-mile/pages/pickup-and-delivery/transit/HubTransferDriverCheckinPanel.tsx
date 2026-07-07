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
  useGetHubsQuery,
} from '../../../api/firstMileApi';
import type { BagDistributionManifest } from '../../../types';
import { CheckinDialog, DriverTab } from './components';
import {
  isTransitCheckinDevFeatureAvailable,
  readTransitDevCheckinPreference,
  resolveTransitDevCheckinCoordinates,
  writeTransitDevCheckinPreference,
} from './transitCheckinDev';
import {
  canDriverCheckin,
  type CheckinMode,
  type CheckinState,
} from '../../dispatchers/second-mile/secondMileDispatchModels';

const makeEmptyCheckinState = (): CheckinState => ({
  latitude: '',
  longitude: '',
  locationLabel: '',
});

export function HubTransferDriverCheckinPanel() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canCheckin = canDriverCheckin(roles);
  const isDevCheckinFeatureAvailable = isTransitCheckinDevFeatureAvailable();
  const [devCheckinMode, setDevCheckinMode] = React.useState(false);

  const [checkinTarget, setCheckinTarget] = React.useState<{
    manifest: BagDistributionManifest;
    mode: CheckinMode;
  } | null>(null);
  const [checkinState, setCheckinState] =
    React.useState<CheckinState>(makeEmptyCheckinState);

  const {
    data: manifestsData,
    isFetching,
    refetch,
  } = useGetBagDistributionManifestsQuery({
    page: 0,
    size: 50,
  });
  const { data: hubsData } = useGetHubsQuery({
    page: 0,
    size: 500,
    status: 'ACTIVE',
  });

  const [driverCheckinStart, { isLoading: isCheckingInStart }] =
    useDriverCheckinBagDistributionStartMutation();
  const [driverCheckinEnd, { isLoading: isCheckingInEnd }] =
    useDriverCheckinBagDistributionEndMutation();

  const manifests = (manifestsData?.items ?? []).filter(
    (manifest) =>
      manifest.status === 'CREATED' || manifest.status === 'OUTBOUND_CONFIRMED'
  );
  const hubs = React.useMemo(() => hubsData?.items ?? [], [hubsData?.items]);

  React.useEffect(() => {
    if (isDevCheckinFeatureAvailable) {
      setDevCheckinMode(readTransitDevCheckinPreference());
    }
  }, [isDevCheckinFeatureAvailable]);

  const applyDevCheckinCoordinates = React.useCallback(
    (
      manifest: BagDistributionManifest,
      mode: CheckinMode,
      showError = true
    ) => {
      const coordinates = resolveTransitDevCheckinCoordinates(
        manifest,
        mode,
        hubs
      );

      if (!coordinates) {
        if (showError) {
          notification.error(
            mode === 'start'
              ? 'Thiếu tọa độ hub xuất phát.'
              : 'Thiếu tọa độ hub nhận.',
            {
              description:
                'Vui lòng cập nhật tọa độ hub trước khi dùng chế độ phát triển.',
            }
          );
        }
        return null;
      }

      setCheckinState((current) => ({
        ...current,
        latitude: coordinates.latitude.toFixed(6),
        longitude: coordinates.longitude.toFixed(6),
        locationLabel: coordinates.locationLabel,
      }));

      return coordinates;
    },
    [hubs, notification]
  );

  React.useEffect(() => {
    if (!devCheckinMode || !checkinTarget) {
      return;
    }

    applyDevCheckinCoordinates(
      checkinTarget.manifest,
      checkinTarget.mode,
      false
    );
  }, [applyDevCheckinCoordinates, checkinTarget, devCheckinMode]);

  const openCheckin = (
    manifest: BagDistributionManifest,
    mode: CheckinMode
  ) => {
    if (!canCheckin) {
      notification.error('Bạn cần quyền tài xế hoặc vận hành hub để check-in.');
      return;
    }
    setCheckinTarget({ manifest, mode });
    setCheckinState(makeEmptyCheckinState());

    if (devCheckinMode) {
      applyDevCheckinCoordinates(manifest, mode, false);
    }
  };

  const handleDevCheckinModeChange = (enabled: boolean) => {
    setDevCheckinMode(enabled);
    writeTransitDevCheckinPreference(enabled);

    if (enabled && checkinTarget) {
      applyDevCheckinCoordinates(checkinTarget.manifest, checkinTarget.mode);
    }
  };

  const handleSubmitCheckin = async () => {
    if (!checkinTarget) return;
    const devCoordinates = devCheckinMode
      ? applyDevCheckinCoordinates(
          checkinTarget.manifest,
          checkinTarget.mode
        )
      : null;

    if (devCheckinMode && !devCoordinates) {
      return;
    }

    const latitude = devCoordinates?.latitude ?? Number(checkinState.latitude);
    const longitude =
      devCoordinates?.longitude ?? Number(checkinState.longitude);

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
    const locationLabel =
      devCoordinates?.locationLabel ?? checkinState.locationLabel.trim();
    if (locationLabel) {
      formData.append('location_label', locationLabel);
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
        isDevCheckinFeatureAvailable={isDevCheckinFeatureAvailable}
        devCheckinMode={devCheckinMode}
        onStateChange={setCheckinState}
        onDevCheckinModeChange={handleDevCheckinModeChange}
        onOpenChange={(open) => {
          if (!open) setCheckinTarget(null);
        }}
        onSubmit={handleSubmitCheckin}
      />
    </>
  );
}
