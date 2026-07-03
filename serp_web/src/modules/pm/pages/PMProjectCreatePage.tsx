/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM project create page
 */

'use client';

import { useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { ArrowLeft, Sparkles } from 'lucide-react';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { selectOrganizationId } from '@/modules/account/store';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import { useGetMyModulesQuery } from '@/modules/account/services/moduleApi';
import { useAppSelector } from '@/shared/hooks';
import { Button } from '@/shared/components/ui';
import {
  useCreatePmProjectMutation,
  useGetProjectBlueprintsQuery,
  useGetProjectCategoriesQuery,
} from '../api/projectApi';
import {
  PMProjectCreateForm,
  type PMProjectCreateFormValues,
} from '../components/projects/PMProjectCreateForm';
import { filterAvailablePMProjectTemplates } from '../utils/projectForm';

export function PMProjectCreatePage() {
  const router = useRouter();
  const organizationId = useAppSelector(selectOrganizationId);
  const [createPmProject] = useCreatePmProjectMutation();
  const {
    data: blueprintResponse,
    isLoading: isBlueprintLoading,
    error: blueprintError,
  } = useGetProjectBlueprintsQuery({
    page: 0,
    pageSize: 100,
    projectTypeKey: 'software',
  });
  const {
    data: categoryResponse,
    isLoading: isCategoryLoading,
    error: categoryError,
  } = useGetProjectCategoriesQuery({ page: 0, pageSize: 100 });
  const { data: myModules } = useGetMyModulesQuery(undefined, {
    skip: !organizationId,
  });
  const pmModuleId = myModules?.find((m) => m.moduleCode === 'PM')?.moduleId;

  const {
    data: usersResponse,
    isLoading: isUserLoading,
    error: usersError,
  } = useGetOrganizationUsersQuery(
    {
      organizationId: organizationId as number,
      page: 0,
      pageSize: 100,
      status: 'ACTIVE',
      moduleId: pmModuleId,
    },
    { skip: !organizationId }
  );

  const templateOptions = useMemo(
    () =>
      filterAvailablePMProjectTemplates(
        (blueprintResponse?.data.items || []).map((blueprint) => ({
          id: blueprint.id,
          name: blueprint.name,
          avatarUrl: blueprint.avatarUrl,
        }))
      ),
    [blueprintResponse]
  );

  const categoryOptions = useMemo(
    () =>
      [...(categoryResponse?.data.items || [])]
        .sort((left, right) => left.name.localeCompare(right.name))
        .map((category) => ({
          id: String(category.id),
          name: category.name,
          description: category.description || undefined,
        })),
    [categoryResponse]
  );

  const leadOptions = useMemo(
    () =>
      [...(usersResponse?.data.items || [])]
        .sort((left, right) => {
          const leftName =
            `${left.firstName || ''} ${left.lastName || ''}`.trim();
          const rightName =
            `${right.firstName || ''} ${right.lastName || ''}`.trim();

          return leftName.localeCompare(rightName);
        })
        .map((user) => {
          const fullName =
            `${user.firstName || ''} ${user.lastName || ''}`.trim();

          return {
            id: String(user.id),
            name: fullName || user.email || `User #${user.id}`,
            email: user.email,
            avatarUrl: user.avatarUrl,
          };
        }),
    [usersResponse]
  );

  const isCatalogLoading =
    isBlueprintLoading ||
    isCategoryLoading ||
    (Boolean(organizationId) && isUserLoading);

  const submitDisabledReason = useMemo(() => {
    if (!organizationId) {
      return 'Organization context is missing, so PM users cannot be loaded.';
    }

    if (blueprintError) {
      return `Unable to load PM blueprints: ${getErrorMessage(blueprintError)}`;
    }

    if (categoryError) {
      return `Unable to load PM categories: ${getErrorMessage(categoryError)}`;
    }

    if (usersError) {
      return `Unable to load project leads: ${getErrorMessage(usersError)}`;
    }

    if (templateOptions.length === 0) {
      return 'No supported PM blueprints are available yet. Add a backend blueprint such as Software Kanban first.';
    }

    if (leadOptions.length === 0) {
      return 'No active organization users are available to assign as project lead.';
    }

    return undefined;
  }, [
    blueprintError,
    categoryError,
    leadOptions.length,
    organizationId,
    templateOptions.length,
    usersError,
  ]);

  const handleCreateProject = async (values: PMProjectCreateFormValues) => {
    const selectedTemplate = templateOptions.find(
      (template) => template.type === values.templateType
    );

    if (!selectedTemplate) {
      toast.error('Selected template is no longer available.');
      return;
    }

    try {
      const createdProject = await createPmProject({
        name: values.name,
        key: values.key,
        description: values.description?.trim() || undefined,
        projectTypeKey: 'software',
        leadUserId: Number(values.leadId),
        categoryId: values.categoryId ? Number(values.categoryId) : undefined,
        blueprintId: selectedTemplate.blueprintId,
        provisioningMode: 'TEMPLATE_DEFAULT',
      }).unwrap();

      toast.success(`Project ${createdProject.name} created successfully.`);
      router.push(`/pm/projects/${createdProject.id}/summary`);
    } catch (error) {
      toast.error('Failed to create project', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-6'>
      <div className='flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between'>
        <div className='space-y-3'>
          <Button
            type='button'
            variant='ghost'
            className='w-fit px-0 text-muted-foreground hover:bg-transparent'
            onClick={() => router.push('/pm/projects')}
          >
            <ArrowLeft className='mr-2 h-4 w-4' />
            Back to projects
          </Button>

          <div className='flex items-center gap-3'>
            <div className='flex h-11 w-11 items-center justify-center rounded-2xl bg-primary/10 text-primary'>
              <Sparkles className='h-5 w-5' />
            </div>
            <div>
              <h1 className='text-3xl font-bold tracking-tight'>
                Create project
              </h1>
              <p className='text-sm text-muted-foreground'>
                Launch a company software workspace from the PM templates that
                are currently available in the backend.
              </p>
            </div>
          </div>
        </div>
      </div>

      <PMProjectCreateForm
        templateOptions={templateOptions}
        leadOptions={leadOptions}
        categoryOptions={categoryOptions}
        isCatalogLoading={isCatalogLoading}
        submitDisabledReason={submitDisabledReason}
        onSubmit={handleCreateProject}
        onCancel={() => router.push('/pm/projects')}
      />
    </div>
  );
}
