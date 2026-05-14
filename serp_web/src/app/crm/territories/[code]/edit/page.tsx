'use client';

import { useRouter, useParams } from 'next/navigation';
import { TerritoryForm } from '@/modules/crm/components/forms/TerritoryForm';
import {
  useGetTerritoryQuery,
  useUpdateTerritoryMutation,
} from '@/modules/crm/api/crmApi';
import { toast } from 'sonner';
import { getErrorMessage } from '@/lib/store/api';
import { Card, CardContent } from '@/shared/components/ui';
import { ArrowLeft } from 'lucide-react';
import { Button } from '@/shared/components/ui';

export default function EditTerritoryPage() {
  const router = useRouter();
  const params = useParams();
  const territoryCode = params.code as string;

  const { data, isLoading } = useGetTerritoryQuery(territoryCode);
  const [updateTerritory] = useUpdateTerritoryMutation();

  const territory = data?.data;

  const handleSubmit = async (data: any) => {
    try {
      await updateTerritory({ territoryCode, data }).unwrap();
      toast.success('Territory updated successfully');
      router.push(`/crm/territories/${territoryCode}`);
    } catch (error) {
      toast.error('Failed to update territory', {
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

  if (!territory) {
    return (
      <Card>
        <CardContent className='py-16 text-center'>
          <h3 className='text-lg font-semibold mb-2'>Territory not found</h3>
          <Button onClick={() => router.push('/crm/territories')}>
            Back to Territories
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
      <TerritoryForm
        territory={territory}
        onSubmit={handleSubmit}
        onCancel={() => router.push(`/crm/territories/${territoryCode}`)}
      />
    </div>
  );
}
