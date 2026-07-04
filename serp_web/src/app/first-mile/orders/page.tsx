/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - First-mile orders redirect route
 */

import { redirect } from 'next/navigation';

export default function FirstMileOrdersRoutePage() {
  redirect('/first-mile/orders/list');
}
