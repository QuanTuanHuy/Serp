/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Billing redirect route
 */

import { redirect } from 'next/navigation';

export default function BillingRoutePage() {
  redirect('/first-mile/settings/billing');
}
