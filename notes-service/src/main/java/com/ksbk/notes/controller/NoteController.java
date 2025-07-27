package com.ksbk.notes.controller;

import com.ksbk.notes.DTO.NoteRequest;
import com.ksbk.notes.DTO.NoteResponse;
import com.ksbk.notes.service.NoteService;
import com.ksbk.notes.exception.NoteNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notes")
@Tag(name = "Notes Controller", description = "The notes controller allows you to call methods to work with notes")
public class NoteController {

    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @Operation(summary = "Create note")
    @PostMapping
    public ResponseEntity<NoteResponse> createNote(@RequestBody NoteRequest request){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("User {} is creating a new note with title: {}", userId, request.getTitle());
        try{
            NoteResponse response = noteService.createNote(userId, request);
            logger.info("User {} successfully created note with id: {}", userId, response.getId());
            return ResponseEntity.ok(response);
        }catch (Exception e)
        {
            logger.error("Failed to create note for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Update note", description = "Update note by id")
    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(@PathVariable Long id, @RequestBody NoteRequest request) throws NoteNotFoundException {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("User {} is updating note with id: {}", userId, id);
        try{
            NoteResponse response = noteService.updateNote(userId, id, request);
            logger.info("User {} successfully updated note with id: {}", userId, id);
            return ResponseEntity.ok(response);
        }catch (Exception e)
        {
            logger.error("Failed to update note for user {} (noteId={}): {}", userId, id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Delete note", description = "Delete note by id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id) throws NoteNotFoundException {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("User {} is deleting note with id: {}", userId, id);
        try
        {
            noteService.deleteNote(userId, id);
            logger.info("User {} successfully deleted note with id: {}", userId, id);
            return ResponseEntity.noContent().build();
        }catch (Exception e)
        {
            logger.error("Failed to delete note for user {} (noteId={}): {}", userId, id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get note", description = "Get note by id")
    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNote(@PathVariable Long id) throws NoteNotFoundException {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.debug("User {} is requesting note with id: {}", userId, id);
        try{
            NoteResponse noteResponse = noteService.getNoteById(userId, id);
            logger.info("User {} retrieved note with id: {}", userId, id);
            return ResponseEntity.ok(noteResponse);
        }catch (Exception e)
        {
            logger.error("Failed to get note for user {} (noteId={}): {}", userId, id, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get all notes", description = "Get all notes by user")
    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllUserNotes(){
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.debug("User {} is requesting all notes", userId);
        try {
            List<NoteResponse> responses = noteService.getAllUserNotes(userId);
            logger.info("User {} retrieved {} notes", userId, responses.size());
            return ResponseEntity.ok(responses);
        }catch (Exception e)
        {
            logger.error("Failed to get all notes for user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Search note", description = "Search note by query(title or description)")
    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(@RequestParam String query) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.debug("User {} is searching notes with query: '{}'", userId, query);
        try {
            List<NoteResponse> responses = noteService.searchNotes(userId, query);
            logger.info("User {} found {} notes matching query '{}'", userId, responses.size(), query);
            return ResponseEntity.ok(responses);
        }catch (Exception e)
        {
            logger.error("Search failed for user {} (query='{}'): {}", userId, query, e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
