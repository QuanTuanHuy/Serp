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
  Pencil,
  Plus,
  RefreshCw,
  ShieldAlert,
  Trash2,
  Unlink,
} from 'lucide-react';
import { TmsCombobox } from '../../../components';
import {
  useAssignPostOfficeToHubMutation,
  useGetHubPostOfficesQuery,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useRemovePostOfficeFromHubMutation,
} from '../../../api';
import type { Hub, HubPostOfficeMapping, PostOffice } from '../../../types';
import {
  HubPostOfficeLinkMap,
  type HubPostOfficeMapHub,
  type HubPostOfficeMapPoint,
} from './components/HubPostOfficeLinkMap';

const HUB_PAGE_SIZE = 300;
const POST_OFFICE_LOOKUP_SIZE = 500;
const LINK_PAGE_SIZE = 500;
const ASSIGN_SEARCH_SIZE = 50;

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
  const [assignSearchKeyword, setAssignSearchKeyword] = React.useState('');
  const [postOfficeCodeToAssign, setPostOfficeCodeToAssign] =
    React.useState('');
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

  const { data: assignPostOfficesData, isFetching: isFetchingAssignOptions } =
    useGetPostOfficesQuery(
      {
        page: 0,
        size: ASSIGN_SEARCH_SIZE,
        keyword: assignSearchKeyword.trim() || undefined,
      },
      { skip: !assignDialogOpen }
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

  const assignPostOfficeOptions = React.useMemo(() => {
    return (assignPostOfficesData?.items ?? [])
      .filter((postOffice) => !linkedCodes.has(postOffice.code))
      .map((postOffice) => ({
        value: postOffice.code,
        label: buildPostOfficeLabel(postOffice),
      }));
  }, [assignPostOfficesData?.items, linkedCodes]);

  const mapPostOffices = React.useMemo(
    () => linkedRows.map(toMapPostOffice),
    [linkedRows]
  );

  const geocodedLinkCount = React.useMemo(
    () =>
      linkedRows.filter((row) => hasPostOfficeLocation(row.postOffice)).length,
    [linkedRows]
  );

  const handleRefresh = () => {
    void refetchHubs();
    void refetchPostOffices();
    if (selectedHub) {
      void refetchLinks();
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

    setAssignSearchKeyword('');
    setPostOfficeCodeToAssign('');
    setAssignDialogOpen(true);
  };

  const handleAssignPostOffice = async () => {
    if (!isTmsAdmin || !selectedHub) {
      return;
    }

    if (!postOfficeCodeToAssign) {
      notification.error('Vui lòng chọn bưu cục để liên kết.');
      return;
    }

    if (linkedCodes.has(postOfficeCodeToAssign)) {
      notification.error('Bưu cục này đã được liên kết với hub.');
      return;
    }

    try {
      await assignPostOfficeToHub({
        hubId: selectedHub.id,
        request: { post_office_code: postOfficeCodeToAssign },
      }).unwrap();

      notification.success('Đã tạo liên kết hub-bưu cục.');
      setAssignDialogOpen(false);
      setAssignSearchKeyword('');
      setPostOfficeCodeToAssign('');
      setSelectedPostOfficeCode(postOfficeCodeToAssign);
      void refetchLinks();
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
        request: { post_office_code: editTarget.mapping.postOfficeCode },
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
            request: { post_office_code: editTarget.mapping.postOfficeCode },
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
              Quản lý bưu cục được gán cho hub chặng giữa trên cùng một màn
              hình mạng lưới.
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
                  placeholder={
                    isFetchingHubs ? 'Đang tải hub...' : 'Chọn hub'
                  }
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
              <CardTitle>Liên kết đã chọn</CardTitle>
              <CardDescription>
                Chọn điểm đánh dấu trên bản đồ hoặc một dòng để xem chi tiết
                liên kết.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              {selectedRow ? (
                <>
                  <div className='space-y-3 text-sm'>
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
                    <div>
                      <p className='text-muted-foreground'>Ngày tạo</p>
                      <p className='font-medium'>
                        {formatDateTime(selectedRow.mapping.createdAt)}
                      </p>
                    </div>
                    <Badge
                      variant={
                        hasPostOfficeLocation(selectedRow.postOffice)
                          ? 'default'
                          : 'outline'
                      }
                    >
                      {hasPostOfficeLocation(selectedRow.postOffice)
                        ? 'Đủ tọa độ'
                        : 'Thiếu tọa độ'}
                    </Badge>
                  </div>

                  <div className='flex flex-wrap gap-2 border-t pt-4'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setDetailDialogOpen(true)}
                    >
                      <Eye className='h-4 w-4' />
                      Xem
                    </Button>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => openEditDialog(selectedRow)}
                      disabled={!isTmsAdmin}
                    >
                      <Pencil className='h-4 w-4' />
                      Chuyển
                    </Button>
                    <Button
                      variant='destructive'
                      size='sm'
                      onClick={() => setDeleteTarget(selectedRow)}
                      disabled={!isTmsAdmin}
                    >
                      <Trash2 className='h-4 w-4' />
                      Xóa
                    </Button>
                  </div>
                </>
              ) : (
                <div className='flex min-h-64 flex-col items-center justify-center gap-3 rounded-md border border-dashed p-6 text-center text-muted-foreground'>
                  <Unlink className='h-9 w-9' />
                  <div>
                    <p className='font-medium text-foreground'>
                      Chưa chọn liên kết
                    </p>
                    <p className='mt-1 text-sm'>
                      Chọn bưu cục liên kết từ bản đồ hoặc danh sách.
                    </p>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </div>

        <Card>
          <CardHeader>
            <div className='flex flex-col gap-3 md:flex-row md:items-center md:justify-between'>
              <div>
                <CardTitle>Bưu cục đã liên kết</CardTitle>
                <CardDescription>
                  Xem, chuyển hoặc xóa các bưu cục đang kết nối với hub đã
                  chọn.
                </CardDescription>
              </div>
              {isFetchingLinks ? (
                <Badge variant='outline' className='gap-1'>
                  <Loader2 className='h-3.5 w-3.5 animate-spin' />
                  Đang tải
                </Badge>
              ) : null}
            </div>
          </CardHeader>
          <CardContent className='space-y-4'>
            {!selectedHub ? (
              <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
                Chọn hub để tải danh sách bưu cục liên kết.
              </div>
            ) : linkedRows.length === 0 && !isFetchingLinks ? (
              <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
                Hub này chưa có bưu cục liên kết.
              </div>
            ) : (
              <div className='space-y-2'>
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
                        setSelectedPostOfficeCode(row.mapping.postOfficeCode)
                      }
                      onKeyDown={(event) => {
                        if (event.key === 'Enter' || event.key === ' ') {
                          event.preventDefault();
                          setSelectedPostOfficeCode(row.mapping.postOfficeCode);
                        }
                      }}
                    >
                      <div className='flex flex-col gap-3 lg:flex-row lg:items-center lg:justify-between'>
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

                        <div className='flex flex-wrap gap-2 lg:justify-end'>
                          <Button
                            type='button'
                            variant='outline'
                            size='sm'
                            onClick={(event) => {
                              event.stopPropagation();
                              setSelectedPostOfficeCode(
                                row.mapping.postOfficeCode
                              );
                              setDetailDialogOpen(true);
                            }}
                          >
                            <Eye className='h-4 w-4' />
                            Xem
                          </Button>
                          <Button
                            type='button'
                            variant='outline'
                            size='sm'
                            disabled={!isTmsAdmin}
                            onClick={(event) => {
                              event.stopPropagation();
                              openEditDialog(row);
                            }}
                          >
                            <ArrowRightLeft className='h-4 w-4' />
                            Chuyển
                          </Button>
                          <Button
                            type='button'
                            variant='destructive'
                            size='sm'
                            disabled={!isTmsAdmin}
                            onClick={(event) => {
                              event.stopPropagation();
                              setDeleteTarget(row);
                            }}
                          >
                            <Trash2 className='h-4 w-4' />
                            Xóa
                          </Button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {(linkData?.hasNext || linkData?.hasPrevious) && (
              <div className='flex items-center justify-between border-t pt-4'>
                <div className='text-sm text-muted-foreground'>
                  Trang {linkPage + 1}
                </div>
                <div className='flex gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!linkData?.hasPrevious || isFetchingLinks}
                    onClick={() => setLinkPage((prev) => Math.max(0, prev - 1))}
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
          </CardContent>
        </Card>
      </div>

      <Dialog
        open={assignDialogOpen}
        onOpenChange={(open) => {
          setAssignDialogOpen(open);
          if (!open) {
            setAssignSearchKeyword('');
            setPostOfficeCodeToAssign('');
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
            <div className='space-y-2'>
              <Label htmlFor='hub-po-assign-search'>Tìm bưu cục</Label>
              <Input
                id='hub-po-assign-search'
                placeholder='Mã, tên hoặc địa chỉ'
                value={assignSearchKeyword}
                onChange={(event) => {
                  setAssignSearchKeyword(event.target.value);
                  setPostOfficeCodeToAssign('');
                }}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-po-assign-post-office'>Bưu cục</Label>
              <TmsCombobox
                id='hub-po-assign-post-office'
                value={postOfficeCodeToAssign}
                onValueChange={setPostOfficeCodeToAssign}
                options={assignPostOfficeOptions}
                placeholder={
                  isFetchingAssignOptions
                    ? 'Đang tải bưu cục...'
                    : 'Chọn bưu cục'
                }
                emptyText='Không tìm thấy bưu cục chưa liên kết'
                loading={isFetchingAssignOptions}
              />
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
                disabled={!postOfficeCodeToAssign || isAssigning}
              >
                {isAssigning ? 'Đang tạo...' : 'Tạo liên kết'}
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
