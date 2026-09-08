import React, { useState } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate, Link } from 'react-router-dom';
import { LogIn } from 'lucide-react';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault(); // Impede o recarregamento "flicker" padrão do formulário
    setError('');
    try {
      await login(email, password);
      navigate('/dashboard'); // Se logou, vai pros eventos!
    } catch (err) {
      setError('Credenciais inválidas. Tente novamente.');
    }
  };

  return (
    <div className="container animate-fade-in" style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '80vh' }}>
      {/* Aqui usamos o nosso Design System CSS puro: glass-panel */}
      <div className="glass-panel" style={{ padding: '2.5rem', width: '100%', maxWidth: '400px' }}>
        
        <div style={{ textAlign: 'center', marginBottom: '2rem' }}>
          <h2>Bem-vindo de volta!</h2>
          <p style={{ color: 'var(--text-muted)' }}>Faça login no EventHub</p>
        </div>

        {error && <div style={{ color: '#ff6b6b', marginBottom: '1rem', textAlign: 'center', fontWeight: '500' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>E-mail</label>
            <input 
              type="email" 
              required 
              value={email} 
              onChange={(e) => setEmail(e.target.value)} 
              placeholder="seu@email.com"
            />
          </div>
          
          <div className="input-group">
            <label>Senha</label>
            <input 
              type="password" 
              required 
              value={password} 
              onChange={(e) => setPassword(e.target.value)} 
              placeholder="••••••••"
            />
          </div>

          <button type="submit" className="btn-primary" style={{ width: '100%', marginTop: '1rem' }}>
            <LogIn size={20} /> Entrar
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: '1.5rem', fontSize: '0.9rem' }}>
          <span style={{ color: 'var(--text-muted)' }}>Ainda não tem conta? </span>
          <Link to="/register" style={{ color: 'var(--color-primary)', textDecoration: 'none', fontWeight: '600' }}>
            Cadastre-se
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Login;
