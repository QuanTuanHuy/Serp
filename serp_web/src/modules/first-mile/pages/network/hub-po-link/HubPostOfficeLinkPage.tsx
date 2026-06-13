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

  return new Date(value).toLocaleString('en-US');
}

function buildHubLabel(hub: Hub): string {
  return `${hub.code} - ${hub.name}`;
}

function buildPostOfficeLabel(postOffice: PostOffice): string {
  return `${postOffice.code} - ${postOffice.name}`;
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
    status: hub.status,
  };
}

function toMapPostOffice(row: HubPostOfficeLinkRow): HubPostOfficeMapPoint {
  return {
    code: row.mapping.postOfficeCode,
    name: row.postOffice?.name || row.mapping.postOfficeCode,
    address: row.postOffice?.addressDetail,
    latitude: row.postOffice?.latitude,
    longitude: row.postOffice?.longitude,
    status: row.postOffice?.status,
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
      notification.error('Only TMS_ADMIN can create hub post office links.');
      return;
    }

    if (!selectedHub) {
      notification.error('Select a hub before creating a link.');
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
      notification.error('Select a post office to link.');
      return;
    }

    if (linkedCodes.has(postOfficeCodeToAssign)) {
      notification.error('This post office is already linked to the hub.');
      return;
    }

    try {
      await assignPostOfficeToHub({
        hubId: selectedHub.id,
        request: { post_office_code: postOfficeCodeToAssign },
      }).unwrap();

      notification.success('Hub post office link created successfully.');
      setAssignDialogOpen(false);
      setAssignSearchKeyword('');
      setPostOfficeCodeToAssign('');
      setSelectedPostOfficeCode(postOfficeCodeToAssign);
      void refetchLinks();
    } catch (error) {
      notification.error('Failed to create hub post office link.', {
        description: getErrorMessage(error),
      });
    }
  };

  const openEditDialog = (row: HubPostOfficeLinkRow) => {
    if (!isTmsAdmin) {
      notification.error('Only TMS_ADMIN can update hub post office links.');
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
      notification.error('Select the destination hub.');
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

      notification.success('Hub post office link moved successfully.');
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
          notification.error('Failed to restore the original link.', {
            description:
              'The destination hub rejected the move and the rollback also failed.',
          });
        }
      }

      notification.error('Failed to move hub post office link.', {
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

      notification.success('Hub post office link removed successfully.');
      setDeleteTarget(null);
      if (selectedPostOfficeCode === deleteTarget.mapping.postOfficeCode) {
        setSelectedPostOfficeCode('');
      }
      void refetchLinks();
    } catch (error) {
      notification.error('Failed to remove hub post office link.', {
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
            <h1 className='text-2xl font-bold tracking-tight'>Hub-PO Links</h1>
            <p className='text-muted-foreground'>
              Manage post office assignments to second-mile hubs from one
              network view.
            </p>
          </div>

          <div className='flex flex-wrap items-center gap-2'>
            {!isTmsAdmin ? (
              <Badge variant='outline' className='gap-1'>
                <ShieldAlert className='h-3.5 w-3.5' />
                View only (write actions require TMS_ADMIN)
              </Badge>
            ) : null}
            <Button variant='outline' onClick={handleRefresh}>
              <RefreshCw className='h-4 w-4' />
              Refresh
            </Button>
            <Button onClick={openAssignDialog} disabled={!selectedHub}>
              <Plus className='h-4 w-4' />
              New Link
            </Button>
          </div>
        </div>

        <Card>
          <CardHeader>
            <CardTitle>Network scope</CardTitle>
            <CardDescription>
              Select a hub to inspect, create, move, or remove linked post
              offices.
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
                    isFetchingHubs ? 'Loading hubs...' : 'Select hub'
                  }
                  emptyText='No hubs found'
                  loading={isFetchingHubs}
                />
              </div>

              <Button variant='outline' asChild>
                <Link href='/first-mile/network/hub'>
                  <Building2 className='h-4 w-4' />
                  Hubs
                </Link>
              </Button>
              <Button variant='outline' asChild>
                <Link href='/first-mile/network/post-office'>
                  <MapPin className='h-4 w-4' />
                  Post Offices
                </Link>
              </Button>
            </div>

            <div className='grid gap-3 md:grid-cols-3'>
              <div className='rounded-md border p-3'>
                <p className='text-sm text-muted-foreground'>Selected hub</p>
                <p className='mt-1 truncate font-medium'>
                  {selectedHub ? buildHubLabel(selectedHub) : '--'}
                </p>
              </div>
              <div className='rounded-md border p-3'>
                <p className='text-sm text-muted-foreground'>Linked POs</p>
                <p className='mt-1 font-medium'>{totalLinkedItems}</p>
              </div>
              <div className='rounded-md border p-3'>
                <p className='text-sm text-muted-foreground'>Geocoded links</p>
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
              <CardTitle>Selected link</CardTitle>
              <CardDescription>
                Click a map marker or row to inspect a specific assignment.
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
                      <p className='text-muted-foreground'>Post office</p>
                      <p className='font-medium'>
                        {selectedRow.postOffice
                          ? buildPostOfficeLabel(selectedRow.postOffice)
                          : selectedRow.mapping.postOfficeCode}
                      </p>
                    </div>
                    <div>
                      <p className='text-muted-foreground'>Created</p>
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
                        ? 'Map ready'
                        : 'Missing coordinates'}
                    </Badge>
                  </div>

                  <div className='flex flex-wrap gap-2 border-t pt-4'>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => setDetailDialogOpen(true)}
                    >
                      <Eye className='h-4 w-4' />
                      View
                    </Button>
                    <Button
                      variant='outline'
                      size='sm'
                      onClick={() => openEditDialog(selectedRow)}
                      disabled={!isTmsAdmin}
                    >
                      <Pencil className='h-4 w-4' />
                      Move
                    </Button>
                    <Button
                      variant='destructive'
                      size='sm'
                      onClick={() => setDeleteTarget(selectedRow)}
                      disabled={!isTmsAdmin}
                    >
                      <Trash2 className='h-4 w-4' />
                      Remove
                    </Button>
                  </div>
                </>
              ) : (
                <div className='flex min-h-64 flex-col items-center justify-center gap-3 rounded-md border border-dashed p-6 text-center text-muted-foreground'>
                  <Unlink className='h-9 w-9' />
                  <div>
                    <p className='font-medium text-foreground'>
                      No link selected
                    </p>
                    <p className='mt-1 text-sm'>
                      Select a linked post office from the map or list.
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
                <CardTitle>Linked post offices</CardTitle>
                <CardDescription>
                  View, move, or remove post offices connected to the selected
                  hub.
                </CardDescription>
              </div>
              {isFetchingLinks ? (
                <Badge variant='outline' className='gap-1'>
                  <Loader2 className='h-3.5 w-3.5 animate-spin' />
                  Loading
                </Badge>
              ) : null}
            </div>
          </CardHeader>
          <CardContent className='space-y-4'>
            {!selectedHub ? (
              <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
                Select a hub to load post office links.
              </div>
            ) : linkedRows.length === 0 && !isFetchingLinks ? (
              <div className='rounded-md border border-dashed p-6 text-center text-sm text-muted-foreground'>
                No post offices are linked to this hub yet.
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
                              'Post office details are not loaded in the current lookup window.'}
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
                            View
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
                            Move
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
                            Remove
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
                  Page {linkPage + 1}
                </div>
                <div className='flex gap-2'>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!linkData?.hasPrevious || isFetchingLinks}
                    onClick={() => setLinkPage((prev) => Math.max(0, prev - 1))}
                  >
                    Previous
                  </Button>
                  <Button
                    variant='outline'
                    size='sm'
                    disabled={!linkData?.hasNext || isFetchingLinks}
                    onClick={() => setLinkPage((prev) => prev + 1)}
                  >
                    Next
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
            <DialogTitle>Create hub post office link</DialogTitle>
            <DialogDescription>
              Link a post office to {selectedHub?.name || 'the selected hub'}.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='space-y-2'>
              <Label htmlFor='hub-po-assign-search'>Search post office</Label>
              <Input
                id='hub-po-assign-search'
                placeholder='Code, name, or address'
                value={assignSearchKeyword}
                onChange={(event) => {
                  setAssignSearchKeyword(event.target.value);
                  setPostOfficeCodeToAssign('');
                }}
              />
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-po-assign-post-office'>Post office</Label>
              <TmsCombobox
                id='hub-po-assign-post-office'
                value={postOfficeCodeToAssign}
                onValueChange={setPostOfficeCodeToAssign}
                options={assignPostOfficeOptions}
                placeholder={
                  isFetchingAssignOptions
                    ? 'Loading post offices...'
                    : 'Select post office'
                }
                emptyText='No unlinked post offices found'
                loading={isFetchingAssignOptions}
              />
            </div>

            <div className='flex justify-end gap-2 border-t pt-4'>
              <Button
                variant='outline'
                onClick={() => setAssignDialogOpen(false)}
              >
                Cancel
              </Button>
              <Button
                onClick={() => void handleAssignPostOffice()}
                disabled={!postOfficeCodeToAssign || isAssigning}
              >
                {isAssigning ? 'Creating...' : 'Create Link'}
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
            <DialogTitle>Move hub post office link</DialogTitle>
            <DialogDescription>
              Move this post office from the current hub to another hub.
            </DialogDescription>
          </DialogHeader>
          <div className='space-y-4'>
            <div className='rounded-md border p-3 text-sm'>
              <p className='text-muted-foreground'>Post office</p>
              <p className='mt-1 font-medium'>
                {editTarget?.postOffice
                  ? buildPostOfficeLabel(editTarget.postOffice)
                  : editTarget?.mapping.postOfficeCode || '--'}
              </p>
            </div>

            <div className='space-y-2'>
              <Label htmlFor='hub-po-move-target-hub'>Destination hub</Label>
              <TmsCombobox
                id='hub-po-move-target-hub'
                value={editTargetHubId}
                onValueChange={setEditTargetHubId}
                options={targetHubOptions}
                placeholder='Select destination hub'
                emptyText='No other hubs found'
              />
            </div>

            <div className='flex justify-end gap-2 border-t pt-4'>
              <Button variant='outline' onClick={() => setEditTarget(null)}>
                Cancel
              </Button>
              <Button
                onClick={() => void handleMoveLink()}
                disabled={!editTargetHubId || isRemoving || isAssigning}
              >
                {isRemoving || isAssigning ? 'Moving...' : 'Move Link'}
              </Button>
            </div>
          </div>
        </DialogContent>
      </Dialog>

      <Dialog open={detailDialogOpen} onOpenChange={setDetailDialogOpen}>
        <DialogContent className='max-w-2xl'>
          <DialogHeader>
            <DialogTitle>Hub post office link details</DialogTitle>
            <DialogDescription>
              Read-only details for the selected hub post office assignment.
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
                <p className='text-muted-foreground'>Post office</p>
                <p className='font-medium'>
                  {selectedRow.postOffice
                    ? buildPostOfficeLabel(selectedRow.postOffice)
                    : selectedRow.mapping.postOfficeCode}
                </p>
              </div>
              <div className='sm:col-span-2'>
                <p className='text-muted-foreground'>Address</p>
                <p className='font-medium'>
                  {selectedRow.postOffice?.addressDetail || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Post office status</p>
                <p className='font-medium'>
                  {selectedRow.postOffice?.status || '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Created</p>
                <p className='font-medium'>
                  {formatDateTime(selectedRow.mapping.createdAt)}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Coordinates</p>
                <p className='font-medium'>
                  {hasPostOfficeLocation(selectedRow.postOffice)
                    ? `${selectedRow.postOffice?.latitude}, ${selectedRow.postOffice?.longitude}`
                    : '--'}
                </p>
              </div>
              <div>
                <p className='text-muted-foreground'>Mapping ID</p>
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
        title='Remove hub post office link'
        description={
          deleteTarget
            ? `This will remove ${deleteTarget.mapping.postOfficeCode} from ${selectedHub?.code || 'the selected hub'}.`
            : 'This action cannot be undone.'
        }
        confirmText='Remove'
        cancelText='Cancel'
        onConfirm={handleRemoveLink}
        isLoading={isRemoving}
        variant='destructive'
      />
    </>
  );
}
