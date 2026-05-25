'use client';

import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  useCreateTransportRequestMutation,
  useGetParentsQuery,
  useGetSchoolsQuery,
  useGetStudentsQuery,
  useGetTransportRequestByIdQuery,
  useUpdateTransportRequestMutation,
} from '../api/schoolBusApi';
import { TransportRequestForm } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import type { SchoolBusTransportRequestUpsertRequest } from '../types';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';

interface SchoolBusRequestFormPageProps {
  requestId?: number;
}

export function SchoolBusRequestFormPage({
  requestId,
}: SchoolBusRequestFormPageProps) {
  const router = useRouter();
  const isEditMode = Boolean(requestId);
  const { data: requestData, isLoading: loadingRequest } =
    useGetTransportRequestByIdQuery(requestId as number, {
      skip: !requestId,
    });
  const { data: parentsData } = useGetParentsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });
  const { data: schoolsData } = useGetSchoolsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'name',
  });
  const { data: studentsData } = useGetStudentsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'fullName',
  });
  const [createTransportRequest, { isLoading: creating }] =
    useCreateTransportRequestMutation();
  const [updateTransportRequest, { isLoading: updating }] =
    useUpdateTransportRequestMutation();

  const handleSubmit = async (values: SchoolBusTransportRequestUpsertRequest) => {
    try {
      const response = isEditMode
        ? await updateTransportRequest({
            id: requestId as number,
            body: values,
          }).unwrap()
        : await createTransportRequest(values).unwrap();
      toast.success(response.message || 'Transport request saved');
      router.push(`/school-bus/requests/${response.data.id}`);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save transport request');
    }
  };

  if (isEditMode && loadingRequest) {
    return (
      <SchoolBusPageShell
        title='Edit transport request'
        description='Loading request details...'
      >
        <SchoolBusEmptyState
          title='Loading request'
          description='Fetching the current request snapshot for editing.'
        />
      </SchoolBusPageShell>
    );
  }

  return (
    <SchoolBusPageShell
      title={isEditMode ? 'Edit transport request' : 'Create transport request'}
      description='Capture transport demand with effective dates, a parent profile, and the student list to be served.'
    >
      <TransportRequestForm
        initialData={requestData?.data}
        parents={getPageItems(parentsData?.data)}
        schools={getPageItems(schoolsData?.data)}
        students={getPageItems(studentsData?.data)}
        isLoading={creating || updating}
        submitLabel={isEditMode ? 'Update request' : 'Create request'}
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
