/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS route redirect helper
 */

import { redirect } from 'next/navigation';

type RedirectSearchParams = Record<string, string | string[] | undefined>;

export interface RedirectWithSearchParamsPageProps {
  searchParams: Promise<RedirectSearchParams>;
}

export async function redirectWithSearchParams(
  pathname: string,
  searchParams: Promise<RedirectSearchParams>
) {
  const params = await searchParams;
  const query = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (Array.isArray(value)) {
      value.forEach((item) => query.append(key, item));
      return;
    }

    if (value !== undefined) {
      query.set(key, value);
    }
  });

  const queryString = query.toString();

  redirect(queryString ? `${pathname}?${queryString}` : pathname);
}
