# SERP Web Application

Modern Enterprise Resource Planning (ERP) system built with Next.js 15, TypeScript, and Tailwind CSS.

## 🛠️ Tech Stack

- **Framework**: Next.js 15 with Turbopack
- **Language**: TypeScript
- **Styling**: Tailwind CSS 4.0
- **UI Components**: ShadCN UI
- **State Management**: Redux Toolkit
- **Theme**: next-themes for dark/light mode
- **Icons**: Lucide React
- **Development**: ESLint, Prettier, Husky

## 📁 Project Structure

```
src/
├── app/                    # Next.js App Router
│   ├── (dashboard)/        # Dashboard route group
│   │   ├── crm/           # CRM module pages
│   │   ├── accounting/    # Accounting module pages
│   │   └── inventory/     # Inventory module pages
│   └── layout.tsx
├── modules/               # Business Logic Modules
│   ├── crm/
│   │   ├── components/    # CRM-specific UI
│   │   ├── hooks/         # CRM custom hooks
│   │   ├── services/      # CRM API calls
│   │   ├── store/         # CRM Redux slices
│   │   ├── types/         # CRM TypeScript types
│   │   └── index.ts       # Barrel exports
│   ├── accounting/        # Same structure
│   └── inventory/         # Same structure
│   └── ptm/               # Same structure
├── shared/                # Cross-Module Resources
│   ├── components/        # Reusable UI components
│   ├── hooks/            # Common hooks
│   ├── services/         # Shared API utilities
│   ├── types/            # Common types
│   └── utils/            # Helper functions
└── lib/                  # Core Configuration
    ├── store.ts          # Redux store setup
    └── api/              # API configuration
```

## 🚦 Getting Started

### Prerequisites

- Node.js 18+
- npm or yarn

### Installation

1. **Install dependencies**:

   ```bash
   npm install
   ```

2. **Start development server**:
   ```bash
   npm run dev
   ```

### Available Scripts

```bash
# Development
npm run dev              # Start development server
npm run build            # Build for production
npm run start            # Start production server

# Code Quality
npm run lint             # Run ESLint
npm run lint:fix         # Fix ESLint errors automatically
npm run format           # Format code with Prettier
npm run format:check     # Check if code is formatted
npm run type-check       # TypeScript type checking
```

## 🤝 Development Guidelines

### Code Quality Standards

- Use TypeScript strict mode (no `any` types)
- Follow ESLint rules for React and TypeScript
- Use Prettier for consistent formatting
- Write meaningful commit messages

### Component Development

- Use ShadCN UI components as base
- Implement proper TypeScript interfaces
- Follow accessibility best practices
- Create responsive designs

### Git Workflow

- Pre-commit hooks ensure code quality
- Automatic linting and formatting
- Type checking before commits
- Import order validation

## 📚 Documentation

- **TypeScript**: Type definitions and interfaces
- **ESLint**: Code quality and consistency rules
- **Prettier**: Code formatting standards

## 🔧 Configuration Files

- `.eslintrc.json`: ESLint rules and settings
- `.prettierrc.json`: Prettier formatting rules
- `components.json`: ShadCN UI configuration
- `tsconfig.json`: TypeScript configuration
- `.env.example`: Environment variables template

---

**Built with ❤️ using Next.js 15, TypeScript, and modern web technologies**

**Author**: QuanTuanHuy  
**Last Updated**: September 14, 2025
