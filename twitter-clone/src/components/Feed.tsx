import React, { useState, useEffect } from 'react';
import { MessageCircle, Repeat2, Heart, Share, ExternalLink, Plus, RotateCcw, Trash2 } from 'lucide-react';
import { FlashCard } from '../types';
import { flashCardApi } from '../services/api';

const Feed: React.FC = () => {
  const [flashCards, setFlashCards] = useState<FlashCard[]>([]);
  const [loading, setLoading] = useState(true);
  const [flippedCards, setFlippedCards] = useState<Set<string>>(new Set());
  const [showAddForm, setShowAddForm] = useState(false);
  const [newFlashCard, setNewFlashCard] = useState({ front: '', back: '' });

  useEffect(() => {
    loadFlashCards();
  }, []);

  const loadFlashCards = async () => {
    try {
      setLoading(true);
      const cards = await flashCardApi.getAllFlashCards();
      setFlashCards(cards);
    } catch (error) {
      console.error('Error loading flash cards:', error);
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

  const handleFlipCard = (cardId: string) => {
    setFlippedCards(prev => {
      const newSet = new Set(prev);
      if (newSet.has(cardId)) {
        newSet.delete(cardId);
      } else {
        newSet.add(cardId);
      }
      return newSet;
    });
  };

  const handleCreateFlashCard = async () => {
    if (newFlashCard.front.trim() && newFlashCard.back.trim()) {
      try {
        await flashCardApi.createFlashCard(newFlashCard);
        setNewFlashCard({ front: '', back: '' });
        setShowAddForm(false);
        loadFlashCards(); // Reload the cards
      } catch (error) {
        console.error('Error creating flash card:', error);
      }
    }
  };

  const handleDeleteFlashCard = async (cardId: string) => {
    try {
      await flashCardApi.deleteFlashCard(cardId);
      loadFlashCards(); // Reload the cards
    } catch (error) {
      console.error('Error deleting flash card:', error);
    }
  };

  const handleMarkAsReviewed = async (cardId: string) => {
    try {
      await flashCardApi.markAsReviewed(cardId);
      loadFlashCards(); // Reload to get updated review count
    } catch (error) {
      console.error('Error marking card as reviewed:', error);
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
        <h1 className="feed-title">Flash Cards</h1>
        <button
          className="tweet-button"
          onClick={() => setShowAddForm(!showAddForm)}
          style={{ marginLeft: 'auto', padding: '8px 16px', fontSize: '14px' }}
        >
          <Plus size={16} style={{ marginRight: '4px' }} />
          Add Card
        </button>
      </div>

      {/* Flash Card Creator */}
      {showAddForm && (
        <div className="tweet-composer">
          <div className="composer-content">
            <div className="avatar">+</div>
            <div className="composer-input">
              <textarea
                className="tweet-input"
                placeholder="Front of the card (question/prompt)"
                value={newFlashCard.front}
                onChange={(e) => setNewFlashCard({ ...newFlashCard, front: e.target.value })}
                maxLength={500}
                style={{ marginBottom: '8px' }}
              />
              <textarea
                className="tweet-input"
                placeholder="Back of the card (answer/explanation)"
                value={newFlashCard.back}
                onChange={(e) => setNewFlashCard({ ...newFlashCard, back: e.target.value })}
                maxLength={500}
              />
              <div className="composer-actions">
                <button
                  className="action-button"
                  onClick={() => setShowAddForm(false)}
                  style={{ color: '#71767b' }}
                >
                  Cancel
                </button>
                <button
                  className="tweet-button"
                  onClick={handleCreateFlashCard}
                  disabled={!newFlashCard.front.trim() || !newFlashCard.back.trim()}
                >
                  Create Card
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Flash Cards */}
      {flashCards.map((card) => {
        const isFlipped = flippedCards.has(card.id);
        return (
          <div key={card.id} className="tweet">
            <div className="tweet-content">
              <div className="avatar">FC</div>
              <div className="tweet-body">
                <div className="tweet-header">
                  <span className="tweet-author">Flash Card</span>
                  <span className="tweet-handle">@flashcard</span>
                  <span className="tweet-time">·</span>
                  <span className="tweet-time">{formatDate(card.createdDate)}</span>
                  <span className="tweet-time" style={{ marginLeft: '8px', color: '#71767b' }}>
                    Reviewed {card.reviewCount} times
                  </span>
                </div>
                <div 
                  className="tweet-text"
                  onClick={() => handleFlipCard(card.id)}
                  style={{ 
                    cursor: 'pointer', 
                    minHeight: '60px', 
                    display: 'flex', 
                    alignItems: 'center',
                    backgroundColor: isFlipped ? '#1e3a8a' : '#1e40af',
                    padding: '16px',
                    borderRadius: '12px',
                    marginBottom: '12px',
                    transition: 'all 0.3s ease',
                    color: 'white'
                  }}
                >
                  <div style={{ width: '100%' }}>
                    <div style={{ 
                      fontSize: '12px', 
                      textTransform: 'uppercase', 
                      fontWeight: '600', 
                      marginBottom: '8px',
                      opacity: 0.8
                    }}>
                      {isFlipped ? 'ANSWER' : 'QUESTION'}
                    </div>
                    <div style={{ fontSize: '16px', lineHeight: '1.4' }}>
                      {isFlipped ? card.back : card.front}
                    </div>
                    {!isFlipped && (
                      <div style={{ 
                        fontSize: '12px', 
                        marginTop: '12px', 
                        opacity: 0.8,
                        fontStyle: 'italic'
                      }}>
                        Click to reveal answer
                      </div>
                    )}
                  </div>
                </div>
                <div className="tweet-actions">
                  <button 
                    className="action-button"
                    onClick={() => handleFlipCard(card.id)}
                    title="Flip card"
                  >
                    <RotateCcw />
                    <span>Flip</span>
                  </button>
                  <button 
                    className="action-button"
                    onClick={() => handleMarkAsReviewed(card.id)}
                    title="Mark as reviewed"
                  >
                    <Heart />
                    <span>{card.reviewCount}</span>
                  </button>
                  <button 
                    className="action-button"
                    onClick={() => handleDeleteFlashCard(card.id)}
                    title="Delete card"
                    style={{ color: '#ef4444' }}
                  >
                    <Trash2 />
                  </button>
                </div>
              </div>
            </div>
          </div>
        );
      })}

      {flashCards.length === 0 && (
        <div style={{ padding: '40px', textAlign: 'center', color: '#71767b' }}>
          No flash cards yet. Click "Add Card" to create your first flash card!
        </div>
      )}
    </div>
  );
};

export default Feed;