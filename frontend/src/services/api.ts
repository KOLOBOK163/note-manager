import axios, { AxiosResponse } from 'axios';
import {
  JwtResponse,
  LoginCredentials,
  RegisterCredentials,
  Note,
  NoteRequest,
  PasswordResetRequest,
} from '../types';

// API configuration
const AUTH_BASE_URL = process.env.REACT_APP_AUTH_API_URL || 'http://localhost:8082/api';
const NOTES_BASE_URL = process.env.REACT_APP_NOTES_API_URL || 'http://localhost:8081/api';

// Create axios instances
const authApi = axios.create({
  baseURL: AUTH_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const notesApi = axios.create({
  baseURL: NOTES_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add token to requests
notesApi.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to handle token refresh
notesApi.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;
      
      const refreshToken = localStorage.getItem('refreshToken');
      if (refreshToken) {
        try {
          const response = await authApi.post('/auth/refresh-token', refreshToken);
          const { accessToken } = response.data;
          
          localStorage.setItem('accessToken', accessToken);
          originalRequest.headers.Authorization = `Bearer ${accessToken}`;
          
          return notesApi(originalRequest);
        } catch (refreshError) {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('refreshToken');
          window.location.href = '/login';
        }
      }
    }
    
    return Promise.reject(error);
  }
);

// Auth API methods
export const authService = {
  register: async (credentials: RegisterCredentials): Promise<string> => {
    const response: AxiosResponse<string> = await authApi.post('/auth/register', credentials);
    return response.data;
  },

  login: async (credentials: LoginCredentials): Promise<JwtResponse> => {
    const response: AxiosResponse<JwtResponse> = await authApi.post('/auth/login', credentials);
    const data = response.data;
    
    // Store tokens
    localStorage.setItem('accessToken', data.accessToken);
    localStorage.setItem('refreshToken', data.refreshToken);
    localStorage.setItem('user', JSON.stringify({
      username: data.username,
      email: data.email,
      roles: data.roles
    }));
    
    return data;
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
  },

  refreshToken: async (refreshToken: string): Promise<JwtResponse> => {
    const response: AxiosResponse<JwtResponse> = await authApi.post('/auth/refresh-token', refreshToken);
    return response.data;
  },

  forgotPassword: async (email: string): Promise<string> => {
    const response: AxiosResponse<string> = await authApi.post('/auth/forgot-password', { email });
    return response.data;
  },

  resetPassword: async (token: string, newPassword: string): Promise<string> => {
    const response: AxiosResponse<string> = await authApi.post('/auth/reset-password', {
      token,
      newPassword
    });
    return response.data;
  },

  getCurrentUser: () => {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('accessToken');
  }
};

// Notes API methods
export const notesService = {
  getAllNotes: async (): Promise<Note[]> => {
    const response: AxiosResponse<Note[]> = await notesApi.get('/notes');
    return response.data;
  },

  getNoteById: async (id: number): Promise<Note> => {
    const response: AxiosResponse<Note> = await notesApi.get(`/notes/${id}`);
    return response.data;
  },

  createNote: async (noteData: NoteRequest): Promise<Note> => {
    const response: AxiosResponse<Note> = await notesApi.post('/notes', noteData);
    return response.data;
  },

  updateNote: async (id: number, noteData: NoteRequest): Promise<Note> => {
    const response: AxiosResponse<Note> = await notesApi.put(`/notes/${id}`, noteData);
    return response.data;
  },

  deleteNote: async (id: number): Promise<void> => {
    await notesApi.delete(`/notes/${id}`);
  },

  searchNotes: async (query: string): Promise<Note[]> => {
    const response: AxiosResponse<Note[]> = await notesApi.get(`/notes/search?query=${encodeURIComponent(query)}`);
    return response.data;
  }
};