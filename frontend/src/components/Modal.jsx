import React, { useState, useEffect } from 'react';
import { AlertTriangle, Trash2, Info } from 'lucide-react';

const VARIANT_CONFIG = {
  danger: {
    icon: <AlertTriangle size={24} />,
    iconClass: 'modal-icon-danger',
    confirmClass: 'btn-danger',
  },
  warning: {
    icon: <AlertTriangle size={24} />,
    iconClass: 'modal-icon-warning',
    confirmClass: 'btn-primary',
  },
  info: {
    icon: <Info size={24} />,
    iconClass: 'modal-icon-info',
    confirmClass: 'btn-primary',
  },
  delete: {
    icon: <Trash2 size={24} />,
    iconClass: 'modal-icon-danger',
    confirmClass: 'btn-danger',
  },
};

const Modal = ({
  isOpen,
  onClose,
  onConfirm,
  title = 'Tem certeza?',
  description = 'Esta ação não poderá ser desfeita.',
  confirmText = 'Confirmar',
  cancelText = 'Cancelar',
  variant = 'danger',
  loading = false,
}) => {
  const [exiting, setExiting] = useState(false);

  // Fecha o modal com animação
  const handleClose = () => {
    setExiting(true);
    setTimeout(() => {
      setExiting(false);
      onClose();
    }, 200);
  };

  // Fecha ao apertar Escape
  useEffect(() => {
    if (!isOpen) return;

    const handleKeyDown = (e) => {
      if (e.key === 'Escape') handleClose();
    };

    document.addEventListener('keydown', handleKeyDown);
    // Previne scroll no body quando o modal está aberto
    document.body.style.overflow = 'hidden';

    return () => {
      document.removeEventListener('keydown', handleKeyDown);
      document.body.style.overflow = '';
    };
  }, [isOpen]);

  if (!isOpen) return null;

  const config = VARIANT_CONFIG[variant] || VARIANT_CONFIG.danger;

  return (
    <div
      className={`modal-overlay ${exiting ? 'modal-overlay-exiting' : ''}`}
      onClick={handleClose}
    >
      <div
        className={`modal ${exiting ? 'modal-exiting' : ''}`}
        onClick={(e) => e.stopPropagation()}
        role="dialog"
        aria-modal="true"
        aria-labelledby="modal-title"
      >
        <div className={`modal-icon ${config.iconClass}`}>
          {config.icon}
        </div>

        <h2 className="modal-title" id="modal-title">{title}</h2>
        <p className="modal-description">{description}</p>

        <div className="modal-actions">
          <button
            className="btn-glass"
            onClick={handleClose}
            disabled={loading}
          >
            {cancelText}
          </button>
          <button
            className={config.confirmClass}
            onClick={() => {
              if (onConfirm) onConfirm();
            }}
            disabled={loading}
          >
            {loading ? 'Processando...' : confirmText}
          </button>
        </div>
      </div>
    </div>
  );
};

export default Modal;
