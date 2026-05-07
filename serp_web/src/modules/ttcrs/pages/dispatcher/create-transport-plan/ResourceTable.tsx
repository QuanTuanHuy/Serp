import {
  Badge,
  Card,
  CardContent,
  CardHeader,
  CardTitle,
  Checkbox,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui';
import { cn } from '@/shared/utils';
import { SkeletonRow } from './common';

export interface DepotColumnConfig<T> {
  header: string;
  renderCell: (item: T, isSelected: boolean) => React.ReactNode;
}

interface ResourceTableProps<T extends { id: number }> {
  title: string;
  icon: React.ReactNode;
  items: T[];
  isLoading: boolean;
  selectedIds: Set<number>;
  onToggle: (item: T) => void;
  renderRow: (item: T) => React.ReactNode;
  headers: string[];
  depotColumn?: DepotColumnConfig<T>;
}

export function ResourceTable<T extends { id: number }>({
  title,
  icon,
  items,
  isLoading,
  selectedIds,
  onToggle,
  renderRow,
  headers,
  depotColumn,
}: ResourceTableProps<T>) {
  const allSelected = items.length > 0 && items.every((item) => selectedIds.has(item.id));

  const toggleAll = () => {
    if (allSelected) {
      items.forEach((item) => selectedIds.has(item.id) && onToggle(item));
      return;
    }

    items.forEach((item) => !selectedIds.has(item.id) && onToggle(item));
  };

  const totalHeaders = headers.length + (depotColumn ? 1 : 0);

  return (
    <Card className='flex flex-col gap-0 py-0'>
      <CardHeader className='border-b px-4 py-3'>
        <CardTitle className='flex items-center gap-2 text-sm font-semibold'>
          {icon}
          {title}
          {selectedIds.size > 0 && (
            <Badge className='ml-auto border-orange-200 bg-orange-100 text-xs text-orange-700'>
              {selectedIds.size} selected
            </Badge>
          )}
        </CardTitle>
      </CardHeader>
      <CardContent className='max-h-72 overflow-y-auto p-0'>
        <Table>
          <TableHeader>
            <TableRow className='bg-muted/20 hover:bg-muted/20'>
              <TableHead className='w-10 px-3 py-2'>
                <Checkbox
                  checked={allSelected}
                  onCheckedChange={toggleAll}
                  disabled={isLoading || items.length === 0}
                />
              </TableHead>
              {headers.map((header) => (
                <TableHead
                  key={header}
                  className='px-3 py-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground'
                >
                  {header}
                </TableHead>
              ))}
              {depotColumn && (
                <TableHead className='px-3 py-2 text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
                  {depotColumn.header}
                </TableHead>
              )}
            </TableRow>
          </TableHeader>
          <TableBody>
            {isLoading ? (
              [...Array(3)].map((_, index) => <SkeletonRow key={index} cols={totalHeaders + 1} />)
            ) : items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={totalHeaders + 1}>
                  <p className='py-6 text-center text-xs text-muted-foreground'>
                    No resources available
                  </p>
                </TableCell>
              </TableRow>
            ) : (
              items.map((item) => (
                <TableRow
                  key={item.id}
                  className={cn(
                    'cursor-pointer',
                    selectedIds.has(item.id) && 'bg-orange-50 dark:bg-orange-950/20'
                  )}
                  onClick={() => onToggle(item)}
                >
                  <TableCell className='px-3 py-2' onClick={(e) => e.stopPropagation()}>
                    <Checkbox
                      checked={selectedIds.has(item.id)}
                      onCheckedChange={() => onToggle(item)}
                    />
                  </TableCell>
                  {renderRow(item)}
                  {depotColumn && depotColumn.renderCell(item, selectedIds.has(item.id))}
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </CardContent>
    </Card>
  );
}
