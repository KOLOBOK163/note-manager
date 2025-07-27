// User types
export interface User {
  username: string;
  email: string;
  avatarUrl?: string;
}

export interface UserDTO extends User {
  password: string;
}

export interface JwtResponse {
  accessToken: string;
  refreshToken: string;
  type: string;
  username: string;
  email: string;
  roles: string[];
}

// Note types
export interface Note {
  id: number;
  title: string;
  description: string;
  userId: number;
  createdAt: string;
  updatedAt: string;
}

export interface NoteRequest {
  title: string;
  description: string;
}

// Auth types
export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterCredentials extends LoginCredentials {
  email: string;
}

export interface PasswordResetRequest {
  email?: string;
  token?: string;
  newPassword?: string;
}

// API Response types
export interface ApiError {
  message: string;
  status?: number;
}