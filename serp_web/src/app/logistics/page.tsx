/*
Author: QuanTuanHuy
Description: Part of Serp Project - Logistics Module Redirect
*/

'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function LogisticsPage() {
  const router = useRouter();

  useEffect(() => {
    router.replace('/logistics/orders');
  }, [router]);

  return null;
}
