import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { AuthProvider, useAuth } from './contexts/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';
import Navbar from './components/Navbar';

import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import EventDetails from './pages/Event/EventDetails';
import CreateEvent from './pages/Event/CreateEvent';

// Layout que inclui a Navbar em páginas autenticadas
const AppLayout = ({ children }) => {
  const { user } = useAuth();
  const location = useLocation();

  // Não renderiza a Navbar em rotas públicas (login, register)
  const publicRoutes = ['/login', '/register'];
  const isPublicRoute = publicRoutes.includes(location.pathname);

  return (
    <>
      {user && !isPublicRoute && <Navbar />}
      {children}
    </>
  );
};

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppLayout>
          <Routes>

            {/* Rotas Públicas */}
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* Redirect raiz */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            {/* Rotas Protegidas */}
            <Route 
              path="/dashboard" 
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              } 
            />

            <Route 
              path="/eventos/:id" 
              element={
                <ProtectedRoute>
                  <EventDetails />
                </ProtectedRoute>
              } 
            />

            <Route 
              path="/novo-evento" 
              element={
                <ProtectedRoute>
                  <CreateEvent />
                </ProtectedRoute>
              } 
            />

          </Routes>
        </AppLayout>
      </Router>
    </AuthProvider>
  );
}

export default App;
