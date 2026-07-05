/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Redirect driver handover to transit
 */

import { redirect } from 'next/navigation';

export default function Page() {
  redirect('/first-mile/pickup-and-delivery/transit');
}
