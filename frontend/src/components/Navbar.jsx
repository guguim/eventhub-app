import React, { useState, useRef, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { LogOut, User, ChevronDown, CalendarDays } from 'lucide-react';
import NotificationBell from './NotificationBell';

const Navbar = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  // Fecha o dropdown ao clicar fora
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  // Gera as iniciais do nome do usuário para o avatar
  const getInitials = (name) => {
    if (!name) return '?';
    return name
      .split(' ')
      .map(word => word[0])
      .slice(0, 2)
      .join('')
      .toUpperCase();
  };

  const handleLogout = () => {
    setDropdownOpen(false);
    logout();
    navigate('/login');
  };

  if (!user) return null;

  return (
    <nav className="navbar">
      {/* Brand / Logo */}
      <Link to="/dashboard" className="navbar-brand">
        <CalendarDays size={26} style={{ color: 'var(--color-primary)' }} />
        <span>EventHub</span>
      </Link>

      {/* Ações da direita */}
      <div className="navbar-actions">
        {/* Sino de Notificações */}
        <NotificationBell />

        {/* Avatar + Dropdown */}
        <div ref={dropdownRef} style={{ position: 'relative' }}>
          <button
            onClick={() => setDropdownOpen(!dropdownOpen)}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '0.5rem',
              background: 'none',
              border: 'none',
              cursor: 'pointer',
              color: 'var(--text-main)',
              padding: '0.25rem',
            }}
          >
            <div className="navbar-avatar">
              {getInitials(user.name)}
            </div>
            <span style={{ 
              fontWeight: 500, 
              fontSize: '0.9rem',
              maxWidth: '120px',
              overflow: 'hidden',
              textOverflow: 'ellipsis',
              whiteSpace: 'nowrap',
            }}>
              {user.name || 'Usuário'}
            </span>
            <ChevronDown 
              size={16} 
              style={{ 
                color: 'var(--text-muted)',
                transition: 'transform var(--transition-fast)',
                transform: dropdownOpen ? 'rotate(180deg)' : 'rotate(0)',
              }} 
            />
          </button>

          {/* Dropdown Menu */}
          {dropdownOpen && (
            <div 
              className="glass-panel-elevated animate-scale-in"
              style={{
                position: 'absolute',
                top: 'calc(100% + 8px)',
                right: 0,
                minWidth: '200px',
                padding: '0.5rem',
                zIndex: 200,
              }}
            >
              {/* Info do usuário no topo */}
              <div style={{ 
                padding: '0.75rem 1rem', 
                borderBottom: '1px solid var(--border-glass)',
                marginBottom: '0.5rem',
              }}>
                <p style={{ fontWeight: 600, fontSize: '0.9rem' }}>{user.name || 'Usuário'}</p>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', marginTop: '0.15rem' }}>Organizador</p>
              </div>

              {/* Menu Items */}
              <button
                onClick={() => {
                  setDropdownOpen(false);
                  navigate('/perfil');
                }}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  width: '100%',
                  padding: '0.65rem 1rem',
                  background: 'none',
                  border: 'none',
                  color: 'var(--text-main)',
                  cursor: 'pointer',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '0.9rem',
                  transition: 'background var(--transition-fast)',
                }}
                onMouseEnter={(e) => e.currentTarget.style.background = 'hsla(0,0%,100%,0.07)'}
                onMouseLeave={(e) => e.currentTarget.style.background = 'none'}
              >
                <User size={18} style={{ color: 'var(--text-muted)' }} />
                Meu Perfil
              </button>

              <div style={{ height: '1px', background: 'var(--border-glass)', margin: '0.25rem 0' }} />

              <button
                onClick={handleLogout}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '0.75rem',
                  width: '100%',
                  padding: '0.65rem 1rem',
                  background: 'none',
                  border: 'none',
                  color: 'var(--color-danger)',
                  cursor: 'pointer',
                  borderRadius: 'var(--radius-sm)',
                  fontSize: '0.9rem',
                  transition: 'background var(--transition-fast)',
                }}
                onMouseEnter={(e) => e.currentTarget.style.background = 'hsla(0,80%,65%,0.1)'}
                onMouseLeave={(e) => e.currentTarget.style.background = 'none'}
              >
                <LogOut size={18} />
                Sair
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
