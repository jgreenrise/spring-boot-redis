export interface RSSItem {
  id: string;
  title: string;
  description: string;
  link: string;
  pubDate: string;
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