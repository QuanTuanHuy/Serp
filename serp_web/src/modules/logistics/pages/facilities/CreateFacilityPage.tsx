/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Create New Facility Page
*/

'use client';

import { useRouter } from 'next/navigation';
import { FacilityForm } from '../../components/forms/FacilityForm';
import { useCreateFacilityMutation } from '../../api/logisticsApi';
import type { FacilityCreationForm } from '../../types';

export const CreateFacilityPage: React.FC = () => {
  const router = useRouter();
  const [createFacility, { isLoading }] = useCreateFacilityMutation();

  const handleSubmit = async (data: FacilityCreationForm) => {
    try {
      const result = await createFacility(data).unwrap();
      if (result.data?.id) {
        router.push(`/logistics/facility/${result.data.id}`);
      } else {
        router.push('/logistics/facility');
      }
    } catch (error) {
      console.error('Không thể tạo kho hàng:', error);
      throw error;
    }
  };

  return (
    <div className='container max-w-4xl mx-auto py-8'>
      <FacilityForm
        onSubmit={handleSubmit as any}
        onCancel={() => router.push('/logistics/facility')}
        isLoading={isLoading}
      />
    </div>
  );
};

export default CreateFacilityPage;
