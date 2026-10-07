import React, { createContext, useState, useEffect, useContext } from 'react';
import axios from 'axios';


const AuthContext = createContext();


export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {

  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('userData');
    return saved ? JSON.parse(saved) : null;
  });
  const [loading, setLoading] = useState(true);


  useEffect(() => {
    axios.defaults.withCredentials = true;
    if (!user) {
      localStorage.removeItem('userData');
    }
    setLoading(false);
  }, [user]);


  const login = async (email, password) => {
    try {
      const response = await axios.post('/api/auth/login', { email, password });

      const userData = { id: response.data.userId, name: response.data.name };
      localStorage.setItem('userData', JSON.stringify(userData));
      setUser(userData);

      return true;
    } catch (error) {
      console.error("Falha no login", error);
      throw error;
    }
  };

  const register = async (name, email, password) => {
    try {
      await axios.post('/api/auth/register', { name, email, password, role: 'ORGANIZER' });

      return await login(email, password);
    } catch (error) {
      console.error("Falha no registro", error);
      throw error;
    }
  };

  const logout = async () => {
    try {
      await axios.post('/api/auth/logout');
    } catch(e) {}
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, register, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
