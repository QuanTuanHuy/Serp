/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Hub post office link management page
 */

'use client';

import React from 'react';
import Link from 'next/link';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Checkbox,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  Input,
  Label,
} from '@/shared/components/ui';
import { ConfirmDialog } from '@/shared/components/ui/confirm-dialog';
import { useNotification } from '@/shared/hooks';
import {
  ArrowRightLeft,
  Building2,
  Eye,
  Loader2,
  MapPin,
  Plus,
  RefreshCw,
  ShieldAlert,
  Trash2,
} from 'lucide-react';
import { TmsCombobox } from '../../../components';
import {
  useAssignPostOfficeToHubMutation,
  useGetAllHubPostOfficesQuery,
  useGetHubPostOfficesQuery,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useGetProvincesQuery,
  useGetWardsByProvinceCodeQuery,
  useRemovePostOfficeFromHubMutation,
} from '../../../api';
import type {
  Hub,
  HubPostOfficeMapping,
  PostOffice,
  Province,
  Ward,
} from '../../../types';
import {
  HubPostOfficeLinkMap,
  type HubPostOfficeMapHub,
  type HubPostOfficeMapPoint,
} from './components/HubPostOfficeLinkMap';

const HUB_PAGE_SIZE = 300;
const POST_OFFICE_LOOKUP_SIZE = 500;
const LINK_PAGE_SIZE = 500;
const ALL_LINK_PAGE_SIZE = 2000;
const ASSIGN_LOCATION_SIZE = 200;
const ASSIGN_POST_OFFICE_SIZE = 500;

interface HubPostOfficeLinkRow {
  mapping: HubPostOfficeMapping;
  postOffice?: PostOffice;
}

function formatDateTime(value?: string): string {
  if (!value) {
    return '--';
  }

  return new Date(value).toLocaleString('vi-VN');
}

function buildHubLabel(hub: Hub): string {
  return `${hub.code} - ${hub.name}`;
}

function buildPostOfficeLabel(postOffice: PostOffice): string {
  return `${postOffice.code} - ${postOffice.name}`;
}

function normalizeSearchText(value: string): string {
  return value.trim().toLocaleLowerCase('vi-VN');
}

function formatStatusLabel(status?: string): string {
  if (!status) {
    return '--';
  }
  if (status === 'ACTIVE') {
    return 'Đang hoạt động';
  }
  if (status === 'INACTIVE') {
    return 'Ngừng hoạt động';
  }
  return status;
}

function toMapHub(hub?: Hub): HubPostOfficeMapHub | undefined {
  if (!hub) {
    return undefined;
  }

  return {
    id: hub.id,
    code: hub.code,
    name: hub.name,
    address: hub.addressDetail,
    latitude: hub.latitude,
    longitude: hub.longitude,
    status: formatStatusLabel(hub.status),
  };
}

function toMapPostOffice(row: HubPostOfficeLinkRow): HubPostOfficeMapPoint {
  return {
    code: row.mapping.postOfficeCode,
    name: row.postOffice?.name || row.mapping.postOfficeCode,
    address: row.postOffice?.addressDetail,
    latitude: row.postOffice?.latitude,
    longitude: row.postOffice?.longitude,
    status: formatStatusLabel(row.postOffice?.status),
  };
}

function hasPostOfficeLocation(postOffice?: PostOffice): boolean {
  return (
    postOffice?.latitude !== undefined &&
    postOffice.latitude !== null &&
    postOffice.longitude !== undefined &&
    postOffice.longitude !== null
  );
}

export function HubPostOfficeLinkPage() {
  const notification = useNotification();
  const isTmsAdmin = useAppSelector((state) =>
    Boolean(state.account.user.profile?.roles?.includes('TMS_ADMIN'))
  );

  const [selectedHubId, setSelectedHubId] = React.useState('');
  const [linkPage, setLinkPage] = React.useState(0);
  const [selectedPostOfficeCode, setSelectedPostOfficeCode] =
    React.useState('');
  const [assignDialogOpen, setAssignDialogOpen] = React.useState(false);
  const [assignProvinceCode, setAssignProvinceCode] = React.useState('');
  const [assignWardCode, setAssignWardCode] = React.useState('');
  const [assignPostOfficeKeyword, setAssignPostOfficeKeyword] =
    React.useState('');
  const [postOfficeCodesToAssign, setPostOfficeCodesToAssign] = React.useState<
    string[]
  >([]);
  const [editTarget, setEditTarget] =
    React.useState<HubPostOfficeLinkRow | null>(null);
  const [editTargetHubId, setEditTargetHubId] = React.useState('');
  const [detailDialogOpen, setDetailDialogOpen] = React.useState(false);
  const [deleteTarget, setDeleteTarget] =
    React.useState<HubPostOfficeLinkRow | null>(null);

  const {
    data: hubsData,
    isFetching: isFetchingHubs,
    refetch: refetchHubs,
  } = useGetHubsQuery({
    page: 0,
    size: HUB_PAGE_SIZE,
  });

  const {
    data: postOfficesData,
    isFetching: isFetchingPostOffices,
    refetch: refetchPostOffices,
  } = useGetPostOfficesQuery({
    page: 0,
    size: POST_OFFICE_LOOKUP_SIZE,
  });

  const hubs = React.useMemo(() => hubsData?.items ?? [], [hubsData?.items]);
  const postOffices = React.useMemo(
    () => postOfficesData?.items ?? [],
    [postOfficesData?.items]
  );

  React.useEffect(() => {
    if (!selectedHubId && hubs.length > 0) {
      setSelectedHubId(String(hubs[0].id));
    }
  }, [hubs, selectedHubId]);

  React.useEffect(() => {
    setLinkPage(0);
    setSelectedPostOfficeCode('');
  }, [selectedHubId]);

  const selectedHub = React.useMemo(
    () => hubs.find((hub) => String(hub.id) === selectedHubId),
    [hubs, selectedHubId]
  );

  const {
    data: linkData,
    isFetching: isFetchingLinks,
    refetch: refetchLinks,
  } = useGetHubPostOfficesQuery(
    {
      hubId: selectedHub?.id ?? 0,
      page: linkPage,
      size: LINK_PAGE_SIZE,
    },
    { skip: !selectedHub }
  );

  const {
    data: allLinkData,
    isFetching: isFetchingAllLinks,
    refetch: refetchAllLinks,
  } = useGetAllHubPostOfficesQuery(
    {
      page: 0,
      size: ALL_LINK_PAGE_SIZE,
    },
    { skip: !assignDialogOpen, refetchOnMountOrArgChange: true }
  );

  const { data: assignPostOfficesData, isFetching: isFetchingAssignOptions } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: ASSIGN_POST_OFFICE_SIZE,
        provinceCode: assignProvinceCode || undefined,
        wardCode: assignWardCode || undefined,
      },
      { skip: !assignDialogOpen || !assignProvinceCode }
    );

  const { data: assignProvincesData, isFetching: isFetchingAssignProvinces } =
    useGetProvincesQuery(
      {
        page: 0,
        size: ASSIGN_LOCATION_SIZE,
      },
      { skip: !assignDialogOpen }
    );

  const { data: assignWardsData, isFetching: isFetchingAssignWards } =
    useGetWardsByProvinceCodeQuery(
      {
        provinceCode: assignProvinceCode,
        page: 0,
        size: ASSIGN_LOCATION_SIZE,
      },
      { skip: !assignDialogOpen || !assignProvinceCode }
    );

  const postOfficeByCode = React.useMemo(() => {
    return postOffices.reduce<Record<string, PostOffice>>((acc, postOffice) => {
      acc[postOffice.code] = postOffice;
      return acc;
    }, {});
  }, [postOffices]);

  const linkedRows = React.useMemo<HubPostOfficeLinkRow[]>(() => {
    return (linkData?.items ?? []).map((mapping) => ({
      mapping,
      postOffice: postOfficeByCode[mapping.postOfficeCode],
    }));
  }, [linkData?.items, postOfficeByCode]);

  const linkedCodes = React.useMemo(
    () => new Set(linkedRows.map((row) => row.mapping.postOfficeCode)),
    [linkedRows]
  );

  const assignedCodes = React.useMemo(
    () =>
      new Set(
        (allLinkData?.items ?? []).map((mapping) => mapping.postOfficeCode)
      ),
    [allLinkData?.items]
  );

  React.useEffect(() => {
    setPostOfficeCodesToAssign((currentCodes) =>
      currentCodes.filter((code) => !assignedCodes.has(code))
    );
  }, [assignedCodes]);

  const selectedRow = React.useMemo(
    () =>
      linkedRows.find(
        (row) => row.mapping.postOfficeCode === selectedPostOfficeCode
      ),
    [linkedRows, selectedPostOfficeCode]
  );

  const hubOptions = React.useMemo(
    () =>
      hubs.map((hub) => ({
        value: String(hub.id),
        label: buildHubLabel(hub),
      })),
    [hubs]
  );

  const targetHubOptions = React.useMemo(
    () =>
      hubs
        .filter((hub) => hub.id !== selectedHub?.id)
        .map((hub) => ({
          value: String(hub.id),
          label: buildHubLabel(hub),
        })),
    [hubs, selectedHub?.id]
  );

  const assignPostOffices = React.useMemo(
    () => assignPostOfficesData?.items ?? [],
    [assignPostOfficesData?.items]
  );

  const filteredAssignPostOffices = React.useMemo(() => {
    const keyword = normalizeSearchText(assignPostOfficeKeyword);

    if (!keyword) {
      return assignPostOffices;
    }

    return assignPostOffices.filter((postOffice) => {
      const code = normalizeSearchText(postOffice.code);
      const name = normalizeSearchText(postOffice.name);
      const label = normalizeSearchText(buildPostOfficeLabel(postOffice));

      return (
        code.includes(keyword) ||
        name.includes(keyword) ||
        label.includes(keyword)
      );
    });
  }, [assignPostOfficeKeyword, assignPostOffices]);

  const assignProvinceOptions = React.useMemo(
    () =>
      (assignProvincesData?.items ?? []).flatMap((province: Province) =>
        province.provinceCode
          ? [
              {
                value: province.provinceCode,
                label: `${province.name} (${province.provinceCode})`,
              },
            ]
          : []
      ),
    [assignProvincesData?.items]
  );

  const assignWardOptions = React.useMemo(
    () =>
      (assignWardsData?.items ?? []).flatMap((ward: Ward) =>
        ward.wardCode
          ? [
              {
                value: ward.wardCode,
                label: `${ward.name} (${ward.wardCode})`,
              },
            ]
          : []
      ),
    [assignWardsData?.items]
  );

  const mapPostOffices = React.useMemo(
    () => linkedRows.map(toMapPostOffice),
    [linkedRows]
  );

  const geocodedLinkCount = React.useMemo(
    () =>
      linkedRows.filter((row) => hasPostOfficeLocation(row.postOffice)).length,
    [linkedRows]
  );

  const togglePostOfficeToAssign = React.useCallback(
    (postOfficeCode: string) => {
      if (assignedCodes.has(postOfficeCode)) {
        return;
      }
      setPostOfficeCodesToAssign((currentCodes) =>
        currentCodes.includes(postOfficeCode)
          ? currentCodes.filter((code) => code !== postOfficeCode)
          : [...currentCodes, postOfficeCode]
      );
    },
    [assignedCodes]
  );

  const handleRefresh = () => {
    void refetchHubs();
    void refetchPostOffices();
    if (selectedHub) {
      void refetchLinks();
    }
    if (assignDialogOpen) {
      void refetchAllLinks();
    }
  };

  const [assignPostOfficeToHub, { isLoading: isAssigning }] =
    useAssignPostOfficeToHubMutation();
  const [removePostOfficeFromHub, { isLoading: isRemoving }] =
    useRemovePostOfficeFromHubMutation();

  const openAssignDialog = () => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được tạo liên kết hub-bưu cục.');
      return;
    }

    if (!selectedHub) {
      notification.error('Vui lòng chọn hub trước khi tạo liên kết.');
      return;
    }

    setAssignProvinceCode('');
    setAssignWardCode('');
    setAssignPostOfficeKeyword('');
    setPostOfficeCodesToAssign([]);
    setAssignDialogOpen(true);
  };

  const handleAssignPostOffice = async () => {
    if (!isTmsAdmin || !selectedHub) {
      return;
    }

    const codesToAssign = postOfficeCodesToAssign.filter(
      (code) => !assignedCodes.has(code)
    );

    if (codesToAssign.length === 0) {
      notification.error('Vui lòng chọn ít nhất một bưu cục để liên kết.');
      return;
    }

    try {
      await assignPostOfficeToHub({
        hubId: selectedHub.id,
        request: { post_office_codes: codesToAssign },
      }).unwrap();

      notification.success(
        `Đã tạo ${codesToAssign.length} liên kết hub-bưu cục.`
      );
      setAssignDialogOpen(false);
      setAssignProvinceCode('');
      setAssignWardCode('');
      setAssignPostOfficeKeyword('');
      setPostOfficeCodesToAssign([]);
      setSelectedPostOfficeCode(codesToAssign[0] ?? '');
      void refetchLinks();
      void refetchAllLinks();
    } catch (error) {
      notification.error('Tạo liên kết hub-bưu cục thất bại.', {
        description: getErrorMessage(error),
      });
    }
  };

  const openEditDialog = (row: HubPostOfficeLinkRow) => {
    if (!isTmsAdmin) {
      notification.error('Chỉ TMS_ADMIN được cập nhật liên kết hub-bưu cục.');
      return;
    }

    setEditTarget(row);
    setEditTargetHubId('');
  };

  const handleMoveLink = async () => {
    if (!isTmsAdmin || !selectedHub || !editTarget) {
      return;
    }

    const nextHubId = Number(editTargetHubId);
    if (!Number.isInteger(nextHubId) || nextHubId <= 0) {
      notification.error('Vui lòng chọn hub đích.');
      return;
    }

    let originalLinkRemoved = false;

    try {
      await removePostOfficeFromHub({
        hubId: selectedHub.id,
        postOfficeCode: editTarget.mapping.postOfficeCode,
      }).unwrap();
      originalLinkRemoved = true;

      await assignPostOfficeToHub({
        hubId: nextHubId,
        request: { post_office_codes: [editTarget.mapping.postOfficeCode] },
      }).unwrap();

      notification.success('Đã chuyển liên kết hub-bưu cục.');
      setEditTarget(null);
      setEditTargetHubId('');
      setSelectedPostOfficeCode('');
      void refetchLinks();
    } catch (error) {
      if (originalLinkRemoved) {
        try {
          await assignPostOfficeToHub({
            hubId: selectedHub.id,
            request: {
              post_office_codes: [editTarget.mapping.postOfficeCode],
            },
          }).unwrap();
        } catch {
          notification.error('Khôi phục liên kết ban đầu thất bại.', {
            description:
              'Hub đích từ chối thao tác chuyển và rollback cũng thất bại.',
          });
        }
      }

      notification.error('Chuyển liên kết hub-bưu cục thất bại.', {
        description: getErrorMessage(error),
      });
    }
  };

  const handleRemoveLink = async () => {
    if (!isTmsAdmin || !selectedHub || !deleteTarget) {
      return;
    }

    try {
      await removePostOfficeFromHub({
        hubId: selectedHub.id,
        postOfficeCode: deleteTarget.mapping.postOfficeCode,
      }).unwrap();

      notification.success('Đã xóa liên kết hub-bưu cục.');
      setDeleteTarget(null);
      if (selectedPostOfficeCode === deleteTarget.mapping.postOfficeCode) {
        setSelectedPostOfficeCode('');
      }
      void refetchLinks();
    } catch (error) {
      notification.error('Xóa liên kết hub-bưu cục thất bại.', {
        description: getErrorMessage(error),
      });
    }
  };

  const isPageLoading =
    isFetchingHubs || isFetchingPostOffices || isFetchingLinks;
  const totalLinkedItems = linkData?.totalItems ?? linkedRows.length;

  return (
    <>
      <div className='space-y-6'>
        <div className='flex flex-col gap-3 xl:flex-row xl:items-center xl:justify-between'>
          <div className='space-y-2'>
            <h1 className='text-2xl font-bold tracking-tight'>
              Liên kết hub-bưu cục
            </h1>
            <p className='text-muted-foreground'>
              Quản lý bưu cục được gán cho hub chặng giữa trên cùng một màn hình
              mạng lưới.
            </p>
          </div>

          <div className='flex flex-wrap items-center gap-2'>
            {!isTmsAdmin ? (
              <Badge variant='outline' className='gap-1'>
                <ShieldAlert className='h-3.5 w-3.5' />
                Chỉ xem (thao tác ghi yêu cầu TMS_ADMIN)
              </Badge>
            ) : null}
            <Button variant='outline' onClick={handleRefresh}>
              <RefreshCw className='h-4 w-4' />
              Làm mới
            </Button>
            <Button onClick={openAssignDialog} disabled={!selectedHub}>
              <Plus className='h-4 w-4' />
              Tạo liên kết
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Phạm vi mạng lưới</CardTitle>
            <CardDescription>
              Chọn hub để xem, tạo, chuyển hoặc xóa các bưu cục liên kết.
            </CardDescription>
          </CardHeader>
          <CardContent className='space-y-4'>
            <div className='grid gap-4 lg:grid-cols-[minmax(0,1fr)_auto_auto] lg:items-end'>
              <div className='space-y-2'>
                <Label htmlFor='hub-po-link-hub'>Hub</Label>
                <TmsCombobox
                  id='hub-po-link-hub'
                  value={selectedHubId}
                  onValueChange={setSelectedHubId}
                  options={hubOptions}
                  placeholder={isFetchingHubs ? 'Đang tải hub...' : 'Chọn hub'}
                  emptyText='Không tìm thấy hub'
                  loading={isFetchingHubs}
                />
              </div>

              <Button variant='outline' asChild>
                <Link href='/first-mile/network/hub'>
                  <Building2 className='h-4 w-4' />
                  Hub
                </Link>
              </Button>
              <Button variant='outline' asChild>
                <Link href='/first-mile/network/post-office'>
                  <MapPin className='h-4 w-4' />
                  Bưu cục
                </Link>
              </Button>
            </div>

            <div className='grid gap-3 md:grid-cols-3'>
              <div className='rounded-md border p-3'>
                <p className='text-sm text-muted-foreground'>Hub đã chọn</p>
                <p className='mt-1 truncate font-medium'>
                  {selectedHub ? buildHubLabel(selectedHub) : '--'}
                </p>
              </div>
              <div className='rounded-md border p-3'>
                <p className='text-sm text-muted-foreground'>
                  Bưu cục liên kết
                </p>
                <p className='mt-1 font-medium'>{totalLinkedItems}</p>
              </div>
              <div className='rounded-md border p-3'>
                <p className='text-sm text-muted-foreground'>
                  Liên kết có tọa độ
                </p>
                <p className='mt-1 font-medium'>{geocodedLinkCount}</p>
              </div>
            </div>
          </CardContent>
        </Card>

        <div className='grid gap-4 xl:grid-cols-[minmax(0,1.55fr)_minmax(340px,0.85fr)]'>
          <HubPostOfficeLinkMap
            hub={toMapHub(selectedHub)}
            postOffices={mapPostOffices}
            selectedPostOfficeCode={selectedPostOfficeCode}
            loading={isPageLoading}
            onPostOfficeClick={setSelectedPostOfficeCode}
          />

          <Card>
            <CardHeader>
              <CardTitle>Danh sách bưu cục liên kết</CardTitle>
              <CardDescription>
                Xem, chuyển hoặc xóa các bưu cục đang kết nối với hub đã chọn.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div>
                <div className='mb-3 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between'>
                  <div>
                    <p className='font-medium'>Bưu cục của hub</p>
                    <p className='text-sm text-muted-foreground'>
                      Chọn một dòng để đánh dấu trên bản đồ, hoặc dùng nút thao
                      tác.
                    </p>
                  </div>
                  {isFetchingLinks ? (
                    <Badge variant='outline' className='w-fit gap-1'>
                      <Loader2 className='h-3.5 w-3.5 animate-spin' />
                      Đang tải
                    </Badge>
                  ) : null}
                </div>

                {!selectedHub ? (
                  <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
                    Chọn hub để tải danh sách bưu cục liên kết.
                  </div>
                ) : linkedRows.length === 0 && !isFetchingLinks ? (
                  <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
                    Hub này chưa có bưu cục liên kết.
                  </div>
                ) : (
                  <div className='max-h-[32rem] space-y-2 overflow-y-auto pr-1'>
                    {linkedRows.map((row) => {
                      const isSelected =
                        row.mapping.postOfficeCode === selectedPostOfficeCode;

                      return (
                        <div
                          key={`${row.mapping.id}-${row.mapping.postOfficeCode}`}
                          role='button'
                          tabIndex={0}
                          className={`w-full rounded-md border p-3 text-left transition-colors hover:bg-accent ${
                            isSelected ? 'border-primary bg-accent' : ''
                          }`}
                          onClick={() =>
                            setSelectedPostOfficeCode(
                              row.mapping.postOfficeCode
                            )
                          }
                          onKeyDown={(event) => {
                            if (event.key === 'Enter' || event.key === ' ') {
                              event.preventDefault();
                              setSelectedPostOfficeCode(
                                row.mapping.postOfficeCode
                              );
                            }
                          }}
                        >
                          <div className='flex flex-col gap-3'>
                            <div className='min-w-0 space-y-1'>
                              <div className='flex flex-wrap items-center gap-2'>
                                <p className='truncate font-medium'>
                                  {row.postOffice?.name ||
                                    row.mapping.postOfficeCode}
                                </p>
                                <Badge variant='outline' className='font-mono'>
                                  {row.mapping.postOfficeCode}
                                </Badge>
                              </div>
                              <p className='line-clamp-2 text-sm text-muted-foreground'>
                                {row.postOffice?.addressDetail ||
                                  'Chi tiết bưu cục chưa được tải trong vùng tra cứu hiện tại.'}
                              </p>
                            </div>

                            <div className='flex flex-wrap gap-2'>
                              <Button
                                type='button'
                                variant='outline'
                                size='icon'
                                aria-label='Xem chi tiết liên kết'
                                title='Xem chi tiết'
                                onClick={(event) => {
                                  event.stopPropagation();
                                  setSelectedPostOfficeCode(
                                    row.mapping.postOfficeCode
                                  );
                                  setDetailDialogOpen(true);
                                }}
                              >
                                <Eye className='h-4 w-4' />
                              </Button>
                              <Button
                                type='button'
                                variant='outline'
                                size='icon'
                                aria-label='Chuyển liên kết'
                                title='Chuyển liên kết'
                                disabled={!isTmsAdmin}
                                onClick={(event) => {
                                  event.stopPropagation();
                                  openEditDialog(row);
                                }}
                              >
                                <ArrowRightLeft className='h-4 w-4' />
                              </Button>
                              <Button
                                type='button'
                                variant='destructive'
                                size='icon'
                                aria-label='Xóa liên kết'
                                title='Xóa liên kết'
                                disabled={!isTmsAdmin}
                                onClick={(event) => {
                                  event.stopPropagation();
                                  setDeleteTarget(row);
                                }}
                              >
                                <Trash2 className='h-4 w-4' />
                              </Button>
                            </div>
                          </div>
                        </div>
                      );
                    })}
                  </div>
                )}

                {(linkData?.hasNext || linkData?.hasPrevious) && (
                  <div className='mt-4 flex items-center justify-between border-t pt-4'>
                    <div className='text-sm text-muted-foreground'>
                      Trang {linkPage + 1}
                    </div>
                    <div className='flex gap-2'>
                      <Button
                        variant='outline'
                        size='sm'
                        disabled={!linkData?.hasPrevious || isFetchingLinks}
                        onClick={() =>
                          setLinkPage((prev) => Math.max(0, prev - 1))
                        }
                      >
                        Trước
                      </Button>
                      <Button
                        variant='outline'
                        size='sm'
                        disabled={!linkData?.hasNext || isFetchingLinks}
                        onClick={() => setLinkPage((prev) => prev + 1)}
                      >
                        Sau
                      </Button>
                    </div>
                  </div>
                )}
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <Dialog
        open={assignDialogOpen}
        onOpenChange={(open) => {
          setAssignDialogOpen(open);
          if (!open) {
            setAssignProvinceCode('');
            setAssignWardCode('');
            setAssignPostOfficeKeyword('');
            setPostOfficeCodesToAssign([]);
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Tạo liên kết hub-bưu cục</DialogTitle>
            <DialogDescription>
              Liên kết bưu cục với {selectedHub?.name || 'hub đã chọn'}.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='grid gap-4 sm:grid-cols-2'>
              <div className='space-y-2'>
                <Label htmlFor='hub-po-assign-province'>Tỉnh/Thành phố</Label>
                <TmsCombobox
                  id='hub-po-assign-province'
                  value={assignProvinceCode}
                  onValueChange={(value) => {
                    setAssignProvinceCode(value);
                    setAssignWardCode('');
                    setAssignPostOfficeKeyword('');
                    setPostOfficeCodesToAssign([]);
                  }}
                  options={assignProvinceOptions}
                  placeholder={
                    isFetchingAssignProvinces
                      ? 'Đang tải tỉnh/thành...'
                      : 'Chọn tỉnh/thành phố'
                  }
                  emptyText='Không tìm thấy tỉnh/thành phố'
                  loading={isFetchingAssignProvinces}
                />
              </div>

              <div className='space-y-2'>
                <Label htmlFor='hub-po-assign-ward'>Phường/Xã</Label>
                <TmsCombobox
                  id='hub-po-assign-ward'
                  value={assignWardCode}
                  onValueChange={(value) => {
                    setAssignWardCode(value);
                    setAssignPostOfficeKeyword('');
                    setPostOfficeCodesToAssign([]);
                  }}
                  options={assignWardOptions}
                  placeholder={
                    !assignProvinceCode
                      ? 'Chọn tỉnh/thành trước'
                      : isFetchingAssignWards
                        ? 'Đang tải phường/xã...'
                        : 'Tất cả phường/xã'
                  }
                  emptyText='Không tìm thấy phường/xã'
                  loading={isFetchingAssignWards}
                  disabled={!assignProvinceCode}
                  clearable
                  clearText='Tất cả phường/xã'
                />
              </div>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-po-assign-post-office-search'>Bưu cục</Label>
              <Input
                id='hub-po-assign-post-office-search'
                value={assignPostOfficeKeyword}
                onChange={(event) =>
                  setAssignPostOfficeKeyword(event.target.value)
                }
                placeholder='Tìm theo mã, tên hoặc mã - tên bưu cục'
                disabled={!assignProvinceCode}
              />
              <div
                id='hub-po-assign-post-office'
                className='max-h-72 space-y-2 overflow-y-auto rounded-md border p-2'
              >
                {!assignProvinceCode ? (
                  <div className='p-4 text-center text-sm text-muted-foreground'>
                    Chọn tỉnh/thành để tải bưu cục.
                  </div>
                ) : isFetchingAssignOptions || isFetchingAllLinks ? (
                  <div className='flex items-center justify-center gap-2 p-4 text-sm text-muted-foreground'>
                    <Loader2 className='h-4 w-4 animate-spin' />
                    Đang tải bưu cục...
                  </div>
                ) : assignPostOffices.length === 0 ? (
                  <div className='p-4 text-center text-sm text-muted-foreground'>
                    Không tìm thấy bưu cục trong khu vực đã chọn.
                  </div>
                ) : filteredAssignPostOffices.length === 0 ? (
                  <div className='p-4 text-center text-sm text-muted-foreground'>
                    Không tìm thấy bưu cục khớp với từ khóa.
                  </div>
                ) : (
                  filteredAssignPostOffices.map((postOffice) => {
                    const isLinked = assignedCodes.has(postOffice.code);
                    const isChecked = postOfficeCodesToAssign.includes(
                      postOffice.code
                    );

                    return (
                      <div
                        key={postOffice.code}
                        role='button'
                        tabIndex={isLinked ? -1 : 0}
                        aria-disabled={isLinked}
                        className={`flex w-full items-start gap-3 rounded-md border p-3 text-left transition-colors ${
                          isLinked
                            ? 'cursor-not-allowed bg-muted/60 opacity-55'
                            : 'hover:bg-accent'
                        }`}
                        onClick={() => {
                          if (!isLinked) {
                            togglePostOfficeToAssign(postOffice.code);
                          }
                        }}
                        onKeyDown={(event) => {
                          if (
                            !isLinked &&
                            (event.key === 'Enter' || event.key === ' ')
                          ) {
                            event.preventDefault();
                            togglePostOfficeToAssign(postOffice.code);
                          }
                        }}
                      >
                        <Checkbox
                          checked={isLinked || isChecked}
                          disabled={isLinked}
                          onCheckedChange={() =>
                            togglePostOfficeToAssign(postOffice.code)
                          }
                          onClick={(event) => event.stopPropagation()}
                          className='mt-0.5'
                        />
                        <div className='min-w-0 flex-1 space-y-1'>
                          <div className='flex flex-wrap items-center gap-2'>
                            <span className='font-medium'>
                              {buildPostOfficeLabel(postOffice)}
                            </span>
                            {isLinked ? (
                              <Badge variant='outline'>Đã liên kết</Badge>
                            ) : null}
                          </div>
                          <p className='line-clamp-2 text-sm text-muted-foreground'>
                            {postOffice.addressDetail ||
                              'Chưa có địa chỉ chi tiết'}
                          </p>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
              <p className='text-xs text-muted-foreground'>
                Đã chọn {postOfficeCodesToAssign.length} bưu cục để liên kết.
              </p>
            </div>

            <div className='flex justify-end gap-2 border-t pt-4'>
              <Button
                variant='outline'
                onClick={() => setAssignDialogOpen(false)}
              >
                Hủy
              </Button>
              <Button
                onClick={() => void handleAssignPostOffice()}
                disabled={postOfficeCodesToAssign.length === 0 || isAssigning}
              >
                {isAssigning
                  ? 'Đang tạo...'
                  : `Tạo ${postOfficeCodesToAssign.length || ''} liên kết`}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog
        open={Boolean(editTarget)}
        onOpenChange={(open) => {
          if (!open && !isRemoving && !isAssigning) {
            setEditTarget(null);
            setEditTargetHubId('');
          }
        }}
      >
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Chuyển liên kết hub-bưu cục</DialogTitle>
            <DialogDescription>
              Chuyển bưu cục này từ hub hiện tại sang hub khác.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='rounded-md border p-3 text-sm'>
              <p className='text-muted-foreground'>Bưu cục</p>
              <p className='mt-1 font-medium'>
                {editTarget?.postOffice
                  ? buildPostOfficeLabel(editTarget.postOffice)
                  : editTarget?.mapping.postOfficeCode || '--'}
              </p>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-po-move-target-hub'>Hub đích</Label>
              <TmsCombobox
                id='hub-po-move-target-hub'
                value={editTargetHubId}
                onValueChange={setEditTargetHubId}
                options={targetHubOptions}
                placeholder='Chọn hub đích'
                emptyText='Không tìm thấy hub khác'
              />
            </div>

            <div className='flex justify-end gap-2 border-t pt-4'>
              <Button variant='outline' onClick={() => setEditTarget(null)}>
                Hủy
              </Button>
              <Button
                onClick={() => void handleMoveLink()}
                disabled={!editTargetHubId || isRemoving || isAssigning}
              >
                {isRemoving || isAssigning
                  ? 'Đang chuyển...'
                  : 'Chuyển liên kết'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={detailDialogOpen} onOpenChange={setDetailDialogOpen}>
        <DialogContent className='max-w-2xl'>
          <DialogHeader>
            <DialogTitle>Chi tiết liên kết hub-bưu cục</DialogTitle>
            <DialogDescription>
              Thông tin chỉ đọc của liên kết hub-bưu cục đã chọn.
            </DialogDescription>
          </DialogHeader>
          {selectedRow ? (
            <div className='grid gap-4 text-sm sm:grid-cols-2'>
              <div>
                <p className='text-muted-foreground'>Hub</p>
                <p className='font-medium'>
                  {selectedHub ? buildHubLabel(selectedHub) : '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Bưu cục</p>
                <p className='font-medium'>
                  {selectedRow.postOffice
                    ? buildPostOfficeLabel(selectedRow.postOffice)
                    : selectedRow.mapping.postOfficeCode}
                </p>
              </div>
              <div className='sm:col-span-2'>
                <p className='text-muted-foreground'>Địa chỉ</p>
                <p className='font-medium'>
                  {selectedRow.postOffice?.addressDetail || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Trạng thái bưu cục</p>
                <p className='font-medium'>
                  {formatStatusLabel(selectedRow.postOffice?.status)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Ngày tạo</p>
                <p className='font-medium'>
                  {formatDateTime(selectedRow.mapping.createdAt)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Tọa độ</p>
                <p className='font-medium'>
                  {hasPostOfficeLocation(selectedRow.postOffice)
                    ? `${selectedRow.postOffice?.latitude}, ${selectedRow.postOffice?.longitude}`
                    : '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>ID liên kết</p>
                <p className='font-medium'>{selectedRow.mapping.id || '--'}</p>
              </div>
            </div>
          ) : null}
        </DialogContent>
      </Dialog>

      <ConfirmDialog
        open={Boolean(deleteTarget)}
        onOpenChange={(open) => {
          if (!open && !isRemoving) {
            setDeleteTarget(null);
          }
        }}
        title='Xóa liên kết hub-bưu cục'
        description={
          deleteTarget
            ? `Thao tác này sẽ xóa ${deleteTarget.mapping.postOfficeCode} khỏi ${selectedHub?.code || 'hub đã chọn'}.`
            : 'Không thể hoàn tác thao tác này.'
        }
        confirmText='Xóa'
        cancelText='Hủy'
        onConfirm={handleRemoveLink}
        isLoading={isRemoving}
        variant='destructive'
      />
    </>
  );
}
