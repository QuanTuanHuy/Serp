// StatusBadge Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import { Badge } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import type {
  LeadStatus,
  OpportunityStage,
  ActivityStatus,
  CustomerStatus,
} from '../../types';

interface StatusBadgeProps {
  status: LeadStatus | OpportunityStage | ActivityStatus | CustomerStatus;
  variant?: 'default' | 'secondary' | 'destructive' | 'outline';
  className?: string;
}

// Status color mappings
const statusColors: Record<string, string> = {
  // Lead Status
  NEW: 'bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200',
  CONTACTED:
    'bg-yellow-100 text-yellow-800 border-yellow-200 hover:bg-yellow-200',
  QUALIFIED: 'bg-green-100 text-green-800 border-green-200 hover:bg-green-200',
  UNQUALIFIED: 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',
  CONVERTED:
    'bg-purple-100 text-purple-800 border-purple-200 hover:bg-purple-200',
  LOST: 'bg-gray-100 text-gray-800 border-gray-200 hover:bg-gray-200',

  // Opportunity Stage
  PROSPECTING: 'bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200',
  QUALIFICATION:
    'bg-indigo-100 text-indigo-800 border-indigo-200 hover:bg-indigo-200',
  PROPOSAL:
    'bg-yellow-100 text-yellow-800 border-yellow-200 hover:bg-yellow-200',
  NEGOTIATION:
    'bg-orange-100 text-orange-800 border-orange-200 hover:bg-orange-200',
  CLOSED_WON: 'bg-green-100 text-green-800 border-green-200 hover:bg-green-200',
  CLOSED_LOST: 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',

  // Activity Status
  PLANNED:
    'bg-yellow-100 text-yellow-800 border-yellow-200 hover:bg-yellow-200',
  IN_PROGRESS: 'bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200',
  COMPLETED: 'bg-green-100 text-green-800 border-green-200 hover:bg-green-200',
  CANCELLED: 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',
  OVERDUE: 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',

  // Customer Status
  ACTIVE: 'bg-green-100 text-green-800 border-green-200 hover:bg-green-200',
  INACTIVE: 'bg-gray-100 text-gray-800 border-gray-200 hover:bg-gray-200',
  POTENTIAL: 'bg-blue-100 text-blue-800 border-blue-200 hover:bg-blue-200',
  CHURNED: 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',
  BLOCKED: 'bg-red-100 text-red-800 border-red-200 hover:bg-red-200',
};

// Status display text mappings
const statusText: Record<string, string> = {
  // Lead Status
  NEW: 'New',
  CONTACTED: 'Contacted',
  QUALIFIED: 'Qualified',
  UNQUALIFIED: 'Unqualified',
  CONVERTED: 'Converted',
  LOST: 'Lost',

  // Opportunity Stage
  PROSPECTING: 'Prospecting',
  QUALIFICATION: 'Qualification',
  PROPOSAL: 'Proposal',
  NEGOTIATION: 'Negotiation',
  CLOSED_WON: 'Closed Won',
  CLOSED_LOST: 'Closed Lost',

  // Activity Status
  PLANNED: 'Planned',
  IN_PROGRESS: 'In Progress',
  COMPLETED: 'Completed',
  CANCELLED: 'Cancelled',
  OVERDUE: 'Overdue',

  // Customer Status
  ACTIVE: 'Active',
  INACTIVE: 'Inactive',
  POTENTIAL: 'Potential',
  CHURNED: 'Churned',
  BLOCKED: 'Blocked',
};

export const StatusBadge: React.FC<StatusBadgeProps> = ({
  status,
  variant = 'outline',
  className,
}) => {
  const colorClass =
    statusColors[status] || 'bg-gray-100 text-gray-800 border-gray-200';
  const displayText = statusText[status] || status;

  return (
    <Badge
      variant={variant}
      className={cn(
        'inline-flex items-center rounded-full border px-2.5 py-0.5 text-xs font-medium transition-colors',
        variant === 'outline' ? colorClass : '',
        className
      )}
    >
      {displayText}
    </Badge>
  );
};

export default StatusBadge;
