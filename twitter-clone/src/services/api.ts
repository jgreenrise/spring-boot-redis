import axios from 'axios';
import { RSSItem, LeaderboardEntry } from '../types';

// Use relative URLs since we have a proxy configured in package.json
const API_BASE_URL = '';

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
});

// RSS Feed API
export const rssApi = {
  fetchAndSaveRSS: async (url: string): Promise<void> => {
    await api.get(`/api/rss/fetch-and-save-rss?url=${encodeURIComponent(url)}`);
  },

  // Note: The backend doesn't have a get all RSS items endpoint, 
  // so we'll create a mock one for the demo
  getAllRSSItems: async (): Promise<RSSItem[]> => {
    // This would need to be implemented in the backend
    // For now, return mock data
    return [
      {
        id: '1',
        title: 'Welcome to RSS Feed Hub',
        description: 'This is your Twitter-like RSS feed aggregator. Add RSS feeds using the RSS Feeds tab to see real content here.',
        link: 'https://github.com',
        pubDate: new Date().toISOString(),
      },
      {
        id: '2',
        title: 'How to Get Started',
        description: 'Navigate to the RSS Feeds section to add your favorite RSS feeds. Then come back here to see all your content in a beautiful Twitter-like interface.',
        link: 'https://example.com',
        pubDate: new Date(Date.now() - 86400000).toISOString(),
      },
      {
        id: '3',
        title: 'Leaderboard Feature',
        description: 'Check out the Leaderboard section to manage user scores and rankings. Perfect for gamification of your RSS reading experience!',
        link: 'https://example.com/leaderboard',
        pubDate: new Date(Date.now() - 172800000).toISOString(),
      },
    ];
  },
};

// Leaderboard API
export const leaderboardApi = {
  addUser: async (key: string, user: string, score: number): Promise<void> => {
    await api.post(`/sortedset/add/${key}?user=${user}&score=${score}`);
  },

  incrementScore: async (key: string, user: string, score: number): Promise<number> => {
    const response = await api.put(`/sortedset/increment/${key}?user=${user}&score=${score}`);
    return response.data;
  },

  getRange: async (key: string, start: number = 0, end: number = 1000): Promise<string[]> => {
    const response = await api.get(`/sortedset/range/${key}?start=${start}&end=${end}`);
    return response.data;
  },

  getReverseRange: async (key: string, start: number = 0, end: number = 1000): Promise<string[]> => {
    const response = await api.get(`/sortedset/reverse-range/${key}?start=${start}&end=${end}`);
    return response.data;
  },

  getUserRank: async (key: string, user: string): Promise<number> => {
    const response = await api.get(`/sortedset/rank/${key}?user=${user}`);
    return response.data;
  },
};

export default api;