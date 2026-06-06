/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile app layout route
 */

import '@/modules/first-mile/api/firstMileApi';
import '@/modules/first-mile/api/billingApi';
import { FirstMileLayout } from '@/modules/first-mile';

export default function FirstMileLayoutRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  return <FirstMileLayout>{children}</FirstMileLayout>;
}
