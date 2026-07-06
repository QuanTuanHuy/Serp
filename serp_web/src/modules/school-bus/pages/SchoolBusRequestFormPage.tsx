'use client';

import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  useCreateTransportRequestMutation,
  useGetParentDropdownOptionsQuery,
  useGetSchoolDropdownOptionsQuery,
  useGetStudentsQuery,
  useGetTransportRequestByIdQuery,
  useUpdateTransportRequestMutation,
} from '../api/schoolBusApi';
import { TransportRequestForm } from '../components/SchoolBusWorkflowForms';
import { SchoolBusBreadcrumb } from '../components/SchoolBusBreadcrumb';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import type { SchoolBusTransportRequestUpsertRequest } from '../types';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';
import { useSchoolBusAccess } from '../security/schoolBusAccess';

interface SchoolBusRequestFormPageProps {
  requestId?: number;
}

export function SchoolBusRequestFormPage({
  requestId,
}: SchoolBusRequestFormPageProps) {
  const router = useRouter();
  const access = useSchoolBusAccess();

  const isEditMode = Boolean(requestId);
  const { data: requestData, isLoading: loadingRequest } =
    useGetTransportRequestByIdQuery(requestId as number, {
      skip: !requestId,
    });

  // For Admin/Dispatcher: fetch all parents for the dropdown.
  // For Parent role: skip the query - backend resolves identity from token.
  const { data: parentsData } = useGetParentDropdownOptionsQuery(undefined, {
    skip: access.isParentOnly,
  });

  const { data: schoolsData } = useGetSchoolDropdownOptionsQuery();

  // Backend scopes students to own children for Parent role.
  // For Admin/Dispatcher: fetch all students (form handles parent+school filtering).
  const { data: studentsData } = useGetStudentsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });

  const [createTransportRequest, { isLoading: creating }] =
    useCreateTransportRequestMutation();
  const [updateTransportRequest, { isLoading: updating }] =
    useUpdateTransportRequestMutation();

  // Derive current parent's parentProfileId from their student list.
  // The User type does not carry parentProfileId directly;
  // students always reference their parent - so we borrow it from there.
  const students = getPageItems(studentsData?.data);
  const currentParentId: number | undefined =
    access.isParentOnly && students.length > 0
      ? (students[0].parentProfileId || undefined)
      : undefined;

  const handleSubmit = async (
    values: SchoolBusTransportRequestUpsertRequest
  ) => {
    try {
      const response = isEditMode
        ? await updateTransportRequest({
            id: requestId as number,
            body: values,
          }).unwrap()
        : await createTransportRequest(values).unwrap();
      toast.success(response.message || 'Đã lưu yêu cầu vận chuyển');
      router.push(`/school-bus/requests/${response.data.id}`);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Không thể lưu yêu cầu xe bus');
    }
  };

  const breadcrumb = (
    <SchoolBusBreadcrumb
      items={[
        { label: 'Điều phối xe buýt', href: '/school-bus/dispatch' },
        {
          label: 'Yêu cầu xe bus',
          href: '/school-bus/requests',
        },
        {
          label: isEditMode ? `Sửa #${requestId}` : 'Yêu cầu mới',
          current: true,
        },
      ]}
    />
  );

  if (isEditMode && loadingRequest) {
    return (
      <SchoolBusPageShell
        title='Sửa yêu cầu đưa đón'
        description='Đang tải chi tiết yêu cầu...'
        breadcrumb={breadcrumb}
      >
        <SchoolBusEmptyState
          title='Đang tải yêu cầu'
          description='Đang tải dữ liệu yêu cầu hiện tại để chỉnh sửa.'
        />
      </SchoolBusPageShell>
    );
  }

  return (
    <SchoolBusPageShell
      title={isEditMode ? 'Sửa yêu cầu đưa đón' : 'Tạo yêu cầu đưa đón'}
      description={
        access.isParentOnly
          ? 'Gửi yêu cầu đưa đón cho con. Nhà trường sẽ xem xét và xử lý trong thời gian sớm nhất.'
          : 'Ghi nhận nhu cầu đưa đón theo thời gian hiệu lực, phụ huynh và danh sách học sinh cần phục vụ.'
      }
      breadcrumb={breadcrumb}
    >
      <TransportRequestForm
        initialData={requestData?.data}
        parents={parentsData?.data || []}
        schools={schoolsData?.data || []}
        students={students}
        isLoading={creating || updating}
        submitLabel={isEditMode ? 'Cập nhật yêu cầu' : 'Tạo yêu cầu'}
        isParentRole={access.isParentOnly}
        currentParentId={currentParentId}
        onCancel={() =>
          router.push(
            isEditMode
              ? `/school-bus/requests/${requestId}`
              : '/school-bus/requests'
          )
        }
        onSubmit={handleSubmit}
      />
    </SchoolBusPageShell>
  );
}

