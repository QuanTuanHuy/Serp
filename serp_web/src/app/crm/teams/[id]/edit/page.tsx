'use client';

import { useRouter, useParams } from 'next/navigation';
import { TeamForm } from '@/modules/crm/components/forms/TeamForm';
import {
  useGetTeamQuery,
  useUpdateTeamMutation,
} from '@/modules/crm/api/crmApi';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Card, CardContent } from '@/shared/components/ui';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/shared/components/ui';

export default function EditTeamPage() {
  const router = useRouter();
  const params = useParams();
  const teamId = params.id as string;

  const { data, isLoading } = useGetTeamQuery(teamId);
  const [updateTeam] = useUpdateTeamMutation();

  const team = data?.data;

  const handleSubmit = async (data: any) => {
    try {
      await updateTeam({ id: teamId, data }).unwrap();
      toast.success('Team updated successfully');
      router.push(`/crm/teams/${teamId}`);
    } catch (error) {
      toast.error('Failed to update team', {
        description: getErrorMessage(error),
      });
    }
  };

  if (isLoading) {
    return (
      <div className='flex items-center justify-center h-64'>
        <div className='animate-spin rounded-full h-8 w-8 border-b-2 border-primary' />
      </div>
    );
  }

  if (!team) {
    return (
      <Card>
        <CardContent className='py-16 text-center'>
          <h3 className='text-lg font-semibold mb-2'>Team not found</h3>
          <Button onClick={() => router.push('/crm/teams')}>
            Back to Teams
          </Button>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className='space-y-4'>
      <Button variant='ghost' onClick={() => router.back()}>
        <ArrowLeft className='h-4 w-4 mr-2' />
        Back
      </Button>
      <TeamForm
        team={team}
        onSubmit={handleSubmit}
        onCancel={() => router.push(`/crm/teams/${teamId}`)}
      />
    </div>
  );
}
