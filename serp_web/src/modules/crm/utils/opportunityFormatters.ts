// Author: QuanTuanHuy, Description: Part of Serp Project

export const formatCurrency = (value?: number): string => {
  if (value === undefined) return 'Not available';
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0,
  }).format(value);
};

export const formatDate = (dateString?: string): string => {
  if (!dateString) return 'Not available';
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
};

export const formatDateTime = (dateString?: string): string => {
  if (!dateString) return 'Not available';
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

export const getActivityBadgeClass = (status?: string): string => {
  switch (status) {
    case 'COMPLETED':
      return 'bg-green-100 text-green-700';
    case 'CANCELLED':
      return 'bg-red-100 text-red-700';
    default:
      return 'bg-blue-100 text-blue-700';
  }
};

export const calculateDaysInPipeline = (createdAt?: string): number => {
  if (!createdAt) return 0;
  return Math.floor(
    (new Date().getTime() - new Date(createdAt).getTime()) /
      (1000 * 60 * 60 * 24)
  );
};

export const calculateDaysUntilClose = (expectedCloseDate?: string): number => {
  if (!expectedCloseDate) return 0;
  return Math.floor(
    (new Date(expectedCloseDate).getTime() - new Date().getTime()) /
      (1000 * 60 * 60 * 24)
  );
};
