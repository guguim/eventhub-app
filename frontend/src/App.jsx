import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';

// Importação das Páginas
import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';
import Dashboard from './pages/Dashboard/Dashboard';
import EventDetails from './pages/Event/EventDetails';
import CreateEvent from './pages/Event/CreateEvent';

function App() {
  return (
    // AuthProvider abraça tudo para garantir que qualquer tela tenha acesso ao Usuário
    <AuthProvider>
      <Router>
        <Routes>
          
          {/* Rotas Públicas */}
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Rota Raiz: Se a pessoa acessar o site raiz, mandamos pro dashboard automaticamente */}
          <Route path="/" element={<Navigate to="/dashboard" replace />} />

          {/* Rotas Protegidas pelo nosso Guarda-Costas */}
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
      </Router>
    </AuthProvider>
  );
}

export default App;
