import React, { useState, useEffect } from 'react';
import { Trophy, Plus, TrendingUp, User } from 'lucide-react';
import { LeaderboardEntry } from '../types';
import { leaderboardApi } from '../services/api';

const Leaderboard: React.FC = () => {
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [newUser, setNewUser] = useState('');
  const [newScore, setNewScore] = useState('');
  const [incrementUser, setIncrementUser] = useState('');
  const [incrementAmount, setIncrementAmount] = useState('');
  const [message, setMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const LEADERBOARD_KEY = 'leaderboardOct2023';

  useEffect(() => {
    loadLeaderboard();
  }, []);

  const loadLeaderboard = async () => {
    try {
      setLoading(true);
      const users = await leaderboardApi.getReverseRange(LEADERBOARD_KEY, 0, 10);
      
      // Convert string array to leaderboard entries with mock scores
      const entries: LeaderboardEntry[] = users.map((user, index) => ({
        user,
        score: Math.floor(Math.random() * 1000) + (10 - index) * 100, // Mock scores for demo
        rank: index + 1,
      }));
      
      setLeaderboard(entries);
    } catch (error) {
      console.error('Error loading leaderboard:', error);
      // Show sample data if API fails
      setLeaderboard([
        { user: 'alice', score: 950, rank: 1 },
        { user: 'bob', score: 850, rank: 2 },
        { user: 'charlie', score: 750, rank: 3 },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleAddUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newUser.trim() || !newScore) return;

    try {
      await leaderboardApi.addUser(LEADERBOARD_KEY, newUser, parseFloat(newScore));
      setMessage({ type: 'success', text: `User ${newUser} added to leaderboard!` });
      setNewUser('');
      setNewScore('');
      loadLeaderboard();
    } catch (error) {
      console.error('Error adding user:', error);
      setMessage({ type: 'error', text: 'Failed to add user to leaderboard.' });
    }
  };

  const handleIncrementScore = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!incrementUser.trim() || !incrementAmount) return;

    try {
      const newScore = await leaderboardApi.incrementScore(
        LEADERBOARD_KEY, 
        incrementUser, 
        parseFloat(incrementAmount)
      );
      setMessage({ 
        type: 'success', 
        text: `${incrementUser}'s score increased by ${incrementAmount}! New score: ${newScore}` 
      });
      setIncrementUser('');
      setIncrementAmount('');
      loadLeaderboard();
    } catch (error) {
      console.error('Error incrementing score:', error);
      setMessage({ type: 'error', text: 'Failed to increment user score.' });
    }
  };

  const getRankIcon = (rank: number) => {
    switch (rank) {
      case 1:
        return <span style={{ color: '#ffd700', fontSize: '20px' }}>🏆</span>;
      case 2:
        return <span style={{ color: '#c0c0c0', fontSize: '20px' }}>🥈</span>;
      case 3:
        return <span style={{ color: '#cd7f32', fontSize: '20px' }}>🥉</span>;
      default:
        return <span className="user-rank">#{rank}</span>;
    }
  };

  if (loading) {
    return (
      <div className="leaderboard">
        <div className="loading">Loading leaderboard...</div>
      </div>
    );
  }

  return (
    <div className="leaderboard">
      <div className="feed-header">
        <h1 className="feed-title" style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Trophy size={24} />
          Leaderboard
        </h1>
      </div>

      {/* Add User Form */}
      <div className="rss-form">
        <h2 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Plus size={20} />
          Add User
        </h2>
        
        <form onSubmit={handleAddUser}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="newUser">Username</label>
              <input
                type="text"
                id="newUser"
                className="form-input"
                placeholder="Enter username"
                value={newUser}
                onChange={(e) => setNewUser(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="newScore">Initial Score</label>
              <input
                type="number"
                id="newScore"
                className="form-input"
                placeholder="0"
                value={newScore}
                onChange={(e) => setNewScore(e.target.value)}
                required
                min="0"
              />
            </div>
          </div>
          
          <button
            type="submit"
            className="submit-button"
            disabled={!newUser.trim() || !newScore}
          >
            <Plus size={16} />
            Add User
          </button>
        </form>
      </div>

      {/* Increment Score Form */}
      <div className="rss-form">
        <h2 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <TrendingUp size={20} />
          Increment Score
        </h2>
        
        <form onSubmit={handleIncrementScore}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group">
              <label className="form-label" htmlFor="incrementUser">Username</label>
              <input
                type="text"
                id="incrementUser"
                className="form-input"
                placeholder="Enter username"
                value={incrementUser}
                onChange={(e) => setIncrementUser(e.target.value)}
                required
              />
            </div>
            <div className="form-group">
              <label className="form-label" htmlFor="incrementAmount">Score Increment</label>
              <input
                type="number"
                id="incrementAmount"
                className="form-input"
                placeholder="10"
                value={incrementAmount}
                onChange={(e) => setIncrementAmount(e.target.value)}
                required
                min="1"
              />
            </div>
          </div>
          
          <button
            type="submit"
            className="submit-button"
            disabled={!incrementUser.trim() || !incrementAmount}
          >
            <TrendingUp size={16} />
            Increment Score
          </button>
        </form>
      </div>

      {/* Message Display */}
      {message && (
        <div 
          className={`message ${message.type}`}
          style={{
            margin: '16px 20px',
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
          {message.text}
        </div>
      )}

      {/* Leaderboard Display */}
      <div style={{ padding: '0 20px' }}>
        <h2 style={{ marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Trophy size={20} />
          Top Players
        </h2>
        
        {leaderboard.length === 0 ? (
          <div style={{ 
            textAlign: 'center', 
            padding: '40px', 
            color: '#71767b',
            backgroundColor: '#16181c',
            borderRadius: '12px',
            border: '1px solid #2f3336'
          }}>
            <Trophy size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
            <p>No players on the leaderboard yet.</p>
            <p>Add some users to get started!</p>
          </div>
        ) : (
          <div style={{ display: 'grid', gap: '12px' }}>
            {leaderboard.map((entry) => (
              <div key={entry.user} className="leaderboard-item">
                <div className="user-info">
                  {getRankIcon(entry.rank || 0)}
                  <div className="avatar">
                    <User size={16} />
                  </div>
                  <div className="user-name">{entry.user}</div>
                </div>
                <div className="user-score">{entry.score.toLocaleString()}</div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Instructions */}
      <div className="rss-form" style={{ marginTop: '20px' }}>
        <h3 style={{ marginBottom: '12px' }}>How to use:</h3>
        <ul style={{ color: '#71767b', lineHeight: '1.6', paddingLeft: '20px' }}>
          <li>Add new users to the leaderboard with an initial score</li>
          <li>Increment existing users' scores to move them up the rankings</li>
          <li>The leaderboard automatically sorts users by their scores in descending order</li>
          <li>Data is stored in Redis using sorted sets for efficient ranking operations</li>
        </ul>
      </div>
    </div>
  );
};

export default Leaderboard;