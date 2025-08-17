import React, { useState, useEffect } from 'react';
import { Plus, Search, LogOut, FileText, Edit, Trash2 } from 'lucide-react';
import { useAuth } from '../contexts/AuthContext';
import { notesService } from '../services/api';
import { Note, NoteRequest } from '../types';
import toast from 'react-hot-toast';
import NoteModal from './NoteModal';

const Dashboard: React.FC = () => {
  const { user, logout } = useAuth();
  const [notes, setNotes] = useState<Note[]>([]);
  const [filteredNotes, setFilteredNotes] = useState<Note[]>([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingNote, setEditingNote] = useState<Note | null>(null);

  useEffect(() => {
    loadNotes();
  }, []);

  useEffect(() => {
    if (searchQuery.trim()) {
      searchNotes(searchQuery);
    } else {
      setFilteredNotes(notes);
    }
  }, [searchQuery, notes]);

  const loadNotes = async () => {
    try {
      setIsLoading(true);
      const data = await notesService.getAllNotes();
      setNotes(data);
      setFilteredNotes(data);
    } catch (error: any) {
      console.error('Error loading notes:', error);
      toast.error('Ошибка загрузки заметок');
    } finally {
      setIsLoading(false);
    }
  };

  const searchNotes = async (query: string) => {
    try {
      const data = await notesService.searchNotes(query);
      setFilteredNotes(data);
    } catch (error: any) {
      console.error('Error searching notes:', error);
      toast.error('Ошибка поиска заметок');
    }
  };

  const handleCreateNote = async (noteData: NoteRequest) => {
    try {
      const newNote = await notesService.createNote(noteData);
      setNotes(prevNotes => [newNote, ...prevNotes]);
      setIsModalOpen(false);
      toast.success('Заметка создана успешно');
    } catch (error: any) {
      console.error('Error creating note:', error);
      toast.error('Ошибка создания заметки');
    }
  };

  const handleUpdateNote = async (id: number, noteData: NoteRequest) => {
    try {
      const updatedNote = await notesService.updateNote(id, noteData);
      setNotes(prevNotes => 
        prevNotes.map(note => note.id === id ? updatedNote : note)
      );
      setEditingNote(null);
      setIsModalOpen(false);
      toast.success('Заметка обновлена успешно');
    } catch (error: any) {
      console.error('Error updating note:', error);
      toast.error('Ошибка обновления заметки');
    }
  };

  const handleDeleteNote = async (id: number) => {
    if (!window.confirm('Вы уверены, что хотите удалить эту заметку?')) {
      return;
    }

    try {
      await notesService.deleteNote(id);
      setNotes(prevNotes => prevNotes.filter(note => note.id !== id));
      toast.success('Заметка удалена успешно');
    } catch (error: any) {
      console.error('Error deleting note:', error);
      toast.error('Ошибка удаления заметки');
    }
  };

  const openEditModal = (note: Note) => {
    setEditingNote(note);
    setIsModalOpen(true);
  };

  const openCreateModal = () => {
    setEditingNote(null);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingNote(null);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ru-RU', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            <div className="flex items-center">
              <FileText className="h-8 w-8 text-blue-600" />
              <h1 className="ml-2 text-xl font-semibold text-gray-900">
                Мои заметки
              </h1>
            </div>
            <div className="flex items-center space-x-4">
              <span className="text-sm text-gray-700">
                Привет, {user?.username}!
              </span>
              <button
                onClick={logout}
                className="flex items-center space-x-1 text-gray-500 hover:text-gray-700 transition-colors"
              >
                <LogOut className="h-4 w-4" />
                <span>Выйти</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Search and Create Bar */}
        <div className="flex flex-col sm:flex-row gap-4 mb-8">
          <div className="flex-1 relative">
            <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 h-5 w-5 text-gray-400" />
            <input
              type="text"
              placeholder="Поиск заметок..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
            />
          </div>
          <button
            onClick={openCreateModal}
            className="flex items-center justify-center space-x-2 px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <Plus className="h-5 w-5" />
            <span>Новая заметка</span>
          </button>
        </div>

        {/* Notes Grid */}
        {isLoading ? (
          <div className="flex justify-center items-center h-64">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          </div>
        ) : filteredNotes.length === 0 ? (
          <div className="text-center py-12">
            <FileText className="mx-auto h-12 w-12 text-gray-400" />
            <h3 className="mt-2 text-sm font-medium text-gray-900">
              {searchQuery ? 'Заметки не найдены' : 'Нет заметок'}
            </h3>
            <p className="mt-1 text-sm text-gray-500">
              {searchQuery
                ? 'Попробуйте изменить поисковый запрос'
                : 'Создайте свою первую заметку'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {filteredNotes.map((note) => (
              <div
                key={note.id}
                className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow"
              >
                <div className="p-6">
                  <div className="flex justify-between items-start mb-3">
                    <h3 className="text-lg font-medium text-gray-900 line-clamp-2">
                      {note.title}
                    </h3>
                    <div className="flex space-x-2 ml-2">
                      <button
                        onClick={() => openEditModal(note)}
                        className="text-gray-400 hover:text-blue-600 transition-colors"
                      >
                        <Edit className="h-4 w-4" />
                      </button>
                      <button
                        onClick={() => handleDeleteNote(note.id)}
                        className="text-gray-400 hover:text-red-600 transition-colors"
                      >
                        <Trash2 className="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                  <p className="text-gray-700 text-sm line-clamp-3 mb-4">
                    {note.description}
                  </p>
                  <div className="flex justify-between items-center text-xs text-gray-500">
                    <span>Создана: {formatDate(note.createdAt)}</span>
                    {note.updatedAt !== note.createdAt && (
                      <span>Изменена: {formatDate(note.updatedAt)}</span>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>

      {/* Note Modal */}
      <NoteModal
        isOpen={isModalOpen}
        onClose={closeModal}
        onSubmit={editingNote ? 
          (data) => handleUpdateNote(editingNote.id, data) : 
          handleCreateNote
        }
        note={editingNote}
        title={editingNote ? 'Редактировать заметку' : 'Создать заметку'}
      />
    </div>
  );
};

export default Dashboard;