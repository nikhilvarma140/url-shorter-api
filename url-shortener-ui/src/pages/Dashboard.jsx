import { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import UrlForm from '../components/UrlForm';
import UrlList from '../components/UrlList';
import api from '../api/axios';

export default function Dashboard() {
  const { user, logout } = useAuth();
  const [urls, setUrls] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchUrls = useCallback(async () => {
    setLoading(true);
    try {
      const res = await api.get('/api/v1/urls/my');
      setUrls(res.data);
    } catch {
      setUrls([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchUrls();
  }, [fetchUrls]);

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>URL Shortener</h1>
        <div className="header-right">
          <span>Welcome, {user}</span>
          <button className="logout-btn" onClick={logout}>Logout</button>
        </div>
      </header>
      <main>
        <UrlForm onUrlCreated={fetchUrls} />
        <UrlList urls={urls} loading={loading} />
      </main>
    </div>
  );
}
