/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile app layout route
 */

import { FirstMileLayout } from '@/modules/first-mile';

export default function FirstMileLayoutRoute({
  children,
}: {
  children: React.ReactNode;
}) {
  return <FirstMileLayout>{children}</FirstMileLayout>;
}
