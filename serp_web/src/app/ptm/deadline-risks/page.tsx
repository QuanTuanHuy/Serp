/*
Author: QuanTuanHuy
Description: Part of Serp Project
*/

'use client';

import { DeadlineRiskAlerts } from '@/modules/ptm/components/alerts';

export default function DeadlineRisksPage() {
  return (
    <div className='container max-w-5xl mx-auto py-6'>
      <DeadlineRiskAlerts />
    </div>
  );
}
