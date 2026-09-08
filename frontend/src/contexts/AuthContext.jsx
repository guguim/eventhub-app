import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';

// Criação do Contexto
const AuthContext = createContext();

// Hook personalizado para os componentes acessarem facilmente: const { user, login } = useAuth();
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  // Tenta pegar o token do "cofre" do navegador (localStorage) quando o App inicia
  const [token, setToken] = useState(localStorage.getItem('token') || null);
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('userData');
    return saved ? { token, ...JSON.parse(saved) } : (token ? { token } : null);
  });
  const [loading, setLoading] = useState(true);

  // Esse 'useEffect' observa a variável 'token'. Sempre que ela mudar (alguém fez login ou logout),
  // ele atualiza o cofre e coloca/tira o Token do cabeçalho de TODAS as futuras requisições do Axios!
  useEffect(() => {
    if (token) {
      localStorage.setItem('token', token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      localStorage.removeItem('token');
      localStorage.removeItem('userData');
      delete axios.defaults.headers.common['Authorization'];
      setUser(null);
    }
    setLoading(false);
  }, [token]);

  // Função que será chamada pela tela de Login
  const login = async (email, password) => {
    try {
      const response = await axios.post('/api/auth/login', { email, password });
      setToken(response.data.token); // Salva o token mágico no estado!
      
      // Salva os dados extras do usuário
      const userData = { id: response.data.userId, name: response.data.name };
      localStorage.setItem('userData', JSON.stringify(userData));
      setUser({ token: response.data.token, ...userData });
      
      return true;
    } catch (error) {
      console.error("Falha no login", error);
      throw error;
    }
  };

  const register = async (name, email, password) => {
    try {
      await axios.post('/api/auth/register', { name, email, password, role: 'ORGANIZER' });
      // Se registrar com sucesso, já faz o login automático!
      return await login(email, password);
    } catch (error) {
      console.error("Falha no registro", error);
      throw error;
    }
  };

  const logout = () => {
    setToken(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
