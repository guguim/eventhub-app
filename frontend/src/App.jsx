import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './contexts/AuthContext';
import { ProtectedRoute } from './components/ProtectedRoute';

// Importação das Páginas
import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';

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
                <div className="container animate-fade-in" style={{textAlign: 'center', marginTop: '20vh'}}>
                  <h1>🎉 Acesso Concedido!</h1>
                  <p style={{color: 'var(--color-primary)'}}>O seu Token JWT foi aceito. Você está no Dashboard Protegido.</p>
                  <p style={{marginTop: '1rem', color: 'var(--text-muted)'}}>Em breve substituiremos isso pela listagem de eventos.</p>
                </div>
              </ProtectedRoute>
            } 
          />

        </Routes>
      </Router>
    </AuthProvider>
  );
}

export default App;
