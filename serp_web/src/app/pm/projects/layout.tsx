/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - PM projects tabs layout
 */

import { PMProjectsTopTabs } from '@/modules/pm/components/projects/PMProjectsTopTabs';

export default function PMProjectsLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className='space-y-4'>
      <PMProjectsTopTabs />
      <div>{children}</div>
    </div>
  );
}
