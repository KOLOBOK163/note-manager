package com.ksbk.notes.DTO;


import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
public class NoteRequest {
    @NotBlank
    @Size(min = 3, max = 100)
    private String title;

    @NotBlank
    @Size(min= 5)
    private String description;

}
