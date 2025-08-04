import React from 'react';
import { Search } from 'lucide-react';

const Header: React.FC = () => {
  return (
    <header className="header">
      <div className="header-content">
        <div className="logo">
          RSS Feed Hub
        </div>
        <div className="search-container" style={{ position: 'relative' }}>
          <Search 
            size={20} 
            style={{ 
              position: 'absolute', 
              left: '12px', 
              top: '50%', 
              transform: 'translateY(-50%)', 
              color: '#71767b' 
            }} 
          />
          <input 
            type="text" 
            placeholder="Search feeds..." 
            className="search-bar"
            style={{ paddingLeft: '44px' }}
          />
        </div>
      </div>
    </header>
  );
};

export default Header;