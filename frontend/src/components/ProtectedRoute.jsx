import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

// Este é um componente "Guarda-Costas". 
// Nós vamos "envolver" as páginas secretas com ele.
export const ProtectedRoute = ({ children }) => {
  const { user } = useAuth();

  // Se o usuário tentar acessar uma URL (ex: /dashboard) e não estiver logado (sem token),
  // o React automaticamente joga ele de volta para a tela de login!
  if (!user) {
    return <Navigate to="/login" replace />;
  }

  // Se estiver logado, deixa passar e renderiza a tela solicitada.
  return children;
};
