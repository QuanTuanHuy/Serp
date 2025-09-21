// EntityCard Component (authors: QuanTuanHuy, Description: Part of Serp Project)

import { Card, CardContent, CardHeader } from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { StatusBadge } from './StatusBadge';
import { PriorityBadge } from './PriorityBadge';
import type {
  Customer,
  Lead,
  Opportunity,
  Activity,
  Priority,
} from '../../types';

interface BaseEntityCardProps {
  onClick?: () => void;
  onEdit?: () => void;
  onDelete?: () => void;
  className?: string;
  children?: React.ReactNode;
}

interface CustomerCardProps extends BaseEntityCardProps {
  type: 'customer';
  entity: Customer;
}

interface LeadCardProps extends BaseEntityCardProps {
  type: 'lead';
  entity: Lead;
}

interface OpportunityCardProps extends BaseEntityCardProps {
  type: 'opportunity';
  entity: Opportunity;
}

interface ActivityCardProps extends BaseEntityCardProps {
  type: 'activity';
  entity: Activity;
}

type EntityCardProps =
  | CustomerCardProps
  | LeadCardProps
  | OpportunityCardProps
  | ActivityCardProps;

// Action Menu Component
const ActionMenu: React.FC<{ onEdit?: () => void; onDelete?: () => void }> = ({
  onEdit,
  onDelete,
}) => (
  <div className='flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity'>
    {onEdit && (
      <button
        onClick={(e) => {
          e.stopPropagation();
          onEdit();
        }}
        className='p-1 rounded-md hover:bg-gray-100 text-gray-500 hover:text-gray-700'
        title='Edit'
      >
        <svg
          className='w-4 h-4'
          fill='none'
          stroke='currentColor'
          viewBox='0 0 24 24'
        >
          <path
            strokeLinecap='round'
            strokeLinejoin='round'
            strokeWidth='2'
            d='M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z'
          />
        </svg>
      </button>
    )}
    {onDelete && (
      <button
        onClick={(e) => {
          e.stopPropagation();
          onDelete();
        }}
        className='p-1 rounded-md hover:bg-red-50 text-gray-500 hover:text-red-600'
        title='Delete'
      >
        <svg
          className='w-4 h-4'
          fill='none'
          stroke='currentColor'
          viewBox='0 0 24 24'
        >
          <path
            strokeLinecap='round'
            strokeLinejoin='round'
            strokeWidth='2'
            d='M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16'
          />
        </svg>
      </button>
    )}
  </div>
);

export const EntityCard: React.FC<EntityCardProps> = ({
  type,
  entity,
  onClick,
  onEdit,
  onDelete,
  className,
  children,
}) => {
  const renderCustomerCard = (customer: Customer) => (
    <>
      <CardHeader className='flex flex-row items-start justify-between space-y-0 pb-2'>
        <div className='flex-1'>
          <h3 className='font-semibold text-sm text-gray-900 truncate'>
            {customer.name}
          </h3>
          <p className='text-xs text-gray-500 truncate'>{customer.email}</p>
          {customer.companyName && (
            <p className='text-xs text-gray-400 truncate'>
              {customer.companyName}
            </p>
          )}
        </div>
        <ActionMenu onEdit={onEdit} onDelete={onDelete} />
      </CardHeader>
      <CardContent className='pt-0'>
        <div className='flex items-center justify-between'>
          <StatusBadge status={customer.status} />
          <span className='text-xs text-gray-500'>
            ${customer.totalValue?.toLocaleString() || '0'}
          </span>
        </div>
        <div className='mt-2 flex items-center gap-2 text-xs text-gray-500'>
          <span>📞 {customer.phone || 'N/A'}</span>
          {customer.assignedSalesRep && (
            <span>👤 {customer.assignedSalesRep}</span>
          )}
        </div>
      </CardContent>
    </>
  );

  const renderLeadCard = (lead: Lead) => (
    <>
      <CardHeader className='flex flex-row items-start justify-between space-y-0 pb-2'>
        <div className='flex-1'>
          <h3 className='font-semibold text-sm text-gray-900 truncate'>
            {lead.firstName} {lead.lastName}
          </h3>
          <p className='text-xs text-gray-500 truncate'>{lead.email}</p>
          {lead.company && (
            <p className='text-xs text-gray-400 truncate'>{lead.company}</p>
          )}
        </div>
        <ActionMenu onEdit={onEdit} onDelete={onDelete} />
      </CardHeader>
      <CardContent className='pt-0'>
        <div className='flex items-center justify-between gap-2'>
          <StatusBadge status={lead.status} />
          <PriorityBadge priority={lead.priority} />
        </div>
        <div className='mt-2 flex items-center gap-2 text-xs text-gray-500'>
          <span>📍 {lead.source}</span>
          {lead.assignedTo && <span>👤 {lead.assignedTo}</span>}
        </div>
      </CardContent>
    </>
  );

  const renderOpportunityCard = (opportunity: Opportunity) => (
    <>
      <CardHeader className='flex flex-row items-start justify-between space-y-0 pb-2'>
        <div className='flex-1'>
          <h3 className='font-semibold text-sm text-gray-900 truncate'>
            {opportunity.name}
          </h3>
          <p className='text-xs text-gray-500 truncate'>
            {opportunity.description}
          </p>
        </div>
        <ActionMenu onEdit={onEdit} onDelete={onDelete} />
      </CardHeader>
      <CardContent className='pt-0'>
        <div className='flex items-center justify-between'>
          <StatusBadge status={opportunity.stage} />
          <span className='text-sm font-semibold text-green-600'>
            ${opportunity.value?.toLocaleString() || '0'}
          </span>
        </div>
        <div className='mt-2 flex items-center gap-2 text-xs text-gray-500'>
          <span>
            📅 {new Date(opportunity.expectedCloseDate).toLocaleDateString()}
          </span>
          <span>📊 {opportunity.probability}%</span>
        </div>
      </CardContent>
    </>
  );

  const renderActivityCard = (activity: Activity) => (
    <>
      <CardHeader className='flex flex-row items-start justify-between space-y-0 pb-2'>
        <div className='flex-1'>
          <h3 className='font-semibold text-sm text-gray-900 truncate'>
            {activity.subject}
          </h3>
          <p className='text-xs text-gray-500 truncate'>
            {activity.description}
          </p>
        </div>
        <ActionMenu onEdit={onEdit} onDelete={onDelete} />
      </CardHeader>
      <CardContent className='pt-0'>
        <div className='flex items-center justify-between gap-2'>
          <StatusBadge status={activity.status} />
          <PriorityBadge priority={activity.priority} />
        </div>
        <div className='mt-2 flex items-center gap-2 text-xs text-gray-500'>
          <span>🏷️ {activity.type}</span>
          <span>
            📅{' '}
            {activity.scheduledDate
              ? new Date(activity.scheduledDate).toLocaleDateString()
              : 'Not scheduled'}
          </span>
        </div>
      </CardContent>
    </>
  );

  return (
    <Card
      className={cn(
        'group cursor-pointer transition-all duration-200 hover:shadow-md hover:border-gray-300',
        className
      )}
      onClick={onClick}
    >
      {type === 'customer' && renderCustomerCard(entity as Customer)}
      {type === 'lead' && renderLeadCard(entity as Lead)}
      {type === 'opportunity' && renderOpportunityCard(entity as Opportunity)}
      {type === 'activity' && renderActivityCard(entity as Activity)}
      {children}
    </Card>
  );
};

export default EntityCard;
