package com.example.plugins

import com.example.data.Note
import com.example.data.NotesDao
import com.example.data.NotesDaoImpl
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    val notesDao:NotesDao = NotesDaoImpl()
    routing {
        route("/api") {
            get("/") {
                call.respondText("Hello World!")
            }
            get("/getAllNotes"){
               try {
                   val result = notesDao.getAllNotes()
                   call.respond(HttpStatusCode.OK,message = result)
               } catch (e:Exception){
                   call.respond(status = HttpStatusCode.ExpectationFailed, message = e.message?:"There have some problem")

               }
            }
            post("/insertNote") {
                try {
                    val note = call.receive<Note>()
                    println(note)
                    val result = notesDao.addNewNotes(note)
                    if (result==null){
                       call.respond(HttpStatusCode.Conflict)
                    }else {
                        call.respond(HttpStatusCode.Created, result)
                    }
                }catch (e:Exception){
                    println(e)
                    call.respond(status = HttpStatusCode.ExpectationFailed, message = e.message?:"There have some problem")
                }
            }
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}
