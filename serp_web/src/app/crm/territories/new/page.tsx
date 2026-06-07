'use client';

import { useRouter } from 'next/navigation';
import { TerritoryForm } from '@/modules/crm/components/forms/TerritoryForm';
import { useCreateTerritoryMutation } from '@/modules/crm/api/crmApi';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Button } from '@/shared/components/ui';
import { ArrowLeft } from 'lucide-react';

export default function CreateTerritoryPage() {
  const router = useRouter();
  const [createTerritory] = useCreateTerritoryMutation();

  const handleSubmit = async (data: any) => {
    try {
      const result = await createTerritory(data).unwrap();
      toast.success('Territory created successfully');
      router.push(`/crm/territories/${result.data.territoryCode}`);
    } catch (error) {
      toast.error('Failed to create territory', {
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
      <TerritoryForm
        onSubmit={handleSubmit}
        onCancel={() => router.push('/crm/territories')}
      />
    </div>
  );
}
