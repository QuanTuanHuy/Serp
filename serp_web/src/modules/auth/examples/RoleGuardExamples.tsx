/**
 * Authors: QuanTuanHuy
 * Description: Part of Serp Project - Examples of how to use RoleGuard and ProtectedRoute
 */

import React from 'react';
import { ProtectedRoute, withAuth } from '../components/ProtectedRoute';
import { RoleGuard } from '../components/RoleGuard';

// Example 1: Basic usage with roles
export const AdminPanel = () => {
  return (
    <ProtectedRoute roles='admin'>
      <div>
        <h1>Admin Panel</h1>
        <p>Only admins can see this content</p>
      </div>
    </ProtectedRoute>
  );
};

// Example 2: Multiple roles (any)
export const ManagementSection = () => {
  return (
    <ProtectedRoute roles={['admin', 'manager']}>
      <div>
        <h1>Management Section</h1>
        <p>Admins and managers can see this</p>
      </div>
    </ProtectedRoute>
  );
};

// Example 3: Menu-based access
export const CRMModule = () => {
  return (
    <ProtectedRoute menuKey='crm'>
      <div>
        <h1>CRM Module</h1>
        <p>Only users with CRM menu access can see this</p>
      </div>
    </ProtectedRoute>
  );
};

// Example 4: Feature-based access
export const AdvancedReporting = () => {
  return (
    <ProtectedRoute featureKey='advanced_reporting'>
      <div>
        <h1>Advanced Reporting</h1>
        <p>Only users with advanced reporting feature enabled</p>
      </div>
    </ProtectedRoute>
  );
};

// Example 5: Permission-based access
export const UserManagement = () => {
  return (
    <ProtectedRoute permissions='user:manage'>
      <div>
        <h1>User Management</h1>
        <p>Only users with user:manage permission</p>
      </div>
    </ProtectedRoute>
  );
};

// Example 6: Complex requirements (roles AND permissions)
export const SystemSettings = () => {
  return (
    <ProtectedRoute
      roles={['admin', 'system_admin']}
      permissions={['system:configure', 'system:manage']}
      requireAllPermissions={true}
    >
      <div>
        <h1>System Settings</h1>
        <p>Requires admin role AND both system permissions</p>
      </div>
    </ProtectedRoute>
  );
};

// Example 7: Using RoleGuard for inline components
export const Dashboard = () => {
  return (
    <div>
      <h1>Dashboard</h1>

      {/* Always visible */}
      <div>
        <p>Welcome to the dashboard</p>
      </div>

      {/* Custom role guard */}
      <RoleGuard
        menuKey='financial_reports'
        fallback={<p>You don't have access to financial reports</p>}
      >
        <div className='bg-green-50 p-4 rounded'>
          <h2>Financial Reports</h2>
          <p>Revenue and expenses</p>
        </div>
      </RoleGuard>

      {/* Hide completely if no access */}
      <RoleGuard permissions='data:export' hideOnNoAccess={true}>
        <button className='bg-blue-500 text-white px-4 py-2 rounded'>
          Export Data
        </button>
      </RoleGuard>
    </div>
  );
};

// Example 8: Using withAuth HOC
const SecretComponent: React.FC<{ title: string }> = ({ title }) => (
  <div>
    <h1>{title}</h1>
    <p>This is a protected component</p>
  </div>
);

export const AdminOnlyComponent = withAuth(SecretComponent, {
  roles: 'admin',
  fallback: <div>You need admin role to access this</div>,
});

// Example 9: Dynamic navigation based on menu access
export const NavigationMenu = () => {
  const menuItems = [
    { key: 'dashboard', label: 'Dashboard', path: '/dashboard' },
    { key: 'crm', label: 'CRM', path: '/crm' },
    { key: 'accounting', label: 'Accounting', path: '/accounting' },
    { key: 'inventory', label: 'Inventory', path: '/inventory' },
    { key: 'reports', label: 'Reports', path: '/reports' },
    { key: 'admin', label: 'Admin', path: '/admin', roles: ['admin'] },
  ];

  return (
    <nav>
      <ul>
        {menuItems.map((item) => (
          <RoleGuard
            key={item.key}
            menuKey={item.key}
            roles={item.roles}
            hideOnNoAccess={true}
          >
            <li>
              <a href={item.path}>{item.label}</a>
            </li>
          </RoleGuard>
        ))}
      </ul>
    </nav>
  );
};

// Example 10: Button with permission check
export const ActionButton = () => {
  return (
    <div>
      <RoleGuard
        permissions='user:create'
        fallback={
          <button
            disabled
            className='bg-gray-300 text-gray-500 px-4 py-2 rounded'
          >
            Create User (No Permission)
          </button>
        }
      >
        <button className='bg-green-500 text-white px-4 py-2 rounded'>
          Create User
        </button>
      </RoleGuard>
    </div>
  );
};
