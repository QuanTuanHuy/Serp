# Role-based và Attribute-based Authorization Guide

## Tổng quan

Hệ thống authorization này cung cấp các component và hooks để kiểm soát quyền truy cập dựa trên:

- **Roles**: Admin, Manager, User, etc.
- **Permissions**: Specific actions like "user:create", "report:view"
- **Menu Access**: Backend trả về danh sách menu user có thể xem
- **Feature Access**: Backend trả về danh sách features user có thể sử dụng
- **Organization**: Multi-tenant support

## 📁 Architecture Overview

```
src/modules/auth/
├── types/
│   ├── auth.ts          # User, AuthState types
│   ├── permissions.ts   # Permission-related types
│   ├── api.ts          # API request/response DTOs
│   └── forms.ts        # Form validation types
├── hooks/
│   ├── useAuth.ts      # Authentication with Redux slice
│   └── usePermissions.ts # Simple permissions hook
└── components/
    ├── ProtectedRoute.tsx # Route-level protection
    └── RoleGuard.tsx     # Component-level protection
```

## Components

### 1. ProtectedRoute

Bảo vệ toàn bộ route/page:

```tsx
// Chỉ authentication
<ProtectedRoute>
  <Dashboard />
</ProtectedRoute>

// Với role
<ProtectedRoute roles="admin">
  <AdminPanel />
</ProtectedRoute>

// Với multiple roles (any)
<ProtectedRoute roles={["admin", "manager"]}>
  <ManagementSection />
</ProtectedRoute>

// Với permissions
<ProtectedRoute permissions="user:manage">
  <UserManagement />
</ProtectedRoute>

// Với menu access
<ProtectedRoute menuKey="crm">
  <CRMModule />
</ProtectedRoute>

// Complex requirements
<ProtectedRoute
  roles={["admin", "system_admin"]}
  permissions={["system:configure", "system:manage"]}
  requireAllPermissions={true}
>
  <SystemSettings />
</ProtectedRoute>
```

### 2. RoleGuard - Simplified Implementation

Bảo vệ component/element cụ thể với performance tối ưu:

```tsx
// ✅ Hide component if no access (better performance)
<RoleGuard roles="admin" hideOnNoAccess={true}>
  <button>Delete All Users</button>
</RoleGuard>

// ✅ Show fallback if no access
<RoleGuard
  permissions="data:export"
  fallback={<p>You don't have export permission</p>}
>
  <ExportButton />
</RoleGuard>

// ✅ Menu-based access
<RoleGuard menuKey="financial_reports">
  <FinancialWidget />
</RoleGuard>

// ⭐ Complex requirements (uses canAccess internally)
<RoleGuard
  roles={["admin", "manager"]}
  permissions={["user:create", "user:update"]}
  requireAllRoles={false}      // any role
  requireAllPermissions={true} // all permissions
  menuKey="user_management"
  fallback={<div>Access denied</div>}
>
  <UserManagement />
</RoleGuard>
```

### 3. Convenience Components

```tsx
// Admin only
<AdminOnly>
  <AdminWidget />
</AdminOnly>

// Manager and above
<ManagerAndAbove>
  <ManagementTools />
</ManagerAndAbove>
```

## Hooks

### usePermissions() - Simplified & Fast

Hook chính để kiểm tra permissions với API đơn giản:

```tsx
const {
  userPermissions, // All permissions data
  isLoading, // Loading state
  hasRole, // Check single role
  hasPermission, // Check single permission
  hasMenuAccess, // Check menu access
  hasFeatureAccess, // Check feature access
  canAccess, // ⭐ All-in-one checker
  getAccessibleMenus, // Get filtered menus
} = usePermissions();

// ✅ Simple checks
if (hasRole('admin')) {
  // Show admin features
}

if (hasPermission('user:create')) {
  // Show create user button
}

if (hasMenuAccess('crm')) {
  // Show CRM navigation
}

// ⭐ Complex check with single function
const canViewReports = canAccess({
  roles: ['manager', 'admin'],
  permissions: ['report:view'],
  menuKey: 'reports',
  requireAllRoles: false, // any role (default)
  requireAllPermissions: false, // any permission (default)
});
```

### Key Features:

- **⚡ Smart Caching**: 5min for permissions, 10min for menus
- **🔄 Auto Sync**: Automatically refetches when needed
- **📱 Offline Support**: Fallback to cached user roles
- **🎯 Simple API**: Single `canAccess()` function for complex checks

## HOCs (Higher Order Components)

### withAuth

Wrap component với authentication check:

```tsx
const AdminComponent = withAuth(MyComponent, {
  roles: 'admin',
  fallback: <div>Access denied</div>,
});
```

### withRoleGuard

Wrap component với role/permission check:

```tsx
const SecureComponent = withRoleGuard(MyComponent, {
  permissions: 'sensitive:access',
  fallback: <div>Insufficient permissions</div>,
});
```

## Backend Integration

### Required API Endpoints

```typescript
// 1. Get user permissions
GET /users/permissions/me
Response: {
  code: number;
  status: string;
  message: string;
  data: {
    roles: string[];
    permissions: string[];
    features: FeatureAccess[];
    organizationPermissions?: OrganizationPermission[];
  }
}

// 2. Get user menus
GET /users/menus/me
Response: {
  code: number;
  status: string;
  message: string;
  data: {
    menus: MenuAccess[];
  }
}
```

### Data Structures

```typescript
interface MenuAccess {
  menuKey: string;
  menuName: string;
  path?: string;
  isVisible: boolean;
  icon?: string; // ✅ New: Menu icon
  order?: number; // ✅ New: Display order
  children?: MenuAccess[];
}

interface FeatureAccess {
  featureKey: string;
  featureName: string;
  isEnabled: boolean;
  description?: string; // ✅ New: Feature description
  permissions?: string[];
}

interface AccessConfig {
  roles?: string[];
  permissions?: string[];
  requireAllRoles?: boolean;
  requireAllPermissions?: boolean;
  menuKey?: string;
  featureKey?: string;
  organizationId?: number;
  organizationRole?: string;
  organizationPermission?: string;
}
```

## Use Cases & Patterns

### 1. Dynamic Navigation (Performance Optimized)

```tsx
const Navigation = () => {
  const { getAccessibleMenus } = usePermissions();

  // ✅ Memoized - only recalculates when menus change
  const menus = useMemo(() => getAccessibleMenus(), [getAccessibleMenus]);

  return (
    <nav>
      {menus.map((menu) => (
        <NavItem key={menu.menuKey} menu={menu} />
      ))}
    </nav>
  );
};
```

### 2. Conditional UI Elements (Simple Pattern)

```tsx
const UserTable = () => {
  const { hasPermission, canAccess } = usePermissions();

  // ✅ Simple check
  const canEdit = hasPermission('user:edit');

  // ✅ Complex check
  const canManage = canAccess({
    roles: ['admin', 'manager'],
    permissions: ['user:manage'],
  });

  return (
    <table>
      <thead>
        <tr>
          <th>Name</th>
          <th>Email</th>
          {canEdit && <th>Edit</th>}
          {canManage && <th>Actions</th>}
        </tr>
      </thead>
      {/* ... */}
    </table>
  );
};
```

### 3. Feature Toggles (Clean Implementation)

```tsx
const Dashboard = () => {
  const { hasFeatureAccess, canAccess } = usePermissions();

  return (
    <div>
      <h1>Dashboard</h1>

      {/* ✅ Simple feature check */}
      {hasFeatureAccess('advanced_analytics') && <AdvancedAnalyticsWidget />}

      {/* ✅ Feature + permission check */}
      {canAccess({
        featureKey: 'real_time_data',
        permissions: ['data:realtime'],
      }) && <RealTimeDataWidget />}
    </div>
  );
};
```

### 4. Button States (UX Pattern)

```tsx
const ActionButtons = () => {
  const { canAccess } = usePermissions();

  const canCreate = canAccess({ permissions: ['user:create'] });
  const canDelete = canAccess({
    roles: ['admin'],
    permissions: ['user:delete'],
  });

  return (
    <div className='flex gap-2'>
      <button
        disabled={!canCreate}
        className={canCreate ? 'btn-primary' : 'btn-disabled'}
      >
        Create User
      </button>

      {/* ✅ Hide dangerous actions completely */}
      <RoleGuard permissions='user:delete' hideOnNoAccess>
        <button className='btn-danger'>Delete User</button>
      </RoleGuard>
    </div>
  );
};
```

## ⚡ Performance Best Practices

### 1. Smart Component Rendering

```tsx
// ✅ Good: Hide completely for better performance
<RoleGuard roles="admin" hideOnNoAccess={true}>
  <ExpensiveAdminComponent />
</RoleGuard>

// ❌ Avoid: Always renders fallback
<RoleGuard roles="admin" fallback={<div>No access</div>}>
  <ExpensiveAdminComponent />
</RoleGuard>
```

### 2. Caching Strategy

```tsx
// ✅ Permissions are automatically cached:
// - Permissions: 5 minutes
// - Menus: 10 minutes
// - Auto refetch on mount if cache is stale

const { userPermissions, isLoading } = usePermissions();
// No need to manage cache manually!
```

### 3. Memoization for Complex Checks

```tsx
const ComplexComponent = () => {
  const { canAccess } = usePermissions();

  // ✅ Memoize complex permission calculations
  const permissions = useMemo(
    () => ({
      canEdit: canAccess({ roles: ['admin', 'editor'], permissions: ['edit'] }),
      canDelete: canAccess({ roles: ['admin'], permissions: ['delete'] }),
      canExport: canAccess({ permissions: ['export'], menuKey: 'reports' }),
    }),
    [canAccess]
  );

  return (
    <div>
      {permissions.canEdit && <EditButton />}
      {permissions.canDelete && <DeleteButton />}
      {permissions.canExport && <ExportButton />}
    </div>
  );
};
```

## 🔒 Security Best Practices

### 1. Defense in Depth

```tsx
// ✅ Frontend: UX/UI experience
<RoleGuard permissions='user:delete'>
  <DeleteButton />
</RoleGuard>;

// ✅ Backend: Real security validation
const deleteUser = async (userId) => {
  // Always validate permissions on backend
  if (!hasPermission(currentUser, 'user:delete')) {
    throw new Error('Insufficient permissions');
  }
  // ...
};
```

### 2. Granular Permissions

```typescript
// ✅ Good: Specific and granular
('user:create', 'user:update', 'user:delete', 'user:view');
('report:view', 'report:export', 'report:create');
('admin:users', 'admin:settings', 'admin:system');

// ❌ Avoid: Too broad
('user:all', 'admin', 'super_user');
```

### 3. Secure Error Handling

```tsx
<RoleGuard
  permissions='sensitive:access'
  fallback={
    <div className='text-red-500 p-4 border border-red-200 rounded'>
      <p className='font-semibold'>Access Denied</p>
      <p>Contact your administrator for access to this feature.</p>
      <p className='text-sm text-gray-500 mt-2'>
        Required permission: View Reports
      </p>
    </div>
  }
>
  <SensitiveComponent />
</RoleGuard>
```

## 🔄 Migration Guide

### From Previous Implementation

```tsx
// ❌ Old: Complex, multiple properties
const { userRoles, userMenus, userFeatures, hasRole, hasPermission } =
  usePermissions();
const hasAccess =
  userRoles.includes('admin') &&
  userMenus.find((m) => m.menuKey === 'users') &&
  userFeatures.find((f) => f.featureKey === 'advanced');

// ✅ New: Simple, single function
const { canAccess } = usePermissions();
const hasAccess = canAccess({
  roles: ['admin'],
  menuKey: 'users',
  featureKey: 'advanced',
});
```

### Step-by-Step Migration

1. **Keep existing `ProtectedRoute`** - fully backward compatible
2. **Replace complex permission checks** with `canAccess()`
3. **Add `hideOnNoAccess={true}`** for better performance
4. **Use modular types** from `auth/types/*`
5. **Update backend endpoints** to match new response format

### Quick Wins

```tsx
// ✅ Replace this pattern
if (hasRole('admin') && hasPermission('user:create')) {
  // show button
}

// ✅ With this
if (canAccess({ roles: ['admin'], permissions: ['user:create'] })) {
  // show button
}
```

## 🧪 Testing Patterns

### Mock Permissions

```tsx
// Test utility
const mockPermissions = (overrides = {}) => ({
  roles: ['user'],
  permissions: ['basic:access'],
  menus: [{ menuKey: 'dashboard', isVisible: true }],
  features: [{ featureKey: 'basic_search', isEnabled: true }],
  ...overrides,
});

// Test with specific permissions
const renderWithPermissions = (component, permissions = {}) => {
  const mockUsePermissions = jest.fn(() => ({
    canAccess: jest.fn(() => true),
    hasRole: jest.fn(() => true),
    ...mockPermissions(permissions),
  }));

  jest.mock('../hooks/usePermissions', () => mockUsePermissions);
  return render(component);
};

// Example test
test('shows admin panel for admin users', () => {
  renderWithPermissions(
    <RoleGuard roles='admin'>
      <AdminPanel />
    </RoleGuard>,
    { roles: ['admin'] }
  );
  expect(screen.getByText('Admin Panel')).toBeInTheDocument();
});
```

## 🎯 Summary

### Key Improvements

- **🚀 Simpler API**: Single `canAccess()` function
- **⚡ Better Performance**: Smart caching + hideOnNoAccess
- **🔧 Modular Types**: Organized in separate files
- **✨ Developer Experience**: Intuitive and predictable

### When to Use Each Pattern

- **`ProtectedRoute`**: Page/route level protection
- **`RoleGuard`**: Component level protection
- **`canAccess()`**: Logic-based permission checks
- **`hasRole/hasPermission`**: Simple single checks

This refactored system provides the **right balance** between power and simplicity, following the principle: **"Make simple things simple, and complex things possible"**.
