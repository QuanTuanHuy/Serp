'use client';

import { useRouter } from 'next/navigation';
import { toast } from 'sonner';
import {
  useCreateRouteMutation,
  useGetRouteByIdQuery,
  useGetSchoolsQuery,
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

export function SchoolBusRouteFormPage({ routeId }: SchoolBusRouteFormPageProps) {
  const router = useRouter();
  const isEditMode = Boolean(routeId);
  const { data: routeData, isLoading: loadingRoute } = useGetRouteByIdQuery(
    routeId as number,
    { skip: !routeId }
  );
  const { data: schoolsData } = useGetSchoolsQuery({
    ...SCHOOL_BUS_OPTION_QUERY,
    sortBy: 'name',
  });
  const [createRoute, { isLoading: creating }] = useCreateRouteMutation();
  const [updateRoute, { isLoading: updating }] = useUpdateRouteMutation();

  const handleSubmit = async (values: SchoolBusRouteUpsertRequest) => {
    try {
      const response = isEditMode
        ? await updateRoute({ id: routeId as number, body: values }).unwrap()
        : await createRoute(values).unwrap();
      toast.success(response.message || 'Route saved');
      router.push(`/school-bus/dispatch/${response.data.id}`);
    } catch (error: any) {
      toast.error(error?.data?.message || 'Failed to save route');
    }
  };

  if (isEditMode && loadingRoute) {
    return (
      <SchoolBusPageShell title='Edit route' description='Loading route detail...'>
        <SchoolBusEmptyState
          title='Loading route'
          description='Fetching the route snapshot for editing.'
        />
      </SchoolBusPageShell>
    );
  }

  return (
    <SchoolBusPageShell
      title={isEditMode ? 'Edit route' : 'Create route'}
      description='Prepare the route shell before generating plans, assigning resources, and running attendance.'
    >
      <RoutePlanForm
        initialData={routeData?.data?.route}
        schools={getPageItems(schoolsData?.data)}
        isLoading={creating || updating}
        submitLabel={isEditMode ? 'Update route' : 'Create route'}
        onCancel={() =>
          router.push(
            isEditMode ? `/school-bus/dispatch/${routeId}` : '/school-bus/dispatch'
          )
        }
        onSubmit={handleSubmit}
      />
    </SchoolBusPageShell>
  );
}
