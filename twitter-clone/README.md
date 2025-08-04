# Twitter-like RSS Feed Hub

A modern, Twitter-inspired web application for managing RSS feeds and leaderboards. Built with React, TypeScript, and designed with a sleek dark theme that mimics Twitter's interface.

## Features

### 🏠 Home Feed
- Twitter-like interface for displaying RSS feed items
- Tweet composer (UI demonstration)
- Interactive tweet actions (like, retweet, reply, share)
- Real-time date formatting
- Responsive design

### 📡 RSS Feed Manager
- Add RSS feeds from any URL
- Popular RSS feeds quick-add buttons
- Real-time feedback on feed processing
- Integration with Spring Boot backend
- Error handling and loading states

### 🏆 Leaderboard
- Add users with initial scores
- Increment user scores
- Real-time leaderboard rankings
- Trophy icons for top 3 positions
- Redis-backed sorted sets

### 🎨 Design Features
- Dark theme inspired by Twitter
- Responsive layout (mobile-friendly)
- Modern glassmorphism effects
- Smooth animations and transitions
- Accessible color scheme

## Tech Stack

- **Frontend**: React 18, TypeScript
- **Styling**: CSS3 with custom properties
- **Icons**: Lucide React
- **HTTP Client**: Axios
- **Backend Integration**: Spring Boot REST APIs

## Getting Started

### Prerequisites

- Node.js 16+ and npm
- Spring Boot backend running on port 8080
- Redis server (for backend functionality)

### Installation

1. **Clone and install dependencies:**
   ```bash
   cd twitter-clone
   npm install
   ```

2. **Start the development server:**
   ```bash
   npm start
   ```

3. **Open your browser:**
   Navigate to `http://localhost:3000`

### Backend Integration

The app is configured to proxy API requests to `http://localhost:8080`. Make sure your Spring Boot backend is running with the following endpoints:

- `GET /api/rss/fetch-and-save-rss?url={rss_url}` - Fetch and save RSS feed
- `POST /sortedset/add/{key}?user={user}&score={score}` - Add user to leaderboard
- `PUT /sortedset/increment/{key}?user={user}&score={score}` - Increment user score
- `GET /sortedset/reverse-range/{key}?start={start}&end={end}` - Get leaderboard rankings

## Project Structure

```
src/
├── components/           # React components
│   ├── Header.tsx       # Top navigation bar
│   ├── Sidebar.tsx      # Left navigation sidebar
│   ├── Feed.tsx         # Main feed display
│   ├── RSSFeedManager.tsx # RSS feed management
│   └── Leaderboard.tsx  # Leaderboard interface
├── services/            # API integration
│   └── api.ts          # Axios configuration and API calls
├── types/              # TypeScript interfaces
│   └── index.ts        # Type definitions
├── App.tsx             # Main application component
├── App.css             # Global styles
└── index.tsx           # Application entry point
```

## API Integration

### RSS Feed API
```typescript
// Fetch and save RSS feed
await rssApi.fetchAndSaveRSS('https://example.com/rss.xml');

// Get all RSS items (mock implementation)
const items = await rssApi.getAllRSSItems();
```

### Leaderboard API
```typescript
// Add user to leaderboard
await leaderboardApi.addUser('leaderboard-key', 'username', 100);

// Increment user score
const newScore = await leaderboardApi.incrementScore('leaderboard-key', 'username', 50);

// Get top users
const topUsers = await leaderboardApi.getReverseRange('leaderboard-key', 0, 10);
```

## Available Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm run eject` - Eject from Create React App

## Environment Setup

### Development
The app uses a proxy configuration to forward API requests to the backend during development. No additional configuration needed.

### Production
For production deployment, update the API base URL in `src/services/api.ts` to point to your production backend.

## Features in Detail

### Twitter-like Interface
- **Header**: Logo, search bar, sticky positioning
- **Sidebar**: Navigation with icons, active states
- **Feed**: Tweet-like cards with user avatars, timestamps, actions
- **Responsive**: Mobile-first design with collapsible sidebar

### RSS Feed Management
- **URL Validation**: Ensures valid RSS feed URLs
- **Popular Feeds**: Quick-add buttons for common RSS feeds
- **Error Handling**: User-friendly error messages
- **Loading States**: Visual feedback during API calls

### Leaderboard System
- **Real-time Updates**: Instant leaderboard updates
- **Visual Rankings**: Trophy icons and rank numbers
- **Score Management**: Add users and increment scores
- **Data Persistence**: Redis-backed storage

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## License

This project is licensed under the MIT License.

## Acknowledgments

- Design inspired by Twitter's interface
- Icons provided by Lucide React
- Built with Create React App
