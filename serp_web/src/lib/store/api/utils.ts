/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Utility functions for API responses
 */

import type { ApiResponse, ApiError, PaginatedResponse } from './types';
import { epochToISOString } from '../../../shared/utils/dateTransformer';

/**
 * Check if API response is successful
 */
export const isSuccessResponse = (response: any): boolean => {
  return response?.code === 200 && response?.status.toLowerCase() === 'success';
};

/**
 * Check if response is an error
 */
export const isErrorResponse = (response: any): response is ApiError => {
  return response?.code !== 200 || response?.status.toLowerCase() !== 'success';
};

/**
 * Transform timestamp fields in API response data
 * Converts epoch timestamps to ISO strings for frontend consistency
 */
export const transformTimestampFields = <T>(data: T): T => {
  if (!data || typeof data !== 'object') {
    return data;
  }

  if (Array.isArray(data)) {
    return data.map((item) => transformTimestampFields(item)) as T;
  }

  const transformed = { ...data } as any;

  const timestampFields = ['createdAt', 'updatedAt'];

  timestampFields.forEach((field) => {
    if (transformed[field] && typeof transformed[field] === 'number') {
      transformed[field] = epochToISOString(transformed[field]);
    }
  });

  Object.keys(transformed).forEach((key) => {
    if (transformed[key] && typeof transformed[key] === 'object') {
      transformed[key] = transformTimestampFields(transformed[key]);
    }
  });

  return transformed;
};

/**
 * Standard transform response for RTK Query endpoints
 * Handles API response structure and timestamp transformation
 */
export const createRtkTransformResponse = () => {
  return (response: any) => {
    if (response?.code === 200 && response?.status === 'success') {
      return {
        ...response,
        data: transformTimestampFields(response.data),
      };
    }
    return response;
  };
};

export const createDataTransform = <T>() => {
  return (response: any): T => {
    if (response?.code === 200 && response?.status === 'success') {
      return transformTimestampFields(response.data);
    }
    return response;
  };
};

export const createPaginatedTransform = <T>() => {
  return (response: any): PaginatedResponse<T> => {
    if (response?.code === 200 && response?.status === 'success') {
      const pageData = response.data;

      return {
        code: response.code,
        status: response.status,
        message: response.message,
        data: {
          items: transformTimestampFields(pageData.items || []),
          totalItems: pageData.totalItems || 0,
          totalPages: pageData.totalPages || 0,
          currentPage: pageData.currentPage || 0,
        },
      };
    }
    return response;
  };
};

/**
 * Extract error message from API response
 */
export const getErrorMessage = (error: any): string => {
  if (error?.data?.message) {
    return error.data.message;
  }

  if (error?.message) {
    return error.message;
  }

  if (error?.data?.data?.errors) {
    const errors = error.data.data.errors;
    const firstError = Object.values(errors)[0];
    if (Array.isArray(firstError) && firstError.length > 0) {
      return firstError[0];
    }
  }

  return 'An unexpected error occurred';
};

/**
 * Create a standardized success response
 */
export const createSuccessResponse = <T>(
  data: T,
  message = 'Success'
): ApiResponse<T> => ({
  code: 200,
  status: 'success',
  message,
  data,
});

/**
 * Create a standardized error response
 */
export const createErrorResponse = (
  code: number = 500,
  message = 'Error',
  details?: any
): ApiError => ({
  code,
  status: 'error',
  message,
  data: details ? { details } : undefined,
});
