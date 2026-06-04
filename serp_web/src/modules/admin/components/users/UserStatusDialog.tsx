'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - User status dialog
 */

import { useMemo } from 'react';
import { useUpdateUserStatusMutation } from '@/modules/admin/services/users/usersApi';
import type { UserStatus } from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';
import { AdminConfirmStatusDialog } from '@/modules/admin/components/shared/AdminConfirmStatusDialog';

interface UserStatusDialogProps {
  open: boolean;
  organizationId?: number;
  userId?: number;
  userName?: string;
  currentStatus?: UserStatus;
  onOpenChange: (open: boolean) => void;
}

export function UserStatusDialog({
  open,
  organizationId,
  userId,
  userName,
  currentStatus,
  onOpenChange,
}: UserStatusDialogProps) {
  const { success, error: showError } = useNotification();
  const [updateUserStatus, statusResult] = useUpdateUserStatusMutation();

  const targetStatus = useMemo(
    () => (currentStatus === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED'),
    [currentStatus]
  );

  const handleConfirm = async () => {
    if (!organizationId || !userId) {
      return;
    }

    try {
      await updateUserStatus({
        organizationId,
        userId,
        status: targetStatus,
      }).unwrap();

      success(
        `${userName ?? 'User'} ${targetStatus === 'SUSPENDED' ? 'suspended' : 'activated'} successfully.`
      );
      onOpenChange(false);
    } catch (error) {
      showError(getErrorMessage(error));
    }
  };

  const title = targetStatus === 'SUSPENDED' ? 'Suspend user' : 'Activate user';
  const description =
    targetStatus === 'SUSPENDED'
      ? 'This will suspend the selected user from accessing the system.'
      : 'This will reactivate the selected user account.';

  return (
    <AdminConfirmStatusDialog
      open={open}
      title={title}
      description={description}
      impactText={userName ? `Target: ${userName}` : undefined}
      confirmLabel={targetStatus === 'SUSPENDED' ? 'Suspend' : 'Activate'}
      confirmVariant={targetStatus === 'SUSPENDED' ? 'destructive' : 'default'}
      onOpenChange={onOpenChange}
      onConfirm={handleConfirm}
      loading={statusResult.isLoading}
    />
  );
}
