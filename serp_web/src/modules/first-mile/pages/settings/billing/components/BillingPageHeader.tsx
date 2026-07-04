/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing page header
 */

import React from 'react';

export const BillingPageHeader: React.FC = () => {
  return (
    <div>
      <h1 className='text-2xl font-bold tracking-tight'>Tính cước</h1>
      <p className='text-muted-foreground mt-1'>
        Tính phí vận chuyển và quản lý quy tắc giá tại một nơi.
      </p>
    </div>
  );
};
