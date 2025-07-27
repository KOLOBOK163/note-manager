package com.ksbk.notes.exception;

public class NoteNotFoundException extends Exception{
    public NoteNotFoundException(Long message){super(String.valueOf(message));}
}
