/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Utility functions for API responses
 */

import type { ApiResponse, ApiError } from './types';

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
