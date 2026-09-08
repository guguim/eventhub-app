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
  const { user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    fetchEventData();
    const cleanupWebSocket = setupWebSocket();
    return cleanupWebSocket; // Desconecta do socket quando o usuário fecha a página
  }, [id]);

  const fetchEventData = async () => {
    try {
      // Usamos o Promise.all para fazer o download das duas coisas ao mesmo tempo!
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
    // 💡 A MÁGICA DA FASE 5 COMEÇA AQUI!
    const client = new Client({
      brokerURL: 'ws://localhost:8080/ws', 
      reconnectDelay: 5000,
      onConnect: () => {
        console.log("Conectado ao Túnel WebSocket!");
        
        // 1. Ouvindo o Rádio de Votos
        client.subscribe(`/topic/events/${id}/votes`, (message) => {
          console.log("Alguém votou! Recarregando dados...");
          fetchEventData(); // Recarrega os votos da tela
        });

        // 2. Ouvindo o Rádio de Tarefas
        client.subscribe(`/topic/events/${id}/tasks`, (message) => {
          const taskData = JSON.parse(message.body);
          console.log("Uma tarefa mudou de status via WebSocket!", taskData);
          // Substitui a tarefa velha pela nova na nossa tela em frações de segundo
          setTasks(prevTasks => prevTasks.map(t => t.id === taskData.id ? taskData : t));
        });
      }
    });
    client.activate();
    return () => client.deactivate();
  };

  const handleVote = async (dateOptionId) => {
    try {
      await axios.post(`/api/votes`, null, { params: { dateOptionId } });
      // Perceba: NÃO CHAMAMOS o fetchEventData() aqui de propósito!
      // Por que? Porque o nosso Backend Java vai gritar no WebSocket para todos na sala que um voto caiu,
      // E o nosso 'client.subscribe' ali em cima vai escutar o grito e recarregar a tela automaticamente!
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
        {/* LADO ESQUERDO: Datas */}
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

        {/* LADO DIREITO: Tarefas */}
        <div className="glass-panel" style={{ padding: '2rem' }}>
          <h2 style={{ marginBottom: '1.5rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <CheckCircle size={24} /> Checklist de Tarefas
          </h2>
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
