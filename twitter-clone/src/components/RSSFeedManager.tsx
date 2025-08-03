import React, { useState } from 'react';
import { Plus, Rss, AlertCircle, CheckCircle } from 'lucide-react';
import { rssApi } from '../services/api';

const RSSFeedManager: React.FC = () => {
  const [feedUrl, setFeedUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const popularFeeds = [
    { name: 'NVIDIA Developer Blog', url: 'https://developer.nvidia.com/blog/feed/' },
    { name: 'TechCrunch', url: 'https://techcrunch.com/feed/' },
    { name: 'Hacker News', url: 'https://hnrss.org/frontpage' },
    { name: 'Reddit Programming', url: 'https://www.reddit.com/r/programming/.rss' },
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!feedUrl.trim()) return;

    setLoading(true);
    setMessage(null);

    try {
      await rssApi.fetchAndSaveRSS(feedUrl);
      setMessage({ type: 'success', text: 'RSS feed fetched and saved successfully!' });
      setFeedUrl('');
    } catch (error) {
      console.error('Error fetching RSS feed:', error);
      setMessage({ type: 'error', text: 'Failed to fetch RSS feed. Please check the URL and try again.' });
    } finally {
      setLoading(false);
    }
  };

  const handlePopularFeed = async (url: string) => {
    setFeedUrl(url);
  };

  return (
    <div className="rss-manager">
      <div className="feed-header">
        <h1 className="feed-title">RSS Feed Manager</h1>
      </div>

      {/* Add RSS Feed Form */}
      <div className="rss-form">
        <h2 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Rss size={20} />
          Add RSS Feed
        </h2>
        
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label" htmlFor="feedUrl">
              RSS Feed URL
            </label>
            <input
              type="url"
              id="feedUrl"
              className="form-input"
              placeholder="https://example.com/rss.xml"
              value={feedUrl}
              onChange={(e) => setFeedUrl(e.target.value)}
              required
            />
          </div>
          
          <button
            type="submit"
            className="submit-button"
            disabled={loading || !feedUrl.trim()}
          >
            {loading ? (
              <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <div style={{ 
                  width: '16px', 
                  height: '16px', 
                  border: '2px solid transparent',
                  borderTop: '2px solid currentColor',
                  borderRadius: '50%',
                  animation: 'spin 1s linear infinite'
                }} />
                Fetching...
              </span>
            ) : (
              <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Plus size={16} />
                Add Feed
              </span>
            )}
          </button>
        </form>

        {/* Message Display */}
        {message && (
          <div 
            className={`message ${message.type}`}
            style={{
              marginTop: '16px',
              padding: '12px 16px',
              borderRadius: '8px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: message.type === 'success' ? '#1a4a3a' : '#4a1a1a',
              border: `1px solid ${message.type === 'success' ? '#2a6a4a' : '#6a2a2a'}`,
              color: message.type === 'success' ? '#4ade80' : '#f87171'
            }}
          >
            {message.type === 'success' ? <CheckCircle size={16} /> : <AlertCircle size={16} />}
            {message.text}
          </div>
        )}
      </div>

      {/* Popular Feeds */}
      <div className="rss-form">
        <h2 style={{ marginBottom: '16px' }}>Popular RSS Feeds</h2>
        <div style={{ display: 'grid', gap: '12px' }}>
          {popularFeeds.map((feed, index) => (
            <div
              key={index}
              className="leaderboard-item"
              style={{ cursor: 'pointer' }}
              onClick={() => handlePopularFeed(feed.url)}
            >
              <div className="user-info">
                <div className="avatar" style={{ width: '32px', height: '32px', fontSize: '14px' }}>
                  <Rss size={16} />
                </div>
                <div>
                  <div className="user-name">{feed.name}</div>
                  <div style={{ fontSize: '14px', color: '#71767b' }}>{feed.url}</div>
                </div>
              </div>
              <button
                className="action-button"
                onClick={(e) => {
                  e.stopPropagation();
                  handlePopularFeed(feed.url);
                }}
              >
                <Plus size={16} />
              </button>
            </div>
          ))}
        </div>
      </div>

      {/* Instructions */}
      <div className="rss-form">
        <h3 style={{ marginBottom: '12px' }}>How to use:</h3>
        <ul style={{ color: '#71767b', lineHeight: '1.6', paddingLeft: '20px' }}>
          <li>Enter a valid RSS feed URL in the form above</li>
          <li>Click "Add Feed" to fetch and save the RSS items to the backend</li>
          <li>The RSS items will be stored in Redis and can be viewed in the Home feed</li>
          <li>You can also click on any popular feed to quickly add it</li>
        </ul>
      </div>

      <style>{`
        @keyframes spin {
          from { transform: rotate(0deg); }
          to { transform: rotate(360deg); }
        }
      `}</style>
    </div>
  );
};

export default RSSFeedManager;