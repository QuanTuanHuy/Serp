/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Bag distribution ready bags tab
 */

import { AlertCircle, Loader2, MapPinned, Search } from 'lucide-react';

import { Button } from '@/shared/components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui/card';
import { Checkbox } from '@/shared/components/ui/checkbox';
import { Input } from '@/shared/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui/select';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/shared/components/ui/table';

import type { SecondMileBag } from '../../../../types';
import type {
  DestinationFilter,
  SelectedBagSummary,
} from '../secondMileDispatchModels';
import {
  destinationLabel,
  formatDateTime,
  formatNumber,
} from '../secondMileDispatchModels';

interface ReadyBagsTabProps {
  bags: SecondMileBag[];
  selectedBagIds: number[];
  isFetching: boolean;
  readySearch: string;
  destinationFilter: DestinationFilter;
  selectedDestinationSummary: SelectedBagSummary | null;
  onReadySearchChange: (value: string) => void;
  onDestinationFilterChange: (value: DestinationFilter) => void;
  onToggleBag: (bag: SecondMileBag, checked: boolean) => void;
  onSelectAll: (checked: boolean) => void;
  onUseBagDestination: () => void;
}

export function ReadyBagsTab({
  bags,
  selectedBagIds,
  isFetching,
  readySearch,
  destinationFilter,
  selectedDestinationSummary,
  onReadySearchChange,
  onDestinationFilterChange,
  onToggleBag,
  onSelectAll,
  onUseBagDestination,
}: ReadyBagsTabProps) {
  const allSelected = bags.length > 0 && selectedBagIds.length === bags.length;

  return (
    <Card>
      <CardHeader>
        <div className='flex flex-col gap-3 lg:flex-row lg:items-start lg:justify-between'>
          <div>
            <CardTitle>Túi hàng sẵn sàng</CardTitle>
            <CardDescription>
              Chọn các túi đã niêm phong, dùng cùng điểm đến để lập kế hoạch
              hoặc tạo biên bản điều phối thủ công.
            </CardDescription>
          </div>
          <Button
            variant='outline'
            disabled={!selectedDestinationSummary?.sameDestination}
            onClick={onUseBagDestination}
          >
            <MapPinned className='h-4 w-4' />
            Dùng điểm đến đã chọn
          </Button>
        </div>
      </CardHeader>
      <CardContent className='space-y-4'>
        <div className='grid gap-3 md:grid-cols-[1fr_220px]'>
          <div className='relative'>
            <Search className='absolute left-3 top-2.5 h-4 w-4 text-muted-foreground' />
            <Input
              value={readySearch}
              onChange={(event) => onReadySearchChange(event.target.value)}
              placeholder='Tìm theo mã túi'
              className='pl-9'
            />
          </div>
          <Select
            value={destinationFilter}
            onValueChange={(value) =>
              onDestinationFilterChange(value as DestinationFilter)
            }
          >
            <SelectTrigger>
              <SelectValue placeholder='Loại điểm đến' />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value='ALL'>Tất cả điểm đến</SelectItem>
              <SelectItem value='HUB'>Hub</SelectItem>
              <SelectItem value='POST_OFFICE'>Bưu cục</SelectItem>
            </SelectContent>
          </Select>
        </div>

        {selectedDestinationSummary &&
          !selectedDestinationSummary.sameDestination && (
            <div className='flex items-start gap-2 rounded-md border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive'>
              <AlertCircle className='mt-0.5 h-4 w-4' />
              Các túi đã chọn không cùng hub xuất phát và điểm đến. Hãy chọn
              một nhóm điểm đến trước khi tạo biên bản.
            </div>
          )}

        <div className='overflow-x-auto rounded-md border'>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead className='w-10'>
                  <Checkbox
                    checked={allSelected}
                    onCheckedChange={(checked) => onSelectAll(Boolean(checked))}
                    aria-label='Chọn tất cả túi sẵn sàng'
                  />
                </TableHead>
                <TableHead>Mã túi</TableHead>
                <TableHead>Điểm đến</TableHead>
                <TableHead>Đơn hàng</TableHead>
                <TableHead>Khối lượng</TableHead>
                <TableHead>Thể tích</TableHead>
                <TableHead>Niêm phong lúc</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {bags.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className='h-24 text-center'>
                    {isFetching ? (
                      <span className='inline-flex items-center gap-2 text-muted-foreground'>
                        <Loader2 className='h-4 w-4 animate-spin' />
                        Đang tải túi sẵn sàng...
                      </span>
                    ) : (
                      <span className='text-muted-foreground'>
                        Không có túi đã niêm phong phù hợp bộ lọc.
                      </span>
                    )}
                  </TableCell>
                </TableRow>
              ) : (
                bags.map((bag) => (
                  <TableRow key={bag.id}>
                    <TableCell>
                      <Checkbox
                        checked={selectedBagIds.includes(bag.id)}
                        onCheckedChange={(checked) =>
                          onToggleBag(bag, Boolean(checked))
                        }
                        aria-label={`Chọn ${bag.bagCode ?? `túi ${bag.id}`}`}
                      />
                    </TableCell>
                    <TableCell className='font-medium'>
                      {bag.bagCode ?? `Túi #${bag.id}`}
                    </TableCell>
                    <TableCell>
                      {destinationLabel(
                        bag.destinationType,
                        bag.destinationHubId,
                        undefined,
                        bag.destinationPostOfficeCode
                      )}
                    </TableCell>
                    <TableCell>{formatNumber(bag.currentOrders, 0)}</TableCell>
                    <TableCell>{formatNumber(bag.currentWeight)} kg</TableCell>
                    <TableCell>{formatNumber(bag.currentVolume)} m3</TableCell>
                    <TableCell>{formatDateTime(bag.sealedAt)}</TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </div>
      </CardContent>
    </Card>
  );
}
