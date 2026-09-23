import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Calendar, Plus, X } from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { useToast } from '../../components/Toast';

const CreateEvent = () => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [location, setLocation] = useState('');
  const [dateOptions, setDateOptions] = useState(['']); // Começa com 1 campo vazio
  const [submitting, setSubmitting] = useState(false);
  const { user } = useAuth();
  const navigate = useNavigate();
  const toast = useToast();

  const addDateField = () => {
    setDateOptions([...dateOptions, '']);
  };

  const removeDateField = (index) => {
    if (dateOptions.length <= 1) return; // Mínimo 1 data
    setDateOptions(dateOptions.filter((_, i) => i !== index));
  };

  const updateDate = (index, value) => {
    const updated = [...dateOptions];
    updated[index] = value;
    setDateOptions(updated);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Filtra datas preenchidas e converte
    const filledDates = dateOptions
      .filter(d => d.trim() !== '')
      .map(d => new Date(d).toISOString().slice(0, 19));

    if (filledDates.length === 0) {
      toast.warning('Forneça pelo menos uma data para o evento.');
      return;
    }

    setSubmitting(true);

    try {
      const response = await axios.post('/api/events', {
        title,
        description,
        location,
        organizerId: user.id,
        dateOptions: filledDates
      });

      toast.success('Evento publicado com sucesso!');
      navigate(`/eventos/${response.data.id}`);
    } catch (err) {
      console.error('Erro ao criar evento:', err);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="container animate-fade-in">
      <button onClick={() => navigate('/dashboard')} className="btn-glass" style={{ marginBottom: '2rem' }}>
        ← Cancelar
      </button>

      <div className="glass-panel" style={{ padding: '2.5rem', maxWidth: '600px', margin: '0 auto' }}>
        <h2 style={{ marginBottom: '1.5rem', color: 'var(--color-primary)' }}>Criar Novo Evento</h2>

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

          <div style={{ marginTop: '2rem', marginBottom: '1.5rem' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
              <h3 style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <Calendar size={20} /> Datas Propostas
              </h3>
              <button
                type="button"
                className="btn-glass"
                onClick={addDateField}
                style={{ padding: '0.4rem 0.75rem', fontSize: '0.85rem' }}
              >
                <Plus size={16} /> Adicionar
              </button>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', marginBottom: '1rem' }}>
              Sugira quantas opções quiser para a galera votar!
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
              {dateOptions.map((date, index) => (
                <div 
                  key={index} 
                  className="animate-fade-in"
                  style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}
                >
                  <span style={{ 
                    color: 'var(--text-muted)', 
                    fontSize: '0.85rem', 
                    fontWeight: 600, 
                    minWidth: '24px',
                    textAlign: 'center',
                  }}>
                    {index + 1}
                  </span>
                  <input
                    type="datetime-local"
                    value={date}
                    onChange={e => updateDate(index, e.target.value)}
                    required={index === 0} // Apenas a primeira é obrigatória
                    style={{
                      flex: 1,
                      background: 'hsla(0, 0%, 0%, 0.2)',
                      border: '1px solid var(--border-glass)',
                      borderRadius: 'var(--radius-sm)',
                      padding: '0.75rem 1rem',
                      color: 'var(--text-main)',
                      fontFamily: 'inherit',
                      fontSize: '0.95rem',
                    }}
                  />
                  {dateOptions.length > 1 && (
                    <button
                      type="button"
                      className="btn-icon"
                      onClick={() => removeDateField(index)}
                      title="Remover esta data"
                      style={{ color: 'var(--color-danger)' }}
                    >
                      <X size={18} />
                    </button>
                  )}
                </div>
              ))}
            </div>
          </div>

          <button 
            type="submit" 
            className="btn-primary" 
            style={{ width: '100%', marginTop: '2rem' }}
            disabled={submitting}
          >
            {submitting ? 'Publicando...' : (
              <>
                <Plus size={20} /> Publicar Evento
              </>
            )}
          </button>
        </form>
      </div>
    </div>
  );
};

export default CreateEvent;
