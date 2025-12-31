/*
Author: QuanTuanHuy
Description: Part of Serp Project - Create New Customer Page
*/

'use client';

import { useRouter } from 'next/navigation';
import { CustomerForm } from '../../components/forms/CustomerForm';
import { useCreateCustomerMutation } from '../../api/salesApi';
import type { CustomerCreationForm } from '../../types';

export const CreateCustomerPage: React.FC = () => {
  const router = useRouter();
  const [createCustomer, { isLoading }] = useCreateCustomerMutation();

  const handleSubmit = async (data: CustomerCreationForm) => {
    try {
      const result = await createCustomer(data).unwrap();
      if (result.data?.id) {
        router.push(`/sales/customers/${result.data.id}`);
      } else {
        router.push('/sales/customers');
      }
    } catch (error) {
      console.error('Không thể tạo khách hàng:', error);
      throw error;
    }
  };

  const handleCancel = () => {
    router.push('/sales/customers');
  };

  return (
    <div className='container max-w-4xl mx-auto py-8'>
      <CustomerForm
        onSubmit={handleSubmit as any}
        onCancel={handleCancel}
        isLoading={isLoading}
      />
    </div>
  );
};

export default CreateCustomerPage;
