package com.ksbk.notes.service.impl;

import com.ksbk.notes.DTO.UserResponse;
import com.ksbk.notes.service.AuthServiceClient;
import com.ksbk.notes.service.NoteService;
import com.ksbk.notes.DTO.NoteRequest;
import com.ksbk.notes.DTO.NoteResponse;
import com.ksbk.notes.entity.Note;
import com.ksbk.notes.exception.NoteNotFoundException;
import com.ksbk.notes.mapper.NoteMapper;
import com.ksbk.notes.repository.NoteRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoteServiceImpl implements NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteRepository noteRepository;

    private final NoteMapper noteMapper;

    private final AuthServiceClient authServiceClient;

    public NoteServiceImpl(NoteRepository noteRepository, NoteMapper noteMapper, AuthServiceClient authServiceClient) {
        this.noteRepository = noteRepository;
        this.noteMapper = noteMapper;
        this.authServiceClient = authServiceClient;
    }

    @Override
    @Transactional
    public NoteResponse createNote(Long userId, NoteRequest request) {
        logger.info("Creating note for user ID: {}", userId);
        logger.debug("Note request data - title: {}, description: {}",
                request.getTitle(), request.getDescription());

        try {
            UserResponse user = authServiceClient.getUserById(userId);
            if (user == null) {
                logger.warn("User not found during note creation: {}", userId);
                throw new UsernameNotFoundException("User not found");
            }

            Note note = new Note();
            note.setTitle(request.getTitle());
            note.setDescription(request.getDescription());
            note.setCreatedAt(LocalDateTime.now());
            note.setUserId(userId);

            Note savedNote = noteRepository.save(note);
            logger.info("Note created successfully. Note ID: {}, User ID: {}",
                    savedNote.getId(), userId);

            return noteMapper.noteToNoteResponse(savedNote);
        } catch (Exception e) {
            logger.error("Failed to create note for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public NoteResponse updateNote(Long userId, Long noteId, NoteRequest request) throws NoteNotFoundException {
        logger.info("Updating note. Note ID: {}, User ID: {}", noteId, userId);
        logger.debug("Update data - title: {}, description: {}",
                request.getTitle(), request.getDescription());

        try {
            Note note = getNoteByIdAndUser(noteId, userId);
            logger.debug("Found note to update: {}", note.getId());

            note.setTitle(request.getTitle());
            note.setDescription(request.getDescription());
            note.setUpdatedAt(LocalDateTime.now());

            Note updatedNote = noteRepository.save(note);
            logger.info("Note updated successfully. Note ID: {}", updatedNote.getId());

            return noteMapper.noteToNoteResponse(updatedNote);
        } catch (NoteNotFoundException e) {
            logger.warn("Note not found during update. Note ID: {}, User ID: {}", noteId, userId);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to update note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void deleteNote(Long userId, Long noteId) throws NoteNotFoundException {
        logger.info("Deleting note. Note ID: {}, User ID: {}", noteId, userId);

        try {
            Note note = getNoteByIdAndUser(noteId, userId);
            noteRepository.delete(note);
            logger.info("Note deleted successfully. Note ID: {}", noteId);
        } catch (NoteNotFoundException e) {
            logger.warn("Note not found during deletion. Note ID: {}, User ID: {}", noteId, userId);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to delete note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public NoteResponse getNoteById(Long userId, Long noteId) throws NoteNotFoundException {
        logger.debug("Fetching note. Note ID: {}, User ID: {}", noteId, userId);

        try {
            Note note = getNoteByIdAndUser(noteId, userId);
            logger.info("Note retrieved successfully. Note ID: {}", noteId);
            return noteMapper.noteToNoteResponse(note);
        } catch (NoteNotFoundException e) {
            logger.warn("Note not found. Note ID: {}, User ID: {}", noteId, userId);
            throw e;
        } catch (Exception e) {
            logger.error("Failed to fetch note {} for user {}: {}", noteId, userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<NoteResponse> getAllUserNotes(Long userId) {
        logger.debug("Fetching all notes for user ID: {}", userId);

        try {
            List<Note> notes = noteRepository.findByUserId(userId);
            logger.info("Retrieved {} notes for user ID: {}", notes.size(), userId);
            return notes.stream()
                    .map(noteMapper::noteToNoteResponse)
                    .toList();
        } catch (Exception e) {
            logger.error("Failed to fetch notes for user {}: {}", userId, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<NoteResponse> searchNotes(Long userId, String query) {
        logger.debug("Searching notes for user ID: {}, query: '{}'", userId, query);

        try {
            List<Note> notes = noteRepository.searchByUserAndQuery(userId, query);
            logger.info("Found {} notes matching query '{}' for user ID: {}",
                    notes.size(), query, userId);
            return notes.stream()
                    .map(noteMapper::noteToNoteResponse)
                    .toList();
        } catch (Exception e) {
            logger.error("Search failed for user {} (query='{}'): {}", userId, query, e.getMessage(), e);
            throw e;
        }
    }

    private Note getNoteByIdAndUser(Long noteId, Long userId) throws NoteNotFoundException {
        logger.trace("Looking for note ID: {} belonging to user ID: {}", noteId, userId);
        return noteRepository.findByIdAndUserId(noteId, userId)
                .orElseThrow(() -> {
                    logger.warn("Note not found. Note ID: {}, User ID: {}", noteId, userId);
                    return new NoteNotFoundException(noteId);
                });
    }
}
