package com.example.data

interface NotesDao {
    suspend fun addNewNotes(note: Note):Note?
    suspend fun getAllNotes():List<Note>
}