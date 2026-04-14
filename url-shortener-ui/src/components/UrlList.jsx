import { useState } from 'react';
import api from '../api/axios';

export default function UrlList({ urls, loading }) {
  const [stats, setStats] = useState(null);
  const [statsLoading, setStatsLoading] = useState(false);

  const viewStats = async (shortCode) => {
    if (stats?.shortCode === shortCode) {
      setStats(null);
      return;
    }
    setStatsLoading(true);
    try {
      const res = await api.get(`/api/v1/urls/${shortCode}/stats`);
      setStats(res.data);
    } catch {
      setStats(null);
    } finally {
      setStatsLoading(false);
    }
  };

  const formatDate = (dateStr) => {
    if (!dateStr) return '-';
    return new Date(dateStr).toLocaleDateString('en-US', {
      year: 'numeric', month: 'short', day: 'numeric'
    });
  };

  if (loading) return <p>Loading your URLs...</p>;
  if (urls.length === 0) return <p className="empty">No URLs yet. Shorten one above!</p>;

  return (
    <div className="url-list">
      <h2>Your URLs ({urls.length})</h2>
      <table>
        <thead>
          <tr>
            <th>Short URL</th>
            <th>Original URL</th>
            <th>Clicks</th>
            <th>Created</th>
            <th>Expires</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {urls.map((url) => (
            <tr key={url.shortCode}>
              <td>
                <a href={url.shortUrl} target="_blank" rel="noopener noreferrer">
                  {url.shortCode}
                </a>
              </td>
              <td className="long-url" title={url.longUrl}>
                {url.longUrl.length > 50 ? url.longUrl.substring(0, 50) + '...' : url.longUrl}
              </td>
              <td>{url.clickCount}</td>
              <td>{formatDate(url.createdAt)}</td>
              <td>{formatDate(url.expiresAt)}</td>
              <td>
                <button className="stats-btn" onClick={() => viewStats(url.shortCode)}>
                  {stats?.shortCode === url.shortCode ? 'Hide' : 'Stats'}
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {statsLoading && <p>Loading stats...</p>}
      {stats && (
        <div className="stats-panel">
          <h3>Stats for /{stats.shortCode}</h3>
          <div className="stats-grid">
            <div className="stat-card">
              <span className="stat-value">{stats.clickCount}</span>
              <span className="stat-label">Total Clicks</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{stats.uniqueCount}</span>
              <span className="stat-label">Unique Visitors</span>
            </div>
            <div className="stat-card">
              <span className="stat-value">{stats.expired ? 'Yes' : 'No'}</span>
              <span className="stat-label">Expired</span>
            </div>
          </div>
          <p className="stats-url">
            <strong>Original:</strong>{' '}
            <a href={stats.longUrl} target="_blank" rel="noopener noreferrer">{stats.longUrl}</a>
          </p>
        </div>
      )}
    </div>
  );
}
