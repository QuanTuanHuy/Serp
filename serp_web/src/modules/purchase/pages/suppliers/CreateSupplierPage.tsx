/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create New Supplier Page
*/

'use client';

import { useRouter } from 'next/navigation';
import { SupplierForm } from '../../components/forms/SupplierForm';
import { useCreateSupplierMutation } from '../../api/purchaseApi';
import type { SupplierCreationForm } from '../../types';

export const CreateSupplierPage: React.FC = () => {
  const router = useRouter();
  const [createSupplier, { isLoading }] = useCreateSupplierMutation();

  const handleSubmit = async (data: SupplierCreationForm) => {
    try {
      const result = await createSupplier(data).unwrap();
      if (result.data?.id) {
        router.push(`/purchase/suppliers/${result.data.id}`);
      } else {
        router.push('/purchase/suppliers');
      }
    } catch (error) {
      console.error('Không thể tạo nhà cung cấp:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    router.push('/purchase/suppliers');
  };

  return (
    <div className='container max-w-4xl mx-auto py-8'>
      <SupplierForm
        onSubmit={handleSubmit as any}
        onCancel={handleCancel}
        isLoading={isLoading}
      />
    </div>
  );
};

export default CreateSupplierPage;
