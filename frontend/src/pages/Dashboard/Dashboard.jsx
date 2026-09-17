import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Link, useNavigate } from 'react-router-dom';
import { Calendar, MapPin, Plus, User } from 'lucide-react';

const Dashboard = () => {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
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

  return (
    <div className="container animate-fade-in">
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '3rem', flexWrap: 'wrap', gap: '1rem' }}>
        <div>
          <h1>Dashboard</h1>
          <p style={{ color: 'var(--text-muted)' }}>Descubra ou organize os próximos encontros.</p>
        </div>
        <button className="btn-primary" onClick={() => navigate('/novo-evento')}>
          <Plus size={20} /> Novo Evento
        </button>
      </header>

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
          ) : (
            events.map(event => (
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
