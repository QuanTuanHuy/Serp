/*
Author: QuanTuanHuy
Description: Part of Serp Project - Quick Add Opportunity Dialog for CRM
*/

'use client';

import { useState } from 'react';
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Button,
  Input,
  Label,
  Textarea,
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/shared/components/ui';
import {
  Target,
  Building2,
  DollarSign,
  Calendar,
  FileText,
  Loader2,
  TrendingUp,
  Users,
  Percent,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import type { OpportunityStage, OpportunityType } from '../../types';
import { MOCK_CUSTOMERS } from '../../mocks';

export interface QuickOpportunityFormData {
  name: string;
  customerId: string;
  stage: OpportunityStage;
  type: OpportunityType;
  value: number;
  probability: number;
  expectedCloseDate: string;
  description?: string;
  nextAction?: string;
}

interface QuickAddOpportunityDialogProps {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onSubmit: (data: QuickOpportunityFormData) => void;
  isLoading?: boolean;
  preselectedCustomerId?: string;
}

export const QuickAddOpportunityDialog: React.FC<
  QuickAddOpportunityDialogProps
> = ({
  open,
  onOpenChange,
  onSubmit,
  isLoading = false,
  preselectedCustomerId,
}) => {
  const [formData, setFormData] = useState<QuickOpportunityFormData>({
    name: '',
    customerId: preselectedCustomerId || '',
    stage: 'PROSPECTING',
    type: 'NEW_BUSINESS',
    value: 0,
    probability: 10,
    expectedCloseDate: '',
    description: '',
    nextAction: '',
  });

  const [errors, setErrors] = useState<
    Partial<Record<keyof QuickOpportunityFormData, string>>
  >({});

  // Stage probability mapping
  const STAGE_PROBABILITY: Record<OpportunityStage, number> = {
    PROSPECTING: 10,
    QUALIFICATION: 25,
    PROPOSAL: 50,
    NEGOTIATION: 75,
    CLOSED_WON: 100,
    CLOSED_LOST: 0,
  };

  const validateForm = (): boolean => {
    const newErrors: Partial<Record<keyof QuickOpportunityFormData, string>> =
      {};

    if (!formData.name.trim()) {
      newErrors.name = 'Vui lòng nhập tên cơ hội';
    }

    if (!formData.customerId) {
      newErrors.customerId = 'Vui lòng chọn khách hàng';
    }

    if (formData.value <= 0) {
      newErrors.value = 'Giá trị phải lớn hơn 0';
    }

    if (!formData.expectedCloseDate) {
      newErrors.expectedCloseDate = 'Vui lòng chọn ngày đóng dự kiến';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateForm()) {
      onSubmit(formData);
      resetForm();
    }
  };

  const resetForm = () => {
    setFormData({
      name: '',
      customerId: preselectedCustomerId || '',
      stage: 'PROSPECTING',
      type: 'NEW_BUSINESS',
      value: 0,
      probability: 10,
      expectedCloseDate: '',
      description: '',
      nextAction: '',
    });
    setErrors({});
  };

  const handleChange = (
    field: keyof QuickOpportunityFormData,
    value: string | number
  ) => {
    setFormData((prev) => {
      const newData = { ...prev, [field]: value };

      // Auto-update probability when stage changes
      if (field === 'stage') {
        newData.probability = STAGE_PROBABILITY[value as OpportunityStage];
      }

      return newData;
    });

    if (errors[field]) {
      setErrors((prev) => ({ ...prev, [field]: undefined }));
    }
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('vi-VN').format(value);
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className='!max-w-3xl max-h-[90vh] overflow-y-auto'>
        <DialogHeader>
          <DialogTitle>Tạo cơ hội kinh doanh</DialogTitle>
          <DialogDescription>
            Nhập thông tin cơ hội kinh doanh mới
          </DialogDescription>
        </DialogHeader>

        <form onSubmit={handleSubmit} className='space-y-4 py-4'>
          {/* Opportunity Name */}
          <div className='space-y-2'>
            <Label htmlFor='name' className='flex items-center gap-2'>
              <Target className='h-4 w-4 text-muted-foreground' />
              Tên cơ hội <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='name'
              value={formData.name}
              onChange={(e) => handleChange('name', e.target.value)}
              placeholder='VD: Hợp đồng dịch vụ Q1 2025'
              className={cn(errors.name && 'border-red-500')}
            />
            {errors.name && (
              <p className='text-xs text-red-500'>{errors.name}</p>
            )}
          </div>

          {/* Customer Selection */}
          <div className='space-y-2'>
            <Label className='flex items-center gap-2'>
              <Building2 className='h-4 w-4 text-muted-foreground' />
              Khách hàng <span className='text-red-500'>*</span>
            </Label>
            <Select
              value={formData.customerId}
              onValueChange={(value) => handleChange('customerId', value)}
            >
              <SelectTrigger
                className={cn(errors.customerId && 'border-red-500')}
              >
                <SelectValue placeholder='Chọn khách hàng' />
              </SelectTrigger>
              <SelectContent>
                {MOCK_CUSTOMERS.map((customer) => (
                  <SelectItem key={customer.id} value={customer.id}>
                    <div className='flex items-center gap-2'>
                      <span>{customer.name}</span>
                      {customer.companyName && (
                        <span className='text-muted-foreground'>
                          ({customer.companyName})
                        </span>
                      )}
                    </div>
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {errors.customerId && (
              <p className='text-xs text-red-500'>{errors.customerId}</p>
            )}
          </div>

          {/* Type & Stage */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <TrendingUp className='h-4 w-4 text-muted-foreground' />
                Loại
              </Label>
              <Select
                value={formData.type}
                onValueChange={(value: OpportunityType) =>
                  handleChange('type', value)
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='NEW_BUSINESS'>Kinh doanh mới</SelectItem>
                  <SelectItem value='EXISTING_BUSINESS'>Mở rộng</SelectItem>
                  <SelectItem value='RENEWAL'>Gia hạn</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className='space-y-2'>
              <Label className='flex items-center gap-2'>
                <Target className='h-4 w-4 text-muted-foreground' />
                Giai đoạn
              </Label>
              <Select
                value={formData.stage}
                onValueChange={(value: OpportunityStage) =>
                  handleChange('stage', value)
                }
              >
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value='PROSPECTING'>Tìm kiếm</SelectItem>
                  <SelectItem value='QUALIFICATION'>Đánh giá</SelectItem>
                  <SelectItem value='PROPOSAL'>Đề xuất</SelectItem>
                  <SelectItem value='NEGOTIATION'>Đàm phán</SelectItem>
                  <SelectItem value='CLOSED_WON'>Thắng</SelectItem>
                  <SelectItem value='CLOSED_LOST'>Thua</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Value & Probability */}
          <div className='grid grid-cols-2 gap-4'>
            <div className='space-y-2'>
              <Label htmlFor='value' className='flex items-center gap-2'>
                <DollarSign className='h-4 w-4 text-muted-foreground' />
                Giá trị (VNĐ) <span className='text-red-500'>*</span>
              </Label>
              <Input
                id='value'
                type='number'
                value={formData.value || ''}
                onChange={(e) => handleChange('value', Number(e.target.value))}
                placeholder='0'
                className={cn(errors.value && 'border-red-500')}
              />
              {formData.value > 0 && (
                <p className='text-xs text-muted-foreground'>
                  {formatCurrency(formData.value)} VNĐ
                </p>
              )}
              {errors.value && (
                <p className='text-xs text-red-500'>{errors.value}</p>
              )}
            </div>

            <div className='space-y-2'>
              <Label htmlFor='probability' className='flex items-center gap-2'>
                <Percent className='h-4 w-4 text-muted-foreground' />
                Xác suất thắng
              </Label>
              <div className='flex items-center gap-2'>
                <Input
                  id='probability'
                  type='number'
                  min={0}
                  max={100}
                  value={formData.probability}
                  onChange={(e) =>
                    handleChange('probability', Number(e.target.value))
                  }
                />
                <span className='text-muted-foreground'>%</span>
              </div>
              <p className='text-xs text-muted-foreground'>
                Giá trị có trọng số:{' '}
                {formatCurrency((formData.value * formData.probability) / 100)}{' '}
                VNĐ
              </p>
            </div>
          </div>

          {/* Expected Close Date */}
          <div className='space-y-2'>
            <Label
              htmlFor='expectedCloseDate'
              className='flex items-center gap-2'
            >
              <Calendar className='h-4 w-4 text-muted-foreground' />
              Ngày đóng dự kiến <span className='text-red-500'>*</span>
            </Label>
            <Input
              id='expectedCloseDate'
              type='date'
              value={formData.expectedCloseDate}
              onChange={(e) =>
                handleChange('expectedCloseDate', e.target.value)
              }
              className={cn(errors.expectedCloseDate && 'border-red-500')}
            />
            {errors.expectedCloseDate && (
              <p className='text-xs text-red-500'>{errors.expectedCloseDate}</p>
            )}
          </div>

          {/* Description */}
          <div className='space-y-2'>
            <Label htmlFor='description' className='flex items-center gap-2'>
              <FileText className='h-4 w-4 text-muted-foreground' />
              Mô tả
            </Label>
            <Textarea
              id='description'
              value={formData.description}
              onChange={(e) => handleChange('description', e.target.value)}
              placeholder='Mô tả chi tiết về cơ hội...'
              rows={3}
            />
          </div>

          {/* Next Action */}
          <div className='space-y-2'>
            <Label htmlFor='nextAction' className='flex items-center gap-2'>
              <Target className='h-4 w-4 text-muted-foreground' />
              Hành động tiếp theo
            </Label>
            <Input
              id='nextAction'
              value={formData.nextAction}
              onChange={(e) => handleChange('nextAction', e.target.value)}
              placeholder='VD: Gọi điện theo dõi'
            />
          </div>

          <DialogFooter className='pt-4'>
            <Button
              type='button'
              variant='outline'
              onClick={() => {
                onOpenChange(false);
                resetForm();
              }}
            >
              Hủy
            </Button>
            <Button type='submit' disabled={isLoading}>
              {isLoading ? (
                <>
                  <Loader2 className='h-4 w-4 mr-2 animate-spin' />
                  Đang tạo...
                </>
              ) : (
                'Tạo cơ hội'
              )}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

export default QuickAddOpportunityDialog;
