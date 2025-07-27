package com.ksbk.notes.DTO;

import lombok.Data;

@Data
public class NoteResponse {
    private Long id;
    private String title;
    private String description;
    private Long userId;
    private String createdAt;
    private String updatedAt;
}
