import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Bell } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';

const NotificationBell = () => {
  const [unreadCount, setUnreadCount] = useState(0);
  const [notifications, setNotifications] = useState([]);
  const [isOpen, setIsOpen] = useState(false);
  const { user } = useAuth();

  useEffect(() => {
    if (user) {
      fetchUnreadCount();

      const interval = setInterval(fetchUnreadCount, 30000);
      return () => clearInterval(interval);
    }
  }, [user]);

  const fetchUnreadCount = async () => {
    try {
      const response = await axios.get('/api/notifications/unread-count');
      setUnreadCount(response.data.unreadCount);
    } catch (e) {
      console.error(e);
    }
  };

  const openDropdown = async () => {
    setIsOpen(!isOpen);

    if (!isOpen) {
      try {
        const response = await axios.get('/api/notifications');
        setNotifications(response.data);
      } catch (e) {
        console.error(e);
      }
    }
  };

  const markAsRead = async (id) => {
    try {
      await axios.patch(`/api/notifications/${id}/read`);

      setNotifications(notifications.map(n => n.id === id ? { ...n, isRead: true } : n));
      setUnreadCount(prev => Math.max(0, prev - 1));
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div style={{ position: 'relative' }}>
      <button onClick={openDropdown} style={{ background: 'none', border: 'none', color: 'var(--text-main)', cursor: 'pointer', position: 'relative', display: 'flex', alignItems: 'center' }}>
        <Bell size={24} />
        {unreadCount > 0 && (
          <span style={{ 
            position: 'absolute', top: '-5px', right: '-5px', 
            background: '#ff4757', color: 'white', borderRadius: '50%', 
            width: '18px', height: '18px', fontSize: '12px', fontWeight: 'bold',
            display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            {unreadCount}
          </span>
        )}
      </button>

      {isOpen && (
        <div className="glass-panel animate-fade-in" style={{ 
          position: 'absolute', top: '40px', right: '0', width: '320px', 
          maxHeight: '400px', overflowY: 'auto', zIndex: 1000, padding: '1.5rem' 
        }}>
          <h3 style={{ marginBottom: '1.5rem', borderBottom: '1px solid var(--border-glass)', paddingBottom: '0.5rem' }}>Notificações</h3>
          {notifications.length === 0 ? (
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', textAlign: 'center' }}>Nenhum aviso no momento.</p>
          ) : (
            notifications.map(n => (
              <div key={n.id} style={{ 
                padding: '1rem', marginBottom: '1rem', borderRadius: '8px',
                background: n.isRead ? 'hsla(0, 0%, 0%, 0.1)' : 'hsla(var(--hue-primary), 100%, 65%, 0.15)',
                border: '1px solid var(--border-glass)', transition: 'all 0.3s'
              }}>
                <p style={{ fontSize: '0.95rem', marginBottom: '0.5rem', color: n.isRead ? 'var(--text-muted)' : 'var(--text-main)' }}>{n.message}</p>
                {!n.isRead && (
                  <button onClick={() => markAsRead(n.id)} style={{ 
                    background: 'none', border: 'none', color: 'var(--color-primary)', 
                    fontSize: '0.8rem', cursor: 'pointer', fontWeight: '600', padding: 0 
                  }}>
                    Marcar como lida
                  </button>
                )}
              </div>
            ))
          )}
        </div>
      )}
    </div>
  );
};

export default NotificationBell;
