'use client';

import { useRouter } from 'next/navigation';
import { TeamForm } from '@/modules/crm/components/forms/TeamForm';
import { useCreateTeamMutation } from '@/modules/crm/api/crmApi';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Button } from '@/shared/components/ui';
import { ArrowLeft } from 'lucide-react';

export default function CreateTeamPage() {
  const router = useRouter();
  const [createTeam] = useCreateTeamMutation();

  const handleSubmit = async (data: any) => {
    try {
      const result = await createTeam(data).unwrap();
      toast.success('Team created successfully');
      router.push(`/crm/teams/${result.data.id}`);
    } catch (error) {
      toast.error('Failed to create team', {
        description: getErrorMessage(error),
      });
    }
  };

  return (
    <div className='space-y-4'>
      <Button variant='ghost' onClick={() => router.back()}>
        <ArrowLeft className='h-4 w-4 mr-2' />
        Back
      </Button>
      <TeamForm
        onSubmit={handleSubmit}
        onCancel={() => router.push('/crm/teams')}
      />
    </div>
  );
}
