import { useEffect } from 'react';
import axios from 'axios';
import { useToast } from './Toast';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';

export const AxiosInterceptor = ({ children }) => {
  const toast = useToast();
  const navigate = useNavigate();
  const { logout } = useAuth();

  useEffect(() => {
    const resInterceptor = response => {
      return response;
    };

    const errInterceptor = error => {
      if (error.response) {
        const { status, data } = error.response;
        
        if (status === 401) {
          toast.error('Sessão expirada. Faça login novamente.');
          logout();
          navigate('/login');
        } else if (status === 403) {
          toast.warning(data.message || 'Acesso negado.');
        } else if (status === 400) {
          if (data.errors) {
            // Validation errors array or object
            const errorMessages = Array.isArray(data.errors) 
                ? data.errors.join(', ') 
                : Object.values(data.errors).join(', ');
            toast.error(errorMessages || 'Erro de validação.');
          } else {
            toast.error(data.message || 'Verifique os dados enviados.');
          }
        } else if (status === 404) {
          toast.error(data.message || 'Recurso não encontrado.');
        } else {
          toast.error(data.message || 'Ocorreu um erro inesperado.');
        }
      } else if (error.request) {
        toast.error('Erro de conexão. Verifique sua rede.');
      } else {
        toast.error('Erro ao processar a requisição.');
      }

      return Promise.reject(error);
    };

    const interceptor = axios.interceptors.response.use(resInterceptor, errInterceptor);

    return () => axios.interceptors.response.eject(interceptor);
  }, [toast, navigate, logout]);

  return children;
};
