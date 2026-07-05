/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Redirect bag distribution to second-mile dispatchers
 */

import { redirect } from 'next/navigation';

export default function FirstMileBagDistributionPage() {
  redirect('/first-mile/dispatchers/second-mile');
}
