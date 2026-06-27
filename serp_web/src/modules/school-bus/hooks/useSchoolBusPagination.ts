'use client';

import * as React from 'react';
import type { SchoolBusListParams } from '../types';

export function useSchoolBusPagination(
  initial: SchoolBusListParams = {
    page: 0,
    size: 10,
    sortDirection: 'DESC',
  }
) {
  const [params, setParams] = React.useState<SchoolBusListParams>(initial);

  const setPage = React.useCallback((page: number) => {
    const nextPage = Math.max(page, 0);
    setParams((current) =>
      current.page === nextPage ? current : { ...current, page: nextPage }
    );
  }, []);

  const setKeyword = React.useCallback((keyword: string) => {
    const normalizedKeyword = keyword.trim();
    setParams((current) => {
      const currentKeyword = current.keyword ?? '';
      if (currentKeyword === normalizedKeyword && current.page === 0) {
        return current;
      }

      if (!normalizedKeyword) {
        const { keyword: _keyword, ...paramsWithoutKeyword } = current;
        return { ...paramsWithoutKeyword, page: 0 };
      }

      return { ...current, keyword: normalizedKeyword, page: 0 };
    });
  }, []);

  return { params, setParams, setPage, setKeyword };
}
