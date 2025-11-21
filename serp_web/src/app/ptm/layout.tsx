/**
 * PTM v2 - Layout
 *
 * @author QuanTuanHuy
 * @description Part of Serp Project - PTM module layout wrapper
 */

import { PTMLayout as Layout } from '@/modules/ptm';

export default function PTMLayout({ children }: { children: React.ReactNode }) {
  return <Layout>{children}</Layout>;
}
