import React, { useState, useEffect } from 'react';
import './App.css';
import Header from './components/Header';
import Sidebar from './components/Sidebar';
import Feed from './components/Feed';
import RSSFeedManager from './components/RSSFeedManager';
import Leaderboard from './components/Leaderboard';

type ActiveView = 'home' | 'rss' | 'leaderboard';

function App() {
  const [activeView, setActiveView] = useState<ActiveView>('home');

  return (
    <div className="App">
      <Header />
      <div className="app-body">
        <Sidebar activeView={activeView} setActiveView={setActiveView} />
        <main className="main-content">
          {activeView === 'home' && <Feed />}
          {activeView === 'rss' && <RSSFeedManager />}
          {activeView === 'leaderboard' && <Leaderboard />}
        </main>
      </div>
    </div>
  );
}

export default App;
