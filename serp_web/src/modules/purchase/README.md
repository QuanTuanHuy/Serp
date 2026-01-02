# Purchase Module

## Overview

The Purchase module manages procurement operations including suppliers, products, purchase orders, facilities, and shipments.

## Key Features

- **Supplier Management**: Track and manage supplier relationships
- **Product Catalog**: Maintain product information and pricing
- **Purchase Orders**: Create and manage purchase orders
- **Facilities**: Manage warehouse and storage facilities
- **Categories**: Organize products into categories
- **Shipments**: Track incoming shipments

## Structure

```
purchase/
├── api/              # API endpoints (purchaseApi.ts)
├── components/       # React components
│   ├── layout/      # Layout components (Header, Layout)
│   └── PurchaseAuthGuard.tsx
├── store/           # Redux state management
├── types/           # TypeScript type definitions
└── index.ts         # Module exports
```

## API Endpoints

- Suppliers: CRUD operations for supplier management
- Products: Product catalog management
- Orders: Purchase order creation and tracking
- Facilities: Warehouse management
- Categories: Product categorization
- Shipments: Shipment tracking

## Technology

- Next.js 15 App Router
- RTK Query for API calls
- Redux Toolkit for state management
- TypeScript for type safety
- Shadcn UI components
