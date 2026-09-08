import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Client } from '@stomp/stompjs';
import { Calendar, MapPin, CheckCircle, Circle, User } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';

const EventDetails = () => {
  const { id } = useParams();
  const [event, setEvent] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [newTaskTitle, setNewTaskTitle] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchEventData();
    const cleanupWebSocket = setupWebSocket();
    return cleanupWebSocket; 
  }, [id]);

  const fetchEventData = async () => {
    try {

      const [eventRes, tasksRes] = await Promise.all([
        axios.get(`/api/events/${id}`),
        axios.get(`/api/events/${id}/tasks`)
      ]);
      setEvent(eventRes.data);
      setTasks(tasksRes.data);
    } catch (error) {
      console.error("Erro ao buscar detalhes", error);
      alert("Evento não encontrado ou servidor indisponível.");
      navigate('/dashboard');
    } finally {
      setLoading(false);
    }
  };

  const setupWebSocket = () => {

    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws', 
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Conectado ao Túnel WebSocket!");


        client.subscribe(`/topic/events/${id}/votes`, (message) => {
          console.log("Alguém votou! Recarregando dados...");
          fetchEventData(); 
        });


        client.subscribe(`/topic/events/${id}/tasks`, (message) => {
          const taskData = JSON.parse(message.body);
          console.log("Uma tarefa mudou de status via WebSocket!", taskData);

          setTasks(prevTasks => prevTasks.map(t => t.id === taskData.id ? taskData : t));
        });
      }
    });
    client.activate();
    return () => client.deactivate();
  };

  const handleVote = async (dateOptionId) => {
    try {
      await axios.post(`/api/dates/${dateOptionId}/vote`);

    } catch (e) {
      alert("Erro ao votar. Provavelmente você já votou nesta data!");
    }
  };

  const toggleTaskStatus = async (task) => {
    try {
      const newStatus = task.status === 'PENDING' ? 'COMPLETED' : 'PENDING';
      await axios.patch(`/api/tasks/${task.id}/status`, { status: newStatus });
    } catch (e) {
      alert("⚠️ Acesso Negado: Nosso Java Object-Level Security (Fase 4) bloqueou você. Apenas o organizador do evento ou o responsável pela tarefa podem alterá-la.");
    }
  };

  const handleCreateTask = async (e) => {
    e.preventDefault();
    if (!newTaskTitle.trim()) return;
    try {
      await axios.post(`/api/events/${id}/tasks`, { title: newTaskTitle });
      setNewTaskTitle('');

    } catch (e) {
      alert("Acesso Negado: Apenas o organizador pode adicionar tarefas!");
    }
  };

  if (loading) return <div className="container animate-fade-in" style={{textAlign: 'center', marginTop: '10vh'}}>Sintonizando no Evento...</div>;
  if (!event) return null;

  return (
    <div className="container animate-fade-in">
      <button onClick={() => navigate('/dashboard')} className="btn-glass" style={{ marginBottom: '2rem' }}>
        ← Voltar
      </button>

      <div className="glass-panel" style={{ padding: '2.5rem', marginBottom: '2rem' }}>
        <h1 style={{ color: 'var(--color-primary)', marginBottom: '0.5rem' }}>{event.title}</h1>
        <p style={{ color: 'var(--text-muted)', marginBottom: '2rem', fontSize: '1.1rem' }}>{event.description}</p>

        <div style={{ display: 'flex', gap: '2rem', color: 'var(--text-main)', flexWrap: 'wrap' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <MapPin size={20} style={{ color: 'var(--color-primary)' }}/> {event.location}
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <User size={20} style={{ color: 'var(--color-primary)' }}/> Organizado por <strong>{event.organizerName}</strong>
          </div>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(400px, 1fr))', gap: '2rem' }}>
        {}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Calendar size={24} /> Enquete de Datas
          </h2>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
            {event.dateOptions.map(date => (
              <div key={date.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '1.25rem', background: 'hsla(0,0%,0%,0.2)', borderRadius: '8px' }}>
                <span style={{ fontWeight: '500', fontSize: '1.1rem' }}>{new Date(date.dateTime).toLocaleString('pt-BR')}</span>
                <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                  <span style={{ color: 'var(--color-primary)', fontWeight: 'bold', fontSize: '1.2rem' }}>{date.voteCount} votos</span>
                  <button className="btn-primary" onClick={() => handleVote(date.id)}>Votar</button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <CheckCircle size={24} /> Checklist de Tarefas
          </h2>

          <form onSubmit={handleCreateTask} style={{ display: 'flex', gap: '0.5rem', marginBottom: '1.5rem' }}>
            <input 
              type="text" 
              placeholder="Adicionar nova tarefa..." 
              value={newTaskTitle}
              onChange={(e) => setNewTaskTitle(e.target.value)}
              style={{ flex: 1, padding: '0.75rem', borderRadius: '8px', border: '1px solid var(--border-glass)', background: 'hsla(0,0%,0%,0.2)', color: 'var(--text-main)' }}
            />
            <button type="submit" className="btn-primary" style={{ padding: '0.75rem 1rem' }}>Adicionar</button>
          </form>

          {tasks.length === 0 ? (
            <p style={{ color: 'var(--text-muted)' }}>Nenhuma tarefa cadastrada ainda.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '1rem' }}>
              {tasks.map(task => (
                <div key={task.id} 
                     onClick={() => toggleTaskStatus(task)}
                     style={{ display: 'flex', alignItems: 'center', gap: '1rem', padding: '1.25rem', background: 'hsla(0,0%,0%,0.2)', borderRadius: '8px', cursor: 'pointer', transition: 'all 0.2s', opacity: task.status === 'COMPLETED' ? 0.5 : 1 }}>
                  {task.status === 'COMPLETED' ? (
                    <CheckCircle size={24} style={{ color: 'var(--color-primary)' }} />
                  ) : (
                    <Circle size={24} style={{ color: 'var(--text-muted)' }} />
                  )}
                  <div style={{ flex: 1 }}>
                    <p style={{ fontWeight: '600', fontSize: '1.1rem', textDecoration: task.status === 'COMPLETED' ? 'line-through' : 'none' }}>{task.title}</p>
                    <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginTop: '0.25rem' }}>Responsável: {task.assigneeName || 'Livre'}</p>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default EventDetails;
