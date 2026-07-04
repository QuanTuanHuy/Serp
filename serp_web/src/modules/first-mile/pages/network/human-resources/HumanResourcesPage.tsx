/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS human resources assignment page
 */

'use client';

import React from 'react';
import { getErrorMessage, useAppSelector } from '@/lib/store';
import {
  Badge,
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
  Label,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { useNotification } from '@/shared/hooks';
import { RefreshCw, ShieldAlert, UserMinus, Users } from 'lucide-react';

import {
  useAssignCourierToPostOfficeMutation,
  useAssignManagerToPostOfficeMutation,
  useAssignSecondMileStaffToHubMutation,
  useGetAssignablePostOfficeStaffsQuery,
  useGetHubsQuery,
  useGetPostOfficesQuery,
  useGetPostOfficeStaffAssignmentsByPostOfficeQuery,
  useGetSecondMileAssignableStaffsQuery,
  useGetSecondMileHubStaffAssignmentsQuery,
  useUnassignPostOfficeStaffAssignmentMutation,
  useUnassignSecondMileHubStaffAssignmentMutation,
} from '../../../api';
import { TmsCombobox, type TmsComboboxOption } from '../../../components';
import type {
  Hub,
  PostOffice,
  PostOfficeStaff,
  PostOfficeStaffAssignment,
  PostOfficeStaffRole,
  SecondMileHubStaff,
  SecondMileHubStaffAssignment,
  SecondMileHubStaffRole,
} from '../../../types';

const LOCATION_PAGE_SIZE = 200;

const POST_OFFICE_ROLE_OPTIONS: Array<{
  value: PostOfficeStaffRole;
  label: string;
}> = [
  { value: 'MANAGER', label: 'Trưởng bưu cục' },
  { value: 'COURIER', label: 'Bưu tá' },
];

const HUB_ROLE_OPTIONS: Array<{
  value: SecondMileHubStaffRole;
  label: string;
}> = [
  { value: 'MANAGER', label: 'Trưởng hub' },
  { value: 'EMPLOYEE', label: 'Nhân viên hub' },
  { value: 'DRIVER', label: 'Tài xế' },
];

function formatDateTime(value?: string) {
  if (!value) return '-';

  return new Date(value).toLocaleString('vi-VN');
}

function formatStatus(value?: string) {
  switch (value) {
    case 'ACTIVE':
      return 'Đang hoạt động';
    case 'INACTIVE':
      return 'Ngừng hoạt động';
    case 'ON_LEAVE':
      return 'Đang nghỉ';
    default:
      return value ?? '-';
  }
}

function getPostOfficeRoleLabel(role?: PostOfficeStaffRole) {
  return (
    POST_OFFICE_ROLE_OPTIONS.find((item) => item.value === role)?.label ??
    role ??
    '-'
  );
}

function getHubRoleLabel(role?: SecondMileHubStaffRole) {
  return (
    HUB_ROLE_OPTIONS.find((item) => item.value === role)?.label ?? role ?? '-'
  );
}

function buildPostOfficeOption(postOffice: PostOffice): TmsComboboxOption {
  return {
    value: String(postOffice.id),
    label: `${postOffice.code} - ${postOffice.name}`,
  };
}

function buildHubOption(hub: Hub): TmsComboboxOption {
  return {
    value: String(hub.id),
    label: `${hub.code} - ${hub.name}`,
  };
}

function getAssignmentName(
  assignment: PostOfficeStaffAssignment | SecondMileHubStaffAssignment
) {
  return assignment.staffFullName || assignment.staffCode || 'Chưa có tên';
}

function getAssignmentCode(
  assignment: PostOfficeStaffAssignment | SecondMileHubStaffAssignment
) {
  return (
    assignment.staffCode ||
    (assignment.staffId ? `#${assignment.staffId}` : '-')
  );
}

function getAssignmentStatus(
  assignment: PostOfficeStaffAssignment | SecondMileHubStaffAssignment
) {
  if ('staffStatus' in assignment) {
    return assignment.staffStatus;
  }

  return undefined;
}

function getStaffName(staff: PostOfficeStaff | SecondMileHubStaff) {
  return staff.fullName || staff.code || 'Chưa có tên';
}

function getStaffCode(staff: PostOfficeStaff | SecondMileHubStaff) {
  return staff.code || `#${staff.id}`;
}

export function HumanResourcesPage() {
  const notification = useNotification();
  const roles = useAppSelector(
    (state) => state.account.user.profile?.roles ?? []
  );
  const isCustomer = roles.includes('TMS_CUSTOMER');
  const canEdit = roles.includes('TMS_ADMIN');
  const canView = !isCustomer;

  const [activeTab, setActiveTab] = React.useState('post-office');
  const [postOfficeId, setPostOfficeId] = React.useState('');
  const [postOfficeRole, setPostOfficeRole] =
    React.useState<PostOfficeStaffRole>('MANAGER');
  const [postOfficeKeyword, setPostOfficeKeyword] = React.useState('');

  const [hubId, setHubId] = React.useState('');
  const [hubRole, setHubRole] =
    React.useState<SecondMileHubStaffRole>('MANAGER');
  const [hubKeyword, setHubKeyword] = React.useState('');

  const { data: postOfficeData, isFetching: isFetchingPostOffices } =
    useGetPostOfficesQuery(
      { page: 0, size: LOCATION_PAGE_SIZE, status: 'ACTIVE' },
      { skip: !canView }
    );
  const { data: hubData, isFetching: isFetchingHubs } = useGetHubsQuery(
    { page: 0, size: LOCATION_PAGE_SIZE, status: 'ACTIVE' },
    { skip: !canView }
  );

  const selectedPostOfficeId = postOfficeId ? Number(postOfficeId) : undefined;
  const selectedHubId = hubId ? Number(hubId) : undefined;

  const {
    data: postOfficeAssignments = [],
    isFetching: isFetchingPostOfficeAssignments,
    refetch: refetchPostOfficeAssignments,
  } = useGetPostOfficeStaffAssignmentsByPostOfficeQuery(
    {
      postOfficeId: selectedPostOfficeId ?? 0,
      role: postOfficeRole,
    },
    { skip: !canView || !selectedPostOfficeId }
  );

  const {
    data: assignablePostOfficeStaff = [],
    isFetching: isFetchingPostOfficeStaff,
    refetch: refetchAssignablePostOfficeStaff,
  } = useGetAssignablePostOfficeStaffsQuery(
    { role: postOfficeRole, keyword: postOfficeKeyword.trim() || undefined },
    { skip: !canEdit }
  );

  const {
    data: hubAssignments = [],
    isFetching: isFetchingHubAssignments,
    refetch: refetchHubAssignments,
  } = useGetSecondMileHubStaffAssignmentsQuery(
    {
      hubId: selectedHubId ?? 0,
      role: hubRole,
    },
    { skip: !canView || !selectedHubId }
  );

  const {
    data: assignableHubStaff = [],
    isFetching: isFetchingHubStaff,
    refetch: refetchAssignableHubStaff,
  } = useGetSecondMileAssignableStaffsQuery(
    { role: hubRole, keyword: hubKeyword.trim() || undefined },
    { skip: !canEdit }
  );

  const [assignCourierToPostOffice, { isLoading: isAssigningCourier }] =
    useAssignCourierToPostOfficeMutation();
  const [assignManagerToPostOffice, { isLoading: isAssigningManager }] =
    useAssignManagerToPostOfficeMutation();
  const [
    unassignPostOfficeStaffAssignment,
    { isLoading: isUnassigningPostOfficeStaff },
  ] = useUnassignPostOfficeStaffAssignmentMutation();
  const [assignSecondMileStaffToHub, { isLoading: isAssigningHubStaff }] =
    useAssignSecondMileStaffToHubMutation();
  const [
    unassignSecondMileHubStaffAssignment,
    { isLoading: isUnassigningHubStaff },
  ] = useUnassignSecondMileHubStaffAssignmentMutation();

  const postOfficeOptions = React.useMemo(
    () => (postOfficeData?.items ?? []).map(buildPostOfficeOption),
    [postOfficeData?.items]
  );
  const hubOptions = React.useMemo(
    () => (hubData?.items ?? []).map(buildHubOption),
    [hubData?.items]
  );
  const handleAssignPostOfficeStaff = async (staffId: number) => {
    if (!canEdit || !selectedPostOfficeId) return;

    try {
      const payload = {
        staffId,
        postOfficeId: selectedPostOfficeId,
      };

      if (postOfficeRole === 'MANAGER') {
        await assignManagerToPostOffice(payload).unwrap();
      } else {
        await assignCourierToPostOffice(payload).unwrap();
      }

      notification.success('Đã phân công nhân sự bưu cục.');
      await refetchPostOfficeAssignments();
      await refetchAssignablePostOfficeStaff();
    } catch (error) {
      notification.error(getErrorMessage(error));
    }
  };

  const handleUnassignPostOfficeStaff = async (assignmentId: number) => {
    if (!canEdit) return;

    try {
      await unassignPostOfficeStaffAssignment(assignmentId).unwrap();
      notification.success('Đã gỡ phân công nhân sự bưu cục.');
      await refetchPostOfficeAssignments();
      await refetchAssignablePostOfficeStaff();
    } catch (error) {
      notification.error(getErrorMessage(error));
    }
  };

  const handleAssignHubStaff = async (staffId: number) => {
    if (!canEdit || !selectedHubId) return;

    try {
      await assignSecondMileStaffToHub({
        staffId,
        hubId: selectedHubId,
      }).unwrap();
      notification.success('Đã phân công nhân sự hub.');
      await refetchHubAssignments();
      await refetchAssignableHubStaff();
    } catch (error) {
      notification.error(getErrorMessage(error));
    }
  };

  const handleUnassignHubStaff = async (assignmentId: number) => {
    if (!canEdit) return;

    try {
      await unassignSecondMileHubStaffAssignment(assignmentId).unwrap();
      notification.success('Đã gỡ phân công nhân sự hub.');
      await refetchHubAssignments();
      await refetchAssignableHubStaff();
    } catch (error) {
      notification.error(getErrorMessage(error));
    }
  };

  if (!canView) {
    return (
      <div className='space-y-6 p-6'>
        <Card>
          <CardContent className='flex items-center gap-3 p-6'>
            <ShieldAlert className='h-5 w-5 text-destructive' />
            <div>
              <p className='font-medium'>Không có quyền truy cập</p>
              <p className='text-sm text-muted-foreground'>
                Khách hàng không được truy cập trang quản lý nhân sự TMS.
              </p>
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className='space-y-6 p-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-2'>
          <div className='flex items-center gap-3'>
            <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
              <Users className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-2xl font-semibold tracking-normal'>
                Quản lý nhân sự
              </h1>
              <p className='text-sm text-muted-foreground'>
                Sắp xếp nhân sự vào vai trò tại bưu cục và hub trong mạng TMS.
              </p>
            </div>
          </div>
        </div>
      </div>

      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList>
          <TabsTrigger value='post-office'>Bưu cục</TabsTrigger>
          <TabsTrigger value='hub'>Hub</TabsTrigger>
        </TabsList>

        <TabsContent value='post-office' className='space-y-4'>
          <Card>
            <CardHeader>
              <CardTitle>Phân công nhân sự bưu cục</CardTitle>
              <CardDescription>
                Chọn bưu cục và quyền nhân sự hiện có để phân công vào vị trí
                phù hợp.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid gap-4 lg:grid-cols-[minmax(0,1.5fr)_minmax(0,0.8fr)]'>
                <div className='space-y-2'>
                  <Label htmlFor='human-resource-post-office'>Bưu cục</Label>
                  <TmsCombobox
                    id='human-resource-post-office'
                    value={postOfficeId}
                    onValueChange={setPostOfficeId}
                    options={postOfficeOptions}
                    placeholder='Chọn bưu cục'
                    emptyText='Không có bưu cục'
                    loading={isFetchingPostOffices}
                    clearable
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='human-resource-post-office-role'>
                    Quyền hiện có
                  </Label>
                  <TmsCombobox
                    id='human-resource-post-office-role'
                    value={postOfficeRole}
                    onValueChange={(value) =>
                      setPostOfficeRole(value as PostOfficeStaffRole)
                    }
                    options={POST_OFFICE_ROLE_OPTIONS}
                    placeholder='Chọn vai trò'
                  />
                </div>
              </div>

              {canEdit && (
                <div className='space-y-4 rounded-md border p-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='human-resource-post-office-keyword'>
                      Tìm nhân sự
                    </Label>
                    <Input
                      id='human-resource-post-office-keyword'
                      value={postOfficeKeyword}
                      onChange={(event) =>
                        setPostOfficeKeyword(event.target.value)
                      }
                      placeholder='Mã hoặc tên nhân sự'
                    />
                  </div>
                  <AssignableStaffTable
                    staffs={assignablePostOfficeStaff}
                    roleLabel={getPostOfficeRoleLabel(postOfficeRole)}
                    isFetching={isFetchingPostOfficeStaff}
                    canAssign={Boolean(selectedPostOfficeId)}
                    isAssigning={isAssigningCourier || isAssigningManager}
                    onAssign={handleAssignPostOfficeStaff}
                    emptyText='Không có nhân sự chưa phân công trong quyền này.'
                    disabledText='Chọn bưu cục'
                  />
                </div>
              )}

              <StaffAssignmentsTable
                assignments={postOfficeAssignments}
                isFetching={isFetchingPostOfficeAssignments}
                canEdit={canEdit}
                getRoleLabel={(assignment) =>
                  getPostOfficeRoleLabel(
                    assignment.staffRole as PostOfficeStaffRole | undefined
                  )
                }
                onUnassign={handleUnassignPostOfficeStaff}
                isUnassigning={isUnassigningPostOfficeStaff}
                emptyText={
                  selectedPostOfficeId
                    ? 'Bưu cục chưa có nhân sự trong vai trò này.'
                    : 'Chọn bưu cục để xem nhân sự.'
                }
              />
            </CardContent>
          </Card>
        </TabsContent>

        <TabsContent value='hub' className='space-y-4'>
          <Card>
            <CardHeader>
              <CardTitle>Phân công nhân sự hub</CardTitle>
              <CardDescription>
                Chọn hub và quyền nhân sự hiện có để phân công vào vị trí phù
                hợp.
              </CardDescription>
            </CardHeader>
            <CardContent className='space-y-4'>
              <div className='grid gap-4 lg:grid-cols-[minmax(0,1.5fr)_minmax(0,0.8fr)]'>
                <div className='space-y-2'>
                  <Label htmlFor='human-resource-hub'>Hub</Label>
                  <TmsCombobox
                    id='human-resource-hub'
                    value={hubId}
                    onValueChange={setHubId}
                    options={hubOptions}
                    placeholder='Chọn hub'
                    emptyText='Không có hub'
                    loading={isFetchingHubs}
                    clearable
                  />
                </div>
                <div className='space-y-2'>
                  <Label htmlFor='human-resource-hub-role'>Quyền hiện có</Label>
                  <TmsCombobox
                    id='human-resource-hub-role'
                    value={hubRole}
                    onValueChange={(value) =>
                      setHubRole(value as SecondMileHubStaffRole)
                    }
                    options={HUB_ROLE_OPTIONS}
                    placeholder='Chọn vai trò'
                  />
                </div>
              </div>

              {canEdit && (
                <div className='space-y-4 rounded-md border p-4'>
                  <div className='space-y-2'>
                    <Label htmlFor='human-resource-hub-keyword'>
                      Tìm nhân sự
                    </Label>
                    <Input
                      id='human-resource-hub-keyword'
                      value={hubKeyword}
                      onChange={(event) => setHubKeyword(event.target.value)}
                      placeholder='Mã hoặc tên nhân sự'
                    />
                  </div>
                  <AssignableStaffTable
                    staffs={assignableHubStaff}
                    roleLabel={getHubRoleLabel(hubRole)}
                    isFetching={isFetchingHubStaff}
                    canAssign={Boolean(selectedHubId)}
                    isAssigning={isAssigningHubStaff}
                    onAssign={handleAssignHubStaff}
                    emptyText='Không có nhân sự chưa phân công trong quyền này.'
                    disabledText='Chọn hub'
                  />
                </div>
              )}

              <StaffAssignmentsTable
                assignments={hubAssignments}
                isFetching={isFetchingHubAssignments}
                canEdit={canEdit}
                getRoleLabel={(assignment) =>
                  getHubRoleLabel(
                    assignment.staffRole as SecondMileHubStaffRole | undefined
                  )
                }
                onUnassign={handleUnassignHubStaff}
                isUnassigning={isUnassigningHubStaff}
                emptyText={
                  selectedHubId
                    ? 'Hub chưa có nhân sự trong vai trò này.'
                    : 'Chọn hub để xem nhân sự.'
                }
              />
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
    </div>
  );
}

interface StaffAssignmentsTableProps<TAssignment> {
  assignments: TAssignment[];
  isFetching: boolean;
  canEdit: boolean;
  isUnassigning: boolean;
  emptyText: string;
  getRoleLabel: (assignment: TAssignment) => string;
  onUnassign: (assignmentId: number) => void;
}

interface AssignableStaffTableProps<TStaff> {
  staffs: TStaff[];
  roleLabel: string;
  isFetching: boolean;
  canAssign: boolean;
  isAssigning: boolean;
  emptyText: string;
  disabledText: string;
  onAssign: (staffId: number) => void;
}

function AssignableStaffTable<
  TStaff extends PostOfficeStaff | SecondMileHubStaff,
>({
  staffs,
  roleLabel,
  isFetching,
  canAssign,
  isAssigning,
  emptyText,
  disabledText,
  onAssign,
}: AssignableStaffTableProps<TStaff>) {
  return (
    <div className='overflow-hidden rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Nhân sự chưa phân công</TableHead>
            <TableHead>Mã</TableHead>
            <TableHead>Quyền hiện có</TableHead>
            <TableHead>Trạng thái</TableHead>
            <TableHead className='w-[140px] text-right'>
              {isFetching ? (
                <RefreshCw className='ml-auto h-4 w-4 animate-spin' />
              ) : (
                'Thao tác'
              )}
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {staffs.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={5}
                className='h-24 text-center text-muted-foreground'
              >
                {isFetching ? 'Đang tải danh sách nhân sự...' : emptyText}
              </TableCell>
            </TableRow>
          ) : (
            staffs.map((staff) => (
              <TableRow key={staff.id}>
                <TableCell className='font-medium'>
                  {getStaffName(staff)}
                </TableCell>
                <TableCell>{getStaffCode(staff)}</TableCell>
                <TableCell>{roleLabel}</TableCell>
                <TableCell>
                  <Badge variant='outline'>{formatStatus(staff.status)}</Badge>
                </TableCell>
                <TableCell className='text-right'>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => onAssign(staff.id)}
                    disabled={!canAssign || isAssigning}
                  >
                    {canAssign ? 'Phân công' : disabledText}
                  </Button>
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}

function StaffAssignmentsTable<
  TAssignment extends PostOfficeStaffAssignment | SecondMileHubStaffAssignment,
>({
  assignments,
  isFetching,
  canEdit,
  isUnassigning,
  emptyText,
  getRoleLabel,
  onUnassign,
}: StaffAssignmentsTableProps<TAssignment>) {
  return (
    <div className='overflow-hidden rounded-md border'>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Nhân sự</TableHead>
            <TableHead>Mã</TableHead>
            <TableHead>Vai trò</TableHead>
            <TableHead>Trạng thái</TableHead>
            <TableHead>Ngày phân công</TableHead>
            <TableHead className='w-[120px] text-right'>
              {isFetching ? (
                <RefreshCw className='ml-auto h-4 w-4 animate-spin' />
              ) : (
                'Thao tác'
              )}
            </TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {assignments.length === 0 ? (
            <TableRow>
              <TableCell
                colSpan={6}
                className='h-24 text-center text-muted-foreground'
              >
                {isFetching ? 'Đang tải danh sách nhân sự...' : emptyText}
              </TableCell>
            </TableRow>
          ) : (
            assignments.map((assignment) => (
              <TableRow key={assignment.id}>
                <TableCell className='font-medium'>
                  {getAssignmentName(assignment)}
                </TableCell>
                <TableCell>{getAssignmentCode(assignment)}</TableCell>
                <TableCell>{getRoleLabel(assignment)}</TableCell>
                <TableCell>
                  <Badge variant='outline'>
                    {formatStatus(getAssignmentStatus(assignment))}
                  </Badge>
                </TableCell>
                <TableCell>{formatDateTime(assignment.assignedFrom)}</TableCell>
                <TableCell className='text-right'>
                  {canEdit ? (
                    <Button
                      variant='ghost'
                      size='icon'
                      onClick={() => onUnassign(assignment.id)}
                      disabled={isUnassigning}
                      title='Gỡ phân công'
                    >
                      <UserMinus className='h-4 w-4' />
                    </Button>
                  ) : (
                    <span className='text-sm text-muted-foreground'>
                      Chỉ xem
                    </span>
                  )}
                </TableCell>
              </TableRow>
            ))
          )}
        </TableBody>
      </Table>
    </div>
  );
}
