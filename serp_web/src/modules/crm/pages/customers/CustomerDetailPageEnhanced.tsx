/*
Author: QuanTuanHuy
Description: Part of Serp Project - Enhanced Customer Detail Page with mock data
*/

'use client';

import { useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import {
  Card,
  CardContent,
  CardHeader,
  Button,
  Badge,
  Input,
  Textarea,
  Avatar,
  AvatarFallback,
  Tabs,
  TabsContent,
  TabsList,
  TabsTrigger,
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  Progress,
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
  Calendar,
  DollarSign,
  TrendingUp,
  User,
  Building2,
  Clock,
  Phone,
  Mail,
  Globe,
  MapPin,
  MessageSquare,
  FileText,
  Plus,
  ExternalLink,
  Package,
  Users,
  History,
  Star,
  AlertCircle,
  CheckCircle2,
  ShoppingCart,
  Receipt,
  CreditCard,
  Tag,
  Activity,
  Send,
  Download,
  Upload,
} from 'lucide-react';
import { cn } from '@/shared/utils';
import {
  MOCK_CUSTOMERS,
  MOCK_OPPORTUNITIES,
  MOCK_ACTIVITIES,
} from '../../mocks';
import type {
  Customer,
  Opportunity,
  Activity as ActivityType,
} from '../../types';

// Customer status configuration
const STATUS_CONFIG = {
  ACTIVE: {
    label: 'Active',
    color: 'text-green-700 dark:text-green-300',
    bgColor: 'bg-green-100 dark:bg-green-900/50',
  },
  INACTIVE: {
    label: 'Inactive',
    color: 'text-gray-700 dark:text-gray-300',
    bgColor: 'bg-gray-100 dark:bg-gray-800',
  },
  POTENTIAL: {
    label: 'Potential',
    color: 'text-blue-700 dark:text-blue-300',
    bgColor: 'bg-blue-100 dark:bg-blue-900/50',
  },
  BLOCKED: {
    label: 'Blocked',
    color: 'text-red-700 dark:text-red-300',
    bgColor: 'bg-red-100 dark:bg-red-900/50',
  },
};

// Mock orders/transactions for customer
interface CustomerOrder {
  id: string;
  orderNumber: string;
  date: string;
  status: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'CANCELLED';
  total: number;
  items: number;
}

const MOCK_ORDERS: CustomerOrder[] = [
  {
    id: '1',
    orderNumber: 'ORD-2024-001',
    date: '2024-12-01',
    status: 'COMPLETED',
    total: 15000000,
    items: 3,
  },
  {
    id: '2',
    orderNumber: 'ORD-2024-002',
    date: '2024-11-15',
    status: 'COMPLETED',
    total: 8500000,
    items: 2,
  },
  {
    id: '3',
    orderNumber: 'ORD-2024-003',
    date: '2024-10-28',
    status: 'COMPLETED',
    total: 22000000,
    items: 5,
  },
  {
    id: '4',
    orderNumber: 'ORD-2024-004',
    date: '2024-10-10',
    status: 'CANCELLED',
    total: 5000000,
    items: 1,
  },
  {
    id: '5',
    orderNumber: 'ORD-2024-005',
    date: '2024-09-20',
    status: 'COMPLETED',
    total: 12500000,
    items: 4,
  },
];

// Mock contacts for the customer
interface CustomerContact {
  id: string;
  name: string;
  role: string;
  email: string;
  phone?: string;
  isPrimary: boolean;
}

const MOCK_CONTACTS: CustomerContact[] = [
  {
    id: '1',
    name: 'Nguyễn Văn An',
    role: 'CEO',
    email: 'an@company.com',
    phone: '+84 912 345 678',
    isPrimary: true,
  },
  {
    id: '2',
    name: 'Trần Thị Bình',
    role: 'CFO',
    email: 'binh@company.com',
    phone: '+84 908 765 432',
    isPrimary: false,
  },
  {
    id: '3',
    name: 'Lê Hoàng Cường',
    role: 'IT Manager',
    email: 'cuong@company.com',
    isPrimary: false,
  },
];

// Mock documents
interface CustomerDocument {
  id: string;
  name: string;
  type: string;
  size: string;
  uploadedAt: string;
  uploadedBy: string;
}

const MOCK_DOCUMENTS: CustomerDocument[] = [
  {
    id: '1',
    name: 'Contract_2024.pdf',
    type: 'PDF',
    size: '2.4 MB',
    uploadedAt: '2024-01-15',
    uploadedBy: 'John Smith',
  },
  {
    id: '2',
    name: 'NDA_Agreement.pdf',
    type: 'PDF',
    size: '1.1 MB',
    uploadedAt: '2024-01-10',
    uploadedBy: 'John Smith',
  },
  {
    id: '3',
    name: 'Requirements_Doc.docx',
    type: 'Word',
    size: '856 KB',
    uploadedAt: '2024-02-20',
    uploadedBy: 'Sarah Johnson',
  },
];

// Mock notes
interface CustomerNote {
  id: string;
  content: string;
  createdAt: string;
  createdBy: string;
}

const MOCK_NOTES: CustomerNote[] = [
  {
    id: '1',
    content:
      'Khách hàng quan tâm đến gói Enterprise. Cần demo chi tiết vào tuần sau.',
    createdAt: '2024-12-02T10:30:00Z',
    createdBy: 'John Smith',
  },
  {
    id: '2',
    content:
      'Đã gửi báo giá mới theo yêu cầu. Chờ phản hồi từ phía khách hàng.',
    createdAt: '2024-11-28T14:15:00Z',
    createdBy: 'Sarah Johnson',
  },
  {
    id: '3',
    content: 'Meeting với CFO về ngân sách Q1/2025. Rất tích cực.',
    createdAt: '2024-11-25T09:00:00Z',
    createdBy: 'John Smith',
  },
];

interface CustomerDetailPageEnhancedProps {
  customerId: string;
  className?: string;
}

export const CustomerDetailPageEnhanced: React.FC<
  CustomerDetailPageEnhancedProps
> = ({ customerId, className }) => {
  const router = useRouter();
  const [activeTab, setActiveTab] = useState('overview');
  const [isNoteDialogOpen, setIsNoteDialogOpen] = useState(false);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [isContactDialogOpen, setIsContactDialogOpen] = useState(false);
  const [noteText, setNoteText] = useState('');

  // Find customer from mock data
  const customer = useMemo(() => {
    return MOCK_CUSTOMERS.find((c) => c.id === customerId);
  }, [customerId]);

  // Get related opportunities
  const relatedOpportunities = useMemo(() => {
    return MOCK_OPPORTUNITIES.filter((o) => o.customerId === customerId).slice(
      0,
      5
    );
  }, [customerId]);

  // Get related activities
  const relatedActivities = useMemo(() => {
    return MOCK_ACTIVITIES.filter(
      (a) => a.relatedTo.type === 'CUSTOMER' && a.relatedTo.id === customerId
    ).slice(0, 5);
  }, [customerId]);

  if (!customer) {
    return (
      <div className={cn('p-6', className)}>
        <Card>
          <CardContent className='py-16 text-center'>
            <AlertCircle className='w-12 h-12 mx-auto mb-4 text-muted-foreground' />
            <h2 className='text-xl font-semibold mb-2'>Customer Not Found</h2>
            <p className='text-muted-foreground mb-4'>
              The customer you're looking for doesn't exist or has been deleted.
            </p>
            <Button onClick={() => router.push('/crm/customers')}>
              Back to Customers
            </Button>
          </CardContent>
        </Card>
      </div>
    );
  }

  const statusConfig = STATUS_CONFIG[customer.status];

  // Calculate customer health score (mock)
  const healthScore = 85;
  const lifetimeValue = customer.totalValue;
  const avgOrderValue =
    MOCK_ORDERS.filter((o) => o.status === 'COMPLETED').reduce(
      (sum, o) => sum + o.total,
      0
    ) / MOCK_ORDERS.filter((o) => o.status === 'COMPLETED').length || 0;

  const handleAddNote = () => {
    if (noteText.trim()) {
      console.log('Adding note:', noteText);
      setIsNoteDialogOpen(false);
      setNoteText('');
    }
  };

  const handleDelete = () => {
    console.log('Deleting customer');
    router.push('/crm/customers');
  };

  const formatCurrency = (value: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      minimumFractionDigits: 0,
    }).format(value);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  const formatDateTime = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('vi-VN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const getOrderStatusBadge = (status: CustomerOrder['status']) => {
    switch (status) {
      case 'COMPLETED':
        return (
          <Badge className='bg-green-100 dark:bg-green-900/50 text-green-700 dark:text-green-300'>
            Completed
          </Badge>
        );
      case 'PROCESSING':
        return (
          <Badge className='bg-blue-100 dark:bg-blue-900/50 text-blue-700 dark:text-blue-300'>
            Processing
          </Badge>
        );
      case 'PENDING':
        return (
          <Badge className='bg-yellow-100 dark:bg-yellow-900/50 text-yellow-700 dark:text-yellow-300'>
            Pending
          </Badge>
        );
      case 'CANCELLED':
        return (
          <Badge className='bg-red-100 dark:bg-red-900/50 text-red-700 dark:text-red-300'>
            Cancelled
          </Badge>
        );
    }
  };

  return (
    <div className={cn('p-6 space-y-6', className)}>
      {/* Header */}
      <div className='flex items-start justify-between'>
        <div className='flex items-start gap-4'>
          <Button
            variant='outline'
            size='icon'
            onClick={() => router.push('/crm/customers')}
          >
            <ArrowLeft className='w-4 h-4' />
          </Button>
          <div className='flex items-start gap-4'>
            <Avatar className='w-16 h-16'>
              <AvatarFallback className='text-xl bg-primary text-primary-foreground'>
                {customer.name
                  .split(' ')
                  .map((n) => n[0])
                  .join('')
                  .slice(0, 2)}
              </AvatarFallback>
            </Avatar>
            <div>
              <div className='flex items-center gap-3 mb-2'>
                <h1 className='text-2xl font-bold text-foreground'>
                  {customer.name}
                </h1>
                <Badge className={cn(statusConfig.bgColor, statusConfig.color)}>
                  {statusConfig.label}
                </Badge>
                {customer.tags.includes('VIP') && (
                  <Badge className='bg-yellow-100 dark:bg-yellow-900/50 text-yellow-700 dark:text-yellow-300'>
                    <Star className='w-3 h-3 mr-1 fill-current' />
                    VIP
                  </Badge>
                )}
              </div>
              <div className='flex items-center gap-4 text-sm text-muted-foreground'>
                {customer.companyName && (
                  <span className='flex items-center gap-1'>
                    <Building2 className='w-4 h-4' />
                    {customer.companyName}
                  </span>
                )}
                <span className='flex items-center gap-1'>
                  <Mail className='w-4 h-4' />
                  {customer.email}
                </span>
                {customer.phone && (
                  <span className='flex items-center gap-1'>
                    <Phone className='w-4 h-4' />
                    {customer.phone}
                  </span>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className='flex items-center gap-2'>
          <Button
            variant='outline'
            onClick={() => router.push(`/crm/customers/${customerId}/edit`)}
          >
            <Edit className='w-4 h-4 mr-2' />
            Edit
          </Button>
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <Button variant='outline' size='icon'>
                <MoreHorizontal className='w-4 h-4' />
              </Button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align='end'>
              <DropdownMenuItem>
                <Send className='w-4 h-4 mr-2' />
                Send Email
              </DropdownMenuItem>
              <DropdownMenuItem>
                <ShoppingCart className='w-4 h-4 mr-2' />
                Create Order
              </DropdownMenuItem>
              <DropdownMenuItem onClick={() => setIsNoteDialogOpen(true)}>
                <MessageSquare className='w-4 h-4 mr-2' />
                Add Note
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                className='text-red-600'
                onClick={() => setIsDeleteDialogOpen(true)}
              >
                <Trash2 className='w-4 h-4 mr-2' />
                Delete Customer
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>
      </div>

      {/* Stats Cards */}
      <div className='grid grid-cols-1 md:grid-cols-4 gap-4'>
        <Card className='bg-gradient-to-br from-green-50 to-green-100/50 dark:from-green-950 dark:to-green-900/50 border-green-200 dark:border-green-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Lifetime Value</p>
                <p className='text-2xl font-bold text-green-700 dark:text-green-300'>
                  {formatCurrency(lifetimeValue)}
                </p>
              </div>
              <div className='p-3 bg-green-500/20 rounded-full'>
                <DollarSign className='w-6 h-6 text-green-600 dark:text-green-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='bg-gradient-to-br from-blue-50 to-blue-100/50 dark:from-blue-950 dark:to-blue-900/50 border-blue-200 dark:border-blue-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Total Orders</p>
                <p className='text-2xl font-bold text-blue-700 dark:text-blue-300'>
                  {MOCK_ORDERS.filter((o) => o.status === 'COMPLETED').length}
                </p>
              </div>
              <div className='p-3 bg-blue-500/20 rounded-full'>
                <ShoppingCart className='w-6 h-6 text-blue-600 dark:text-blue-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='bg-gradient-to-br from-purple-50 to-purple-100/50 dark:from-purple-950 dark:to-purple-900/50 border-purple-200 dark:border-purple-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>
                  Avg. Order Value
                </p>
                <p className='text-2xl font-bold text-purple-700 dark:text-purple-300'>
                  {formatCurrency(avgOrderValue)}
                </p>
              </div>
              <div className='p-3 bg-purple-500/20 rounded-full'>
                <Receipt className='w-6 h-6 text-purple-600 dark:text-purple-400' />
              </div>
            </div>
          </CardContent>
        </Card>

        <Card className='bg-gradient-to-br from-orange-50 to-orange-100/50 dark:from-orange-950 dark:to-orange-900/50 border-orange-200 dark:border-orange-800/50'>
          <CardContent className='py-4'>
            <div className='flex items-center justify-between'>
              <div>
                <p className='text-sm text-muted-foreground'>Health Score</p>
                <p className='text-2xl font-bold text-orange-700 dark:text-orange-300'>
                  {healthScore}%
                </p>
              </div>
              <div className='p-3 bg-orange-500/20 rounded-full'>
                <Activity className='w-6 h-6 text-orange-600 dark:text-orange-400' />
              </div>
            </div>
            <Progress value={healthScore} className='mt-2 h-1.5' />
          </CardContent>
        </Card>
      </div>

      {/* Tabs */}
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList>
          <TabsTrigger value='overview'>Overview</TabsTrigger>
          <TabsTrigger value='contacts'>
            Contacts ({MOCK_CONTACTS.length})
          </TabsTrigger>
          <TabsTrigger value='orders'>
            Orders ({MOCK_ORDERS.length})
          </TabsTrigger>
          <TabsTrigger value='opportunities'>Opportunities</TabsTrigger>
          <TabsTrigger value='activities'>Activities</TabsTrigger>
          <TabsTrigger value='documents'>Documents</TabsTrigger>
        </TabsList>

        {/* Overview Tab */}
        <TabsContent value='overview' className='mt-6'>
          <div className='grid grid-cols-1 lg:grid-cols-3 gap-6'>
            {/* Main Info */}
            <div className='lg:col-span-2 space-y-6'>
              {/* Customer Information */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Customer Information</h3>
                </CardHeader>
                <CardContent>
                  <div className='grid grid-cols-2 gap-4'>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Customer Type
                      </label>
                      <p className='font-medium capitalize'>
                        {customer.customerType.toLowerCase()}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Status
                      </label>
                      <p className='font-medium'>
                        <Badge
                          className={cn(
                            statusConfig.bgColor,
                            statusConfig.color
                          )}
                        >
                          {statusConfig.label}
                        </Badge>
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Email
                      </label>
                      <p className='font-medium flex items-center gap-2'>
                        <Mail className='w-4 h-4 text-muted-foreground' />
                        {customer.email}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Phone
                      </label>
                      <p className='font-medium flex items-center gap-2'>
                        <Phone className='w-4 h-4 text-muted-foreground' />
                        {customer.phone || 'N/A'}
                      </p>
                    </div>
                    {customer.companyName && (
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Company
                        </label>
                        <p className='font-medium flex items-center gap-2'>
                          <Building2 className='w-4 h-4 text-muted-foreground' />
                          {customer.companyName}
                        </p>
                      </div>
                    )}
                    {customer.website && (
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Website
                        </label>
                        <p className='font-medium flex items-center gap-2'>
                          <Globe className='w-4 h-4 text-muted-foreground' />
                          <a
                            href={customer.website}
                            target='_blank'
                            rel='noopener noreferrer'
                            className='text-primary hover:underline'
                          >
                            {customer.website.replace('https://', '')}
                          </a>
                        </p>
                      </div>
                    )}
                    {customer.address && (
                      <div className='col-span-2'>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Address
                        </label>
                        <p className='font-medium flex items-center gap-2'>
                          <MapPin className='w-4 h-4 text-muted-foreground' />
                          {customer.address}
                        </p>
                      </div>
                    )}
                    {customer.taxNumber && (
                      <div>
                        <label className='text-sm font-medium text-muted-foreground'>
                          Tax Number
                        </label>
                        <p className='font-medium'>{customer.taxNumber}</p>
                      </div>
                    )}
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Customer Since
                      </label>
                      <p className='font-medium'>
                        {formatDate(customer.createdAt)}
                      </p>
                    </div>
                    <div>
                      <label className='text-sm font-medium text-muted-foreground'>
                        Last Contact
                      </label>
                      <p className='font-medium'>
                        {customer.lastContactDate
                          ? formatDate(customer.lastContactDate)
                          : 'N/A'}
                      </p>
                    </div>
                  </div>
                </CardContent>
              </Card>

              {/* Recent Orders */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between'>
                  <h3 className='font-semibold'>Recent Orders</h3>
                  <Button variant='outline' size='sm'>
                    View All
                  </Button>
                </CardHeader>
                <CardContent>
                  <div className='space-y-3'>
                    {MOCK_ORDERS.slice(0, 3).map((order) => (
                      <div
                        key={order.id}
                        className='flex items-center justify-between p-3 bg-muted rounded-lg hover:bg-muted/80 transition-colors cursor-pointer'
                      >
                        <div className='flex items-center gap-3'>
                          <div className='p-2 bg-background rounded'>
                            <ShoppingCart className='w-4 h-4 text-muted-foreground' />
                          </div>
                          <div>
                            <p className='font-medium'>{order.orderNumber}</p>
                            <p className='text-sm text-muted-foreground'>
                              {order.items} items • {formatDate(order.date)}
                            </p>
                          </div>
                        </div>
                        <div className='text-right'>
                          <p className='font-semibold'>
                            {formatCurrency(order.total)}
                          </p>
                          {getOrderStatusBadge(order.status)}
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>

              {/* Notes */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between'>
                  <h3 className='font-semibold'>Notes</h3>
                  <Button
                    variant='outline'
                    size='sm'
                    onClick={() => setIsNoteDialogOpen(true)}
                  >
                    <Plus className='w-4 h-4 mr-2' />
                    Add Note
                  </Button>
                </CardHeader>
                <CardContent>
                  <div className='space-y-4'>
                    {MOCK_NOTES.map((note) => (
                      <div key={note.id} className='p-4 bg-muted rounded-lg'>
                        <p className='text-sm'>{note.content}</p>
                        <div className='flex items-center gap-2 mt-2 text-xs text-muted-foreground'>
                          <span>{note.createdBy}</span>
                          <span>•</span>
                          <span>{formatDateTime(note.createdAt)}</span>
                        </div>
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Sidebar */}
            <div className='space-y-6'>
              {/* Quick Actions */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Quick Actions</h3>
                </CardHeader>
                <CardContent className='space-y-2'>
                  <Button variant='outline' className='w-full justify-start'>
                    <Phone className='w-4 h-4 mr-2' />
                    Call Customer
                  </Button>
                  <Button variant='outline' className='w-full justify-start'>
                    <Mail className='w-4 h-4 mr-2' />
                    Send Email
                  </Button>
                  <Button variant='outline' className='w-full justify-start'>
                    <Calendar className='w-4 h-4 mr-2' />
                    Schedule Meeting
                  </Button>
                  <Button variant='outline' className='w-full justify-start'>
                    <ShoppingCart className='w-4 h-4 mr-2' />
                    Create Order
                  </Button>
                </CardContent>
              </Card>

              {/* Tags */}
              <Card>
                <CardHeader className='flex flex-row items-center justify-between'>
                  <h3 className='font-semibold'>Tags</h3>
                  <Button variant='ghost' size='sm'>
                    <Plus className='w-4 h-4' />
                  </Button>
                </CardHeader>
                <CardContent>
                  <div className='flex flex-wrap gap-2'>
                    {customer.tags.length > 0 ? (
                      customer.tags.map((tag) => (
                        <Badge
                          key={tag}
                          variant='secondary'
                          className='flex items-center gap-1'
                        >
                          <Tag className='w-3 h-3' />
                          {tag}
                        </Badge>
                      ))
                    ) : (
                      <p className='text-sm text-muted-foreground'>No tags</p>
                    )}
                  </div>
                </CardContent>
              </Card>

              {/* Primary Contact */}
              <Card>
                <CardHeader>
                  <h3 className='font-semibold'>Primary Contact</h3>
                </CardHeader>
                <CardContent>
                  {MOCK_CONTACTS.find((c) => c.isPrimary) ? (
                    <div className='flex items-start gap-3'>
                      <Avatar>
                        <AvatarFallback>
                          {MOCK_CONTACTS[0].name
                            .split(' ')
                            .map((n) => n[0])
                            .join('')}
                        </AvatarFallback>
                      </Avatar>
                      <div>
                        <p className='font-medium'>{MOCK_CONTACTS[0].name}</p>
                        <p className='text-sm text-muted-foreground'>
                          {MOCK_CONTACTS[0].role}
                        </p>
                        <div className='flex items-center gap-2 mt-2'>
                          <Button variant='outline' size='sm'>
                            <Mail className='w-3 h-3 mr-1' />
                            Email
                          </Button>
                          <Button variant='outline' size='sm'>
                            <Phone className='w-3 h-3 mr-1' />
                            Call
                          </Button>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <p className='text-sm text-muted-foreground'>
                      No primary contact
                    </p>
                  )}
                </CardContent>
              </Card>
            </div>
          </div>
        </TabsContent>

        {/* Contacts Tab */}
        <TabsContent value='contacts' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Customer Contacts</h3>
              <Button onClick={() => setIsContactDialogOpen(true)}>
                <Plus className='w-4 h-4 mr-2' />
                Add Contact
              </Button>
            </CardHeader>
            <CardContent>
              <div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
                {MOCK_CONTACTS.map((contact) => (
                  <div key={contact.id} className='p-4 bg-muted rounded-lg'>
                    <div className='flex items-start justify-between'>
                      <div className='flex items-start gap-3'>
                        <Avatar>
                          <AvatarFallback>
                            {contact.name
                              .split(' ')
                              .map((n) => n[0])
                              .join('')}
                          </AvatarFallback>
                        </Avatar>
                        <div>
                          <div className='flex items-center gap-2'>
                            <p className='font-medium'>{contact.name}</p>
                            {contact.isPrimary && (
                              <Badge variant='secondary' className='text-xs'>
                                Primary
                              </Badge>
                            )}
                          </div>
                          <p className='text-sm text-muted-foreground'>
                            {contact.role}
                          </p>
                        </div>
                      </div>
                      <Button variant='ghost' size='icon' className='h-8 w-8'>
                        <MoreHorizontal className='w-4 h-4' />
                      </Button>
                    </div>
                    <div className='mt-3 space-y-1'>
                      <p className='text-sm flex items-center gap-2'>
                        <Mail className='w-4 h-4 text-muted-foreground' />
                        {contact.email}
                      </p>
                      {contact.phone && (
                        <p className='text-sm flex items-center gap-2'>
                          <Phone className='w-4 h-4 text-muted-foreground' />
                          {contact.phone}
                        </p>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Orders Tab */}
        <TabsContent value='orders' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Order History</h3>
              <Button>
                <Plus className='w-4 h-4 mr-2' />
                Create Order
              </Button>
            </CardHeader>
            <CardContent>
              <div className='overflow-x-auto'>
                <table className='w-full'>
                  <thead>
                    <tr className='border-b'>
                      <th className='text-left py-3 px-4 font-medium text-muted-foreground'>
                        Order #
                      </th>
                      <th className='text-left py-3 px-4 font-medium text-muted-foreground'>
                        Date
                      </th>
                      <th className='text-center py-3 px-4 font-medium text-muted-foreground'>
                        Items
                      </th>
                      <th className='text-right py-3 px-4 font-medium text-muted-foreground'>
                        Total
                      </th>
                      <th className='text-center py-3 px-4 font-medium text-muted-foreground'>
                        Status
                      </th>
                      <th className='w-10'></th>
                    </tr>
                  </thead>
                  <tbody>
                    {MOCK_ORDERS.map((order) => (
                      <tr
                        key={order.id}
                        className='border-b last:border-0 hover:bg-muted/50'
                      >
                        <td className='py-3 px-4 font-medium'>
                          {order.orderNumber}
                        </td>
                        <td className='py-3 px-4'>{formatDate(order.date)}</td>
                        <td className='py-3 px-4 text-center'>{order.items}</td>
                        <td className='py-3 px-4 text-right font-medium'>
                          {formatCurrency(order.total)}
                        </td>
                        <td className='py-3 px-4 text-center'>
                          {getOrderStatusBadge(order.status)}
                        </td>
                        <td className='py-3 px-4'>
                          <Button
                            variant='ghost'
                            size='icon'
                            className='h-8 w-8'
                          >
                            <ExternalLink className='w-4 h-4' />
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </TabsContent>

        {/* Opportunities Tab */}
        <TabsContent value='opportunities' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Related Opportunities</h3>
              <Button>
                <Plus className='w-4 h-4 mr-2' />
                Create Opportunity
              </Button>
            </CardHeader>
            <CardContent>
              {relatedOpportunities.length > 0 ? (
                <div className='space-y-4'>
                  {relatedOpportunities.map((opp) => (
                    <div
                      key={opp.id}
                      className='flex items-center justify-between p-4 bg-muted rounded-lg hover:bg-muted/80 transition-colors cursor-pointer'
                      onClick={() =>
                        router.push(`/crm/opportunities/${opp.id}`)
                      }
                    >
                      <div>
                        <p className='font-medium'>{opp.name}</p>
                        <div className='flex items-center gap-2 mt-1'>
                          <Badge variant='outline'>{opp.stage}</Badge>
                          <span className='text-sm text-muted-foreground'>
                            {opp.probability}% probability
                          </span>
                        </div>
                      </div>
                      <div className='text-right'>
                        <p className='font-semibold text-green-600 dark:text-green-400'>
                          {formatCurrency(opp.value)}
                        </p>
                        <p className='text-sm text-muted-foreground'>
                          Close: {formatDate(opp.expectedCloseDate)}
                        </p>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='text-center py-8'>
                  <TrendingUp className='w-12 h-12 mx-auto mb-4 text-muted-foreground' />
                  <p className='text-muted-foreground mb-4'>
                    No opportunities yet.
                  </p>
                  <Button>Create First Opportunity</Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Activities Tab */}
        <TabsContent value='activities' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Recent Activities</h3>
              <Button>
                <Plus className='w-4 h-4 mr-2' />
                Log Activity
              </Button>
            </CardHeader>
            <CardContent>
              {relatedActivities.length > 0 ? (
                <div className='space-y-4'>
                  {relatedActivities.map((activity) => (
                    <div
                      key={activity.id}
                      className='flex items-start gap-4 p-4 bg-muted rounded-lg'
                    >
                      <div
                        className={cn(
                          'p-2 rounded-full',
                          activity.status === 'COMPLETED'
                            ? 'bg-green-100 dark:bg-green-900/50'
                            : 'bg-blue-100 dark:bg-blue-900/50'
                        )}
                      >
                        {activity.type === 'CALL' && (
                          <Phone className='w-4 h-4 text-blue-600 dark:text-blue-400' />
                        )}
                        {activity.type === 'EMAIL' && (
                          <Mail className='w-4 h-4 text-blue-600 dark:text-blue-400' />
                        )}
                        {activity.type === 'MEETING' && (
                          <Users className='w-4 h-4 text-purple-600 dark:text-purple-400' />
                        )}
                        {activity.type === 'TASK' && (
                          <CheckCircle2 className='w-4 h-4 text-green-600 dark:text-green-400' />
                        )}
                      </div>
                      <div className='flex-1'>
                        <div className='flex items-center justify-between'>
                          <h4 className='font-medium'>{activity.subject}</h4>
                          <Badge
                            variant={
                              activity.status === 'COMPLETED'
                                ? 'default'
                                : 'secondary'
                            }
                          >
                            {activity.status}
                          </Badge>
                        </div>
                        <p className='text-sm text-muted-foreground mt-1'>
                          {activity.description || 'No description'}
                        </p>
                        <div className='flex items-center gap-4 mt-2 text-xs text-muted-foreground'>
                          <span>{activity.assignedToName}</span>
                          <span>•</span>
                          <span>
                            {formatDateTime(
                              activity.scheduledDate || activity.createdAt
                            )}
                          </span>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='text-center py-8'>
                  <Calendar className='w-12 h-12 mx-auto mb-4 text-muted-foreground' />
                  <p className='text-muted-foreground mb-4'>
                    No activities recorded yet.
                  </p>
                  <Button>Log First Activity</Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* Documents Tab */}
        <TabsContent value='documents' className='mt-6'>
          <Card>
            <CardHeader className='flex flex-row items-center justify-between'>
              <h3 className='font-semibold'>Documents</h3>
              <Button>
                <Upload className='w-4 h-4 mr-2' />
                Upload Document
              </Button>
            </CardHeader>
            <CardContent>
              {MOCK_DOCUMENTS.length > 0 ? (
                <div className='space-y-3'>
                  {MOCK_DOCUMENTS.map((doc) => (
                    <div
                      key={doc.id}
                      className='flex items-center justify-between p-3 bg-muted rounded-lg'
                    >
                      <div className='flex items-center gap-3'>
                        <div className='p-2 bg-background rounded'>
                          <FileText className='w-5 h-5 text-muted-foreground' />
                        </div>
                        <div>
                          <p className='font-medium'>{doc.name}</p>
                          <p className='text-sm text-muted-foreground'>
                            {doc.type} • {doc.size} • Uploaded by{' '}
                            {doc.uploadedBy}
                          </p>
                        </div>
                      </div>
                      <div className='flex items-center gap-2'>
                        <span className='text-sm text-muted-foreground'>
                          {formatDate(doc.uploadedAt)}
                        </span>
                        <Button variant='ghost' size='icon' className='h-8 w-8'>
                          <Download className='w-4 h-4' />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className='text-center py-8'>
                  <FileText className='w-12 h-12 mx-auto mb-4 text-muted-foreground' />
                  <p className='text-muted-foreground mb-4'>
                    No documents uploaded yet.
                  </p>
                  <Button>Upload First Document</Button>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>

      {/* Add Note Dialog */}
      <Dialog open={isNoteDialogOpen} onOpenChange={setIsNoteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Note</DialogTitle>
            <DialogDescription>
              Add a note about this customer for future reference.
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <Textarea
              placeholder='Enter your note...'
              value={noteText}
              onChange={(e) => setNoteText(e.target.value)}
              rows={4}
            />
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsNoteDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button onClick={handleAddNote} disabled={!noteText.trim()}>
              Add Note
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Delete Confirmation Dialog */}
      <Dialog open={isDeleteDialogOpen} onOpenChange={setIsDeleteDialogOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete Customer</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete this customer? This action cannot
              be undone and will also delete all related data.
            </DialogDescription>
          </DialogHeader>
          <div className='py-4'>
            <div className='p-4 bg-red-50 dark:bg-red-950/50 rounded-lg border border-red-200 dark:border-red-800'>
              <div className='flex items-center gap-3'>
                <Avatar>
                  <AvatarFallback>
                    {customer.name
                      .split(' ')
                      .map((n) => n[0])
                      .join('')
                      .slice(0, 2)}
                  </AvatarFallback>
                </Avatar>
                <div>
                  <p className='font-medium text-red-700 dark:text-red-300'>
                    {customer.name}
                  </p>
                  <p className='text-sm text-red-600 dark:text-red-400'>
                    {customer.email}
                  </p>
                </div>
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button
              variant='outline'
              onClick={() => setIsDeleteDialogOpen(false)}
            >
              Cancel
            </Button>
            <Button variant='destructive' onClick={handleDelete}>
              Delete Customer
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default CustomerDetailPageEnhanced;
