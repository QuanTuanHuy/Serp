/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Second-mile bag results table
 */

import { Eye, ListPlus, Lock, Pencil, RotateCcw, Trash2 } from 'lucide-react';

import {
  Badge,
  Button,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Progress,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
  Tooltip,
  TooltipContent,
  TooltipTrigger,
} from '@/shared/components';

import type {
  FirstMilePaginatedData,
  Hub,
  SecondMileBag,
  SecondMileVehicle,
} from '../../../types';
import {
  formatDateTime,
  formatNumber,
  getBagStatusLabel,
  getBagStatusVariant,
  getDestinationLabel,
  getHubLabel,
  getVehicleLabel,
} from '../bagPageModels';

interface BagResultsTableProps {
  data?: FirstMilePaginatedData<SecondMileBag>;
  hubs: Hub[];
  vehicles: SecondMileVehicle[];
  isFetching?: boolean;
  canManage: boolean;
  canOperate: boolean;
  page: number;
  onPageChange: (page: number) => void;
  onView: (bag: SecondMileBag) => void;
  onEdit: (bag: SecondMileBag) => void;
  onDelete: (bag: SecondMileBag) => void;
  onScan: (bag: SecondMileBag) => void;
  onSeal: (bag: SecondMileBag) => void;
  onReopen: (bag: SecondMileBag) => void;
}

export function BagResultsTable({
  data,
  hubs,
  vehicles,
  isFetching,
  canManage,
  canOperate,
  page,
  onPageChange,
  onView,
  onEdit,
  onDelete,
  onScan,
  onSeal,
  onReopen,
}: BagResultsTableProps) {
  const bags = data?.items ?? [];

  return (
    <Card>
      <CardHeader>
        <CardTitle>Results ({data?.totalItems ?? 0})</CardTitle>
      </CardHeader>
      <CardContent>
        <div className='overflow-x-auto'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Bag code</TableHead>
                <TableHead>Origin hub</TableHead>
                <TableHead>Destination</TableHead>
                <TableHead>Vehicle</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Orders</TableHead>
                <TableHead>Weight</TableHead>
                <TableHead>Volume</TableHead>
                <TableHead>Sealed at</TableHead>
                <TableHead className='text-right'>Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {bags.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={10}
                    className='h-24 text-center text-muted-foreground'
                  >
                    {isFetching ? 'Loading bags...' : 'No bags found.'}
                  </TableCell>
                </TableRow>
              ) : (
                bags.map((bag) => (
                  <TableRow key={bag.id}>
                    <TableCell className='font-medium'>
                      {bag.bagCode ?? `Bag #${bag.id}`}
                    </TableCell>
                    <TableCell className='min-w-48 text-xs text-muted-foreground'>
                      {getHubLabel(hubs, bag.originHubId)}
                    </TableCell>
                    <TableCell className='min-w-48 text-xs text-muted-foreground'>
                      {getDestinationLabel(bag, hubs)}
                    </TableCell>
                    <TableCell className='text-xs text-muted-foreground'>
                      {getVehicleLabel(vehicles, bag.vehicleId)}
                    </TableCell>
                    <TableCell>
                      <Badge variant={getBagStatusVariant(bag.status)}>
                        {getBagStatusLabel(bag.status)}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {formatNumber(bag.currentOrders, 0)} /{' '}
                      {formatNumber(bag.maxOrders, 0)}
                    </TableCell>
                    <TableCell className='min-w-36'>
                      <CapacityValue
                        current={bag.currentWeight}
                        max={bag.maxWeight}
                        suffix='kg'
                      />
                    </TableCell>
                    <TableCell className='min-w-36'>
                      <CapacityValue
                        current={bag.currentVolume}
                        max={bag.maxVolume}
                        suffix='m3'
                      />
                    </TableCell>
                    <TableCell className='min-w-36 text-xs text-muted-foreground'>
                      {formatDateTime(bag.sealedAt)}
                    </TableCell>
                    <TableCell className='text-right'>
                      <div className='flex justify-end gap-1'>
                        <IconAction
                          label='View detail'
                          onClick={() => onView(bag)}
                        >
                          <Eye className='h-4 w-4' />
                        </IconAction>
                        {bag.status === 'CREATED' && canOperate && (
                          <IconAction
                            label='Add orders'
                            onClick={() => onScan(bag)}
                          >
                            <ListPlus className='h-4 w-4' />
                          </IconAction>
                        )}
                        {bag.status === 'CREATED' && canManage && (
                          <>
                            <IconAction
                              label='Edit'
                              onClick={() => onEdit(bag)}
                            >
                              <Pencil className='h-4 w-4' />
                            </IconAction>
                            <IconAction
                              label='Seal bag'
                              onClick={() => onSeal(bag)}
                              disabled={(bag.currentOrders ?? 0) <= 0}
                            >
                              <Lock className='h-4 w-4' />
                            </IconAction>
                            <IconAction
                              label='Delete'
                              onClick={() => onDelete(bag)}
                              variant='destructive'
                            >
                              <Trash2 className='h-4 w-4' />
                            </IconAction>
                          </>
                        )}
                        {bag.status === 'SEALED' && canManage && (
                          <IconAction
                            label='Reopen'
                            onClick={() => onReopen(bag)}
                          >
                            <RotateCcw className='h-4 w-4' />
                          </IconAction>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>

        <div className='mt-4 flex items-center justify-between text-sm text-muted-foreground'>
          <span>
            Page {page + 1} of {Math.max(data?.totalPages ?? 1, 1)}
          </span>
          <div className='flex gap-2'>
            <Button
              variant='outline'
              size='sm'
              disabled={!data?.hasPrevious}
              onClick={() => onPageChange(Math.max(page - 1, 0))}
            >
              Previous
            </Button>
            <Button
              variant='outline'
              size='sm'
              disabled={!data?.hasNext}
              onClick={() => onPageChange(page + 1)}
            >
              Next
            </Button>
          </div>
        </div>
      </CardContent>
    </Card>
  );
}

interface IconActionProps {
  label: string;
  onClick: () => void;
  children: React.ReactNode;
  disabled?: boolean;
  variant?: 'ghost' | 'destructive';
}

function IconAction({
  label,
  onClick,
  children,
  disabled,
  variant = 'ghost',
}: IconActionProps) {
  return (
    <Tooltip>
      <TooltipTrigger asChild>
        <Button
          type='button'
          variant={variant}
          size='icon'
          disabled={disabled}
          onClick={onClick}
          aria-label={label}
        >
          {children}
        </Button>
      </TooltipTrigger>
      <TooltipContent>{label}</TooltipContent>
    </Tooltip>
  );
}

interface CapacityValueProps {
  current?: number;
  max?: number;
  suffix: string;
}

function CapacityValue({ current, max, suffix }: CapacityValueProps) {
  const ratio =
    max && max > 0 ? Math.min(((current ?? 0) / max) * 100, 100) : 0;
  return (
    <div className='space-y-1'>
      <div className='text-xs text-muted-foreground'>
        {formatNumber(current)} / {formatNumber(max)} {suffix}
      </div>
      <Progress value={ratio} className='h-1.5' />
    </div>
  );
}
