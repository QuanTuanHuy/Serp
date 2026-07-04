'use client';

import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  useGetDepotsQuery,
  useGetRouteByIdQuery,
  useGetSchoolDropdownOptionsQuery,
  useUpdateRouteMutation,
} from '../api/schoolBusApi';
import { RoutePlanForm } from '../components/SchoolBusWorkflowForms';
import { SchoolBusEmptyState } from '../components/SchoolBusEmptyState';
import { SchoolBusPageShell } from '../components/SchoolBusPageShell';
import type { SchoolBusRouteUpsertRequest } from '../types';
import { getPageItems, SCHOOL_BUS_OPTION_QUERY } from '../utils';

interface SchoolBusRouteFormPageProps {
  routeId?: number;
}

export function SchoolBusRouteFormPage({
  routeId,
}: SchoolBusRouteFormPageProps) {
  const router = useRouter();
  const { data: routeData, isLoading: loadingRoute } = useGetRouteByIdQuery(
    routeId as number,
    { skip: !routeId, refetchOnMountOrArgChange: true }
  );
  const { data: schoolsData } = useGetSchoolDropdownOptionsQuery();
  const { data: depotsData } = useGetDepotsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'name',
  });
  const [updateRoute, { isLoading: updating }] = useUpdateRouteMutation();

  const handleSubmit = async (values: SchoolBusRouteUpsertRequest) => {
    try {
      const response = await updateRoute({
        id: routeId as number,
        body: values,
      }).unwrap();
      toast.success(response.message || 'Route saved');
      router.push(`/school-bus/dispatch/${response.data.id}`);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save route');
    }
  };

  if (loadingRoute) {
    return (
      <SchoolBusPageShell
        title='Edit route'
        description='Loading route detail...'
      >
        <SchoolBusEmptyState
          title='Loading route'
          description='Fetching the route snapshot for editing.'
        />
      </SchoolBusPageShell>
    );
  }

  return (
    <SchoolBusPageShell
      title='Edit route'
      description='Prepare the route shell before generating plans, assigning resources, and running attendance.'
    >
      <RoutePlanForm
        initialData={routeData?.data?.route}
        schools={schoolsData?.data || []}
        depots={getPageItems(depotsData?.data)}
        isLoading={updating}
        submitLabel='Update route'
        onCancel={() => router.push(`/school-bus/dispatch/${routeId}`)}
        onSubmit={handleSubmit}
      />
    </SchoolBusPageShell>
  );
}
