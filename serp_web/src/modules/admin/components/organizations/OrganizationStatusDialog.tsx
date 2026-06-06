'use client';

/**
 * Author: QuanTuanHuy
 * Description: Part of Serp Project - Organization status dialog
 */

import { useMemo } from 'react';
import { useUpdateOrganizationStatusMutation } from '@/modules/admin/services/organizations/organizationsApi';
import type { OrganizationStatus } from '@/modules/admin/types';
import { useNotification } from '@/shared/hooks/use-notification';
import { getErrorMessage } from '@/lib/store/api/utils';
import { AdminConfirmStatusDialog } from '@/modules/admin/components/shared/AdminConfirmStatusDialog';

interface OrganizationStatusDialogProps {
  open: boolean;
  organizationId?: number;
  organizationName?: string;
  currentStatus?: OrganizationStatus;
  onOpenChange: (open: boolean) => void;
}

export function OrganizationStatusDialog({
  open,
  organizationId,
  organizationName,
  currentStatus,
  onOpenChange,
}: OrganizationStatusDialogProps) {
  const { success, error: showError } = useNotification();
  const [updateOrganizationStatus, statusResult] =
    useUpdateOrganizationStatusMutation();

  const targetStatus = useMemo(
    () => (currentStatus === 'SUSPENDED' ? 'ACTIVE' : 'SUSPENDED'),
    [currentStatus]
  );

  const handleConfirm = async () => {
    if (!organizationId) {
      return;
    }

    try {
      const response = await updateOrganizationStatus({
        organizationId,
        status: targetStatus,
      }).unwrap();

      const affectedUsers = response.data?.affectedUsers ?? 0;
      const summary =
        targetStatus === 'SUSPENDED'
          ? `${affectedUsers} users were suspended with the organization.`
          : `${response.data?.activatedUsers ?? 0} suspended users were reactivated.`;

      success(summary);
      onOpenChange(false);
    } catch (error) {
      showError(getErrorMessage(error));
    }
  };

  const title =
    targetStatus === 'SUSPENDED'
      ? 'Suspend organization'
      : 'Activate organization';
  const description =
    targetStatus === 'SUSPENDED'
      ? 'This will suspend the organization and all users in it.'
      : 'This will reactivate the organization and bring suspended users back to active state.';

  return (
    <AdminConfirmStatusDialog
      open={open}
      title={title}
      description={description}
      impactText={organizationName ? `Target: ${organizationName}` : undefined}
      confirmLabel={targetStatus === 'SUSPENDED' ? 'Suspend' : 'Activate'}
      confirmVariant={targetStatus === 'SUSPENDED' ? 'destructive' : 'default'}
      onOpenChange={onOpenChange}
      onConfirm={handleConfirm}
      loading={statusResult.isLoading}
    />
  );
}
