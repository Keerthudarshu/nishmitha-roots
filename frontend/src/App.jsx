import React, { Suspense } from "react";
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { CartProvider } from './contexts/CartContext';
import ErrorBoundary from './components/ErrorBoundary';
import ScrollToTop from './components/ScrollToTop';
import ProtectedRoute from './components/ProtectedRoute';

// Lazy load page components for better performance
const Homepage = React.lazy(() => import('./pages/homepage'));
const ProductCollectionGrid = React.lazy(() => import('./pages/product-collection-grid'));
const ProductDetailPage = React.lazy(() => import('./pages/product-detail-page'));
const ShoppingCart = React.lazy(() => import('./pages/shopping-cart'));
const CheckoutProcess = React.lazy(() => import('./pages/checkout-process'));
const UserAuth = React.lazy(() => import('./pages/user-auth'));
const UserAccountDashboard = React.lazy(() => import('./pages/user-account-dashboard'));
const AdminLogin = React.lazy(() => import('./pages/admin-login'));
const AdminPanel = React.lazy(() => import('./pages/admin-panel'));
const AdminDashboard = React.lazy(() => import('./pages/admin-dashboard'));
const NotFound = React.lazy(() => import('./pages/NotFound'));

// Loading component
const PageLoader = () => (
  <div className="min-h-screen bg-background flex items-center justify-center">
    <div className="flex flex-col items-center gap-4">
      <div className="w-8 h-8 border-4 border-primary border-t-transparent rounded-full animate-spin"></div>
      <p className="text-muted-foreground">Loading...</p>
    </div>
  </div>
);

// Protected Route Component
const ProtectedAdminRoute = ({ children }) => {
  const adminUser = JSON.parse(localStorage.getItem('adminUser') || 'null');
  const sessionData = localStorage.getItem('neenu_auth_session');
  
  let isValidAdmin = false;
  
  // Trust backend-issued admin role persisted at login
  if (adminUser && (adminUser.role || '').toLowerCase() === 'admin') {
    isValidAdmin = true;
  } else if (sessionData) {
    try {
      const session = JSON.parse(sessionData);
      // Minimal fallback; main trust is adminUser role
      isValidAdmin = !!session?.userId;
    } catch (error) {
      console.error('Invalid session data:', error);
    }
  }
  
  if (!isValidAdmin) {
    localStorage.removeItem('adminUser');
    localStorage.removeItem('neenu_auth_session');
    return <Navigate to="/admin-login" replace />;
  }
  
  return children;
};

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <CartProvider>
          <div className="App">
            <ErrorBoundary>
              <ScrollToTop />
              <Suspense fallback={<PageLoader />}>
                <Routes>
                  <Route path="/" element={<Homepage />} />
                  <Route path="/homepage" element={<Homepage />} />
                  <Route path="/product-collection-grid" element={<ProductCollectionGrid />} />
                  <Route path="/product-detail-page/:id" element={<ProductDetailPage />} />
                  <Route path="/product-detail-page" element={<ProductDetailPage />} />
                  <Route path="/shopping-cart" element={<ShoppingCart />} />
                  <Route 
                    path="/checkout-process" 
                    element={
                      <ProtectedRoute message="Please sign in to continue with checkout">
                        <CheckoutProcess />
                      </ProtectedRoute>
                    } 
                  />
                  <Route path="/user-login" element={<UserAuth />} />
                  <Route path="/user-register" element={<UserAuth />} />
                  <Route 
                    path="/user-account-dashboard" 
                    element={
                      <ProtectedRoute message="Please sign in to access your account dashboard">
                        <UserAccountDashboard />
                      </ProtectedRoute>
                    } 
                  />
                  <Route path="/admin-login" element={<AdminLogin />} />
                  <Route 
                    path="/admin-dashboard" 
                    element={
                      <ProtectedAdminRoute>
                        <AdminDashboard />
                      </ProtectedAdminRoute>
                    } 
                  />
                  <Route 
                    path="/admin-panel" 
                    element={
                      <ProtectedAdminRoute>
                        <AdminPanel />
                      </ProtectedAdminRoute>
                    } 
                  />
                  <Route path="*" element={<NotFound />} />
                </Routes>
              </Suspense>
            </ErrorBoundary>
          </div>
        </CartProvider>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
