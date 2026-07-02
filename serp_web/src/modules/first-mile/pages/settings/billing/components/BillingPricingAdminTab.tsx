/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - Pricing administration tab
 */

'use client';

import React from 'react';
import {
  Alert,
  AlertDescription,
  AlertTitle,
  TableCell,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
} from '@/shared/components/ui';
import { CircleHelp } from 'lucide-react';
import {
  useListSurchargeRulesQuery,
  useListTariffsQuery,
} from '../../../../api';
import type {
  BillingDeliveryService,
  SurchargeRuleAdminResponse,
  TariffAdminResponse,
} from '../../../../types';
import { useSurchargeRuleForm, useTariffRuleForm } from '../hooks';
import {
  BillingPricingRulesTable,
  formatTariffMoney,
  getTariffRouteLabel,
} from './BillingPricingRulesTable';
import { getCalculationTypeLabel } from '../billingPageModels';
import {
  SurchargeRuleFormDialog,
  type SurchargeRuleFormMode,
} from './SurchargeRuleFormDialog';
import {
  TariffRuleFormDialog,
  type TariffRuleFormMode,
} from './TariffRuleFormDialog';

export const BillingPricingAdminTab: React.FC = () => {
  const [activeTab, setActiveTab] = React.useState('tariff');
  const [tariffServiceFilter, setTariffServiceFilter] = React.useState<
    BillingDeliveryService | 'ALL'
  >('ALL');

  const [tariffDialogOpen, setTariffDialogOpen] = React.useState(false);
  const [tariffFormMode, setTariffFormMode] =
    React.useState<TariffRuleFormMode>('create');
  const [surchargeDialogOpen, setSurchargeDialogOpen] = React.useState(false);
  const [surchargeFormMode, setSurchargeFormMode] =
    React.useState<SurchargeRuleFormMode>('create');

  const tariff = useTariffRuleForm();
  const surcharge = useSurchargeRuleForm();

  const tariffQuery = useListTariffsQuery(
    tariffServiceFilter === 'ALL'
      ? undefined
      : { serviceCode: tariffServiceFilter }
  );
  const surchargeQuery = useListSurchargeRulesQuery();

  const filteredTariffs = React.useMemo(() => {
    const items = tariffQuery.data ?? [];
    if (tariffServiceFilter === 'ALL') {
      return items;
    }
    return items.filter((item) => item.serviceCode === tariffServiceFilter);
  }, [tariffQuery.data, tariffServiceFilter]);

  const openTariffCreate = () => {
    tariff.reset();
    setTariffFormMode('create');
    setTariffDialogOpen(true);
  };

  const openTariffEdit = (row: TariffAdminResponse) => {
    tariff.loadFromSaved(row);
    setTariffFormMode('edit');
    setTariffDialogOpen(true);
  };

  const handleTariffDialogChange = (open: boolean) => {
    if (!open && !tariff.isLoading) {
      setTariffDialogOpen(false);
      tariff.reset();
    }
  };

  const handleTariffSubmit = async (event: React.FormEvent) => {
    const saved = await tariff.submit(event);
    if (saved) {
      setTariffDialogOpen(false);
      tariff.reset();
    }
  };

  const openSurchargeCreate = () => {
    surcharge.reset();
    setSurchargeFormMode('create');
    setSurchargeDialogOpen(true);
  };

  const openSurchargeEdit = (row: SurchargeRuleAdminResponse) => {
    surcharge.loadFromSaved(row);
    setSurchargeFormMode('edit');
    setSurchargeDialogOpen(true);
  };

  const handleSurchargeDialogChange = (open: boolean) => {
    if (!open && !surcharge.isLoading) {
      setSurchargeDialogOpen(false);
      surcharge.reset();
    }
  };

  const handleSurchargeSubmit = async (event: React.FormEvent) => {
    const saved = await surcharge.submit(event);
    if (saved) {
      setSurchargeDialogOpen(false);
      surcharge.reset();
    }
  };

  return (
    <div className='space-y-6'>
      <Alert>
        <CircleHelp className='h-4 w-4' />
        <AlertTitle>Cách dùng quản trị bảng giá</AlertTitle>
        <AlertDescription>
          Xem các quy tắc hiện có từ dịch vụ tính cước. Chọn Sửa trên từng dòng
          hoặc Thêm để mở biểu mẫu, sau đó lưu thay đổi. Biểu phí được xác định
          theo dịch vụ, loại tuyến và ngày hiệu lực.
        </AlertDescription>
      </Alert>

      <Tabs
        value={activeTab}
        onValueChange={setActiveTab}
        className='space-y-4'
      >
        <TabsList>
          <TabsTrigger value='tariff'>Biểu phí</TabsTrigger>
          <TabsTrigger value='surcharge'>Phụ phí</TabsTrigger>
        </TabsList>

        <TabsContent value='tariff' className='space-y-6'>
          <BillingPricingRulesTable<TariffAdminResponse>
            title='Biểu phí đã cấu hình'
            description='Thang phí vận chuyển cơ bản theo dịch vụ và loại tuyến.'
            columns={[
              { key: 'service', label: 'Dịch vụ' },
              { key: 'route', label: 'Tuyến' },
              { key: 'base', label: 'Cơ bản (khối lượng / giá)' },
              { key: 'step', label: 'Bước tăng (khối lượng / giá)' },
              { key: 'dates', label: 'Hiệu lực' },
            ]}
            rows={filteredTariffs}
            isLoading={tariffQuery.isLoading}
            isError={tariffQuery.isError}
            emptyMessage='Chưa có biểu phí. Hãy thêm quy tắc biểu phí hoặc chạy dữ liệu khởi tạo.'
            getRowKey={(row) => row.id}
            onEdit={openTariffEdit}
            onCreate={openTariffCreate}
            createLabel='Thêm biểu phí'
            serviceFilter={{
              value: tariffServiceFilter,
              onChange: setTariffServiceFilter,
            }}
            renderCells={(row) => (
              <>
                <TableCell className='font-medium'>{row.serviceCode}</TableCell>
                <TableCell>{getTariffRouteLabel(row.routeTypeCode)}</TableCell>
                <TableCell>
                  {row.baseWeight}g / {formatTariffMoney(row.basePrice)}
                </TableCell>
                <TableCell>
                  {row.stepWeight}g / {formatTariffMoney(row.stepPrice)}
                </TableCell>
                <TableCell>
                  {row.effectiveDate}
                  {row.expirationDate ? ` → ${row.expirationDate}` : ''}
                </TableCell>
              </>
            )}
          />
        </TabsContent>

        <TabsContent value='surcharge' className='space-y-6'>
          <BillingPricingRulesTable<SurchargeRuleAdminResponse>
            title='Quy tắc phụ phí đã cấu hình'
            description='Các khoản phí bổ sung như xử lý khu vực xa.'
            columns={[
              { key: 'code', label: 'Mã' },
              { key: 'name', label: 'Tên' },
              { key: 'type', label: 'Cách tính' },
              { key: 'amount', label: 'Cấu hình số tiền' },
            ]}
            rows={surchargeQuery.data ?? []}
            isLoading={surchargeQuery.isLoading}
            isError={surchargeQuery.isError}
            emptyMessage='Chưa có quy tắc phụ phí.'
            getRowKey={(row) => row.id}
            onEdit={openSurchargeEdit}
            onCreate={openSurchargeCreate}
            createLabel='Thêm phụ phí'
            renderCells={(row) => (
              <>
                <TableCell className='font-medium'>{row.code}</TableCell>
                <TableCell>{row.name}</TableCell>
                <TableCell>
                  {getCalculationTypeLabel(row.calculationType)}
                </TableCell>
                <TableCell>
                  {row.calculationType === 'STEP_WEIGHT'
                    ? `${row.baseWeight ?? '-'}g / ${formatTariffMoney(row.basePrice)} + ${row.stepWeight ?? '-'}g / ${formatTariffMoney(row.stepPrice)}`
                    : row.fixedAmount != null
                      ? formatTariffMoney(row.fixedAmount)
                      : row.ratePercent != null
                        ? `${row.ratePercent}%`
                        : '-'}
                </TableCell>
              </>
            )}
          />
        </TabsContent>
      </Tabs>

      <TariffRuleFormDialog
        open={tariffDialogOpen}
        mode={tariffFormMode}
        form={tariff.form}
        onFormChange={tariff.setForm}
        isLoading={tariff.isLoading}
        onOpenChange={handleTariffDialogChange}
        onSubmit={handleTariffSubmit}
      />

      <SurchargeRuleFormDialog
        open={surchargeDialogOpen}
        mode={surchargeFormMode}
        form={surcharge.form}
        onFormChange={surcharge.setForm}
        calculationTypeHelper={surcharge.calculationTypeHelper}
        isLoading={surcharge.isLoading}
        onOpenChange={handleSurchargeDialogChange}
        onSubmit={handleSurchargeSubmit}
      />
    </div>
  );
};
