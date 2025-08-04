import React from 'react';
import { Home, Rss, Trophy } from 'lucide-react';

type ActiveView = 'home' | 'rss' | 'leaderboard';

interface SidebarProps {
  activeView: ActiveView;
  setActiveView: (view: ActiveView) => void;
}

const Sidebar: React.FC<SidebarProps> = ({ activeView, setActiveView }) => {
  const navItems = [
    { id: 'home' as ActiveView, label: 'Home', icon: Home },
    { id: 'rss' as ActiveView, label: 'RSS Feeds', icon: Rss },
    { id: 'leaderboard' as ActiveView, label: 'Leaderboard', icon: Trophy },
  ];

  return (
    <aside className="sidebar">
      <nav>
        <ul className="sidebar-nav">
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <li key={item.id}>
                <button
                  className={`nav-item ${activeView === item.id ? 'active' : ''}`}
                  onClick={() => setActiveView(item.id)}
                >
                  <Icon />
                  <span>{item.label}</span>
                </button>
              </li>
            );
          })}
        </ul>
      </nav>
    </aside>
  );
};

export default Sidebar;