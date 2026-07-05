/**
 * CRM Calendar Page Route Redirect
 * Author: QuanTuanHuy
 * Description: Part of Serp Project
 */

import { redirect } from 'next/navigation';

export default function CRMCalendarPage() {
  redirect('/crm/activities?view=calendar');
}
