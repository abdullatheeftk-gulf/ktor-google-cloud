package com.example.data

import com.example.data.DatabaseFactory.dbQuery
import kotlinx.css.select
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class NotesDaoImpl:NotesDao {
    private fun resultRawToNote(row: ResultRow)= Note(
        id = row[Notes.id],
        title = row[Notes.title],
        description = row[Notes.description]
    )

    override suspend fun addNewNotes(note: Note): Note? {
        return dbQuery{
            val insertStatement = Notes.insert{
                it[Notes.title] = note.title
                it[Notes.description] = note.description
            }
            insertStatement.resultedValues?.singleOrNull()?.let(::resultRawToNote)
        }
    }

    override suspend fun getAllNotes(): List<Note> {
        return  dbQuery {
            Notes.selectAll().map(::resultRawToNote)
        }
    }
}