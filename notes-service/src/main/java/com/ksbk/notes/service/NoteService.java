package com.ksbk.notes.service;

import com.ksbk.notes.DTO.NoteRequest;
import com.ksbk.notes.DTO.NoteResponse;
import com.ksbk.notes.exception.NoteNotFoundException;

import java.util.List;

public interface NoteService {
    NoteResponse createNote(Long userId, NoteRequest request);
    NoteResponse updateNote(Long userId, Long noteId, NoteRequest request) throws NoteNotFoundException;
    void deleteNote(Long userId, Long noteId) throws NoteNotFoundException;
    NoteResponse getNoteById(Long userId, Long noteId) throws NoteNotFoundException;
    List<NoteResponse> getAllUserNotes(Long userId);
    List<NoteResponse> searchNotes(Long userId, String query);
}
