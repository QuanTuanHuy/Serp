import { Plus, X } from 'lucide-react';
import {
  Badge,
  Button,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
  TableCell,
} from '@/shared/components/ui';
import type { LocationItem } from '../../../types';

interface ReturnDepotCellProps {
  resourceId: number;
  isSelected: boolean;
  depotCodes: string[];
  availableDepots: LocationItem[];
  isDepotsLoading: boolean;
  onAdd: (resourceId: number, code: string) => void;
  onRemove: (resourceId: number, code: string) => void;
}

export function ReturnDepotCell({
  resourceId,
  isSelected,
  depotCodes,
  availableDepots,
  isDepotsLoading,
  onAdd,
  onRemove,
}: ReturnDepotCellProps) {
  if (!isSelected) {
    return <TableCell className='px-3 py-2 text-xs text-muted-foreground'>-</TableCell>;
  }

  const remaining = availableDepots.filter(
    (depot) => !depotCodes.includes(depot.locationCode)
  );

  return (
    <TableCell className='px-3 py-2' onClick={(e) => e.stopPropagation()}>
      <div className='flex flex-wrap items-center gap-1'>
        {depotCodes.map((code) => (
          <Badge
            key={code}
            variant='secondary'
            className='flex items-center gap-0.5 py-0 pl-1.5 pr-0.5 font-mono text-xs'
          >
            {code}
            <button
              type='button'
              className='ml-0.5 rounded hover:text-destructive focus:outline-none'
              onClick={() => onRemove(resourceId, code)}
            >
              <X className='h-3 w-3' />
            </button>
          </Badge>
        ))}

        {remaining.length > 0 && (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button
                type='button'
                variant='outline'
                size='sm'
                className='h-6 gap-1 border-dashed px-2 text-xs'
                disabled={isDepotsLoading}
              >
                <Plus className='h-3 w-3' />
                Add
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='start' className='max-h-48 overflow-y-auto'>
              {remaining.map((depot) => (
                <DropdownMenuItem
                  key={depot.locationCode}
                  className='font-mono text-xs'
                  onSelect={() => onAdd(resourceId, depot.locationCode)}
                >
                  {depot.locationCode}
                </DropdownMenuItem>
              ))}
            </DropdownMenuContent>
          </DropdownMenu>
        )}

        {depotCodes.length === 0 && remaining.length === 0 && (
          <span className='text-xs italic text-muted-foreground'>No depots available</span>
        )}
      </div>
    </TableCell>
  );
}
