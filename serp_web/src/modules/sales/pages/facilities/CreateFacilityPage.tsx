'use client';

import { useRouter } from 'next/navigation';
import { FacilityForm } from '../../components/forms/FacilityForm';
import { useCreateFacilityMutation } from '../../api/salesApi';
import type { FacilityCreationForm } from '../../types';

export const CreateFacilityPage: React.FC = () => {
  const router = useRouter();
  const [createFacility, { isLoading }] = useCreateFacilityMutation();

  const handleSubmit = async (data: FacilityCreationForm) => {
    try {
      const result = await createFacility(data).unwrap();
      if (result.data?.id) {
        router.push(`/sales/facility/${result.data.id}`);
      } else {
        router.push('/sales/facility');
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
        onCancel={() => router.push('/sales/facility')}
        isLoading={isLoading}
      />
    </div>
  );
};

export default CreateFacilityPage;
