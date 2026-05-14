'use client';

import { useForm, type Resolver } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import { z } from 'zod';
import { useGetMyOrganizationQuery } from '@/modules/settings/services/organizations/organizationsApi';
import { useGetOrganizationUsersQuery } from '@/modules/settings/services/users/usersApi';
import {
  Button,
  Input,
  Label,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  Team,
  CreateTeamRequest,
  UpdateTeamRequest,
  TeamStatus,
} from '../../types';

const teamSchema = z.object({
  name: z.string().min(1, 'Name is required').max(255, 'Name is too long'),
  description: z.string().optional(),
  managerUserId: z.coerce.number().min(1, 'Manager is required'),
  notes: z.string().optional(),
  status: z.enum(['ACTIVE', 'INACTIVE']),
});

type TeamFormData = z.infer<typeof teamSchema>;

interface TeamFormProps {
  team?: Team;
  onSubmit: (data: CreateTeamRequest | UpdateTeamRequest) => Promise<void>;
  onCancel?: () => void;
  isLoading?: boolean;
  className?: string;
}

export const TeamForm: React.FC<TeamFormProps> = ({
  team,
  onSubmit,
  onCancel,
  isLoading = false,
  className,
}) => {
  const isEditing = !!team;
  const { data: organization } = useGetMyOrganizationQuery();
  const organizationId = organization?.id;
  const { data: usersResponse, isLoading: isLoadingUsers } =
    useGetOrganizationUsersQuery(
      {
        organizationId: organizationId as number,
        page: 0,
        pageSize: 100,
        status: 'ACTIVE',
      },
      { skip: !organizationId }
    );

  const users = usersResponse?.data.items ?? [];

  const teamForm = useForm<TeamFormData>({
    resolver: zodResolver(teamSchema) as Resolver<TeamFormData>,
    defaultValues: team
      ? {
          name: team.name,
          description: team.description || '',
          managerUserId: team.managerUserId ?? 0,
          notes: team.notes || '',
          status: team.status,
        }
      : {
          name: '',
          description: '',
          managerUserId: 0,
          notes: '',
          status: 'ACTIVE' as TeamStatus,
        },
  });

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
    setValue,
    watch,
  } = teamForm;

  const onFormSubmit = handleSubmit(async (data) => {
    try {
      if (isEditing) {
        await onSubmit(data as UpdateTeamRequest);
        return;
      }

      await onSubmit({
        name: data.name,
        description: data.description,
        managerUserId: data.managerUserId,
        notes: data.notes,
      } as CreateTeamRequest);
    } catch (error) {
      console.error('Form submission error:', error);
    }
  });

  return (
    <Card className={cn('w-full', className)}>
      <CardHeader className='pb-4'>
        <CardTitle className='text-xl'>
          {isEditing ? 'Edit Team' : 'Create Team'}
        </CardTitle>
      </CardHeader>
      <CardContent>
        <form onSubmit={onFormSubmit} className='space-y-6'>
          <div className='space-y-4'>
            <h3 className='text-base font-medium text-foreground'>
              Basic Information
            </h3>

            <div className='grid grid-cols-1 md:grid-cols-2 gap-4'>
              <div className='space-y-2'>
                <Label htmlFor='name'>Name *</Label>
                <Input
                  id='name'
                  {...register('name')}
                  className={cn(errors.name && 'border-destructive')}
                  disabled={isLoading}
                  placeholder='Enter team name'
                />
                {errors.name && (
                  <p className='text-sm text-destructive'>
                    {errors.name.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='managerUserId'>Manager *</Label>
                <Select
                  value={
                    watch('managerUserId') ? String(watch('managerUserId')) : ''
                  }
                  onValueChange={(value) =>
                    setValue('managerUserId', Number(value), {
                      shouldValidate: true,
                    })
                  }
                  disabled={isLoading || isLoadingUsers || users.length === 0}
                >
                  <SelectTrigger
                    className={cn(errors.managerUserId && 'border-destructive')}
                  >
                    <SelectValue
                      placeholder={
                        isLoadingUsers ? 'Loading users...' : 'Select manager'
                      }
                    />
                  </SelectTrigger>
                  <SelectContent>
                    {users.map((user) => {
                      const fullName = [user.firstName, user.lastName]
                        .filter(Boolean)
                        .join(' ')
                        .trim();

                      return (
                        <SelectItem key={user.id} value={String(user.id)}>
                          {fullName || user.email} ({user.email})
                        </SelectItem>
                      );
                    })}
                  </SelectContent>
                </Select>
                {errors.managerUserId && (
                  <p className='text-sm text-destructive'>
                    {errors.managerUserId.message}
                  </p>
                )}
              </div>

              <div className='space-y-2'>
                <Label htmlFor='status'>Status *</Label>
                <Select
                  value={watch('status')}
                  onValueChange={(value) =>
                    setValue('status', value as TeamStatus)
                  }
                  disabled={isLoading}
                >
                  <SelectTrigger>
                    <SelectValue placeholder='Select status' />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value='ACTIVE'>Active</SelectItem>
                    <SelectItem value='INACTIVE'>Inactive</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='description'>Description</Label>
                <Textarea
                  id='description'
                  {...register('description')}
                  rows={3}
                  disabled={isLoading}
                  placeholder='Describe the team purpose...'
                />
              </div>

              <div className='space-y-2 md:col-span-2'>
                <Label htmlFor='notes'>Notes</Label>
                <Textarea
                  id='notes'
                  {...register('notes')}
                  rows={2}
                  disabled={isLoading}
                  placeholder='Additional notes...'
                />
              </div>
            </div>
          </div>

          <div className='flex justify-end gap-3 pt-6 border-t'>
            {onCancel && (
              <Button
                type='button'
                variant='outline'
                onClick={onCancel}
                disabled={isLoading || isSubmitting}
              >
                Cancel
              </Button>
            )}
            <Button type='submit' disabled={isLoading || isSubmitting}>
              {isSubmitting
                ? 'Saving...'
                : isEditing
                  ? 'Update Team'
                  : 'Create Team'}
            </Button>
          </div>
        </form>
      </CardContent>
    </Card>
  );
};

export default TeamForm;
