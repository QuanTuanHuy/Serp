/**
 * PTM v2 - Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM module layout wrapper
 */

import { PTMLayout } from '@/modules/ptmv2';

export default function PTMv2Layout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <PTMLayout>{children}</PTMLayout>;
}
