/**
 * Author: Nguyễn Thế Anh
 * Description: Part of Serp Project - First-mile API transform helpers
 */

import { transformTimestampFields } from '@/lib/store/api/utils';
import type {
  FirstMileApiResponse,
  FirstMilePageResponse,
  FirstMilePaginatedData,
} from '../types';

export const unwrapFirstMileResult = <T>(
  response: FirstMileApiResponse<T>
): T => {
  return transformTimestampFields((response?.result ?? null) as T);
};

export const unwrapFirstMilePageResult = <T>(
  response: FirstMileApiResponse<FirstMilePageResponse<T>>
): FirstMilePaginatedData<T> => {
  const pageData = response?.result;

  if (!pageData) {
    return {
      items: [],
      totalItems: 0,
      totalPages: 0,
      currentPage: 0,
      pageSize: 0,
      hasNext: false,
      hasPrevious: false,
    };
  }

  return {
    items: transformTimestampFields(pageData.items || []),
    totalItems: pageData.totalElements || 0,
    totalPages: pageData.totalPages || 0,
    currentPage: pageData.page || 0,
    pageSize: pageData.size || 0,
    hasNext: !!pageData.hasNext,
    hasPrevious: !!pageData.hasPrevious,
  };
};
