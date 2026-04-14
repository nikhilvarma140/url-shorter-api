import { createContext, useContext, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(localStorage.getItem('username'));
  const [token, setToken] = useState(localStorage.getItem('token'));
  const navigate = useNavigate();

  const login = async (username, password) => {
    const res = await api.post('/api/v1/auth/login', { username, password });
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('username', res.data.username);
    setToken(res.data.token);
    setUser(res.data.username);
    navigate('/dashboard');
  };

  const register = async (username, email, password) => {
    const res = await api.post('/api/v1/auth/register', { username, email, password });
    localStorage.setItem('token', res.data.token);
    localStorage.setItem('username', res.data.username);
    setToken(res.data.token);
    setUser(res.data.username);
    navigate('/dashboard');
  };

  const logout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('username');
    setToken(null);
    setUser(null);
    navigate('/login');
  };

  return (
    <AuthContext.Provider value={{ user, token, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
