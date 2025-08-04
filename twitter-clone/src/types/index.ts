export interface RSSItem {
  id: string;
  title: string;
  description: string;
  link: string;
  pubDate: string;
}

export interface FlashCard {
  id: string;
  front: string;
  back: string;
  createdDate: string;
  lastReviewed?: string;
  reviewCount: number;
}

export interface LeaderboardEntry {
  user: string;
  score: number;
  rank?: number;
}

export interface ApiResponse<T> {
  data: T;
  success: boolean;
  message?: string;
}