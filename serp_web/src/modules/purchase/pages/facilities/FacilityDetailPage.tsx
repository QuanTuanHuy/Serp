'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
  Avatar,
  AvatarFallback,
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '@/shared/components/ui';
import {
  ArrowLeft,
  MoreHorizontal,
  Edit,
  Trash2,
  Phone,
  MapPin,
  Calendar,
  Warehouse,
  Ruler,
  Hash,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  useGetFacilityQuery,
  useDeleteFacilityMutation,
} from '../../api/purchaseApi';
import { formatDateStringVN, formatPhoneNumber } from '@/shared/utils/format';
import { toast } from 'sonner';
import { UpdateAddressDialog } from '../../components/dialogs/UpdateAddressDialog';
import { useUser } from '@/modules/account';

interface FacilityDetailPageProps {
  facilityId: string;
}

const STATUS_CONFIG = {
  ACTIVE: {
    label: 'Hoạt động',
    color: 'text-green-700 dark:text-green-300',
    bgColor: 'bg-green-100 dark:bg-green-900/50',
  },
  INACTIVE: {
    label: 'Không hoạt động',
    color: 'text-gray-700 dark:text-gray-300',
    bgColor: 'bg-gray-100 dark:bg-gray-800',
  },
};

export const FacilityDetailPage: React.FC<FacilityDetailPageProps> = ({
  facilityId,
}) => {
  const router = useRouter();

  const { user } = useUser();
  const isPurchaseAdmin = user?.roles.includes('PURCHASE_ADMIN');

  const [isAddressDialogOpen, setIsAddressDialogOpen] = useState(false);

  const {
    data: facilityResponse,
    isLoading,
    isError,
  } = useGetFacilityQuery(facilityId);
  const [deleteFacility, { isLoading: isDeleting }] =
    useDeleteFacilityMutation();

  const facility = facilityResponse?.data;

  const handleEdit = () => {
    router.push(`/purchase/facilities/${facilityId}/edit`);
  };

  const handleDelete = async () => {
    if (!confirm('Bạn có chắc chắn muốn xóa kho hàng này không?')) return;

    try {
      await deleteFacility(facilityId).unwrap();
      router.push('/purchase/facilities');
    } catch (error) {
      console.error('Lỗi khi xóa kho hàng:', error);
      toast.error('Đã xảy ra lỗi khi xóa kho hàng. Vui lòng thử lại.');
    }
  };

  if (isLoading) {
    return (
      <div className='flex items-center justify-center min-h-[400px]'>
        <div className='text-center'>
          <div className='animate-spin rounded-full h-12 w-12 border-b-2 border-primary mx-auto mb-4'></div>
          <p className='text-muted-foreground'>
            Đang tải thông tin kho hàng...
          </p>
        </div>
      </div>
    );
  }

  if (isError || !facility) {
    return (
      <div className='p-6'>
        <Card className='border-red-200 bg-red-50 dark:border-red-800 dark:bg-red-950/50'>
          <CardContent className='p-6 text-center'>
            <h3 className='text-lg font-semibold text-red-900 dark:text-red-100 mb-2'>
              Không tìm thấy kho hàng
            </h3>
            <p className='text-red-600 dark:text-red-400 mb-4'>
              Kho hàng bạn đang tìm không tồn tại hoặc đã bị xóa.
            </p>
            <Button variant='outline' onClick={() => router.back()}>
              Quay lại
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const statusConfig =
    STATUS_CONFIG[facility.statusId as keyof typeof STATUS_CONFIG] ||
    STATUS_CONFIG.INACTIVE;

  return (
    <div className='p-6 space-y-6'>
      {/* Header */}
      <div className='flex items-start justify-between'>
        <div className='flex items-start gap-4'>
          <Button variant='outline' size='icon' onClick={() => router.back()}>
            <ArrowLeft className='h-4 w-4' />
          </Button>

          <div className='flex items-start gap-4'>
            <Avatar className='h-16 w-16'>
              <AvatarFallback className='text-xl'>
                {facility.name.charAt(0).toUpperCase()}
              </AvatarFallback>
            </Avatar>

            <div>
              <div className='flex items-center gap-2 mb-2'>
                <h1 className='text-2xl font-bold text-foreground'>
                  {facility.name}
                </h1>
                <Badge
                  className={cn(
                    statusConfig.bgColor,
                    statusConfig.color,
                    'border-0'
                  )}
                >
                  {statusConfig.label}
                </Badge>
              </div>

              <div className='flex items-center gap-4 text-sm text-muted-foreground'>
                <div className='flex items-center gap-1'>
                  <Warehouse className='h-4 w-4' />
                  <span>ID: {facility.id}</span>
                </div>
                <div className='flex items-center gap-1'>
                  <Calendar className='h-4 w-4' />
                  <span>
                    Tạo ngày {formatDateStringVN(facility.createdStamp)}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {isPurchaseAdmin && (
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon' disabled={isDeleting}>
                <MoreHorizontal className='h-4 w-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem onClick={handleEdit}>
                <Edit className='mr-2 h-4 w-4' />
                Chỉnh sửa kho hàng
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setIsAddressDialogOpen(true)}>
                <MapPin className='mr-2 h-4 w-4' />
                Thay đổi địa chỉ
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                onClick={handleDelete}
                className='text-destructive focus:text-destructive'
              >
                <Trash2 className='mr-2 h-4 w-4' />
                Xóa kho hàng
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        )}
      </div>

      {/* Detail Cards */}
      <div className='grid gap-4 md:grid-cols-2 lg:grid-cols-3'>
        {/* Contact Information */}
        <Card>
          <CardHeader className='pb-3'>
            <h3 className='font-semibold text-base'>Thông tin liên hệ</h3>
          </CardHeader>
          <CardContent className='space-y-3'>
            {facility.phone && (
              <div className='flex items-center gap-2 text-sm'>
                <Phone className='h-4 w-4 text-muted-foreground' />
                <span className='text-foreground'>
                  {formatPhoneNumber(facility.phone)}
                </span>
              </div>
            )}
            {facility.postalCode && (
              <div className='flex items-center gap-2 text-sm'>
                <Hash className='h-4 w-4 text-muted-foreground' />
                <span className='text-foreground'>
                  Mã bưu chính: {facility.postalCode}
                </span>
              </div>
            )}
            {facility.address && (
              <div className='flex items-start gap-2 text-sm'>
                <MapPin className='h-4 w-4 text-muted-foreground mt-0.5' />
                <span className='text-foreground'>
                  {facility.address.fullAddress}
                </span>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Dimensions */}
        <Card>
          <CardHeader className='pb-3'>
            <h3 className='font-semibold text-base'>Kích thước & Sức chứa</h3>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div className='flex items-center justify-between text-sm'>
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Ruler className='h-4 w-4' />
                <span>Chiều dài</span>
              </div>
              <span className='font-semibold'>{facility.length} m</span>
            </div>
            <div className='flex items-center justify-between text-sm'>
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Ruler className='h-4 w-4' />
                <span>Chiều rộng</span>
              </div>
              <span className='font-semibold'>{facility.width} m</span>
            </div>
            <div className='flex items-center justify-between text-sm'>
              <div className='flex items-center gap-2 text-muted-foreground'>
                <Ruler className='h-4 w-4' />
                <span>Chiều cao</span>
              </div>
              <span className='font-semibold'>{facility.height} m</span>
            </div>
          </CardContent>
        </Card>

        {/* Additional Info */}
        <Card>
          <CardHeader className='pb-3'>
            <h3 className='font-semibold text-base'>Thông tin bổ sung</h3>
          </CardHeader>
          <CardContent className='space-y-3'>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground'>Tạo vào</span>
              <span className='font-medium'>
                {formatDateStringVN(facility.createdStamp)}
              </span>
            </div>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground'>Cập nhật lần cuối</span>
              <span className='font-medium'>
                {formatDateStringVN(facility.lastUpdatedStamp)}
              </span>
            </div>
            <div className='flex items-center justify-between text-sm'>
              <span className='text-muted-foreground'>Mặc định</span>
              <span className='font-medium'>
                {facility.isDefault ? 'Có' : 'Không'}
              </span>
            </div>
          </CardContent>
        </Card>
      </div>

      <UpdateAddressDialog
        open={isAddressDialogOpen}
        onOpenChange={setIsAddressDialogOpen}
        entityId={facilityId}
        entityType='FACILITY'
        currentAddress={facility.address}
      />
    </div>
  );
};

export default FacilityDetailPage;
