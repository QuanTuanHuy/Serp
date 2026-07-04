/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Order access scope banner
 */

import React from 'react';
import { TmsAccessScopeBanner } from '../../../../components/list';

interface OrderAccessScopeCardProps {
  canViewOrders: boolean;
  badgeLabel: string;
  description: string;
  className?: string;
}

export const OrderAccessScopeCard: React.FC<OrderAccessScopeCardProps> = ({
  canViewOrders,
  badgeLabel,
  description,
  className,
}) => {
  return (
    <TmsAccessScopeBanner
      canAccess={canViewOrders}
      badgeLabel={badgeLabel}
      description={description}
      className={className}
    />
  );
};
