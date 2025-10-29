/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Settings layout wrapper
 */

import { SettingsLayout } from '@/modules/settings';

export default function Layout({ children }: { children: React.ReactNode }) {
  return <SettingsLayout>{children}</SettingsLayout>;
}
