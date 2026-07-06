/**
 * Author: Nguyen The Anh
 * Description: Part of Serp Project - TMS settings overview page
 */

import Link from 'next/link';
import {
  ArrowRight,
  Package,
  PackageCheck,
  ReceiptText,
  Truck,
} from 'lucide-react';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/shared/components/ui';

export function TmsSettingsPage() {
  return (
    <div className='space-y-6'>
      <div>
        <h1 className='text-2xl font-bold tracking-tight'>Thiết lập</h1>
        <p className='text-muted-foreground'>
          Quản lý cấu hình dùng chung và dữ liệu vận hành nền tảng của TMS.
        </p>
      </div>

      <div className='grid gap-4 md:grid-cols-2 xl:grid-cols-3'>
        <Link href='/first-mile/settings/billing'>
          <Card className='h-full transition-colors hover:border-primary'>
            <CardHeader className='space-y-3'>
              <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
                <ReceiptText className='h-5 w-5' />
              </div>
              <div>
                <CardTitle>Tính cước</CardTitle>
                <CardDescription>
                  Quản lý cách tính phí vận chuyển, bảng giá, phụ phí và giá
                  dịch vụ gia tăng.
                </CardDescription>
              </div>
            </CardHeader>
            <CardContent className='flex items-center gap-2 text-sm font-medium text-primary'>
              Mở thiết lập cước
              <ArrowRight className='h-4 w-4' />
            </CardContent>
          </Card>
        </Link>

        <Link href='/first-mile/settings/vehicles'>
          <Card className='h-full transition-colors hover:border-primary'>
            <CardHeader className='space-y-3'>
              <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
                <Truck className='h-5 w-5' />
              </div>
              <div>
                <CardTitle>Phương tiện</CardTitle>
                <CardDescription>
                  Quản lý phương tiện chặng đầu và chặng giữa trong cùng một
                  nơi.
                </CardDescription>
              </div>
            </CardHeader>
            <CardContent className='flex items-center gap-2 text-sm font-medium text-primary'>
              Mở thiết lập phương tiện
              <ArrowRight className='h-4 w-4' />
            </CardContent>
          </Card>
        </Link>

        <Link href='/first-mile/settings/product-types'>
          <Card className='h-full transition-colors hover:border-primary'>
            <CardHeader className='space-y-3'>
              <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
                <Package className='h-5 w-5' />
              </div>
              <div>
                <CardTitle>Loại hàng</CardTitle>
                <CardDescription>
                  Cấu hình phân loại hàng hóa dùng cho đơn TMS và dữ liệu nhập.
                </CardDescription>
              </div>
            </CardHeader>
            <CardContent className='flex items-center gap-2 text-sm font-medium text-primary'>
              Mở thiết lập loại hàng
              <ArrowRight className='h-4 w-4' />
            </CardContent>
          </Card>
        </Link>

        <Link href='/first-mile/settings/bags'>
          <Card className='h-full transition-colors hover:border-primary'>
            <CardHeader className='space-y-3'>
              <div className='flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary'>
                <PackageCheck className='h-5 w-5' />
              </div>
              <div>
                <CardTitle>Túi hàng</CardTitle>
                <CardDescription>
                  Thiết lập sức chứa mặc định dùng khi tạo túi mới và lập túi tự
                  động.
                </CardDescription>
              </div>
            </CardHeader>
            <CardContent className='flex items-center gap-2 text-sm font-medium text-primary'>
              Mở thiết lập túi
              <ArrowRight className='h-4 w-4' />
            </CardContent>
          </Card>
        </Link>
      </div>
    </div>
  );
}
