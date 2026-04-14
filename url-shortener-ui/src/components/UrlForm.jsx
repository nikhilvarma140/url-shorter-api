import { useState } from 'react';
import api from '../api/axios';

export default function UrlForm({ onUrlCreated }) {
  const [longUrl, setLongUrl] = useState('');
  const [expirationDays, setExpirationDays] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [copied, setCopied] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setResult(null);
    setLoading(true);
    try {
      const body = { longUrl };
      if (expirationDays) body.expirationDays = parseInt(expirationDays);
      const res = await api.post('/api/v1/urls', body);
      setResult(res.data);
      setLongUrl('');
      setExpirationDays('');
      onUrlCreated();
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to shorten URL');
    } finally {
      setLoading(false);
    }
  };

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  return (
    <div className="url-form">
      <h2>Shorten a URL</h2>
      {error && <p className="error">{error}</p>}
      <form onSubmit={handleSubmit}>
        <input
          type="url"
          placeholder="https://example.com/very-long-url"
          value={longUrl}
          onChange={(e) => setLongUrl(e.target.value)}
          required
        />
        <div className="form-row">
          <input
            type="number"
            placeholder="Expiration days (default: 365)"
            value={expirationDays}
            onChange={(e) => setExpirationDays(e.target.value)}
            min="1"
            max="3650"
          />
          <button type="submit" disabled={loading}>
            {loading ? 'Shortening...' : 'Shorten'}
          </button>
        </div>
      </form>
      {result && (
        <div className="result">
          <p>Short URL created:</p>
          <div className="short-url-display">
            <a href={result.shortUrl} target="_blank" rel="noopener noreferrer">
              {result.shortUrl}
            </a>
            <button className="copy-btn" onClick={() => copyToClipboard(result.shortUrl)}>
              {copied ? 'Copied!' : 'Copy'}
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
