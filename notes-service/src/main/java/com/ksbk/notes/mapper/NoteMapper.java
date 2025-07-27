package com.ksbk.notes.mapper;

import com.ksbk.notes.DTO.NoteResponse;
import com.ksbk.notes.entity.Note;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NoteMapper {
    @Mapping(source = "userId", target = "userId")
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd HH:mm")
    @Mapping(source = "updatedAt", target = "updatedAt", dateFormat = "yyyy-MM-dd HH:mm")
    NoteResponse noteToNoteResponse(Note note);
}
