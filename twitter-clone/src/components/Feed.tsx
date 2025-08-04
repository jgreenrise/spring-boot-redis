import React, { useState, useEffect } from 'react';
import { MessageCircle, Repeat2, Heart, Share, ExternalLink } from 'lucide-react';
import { RSSItem } from '../types';
import { rssApi } from '../services/api';

const Feed: React.FC = () => {
  const [rssItems, setRssItems] = useState<RSSItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [newTweet, setNewTweet] = useState('');

  useEffect(() => {
    loadRSSItems();
  }, []);

  const loadRSSItems = async () => {
    try {
      setLoading(true);
      const items = await rssApi.getAllRSSItems();
      setRssItems(items);
    } catch (error) {
      console.error('Error loading RSS items:', error);
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMs = now.getTime() - date.getTime();
    const diffInHours = Math.floor(diffInMs / (1000 * 60 * 60));
    
    if (diffInHours < 1) {
      const diffInMinutes = Math.floor(diffInMs / (1000 * 60));
      return `${diffInMinutes}m`;
    } else if (diffInHours < 24) {
      return `${diffInHours}h`;
    } else {
      const diffInDays = Math.floor(diffInHours / 24);
      return `${diffInDays}d`;
    }
  };

  const handleTweet = () => {
    if (newTweet.trim()) {
      // In a real app, this would post to a backend
      console.log('New tweet:', newTweet);
      setNewTweet('');
    }
  };

  if (loading) {
    return (
      <div className="feed">
        <div className="loading">Loading feed...</div>
      </div>
    );
  }

  return (
    <div className="feed">
      <div className="feed-header">
        <h1 className="feed-title">Home</h1>
      </div>

      {/* Tweet Composer */}
      <div className="tweet-composer">
        <div className="composer-content">
          <div className="avatar">U</div>
          <div className="composer-input">
            <textarea
              className="tweet-input"
              placeholder="What's happening?"
              value={newTweet}
              onChange={(e) => setNewTweet(e.target.value)}
              maxLength={280}
            />
            <div className="composer-actions">
              <div></div>
              <button
                className="tweet-button"
                onClick={handleTweet}
                disabled={!newTweet.trim()}
              >
                Tweet
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* RSS Items as Tweets */}
      {rssItems.map((item) => (
        <div key={item.id} className="tweet">
          <div className="tweet-content">
            <div className="avatar">RSS</div>
            <div className="tweet-body">
              <div className="tweet-header">
                <span className="tweet-author">RSS Feed</span>
                <span className="tweet-handle">@rssfeed</span>
                <span className="tweet-time">·</span>
                <span className="tweet-time">{formatDate(item.pubDate)}</span>
              </div>
              <div className="tweet-text">
                <h3 style={{ marginBottom: '8px', fontSize: '16px', fontWeight: '600' }}>
                  {item.title}
                </h3>
                <p>{item.description}</p>
                {item.link && (
                  <a
                    href={item.link}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="tweet-link"
                    style={{ 
                      display: 'inline-flex', 
                      alignItems: 'center', 
                      gap: '4px', 
                      marginTop: '8px' 
                    }}
                  >
                    Read more <ExternalLink size={14} />
                  </a>
                )}
              </div>
              <div className="tweet-actions">
                <button className="action-button">
                  <MessageCircle />
                  <span>0</span>
                </button>
                <button className="action-button">
                  <Repeat2 />
                  <span>0</span>
                </button>
                <button className="action-button">
                  <Heart />
                  <span>0</span>
                </button>
                <button className="action-button">
                  <Share />
                </button>
              </div>
            </div>
          </div>
        </div>
      ))}

      {rssItems.length === 0 && (
        <div style={{ padding: '40px', textAlign: 'center', color: '#71767b' }}>
          No RSS items to display. Add some RSS feeds to get started!
        </div>
      )}
    </div>
  );
};

export default Feed;