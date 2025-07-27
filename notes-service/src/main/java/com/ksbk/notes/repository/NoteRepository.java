package com.ksbk.notes.repository;

import com.ksbk.notes.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserId(Long userId);
    Optional<Note> findByIdAndUserId(Long noteId, Long userId);

    @Query("SELECT n FROM Note n WHERE n.userId = :userId AND (LOWER(n.title) " +
            "LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(n.description) " +
            "LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Note> searchByUserAndQuery(@Param("userId") Long userId, @Param("query") String query);
}
