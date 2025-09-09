/**
 * Use Notification Hook - Toast notifications management
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

'use client';

import { toast } from 'sonner';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface NotificationOptions {
  title?: string;
  description?: string;
  duration?: number;
  action?: {
    label: string;
    onClick: () => void;
  };
  cancel?: {
    label: string;
    onClick?: () => void;
  };
}

export function useNotification() {
  const showNotification = (
    type: NotificationType,
    message: string,
    options?: NotificationOptions
  ) => {
    const toastOptions: any = {
      description: options?.description,
      duration: options?.duration || 4000,
    };

    if (options?.action) {
      toastOptions.action = {
        label: options.action.label,
        onClick: options.action.onClick,
      };
    }

    if (options?.cancel) {
      toastOptions.cancel = {
        label: options.cancel.label,
        onClick: options.cancel.onClick || (() => {}),
      };
    }

    switch (type) {
      case 'success':
        return toast.success(message, toastOptions);
      case 'error':
        return toast.error(message, toastOptions);
      case 'warning':
        return toast.warning(message, toastOptions);
      case 'info':
        return toast.info(message, toastOptions);
      default:
        return toast(message, toastOptions);
    }
  };

  const success = (message: string, options?: NotificationOptions) => {
    return showNotification('success', message, options);
  };

  const error = (message: string, options?: NotificationOptions) => {
    return showNotification('error', message, options);
  };

  const warning = (message: string, options?: NotificationOptions) => {
    return showNotification('warning', message, options);
  };

  const info = (message: string, options?: NotificationOptions) => {
    return showNotification('info', message, options);
  };

  const promise = <T>(
    promise: Promise<T>,
    options: {
      loading: string;
      success: string | ((data: T) => string);
      error: string | ((error: any) => string);
    }
  ) => {
    return toast.promise(promise, options);
  };

  const dismiss = (id?: string | number) => {
    if (id) {
      toast.dismiss(id);
    } else {
      toast.dismiss();
    }
  };

  return {
    show: showNotification,
    success,
    error,
    warning,
    info,
    promise,
    dismiss,
  };
}
