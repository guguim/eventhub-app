import React, { useState, useEffect, useMemo } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { Calendar, MapPin, Plus, User, Search, X } from 'lucide-react';

const SORT_OPTIONS = [
  { value: 'newest', label: 'Mais recentes' },
  { value: 'oldest', label: 'Mais antigos' },
  { value: 'name-asc', label: 'A → Z' },
  { value: 'name-desc', label: 'Z → A' },
  { value: 'most-dates', label: 'Mais datas' },
];

const Dashboard = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortBy, setSortBy] = useState('newest');
  const navigate = useNavigate();

  useEffect(() => {
    fetchEvents();
  }, []);

  const fetchEvents = async () => {
    try {
      const response = await axios.get('/api/events');
      setEvents(response.data);
    } catch (error) {
      console.error("Erro ao buscar eventos", error);
    } finally {
      setLoading(false);
    }
  };

  // Filtra e ordena os eventos baseado na busca e no sort
  const filteredEvents = useMemo(() => {
    let result = [...events];

    // Filtro por busca
    if (searchQuery.trim()) {
      const query = searchQuery.toLowerCase();
      result = result.filter(event =>
        event.title.toLowerCase().includes(query) ||
        event.description?.toLowerCase().includes(query) ||
        event.organizerName?.toLowerCase().includes(query) ||
        event.location?.toLowerCase().includes(query)
      );
    }

    // Ordenação
    switch (sortBy) {
      case 'newest':
        result.sort((a, b) => b.id - a.id);
        break;
      case 'oldest':
        result.sort((a, b) => a.id - b.id);
        break;
      case 'name-asc':
        result.sort((a, b) => a.title.localeCompare(b.title));
        break;
      case 'name-desc':
        result.sort((a, b) => b.title.localeCompare(a.title));
        break;
      case 'most-dates':
        result.sort((a, b) => b.dateOptions.length - a.dateOptions.length);
        break;
      default:
        break;
    }

    return result;
  }, [events, searchQuery, sortBy]);

  return (
    <div className="container animate-fade-in">
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1>Dashboard</h1>
          <p style={{ color: 'var(--text-muted)' }}>Descubra ou organize os próximos encontros.</p>
        </div>
        <button className="btn-primary" onClick={() => navigate('/novo-evento')}>
          <Plus size={20} /> Novo Evento
        </button>
      </header>

      {/* Barra de busca + Filtro */}
      {!loading && events.length > 0 && (
        <div style={{ 
          display: 'flex', 
          gap: '0.75rem', 
          marginBottom: '2rem', 
          flexWrap: 'wrap',
          alignItems: 'center',
        }}>
          {/* Search Input */}
          <div style={{ 
            flex: 1, 
            minWidth: '240px', 
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
          }}>
            <Search 
              size={18} 
              style={{ 
                position: 'absolute', 
                left: '1rem', 
                color: 'var(--text-subtle)',
                pointerEvents: 'none',
              }} 
            />
            <input
              type="text"
              placeholder="Buscar por título, local, organizador..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              style={{
                width: '100%',
                padding: '0.75rem 1rem 0.75rem 2.75rem',
                background: 'var(--bg-surface)',
                border: '1px solid var(--border-glass)',
                borderRadius: 'var(--radius-sm)',
                color: 'var(--text-main)',
                fontFamily: 'inherit',
                fontSize: '0.95rem',
                transition: 'all var(--transition-normal)',
              }}
              onFocus={(e) => {
                e.target.style.borderColor = 'var(--color-primary)';
                e.target.style.boxShadow = '0 0 0 3px hsla(250, 100%, 65%, 0.2)';
              }}
              onBlur={(e) => {
                e.target.style.borderColor = 'var(--border-glass)';
                e.target.style.boxShadow = 'none';
              }}
            />
            {searchQuery && (
              <button
                className="btn-icon"
                onClick={() => setSearchQuery('')}
                style={{ position: 'absolute', right: '0.5rem' }}
              >
                <X size={16} />
              </button>
            )}
          </div>

          {/* Sort Select */}
          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            style={{
              padding: '0.75rem 1rem',
              background: 'var(--bg-surface)',
              border: '1px solid var(--border-glass)',
              borderRadius: 'var(--radius-sm)',
              color: 'var(--text-main)',
              fontFamily: 'inherit',
              fontSize: '0.9rem',
              cursor: 'pointer',
              minWidth: '160px',
            }}
          >
            {SORT_OPTIONS.map(opt => (
              <option key={opt.value} value={opt.value}>{opt.label}</option>
            ))}
          </select>

          {/* Resultado da busca */}
          {searchQuery && (
            <span style={{ 
              color: 'var(--text-muted)', 
              fontSize: '0.85rem',
              whiteSpace: 'nowrap',
            }}>
              {filteredEvents.length} resultado{filteredEvents.length !== 1 ? 's' : ''}
            </span>
          )}
        </div>
      )}

      {loading ? (
        /* Skeleton Loader */
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>
          {[1, 2, 3].map(i => (
            <div key={i} className="glass-panel" style={{ padding: '1.5rem', height: '200px' }}>
              <div className="skeleton skeleton-title" />
              <div className="skeleton skeleton-text" />
              <div className="skeleton skeleton-text-sm" style={{ marginBottom: '1.5rem' }} />
              <div className="skeleton skeleton-text-sm" style={{ width: '45%' }} />
            </div>
          ))}
        </div>
      ) : (
        <div className="stagger-children" style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '1.5rem' }}>

          {events.length === 0 ? (
            <div className="glass-panel empty-state" style={{ gridColumn: '1 / -1' }}>
              <div className="empty-state-icon">
                <Calendar size={48} />
              </div>
              <h3>Nenhum evento encontrado</h3>
              <p>A plataforma está vazia. Seja o primeiro a organizar algo incrível!</p>
            </div>
          ) : filteredEvents.length === 0 ? (
            <div className="glass-panel empty-state" style={{ gridColumn: '1 / -1' }}>
              <div className="empty-state-icon">
                <Search size={48} />
              </div>
              <h3>Nenhum resultado</h3>
              <p>Nenhum evento corresponde à sua busca por "{searchQuery}"</p>
            </div>
          ) : (
            filteredEvents.map(event => (
              <Link to={`/eventos/${event.id}`} key={event.id} style={{ textDecoration: 'none', color: 'inherit' }}>
                <div 
                  className="glass-panel animate-fade-in-up" 
                  style={{ padding: '1.5rem', height: '100%', transition: 'all 0.3s ease', cursor: 'pointer' }}
                  onMouseEnter={(e) => {
                    e.currentTarget.style.transform = 'translateY(-5px)';
                    e.currentTarget.style.borderColor = 'var(--color-primary)';
                  }}
                  onMouseLeave={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.borderColor = 'var(--border-glass)';
                  }}
                >
                  <h3 style={{ marginBottom: '0.5rem', color: 'var(--color-primary)' }}>{event.title}</h3>
                  <p className="line-clamp-2" style={{ color: 'var(--text-muted)', marginBottom: '1.5rem', fontSize: '0.9rem' }}>
                    {event.description}
                  </p>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-main)', fontSize: '0.85rem' }}>
                      <User size={16} style={{ color: 'var(--color-primary)' }}/>
                      <span>Organizado por <strong>{event.organizerName}</strong></span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      <MapPin size={16} />
                      <span>{event.location}</span>
                    </div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                      <Calendar size={16} />
                      <span>{event.dateOptions.length} datas propostas</span>
                    </div>
                  </div>
                </div>
              </Link>
            ))
          )}

        </div>
      )}
    </div>
  );
};

export default Dashboard;
