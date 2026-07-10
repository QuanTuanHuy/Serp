/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub transfer driver check-in panel
 */

'use client';

import * as React from 'react';

import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { useNotification } from '@/shared/hooks';

import {
  useDriverCheckinBagDistributionEndMutation,
  useDriverCheckinBagDistributionStartMutation,
  useGetBagDistributionManifestsQuery,
  useGetHubsQuery,
  useGetPostOfficesQuery,
} from '../../../api/firstMileApi';
import type {
  BagDistributionManifest,
  BagDistributionManifestStatus,
} from '../../../types';
import {
  CheckinDialog,
  CheckinHistoryCard,
  DriverTab,
  TransitCheckinMap,
  filterCheckinHistory,
  type CheckinHistoryFilters,
} from './components';
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

const makeDefaultHistoryFilters = (): CheckinHistoryFilters => ({
  stage: 'ALL',
  status: 'ALL',
  driverId: '',
});

const parsePositiveInteger = (value: string): number | undefined => {
  const parsed = Number(value.trim());
  return Number.isInteger(parsed) && parsed > 0 ? parsed : undefined;
};

export function HubTransferDriverCheckinPanel() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const canCheckin = canDriverCheckin(roles);
  const isAdmin = roles.includes('TMS_ADMIN');
  const isDevCheckinFeatureAvailable = isTransitCheckinDevFeatureAvailable();
  const [devCheckinMode, setDevCheckinMode] = React.useState(false);
  const [selectedManifestId, setSelectedManifestId] = React.useState<
    number | undefined
  >();
  const [historyFilters, setHistoryFilters] =
    React.useState<CheckinHistoryFilters>(makeDefaultHistoryFilters);

  const [checkinTarget, setCheckinTarget] = React.useState<{
    manifest: BagDistributionManifest;
    mode: CheckinMode;
  } | null>(null);
  const [checkinState, setCheckinState] = React.useState<CheckinState>(
    makeEmptyCheckinState
  );

  const {
    data: manifestsData,
    isFetching,
    refetch,
  } = useGetBagDistributionManifestsQuery(
    {
      page: 0,
      size: 50,
    },
    { skip: !canCheckin }
  );
  const historyStatus =
    historyFilters.status === 'ALL'
      ? undefined
      : (historyFilters.status as BagDistributionManifestStatus);
  const historyDriverId = isAdmin
    ? parsePositiveInteger(historyFilters.driverId)
    : undefined;
  const { data: historyData, isFetching: isFetchingHistory } =
    useGetBagDistributionManifestsQuery(
      {
        page: 0,
        size: 200,
        ...(historyStatus ? { status: historyStatus } : {}),
        ...(historyDriverId ? { assignedDriverId: historyDriverId } : {}),
      },
      { skip: !canCheckin }
    );
  const { data: hubsData } = useGetHubsQuery(
    {
      page: 0,
      size: 500,
      status: 'ACTIVE',
    },
    { skip: !canCheckin }
  );
  const { data: postOfficesData, isFetching: isFetchingPostOffices } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: 500,
        status: 'ACTIVE',
        hasLocation: true,
      },
      { skip: !canCheckin }
    );

  const [driverCheckinStart, { isLoading: isCheckingInStart }] =
    useDriverCheckinBagDistributionStartMutation();
  const [driverCheckinEnd, { isLoading: isCheckingInEnd }] =
    useDriverCheckinBagDistributionEndMutation();

  const manifests = (manifestsData?.items ?? []).filter(
    (manifest) =>
      manifest.status === 'CREATED' || manifest.status === 'OUTBOUND_CONFIRMED'
  );
  const hubs = React.useMemo(() => hubsData?.items ?? [], [hubsData?.items]);
  const postOffices = React.useMemo(
    () => postOfficesData?.items ?? [],
    [postOfficesData?.items]
  );
  const historyManifests = React.useMemo(
    () =>
      filterCheckinHistory(historyData?.items ?? [], historyFilters).sort(
        (left, right) => {
          const leftTime =
            left.driverEndCheckinAt ??
            left.driverStartCheckinAt ??
            left.updatedAt ??
            left.createdAt ??
            '';
          const rightTime =
            right.driverEndCheckinAt ??
            right.driverStartCheckinAt ??
            right.updatedAt ??
            right.createdAt ??
            '';

          return rightTime.localeCompare(leftTime);
        }
      ),
    [historyData?.items, historyFilters]
  );

  React.useEffect(() => {
    if (isDevCheckinFeatureAvailable) {
      setDevCheckinMode(readTransitDevCheckinPreference());
    }
  }, [isDevCheckinFeatureAvailable]);

  React.useEffect(() => {
    if (
      selectedManifestId &&
      !historyManifests.some((manifest) => manifest.id === selectedManifestId)
    ) {
      setSelectedManifestId(undefined);
    }
  }, [historyManifests, selectedManifestId]);

  const applyDevCheckinCoordinates = React.useCallback(
    (
      manifest: BagDistributionManifest,
      mode: CheckinMode,
      showError = true
    ) => {
      const coordinates = resolveTransitDevCheckinCoordinates(
        manifest,
        mode,
        hubs,
        postOffices
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

      const nextLatitude = coordinates.latitude.toFixed(6);
      const nextLongitude = coordinates.longitude.toFixed(6);

      setCheckinState((current) => {
        if (
          current.latitude === nextLatitude &&
          current.longitude === nextLongitude &&
          current.locationLabel === coordinates.locationLabel
        ) {
          return current;
        }

        return {
          ...current,
          latitude: nextLatitude,
          longitude: nextLongitude,
          locationLabel: coordinates.locationLabel,
        };
      });

      return coordinates;
    },
    [hubs, notification, postOffices]
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
    setSelectedManifestId(manifest.id);
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
      ? applyDevCheckinCoordinates(checkinTarget.manifest, checkinTarget.mode)
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
    <div className='space-y-6'>
      <Card>
        <CardHeader>
          <CardTitle>Bản đồ check-in trung chuyển</CardTitle>
          <CardDescription>
            Theo dõi điểm xuất phát, điểm nhận và các vị trí tài xế đã check-in
            trên từng chuyến.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <TransitCheckinMap
            manifests={historyManifests}
            hubs={hubs}
            postOffices={postOffices}
            selectedManifestId={selectedManifestId}
            loading={isFetchingHistory || isFetchingPostOffices}
            onSelectManifest={setSelectedManifestId}
          />
        </CardContent>
      </Card>

      <DriverTab
        manifests={manifests}
        isFetching={isFetching}
        canCheckin={canCheckin}
        onOpenCheckin={openCheckin}
      />

      <CheckinHistoryCard
        manifests={historyManifests}
        filters={historyFilters}
        isAdmin={isAdmin}
        isFetching={isFetchingHistory}
        selectedManifestId={selectedManifestId}
        onFiltersChange={setHistoryFilters}
        onSelectManifest={setSelectedManifestId}
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
    </div>
  );
}
