/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Vehicle search card
 */

import React from 'react';
import {
  Button,
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
  Input,
} from '@/shared/components/ui';
import { RefreshCw, Search } from 'lucide-react';

interface VehicleSearchCardProps {
  canViewVehicles: boolean;
  keywordInput: string;
  isFetching: boolean;
  onKeywordInputChange: (value: string) => void;
  onSubmit: (event: React.FormEvent) => void;
  onRefresh: () => void;
}

export const VehicleSearchCard: React.FC<VehicleSearchCardProps> = ({
  canViewVehicles,
  keywordInput,
  isFetching,
  onKeywordInputChange,
  onSubmit,
  onRefresh,
}) => {
  return (
    <Card>
      <CardHeader>
        <CardTitle>Tìm kiếm</CardTitle>
        <CardDescription>
          Tìm phương tiện theo biển số trong phạm vi dữ liệu được phép.
        </CardDescription>
      </CardHeader>
      <CardContent>
        <form onSubmit={onSubmit} className='flex flex-col gap-2 md:flex-row'>
          <div className='relative flex-1'>
            <Search className='absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground' />
            <Input
              className='pl-10'
              value={keywordInput}
              onChange={(event) => onKeywordInputChange(event.target.value)}
              placeholder='Tìm theo biển số xe...'
              disabled={!canViewVehicles}
            />
          </div>
          <Button type='submit' disabled={!canViewVehicles}>
            Áp dụng
          </Button>
          <Button
            type='button'
            variant='outline'
            onClick={onRefresh}
            disabled={!canViewVehicles || isFetching}
          >
            <RefreshCw className='mr-2 h-4 w-4' />
            Làm mới
          </Button>
        </form>
      </CardContent>
    </Card>
  );
};
