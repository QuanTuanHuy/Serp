/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile post offices route redirect
 */

import {
  type RedirectWithSearchParamsPageProps,
  redirectWithSearchParams,
} from '../redirectWithSearchParams';

export default async function FirstMilePostOfficesRoutePage({
  searchParams,
}: RedirectWithSearchParamsPageProps) {
  return redirectWithSearchParams(
    '/first-mile/network/post-office',
    searchParams
  );
}
