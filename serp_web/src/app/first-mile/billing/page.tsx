/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile billing redirect route
 */

import { redirect } from 'next/navigation';

export default function FirstMileBillingRoutePage() {
  redirect('/first-mile/settings/billing');
}
