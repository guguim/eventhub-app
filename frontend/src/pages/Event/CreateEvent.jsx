import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Calendar, Plus } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';

const CreateEvent = () => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [date1, setDate1] = useState('');
  const [date2, setDate2] = useState('');
  const [error, setError] = useState('');
  const { user } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');


    const dateOptions = [];
    if (date1) dateOptions.push(new Date(date1).toISOString().slice(0, 19));
    if (date2) dateOptions.push(new Date(date2).toISOString().slice(0, 19));

    if (dateOptions.length === 0) {
      setError("Forneça pelo menos uma data para o evento.");
      return;
    }

    try {

      const response = await axios.post('/api/events', {
        title,
        description,
        location,
        organizerId: user.id, 
        dateOptions
      });

      navigate(`/eventos/${response.data.id}`);
    } catch (err) {
      setError('Erro ao criar evento. Verifique os dados.');
    }
  };

  return (
    <div className="container animate-fade-in">
      <button onClick={() => navigate('/dashboard')} className="btn-glass" style={{ marginBottom: '2rem' }}>
        ← Cancelar
      </button>

      <div className="glass-panel" style={{ padding: '2.5rem', maxWidth: '600px', margin: '0 auto' }}>
        <h2 style={{ marginBottom: '1.5rem', color: 'var(--color-primary)' }}>Criar Novo Evento</h2>

        {error && <div style={{ color: '#ff6b6b', marginBottom: '1rem', fontWeight: '500' }}>{error}</div>}

        <form onSubmit={handleSubmit}>
          <div className="input-group">
            <label>Título do Evento</label>
            <input type="text" required value={title} onChange={e => setTitle(e.target.value)} placeholder="Ex: Festa de Fim de Ano" />
          </div>

          <div className="input-group">
            <label>Descrição</label>
            <textarea required value={description} onChange={e => setDescription(e.target.value)} placeholder="Detalhes do evento..." rows="3" />
          </div>

          <div className="input-group">
            <label>Local (Opcional)</label>
            <input type="text" value={location} onChange={e => setLocation(e.target.value)} placeholder="Onde vai ser?" />
          </div>

          <h3 style={{ marginTop: '2rem', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
            <Calendar size={20} /> Datas Propostas
          </h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1rem' }}>Sugira até 2 opções para a galera votar!</p>

          <div className="input-group">
            <label>Opção de Data 1</label>
            <input type="datetime-local" required value={date1} onChange={e => setDate1(e.target.value)} />
          </div>

          <div className="input-group">
            <label>Opção de Data 2 (Opcional)</label>
            <input type="datetime-local" value={date2} onChange={e => setDate2(e.target.value)} />
          </div>

          <button type="submit" className="btn-primary" style={{ width: '100%', marginTop: '2rem' }}>
            <Plus size={20} /> Publicar Evento
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateEvent;
