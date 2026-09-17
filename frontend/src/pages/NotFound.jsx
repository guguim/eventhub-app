import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Ghost, ArrowLeft } from 'lucide-react';

const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className="container animate-fade-in" style={{ 
      display: 'flex', 
      justifyContent: 'center', 
      alignItems: 'center', 
      minHeight: 'calc(100vh - var(--navbar-height))',
    }}>
      <div style={{ textAlign: 'center', maxWidth: '480px' }}>
        {/* Ícone animado */}
        <div style={{ marginBottom: '2rem' }}>
          <Ghost 
            size={80} 
            style={{ color: 'var(--color-primary)', opacity: 0.8 }} 
            className="animate-pulse" 
          />
        </div>

        {/* Código do erro com gradiente */}
        <h1 className="text-gradient" style={{ 
          fontSize: '6rem', 
          fontWeight: 800, 
          lineHeight: 1, 
          marginBottom: '0.5rem',
          letterSpacing: '-0.05em',
        }}>
          404
        </h1>

        <h2 style={{ marginBottom: '1rem', fontSize: '1.5rem' }}>
          Página não encontrada
        </h2>

        <p style={{ 
          color: 'var(--text-muted)', 
          fontSize: '1.05rem', 
          lineHeight: 1.6,
          marginBottom: '2.5rem',
        }}>
          Parece que essa página saiu pra organizar um evento e esqueceu de voltar. 
          Que tal voltar ao Dashboard?
        </p>

        <button 
          className="btn-primary" 
          onClick={() => navigate('/dashboard')}
          style={{ padding: '0.85rem 2rem', fontSize: '1rem' }}
        >
          <ArrowLeft size={20} />
          Voltar ao Dashboard
        </button>
      </div>
    </div>
  );
};

export default NotFound;
