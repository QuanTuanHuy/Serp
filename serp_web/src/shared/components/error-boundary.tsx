/**
 * Error Boundary Component - Global error handling for React components
 * (authors: QuanTuanHuy, Description: Part of Serp Project)
 */

'use client';

import React, { Component, ErrorInfo, ReactNode } from 'react';
import { Button } from './ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from './ui/card';
import {
  AlertTriangle,
  RefreshCw,
  Home,
  ChevronDown,
  ChevronUp,
} from 'lucide-react';

interface ErrorBoundaryProps {
  children: ReactNode;
  fallback?: ReactNode;
  onError?: (error: Error, errorInfo: ErrorInfo) => void;
  showErrorDetails?: boolean;
  level?: 'page' | 'component' | 'module';
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
  errorInfo: ErrorInfo | null;
  showDetails: boolean;
}

export class ErrorBoundary extends Component<
  ErrorBoundaryProps,
  ErrorBoundaryState
> {
  constructor(props: ErrorBoundaryProps) {
    super(props);
    this.state = {
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    };
  }

  static getDerivedStateFromError(error: Error): Partial<ErrorBoundaryState> {
    return {
      hasError: true,
      error,
    };
  }

  componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);

    this.setState({
      error,
      errorInfo,
    });

    // Call custom error handler if provided
    if (this.props.onError) {
      this.props.onError(error, errorInfo);
    }

    // Log to monitoring service (e.g., Sentry, LogRocket)
    this.logErrorToService(error, errorInfo);
  }

  private logErrorToService = (error: Error, errorInfo: ErrorInfo) => {
    // TODO: Integrate with monitoring service
    console.log('Logging error to monitoring service:', {
      error: error.message,
      stack: error.stack,
      componentStack: errorInfo.componentStack,
      props: this.props,
      level: this.props.level || 'component',
      timestamp: new Date().toISOString(),
    });
  };

  private handleRetry = () => {
    this.setState({
      hasError: false,
      error: null,
      errorInfo: null,
      showDetails: false,
    });
  };

  private handleReload = () => {
    window.location.reload();
  };

  private handleGoHome = () => {
    window.location.href = '/';
  };

  private toggleDetails = () => {
    this.setState((prev) => ({ showDetails: !prev.showDetails }));
  };

  render() {
    if (this.state.hasError) {
      // Use custom fallback if provided
      if (this.props.fallback) {
        return this.props.fallback;
      }

      // Default error UI based on error level
      const { level = 'component' } = this.props;
      const { error, errorInfo, showDetails } = this.state;

      if (level === 'page') {
        return (
          <div className='min-h-screen flex items-center justify-center p-4 bg-background'>
            <Card className='w-full max-w-2xl'>
              <CardHeader className='text-center'>
                <div className='mx-auto w-16 h-16 bg-destructive/10 rounded-full flex items-center justify-center mb-4'>
                  <AlertTriangle className='w-8 h-8 text-destructive' />
                </div>
                <CardTitle className='text-2xl'>Something went wrong</CardTitle>
                <CardDescription className='text-base'>
                  We're sorry, but there was an error loading this page. Please
                  try again.
                </CardDescription>
              </CardHeader>

              <CardContent className='space-y-4'>
                <div className='flex flex-col sm:flex-row gap-2 justify-center'>
                  <Button
                    onClick={this.handleRetry}
                    className='flex items-center gap-2'
                  >
                    <RefreshCw className='w-4 h-4' />
                    Try Again
                  </Button>
                  <Button
                    variant='outline'
                    onClick={this.handleReload}
                    className='flex items-center gap-2'
                  >
                    <RefreshCw className='w-4 h-4' />
                    Reload Page
                  </Button>
                  <Button
                    variant='ghost'
                    onClick={this.handleGoHome}
                    className='flex items-center gap-2'
                  >
                    <Home className='w-4 h-4' />
                    Go Home
                  </Button>
                </div>

                {this.props.showErrorDetails !== false && error && (
                  <div className='space-y-2'>
                    <Button
                      variant='ghost'
                      size='sm'
                      onClick={this.toggleDetails}
                      className='flex items-center gap-2 mx-auto'
                    >
                      {showDetails ? (
                        <>
                          <ChevronUp className='w-4 h-4' />
                          Hide Details
                        </>
                      ) : (
                        <>
                          <ChevronDown className='w-4 h-4' />
                          Show Details
                        </>
                      )}
                    </Button>

                    {showDetails && (
                      <div className='text-left space-y-3'>
                        <div>
                          <h4 className='font-medium text-sm mb-1'>
                            Error Message:
                          </h4>
                          <code className='text-xs bg-muted p-2 rounded block'>
                            {error.message}
                          </code>
                        </div>

                        {error.stack && (
                          <div>
                            <h4 className='font-medium text-sm mb-1'>
                              Stack Trace:
                            </h4>
                            <pre className='text-xs bg-muted p-2 rounded overflow-auto max-h-40'>
                              {error.stack}
                            </pre>
                          </div>
                        )}

                        {errorInfo?.componentStack && (
                          <div>
                            <h4 className='font-medium text-sm mb-1'>
                              Component Stack:
                            </h4>
                            <pre className='text-xs bg-muted p-2 rounded overflow-auto max-h-40'>
                              {errorInfo.componentStack}
                            </pre>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        );
      }

      if (level === 'module') {
        return (
          <Card className='w-full'>
            <CardHeader>
              <div className='flex items-center gap-3'>
                <div className='w-10 h-10 bg-destructive/10 rounded-full flex items-center justify-center'>
                  <AlertTriangle className='w-5 h-5 text-destructive' />
                </div>
                <div>
                  <CardTitle className='text-lg'>Module Error</CardTitle>
                  <CardDescription>
                    This module encountered an error and couldn't load properly.
                  </CardDescription>
                </div>
              </div>
            </CardHeader>

            <CardContent className='space-y-3'>
              <div className='flex gap-2'>
                <Button
                  size='sm'
                  onClick={this.handleRetry}
                  className='flex items-center gap-2'
                >
                  <RefreshCw className='w-3 h-3' />
                  Retry
                </Button>
                <Button size='sm' variant='outline' onClick={this.handleReload}>
                  Reload Page
                </Button>
              </div>

              {this.props.showErrorDetails !== false && error && (
                <details className='text-sm'>
                  <summary className='cursor-pointer font-medium mb-2'>
                    Error Details
                  </summary>
                  <code className='text-xs bg-muted p-2 rounded block'>
                    {error.message}
                  </code>
                </details>
              )}
            </CardContent>
          </Card>
        );
      }

      // Component level (default)
      return (
        <div className='p-4 border-2 border-dashed border-destructive/20 rounded-lg bg-destructive/5'>
          <div className='flex items-center gap-3 mb-3'>
            <AlertTriangle className='w-5 h-5 text-destructive' />
            <div>
              <h3 className='font-medium text-sm'>Component Error</h3>
              <p className='text-xs text-muted-foreground'>
                This component failed to render
              </p>
            </div>
          </div>

          <div className='flex gap-2'>
            <Button size='sm' variant='outline' onClick={this.handleRetry}>
              <RefreshCw className='w-3 h-3 mr-1' />
              Retry
            </Button>
          </div>

          {this.props.showErrorDetails !== false && error && (
            <details className='mt-3 text-xs'>
              <summary className='cursor-pointer mb-1'>Show Error</summary>
              <code className='bg-muted p-1 rounded text-xs'>
                {error.message}
              </code>
            </details>
          )}
        </div>
      );
    }

    return this.props.children;
  }
}

// HOC for wrapping components with error boundary
export function withErrorBoundary<P extends object>(
  Component: React.ComponentType<P>,
  errorBoundaryProps?: Omit<ErrorBoundaryProps, 'children'>
) {
  const WrappedComponent = (props: P) => (
    <ErrorBoundary {...errorBoundaryProps}>
      <Component {...props} />
    </ErrorBoundary>
  );

  WrappedComponent.displayName = `withErrorBoundary(${Component.displayName || Component.name})`;

  return WrappedComponent;
}

// Hook for using error boundary in functional components
export function useErrorHandler() {
  return (error: Error, errorInfo?: ErrorInfo) => {
    console.error('Manual error boundary trigger:', error, errorInfo);
    throw error;
  };
}
