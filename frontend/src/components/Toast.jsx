import React, { createContext, useContext, useState, useCallback } from 'react';
import { CheckCircle, AlertCircle, AlertTriangle, Info, X } from 'lucide-react';

const ToastContext = createContext();

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast deve ser usado dentro de um ToastProvider');
  }
  return context;
};

const ICONS = {
  success: <CheckCircle size={20} style={{ color: 'var(--color-success)' }} />,
  error: <AlertCircle size={20} style={{ color: 'var(--color-danger)' }} />,
  warning: <AlertTriangle size={20} style={{ color: 'var(--color-warning)' }} />,
  info: <Info size={20} style={{ color: 'var(--color-info)' }} />,
};

const DEFAULT_TITLES = {
  success: 'Sucesso',
  error: 'Erro',
  warning: 'Atenção',
  info: 'Informação',
};

let toastIdCounter = 0;

export const ToastProvider = ({ children }) => {
  const [toasts, setToasts] = useState([]);

  const removeToast = useCallback((id) => {
    setToasts(prev =>
      prev.map(t => (t.id === id ? { ...t, exiting: true } : t))
    );
    // Remove do DOM após a animação de saída
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id));
    }, 300);
  }, []);

  const addToast = useCallback(({ type = 'info', title, message, duration = 4000 }) => {
    const id = ++toastIdCounter;

    const toast = {
      id,
      type,
      title: title || DEFAULT_TITLES[type],
      message,
      duration,
      exiting: false,
    };

    setToasts(prev => [...prev, toast]);

    // Auto-remove após a duração
    if (duration > 0) {
      setTimeout(() => removeToast(id), duration);
    }

    return id;
  }, [removeToast]);

  // Atalhos
  const success = useCallback((message, title) => addToast({ type: 'success', message, title }), [addToast]);
  const error = useCallback((message, title) => addToast({ type: 'error', message, title, duration: 6000 }), [addToast]);
  const warning = useCallback((message, title) => addToast({ type: 'warning', message, title, duration: 5000 }), [addToast]);
  const info = useCallback((message, title) => addToast({ type: 'info', message, title }), [addToast]);

  return (
    <ToastContext.Provider value={{ addToast, removeToast, success, error, warning, info }}>
      {children}

      {/* Renderiza os Toasts */}
      <div className="toast-container">
        {toasts.map(toast => (
          <div
            key={toast.id}
            className={`toast toast-${toast.type} ${toast.exiting ? 'toast-exiting' : ''}`}
            onClick={() => removeToast(toast.id)}
            role="alert"
          >
            <div className="toast-icon">
              {ICONS[toast.type]}
            </div>

            <div className="toast-content">
              <div className="toast-title">{toast.title}</div>
              {toast.message && <div className="toast-message">{toast.message}</div>}
            </div>

            <button
              className="toast-close"
              onClick={(e) => {
                e.stopPropagation();
                removeToast(toast.id);
              }}
              aria-label="Fechar notificação"
            >
              <X size={16} />
            </button>

            {/* Barra de progresso */}
            {toast.duration > 0 && (
              <div
                className="toast-progress"
                style={{ animationDuration: `${toast.duration}ms` }}
              />
            )}
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
};
