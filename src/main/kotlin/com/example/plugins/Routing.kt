package com.example.plugins

import com.example.data.Note
import com.example.data.NotesDao
import com.example.data.NotesDaoImpl
import com.example.iText.GeneratePdf
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.nio.file.Paths
import java.util.Date
import kotlin.time.measureTimedValue

fun Application.configureRouting() {
    val notesDao: NotesDao = NotesDaoImpl()
    routing {
        get("/") {
            call.respondText("Hello World!")
        }
        route("/api") {
            get("/") {
                call.respondText("Hello World!")
            }
            get("/getAllNotes") {
                try {
                    val result = notesDao.getAllNotes()
                    call.respond(HttpStatusCode.OK, message = result)
                } catch (e: Exception) {
                    call.respond(
                        status = HttpStatusCode.ExpectationFailed,
                        message = e.message ?: "There have some problem"
                    )

                }
            }
            post("/insertNote") {
                try {
                    val note = call.receive<Note>()
                    println(note)
                    val result = notesDao.addNewNotes(note)
                    if (result == null) {
                        call.respond(HttpStatusCode.Conflict)
                    } else {
                        call.respond(HttpStatusCode.Created, result)
                    }
                } catch (e: Exception) {
                    println(e)
                    call.respond(
                        status = HttpStatusCode.ExpectationFailed,
                        message = e.message ?: "There have some problem"
                    )
                }
            }

            post("/insertMultiNote") {
                try {
                    val count = (call.request.queryParameters["count"]?:"1").toInt()
                    println("$count")
                    val note = call.receive<Note>()


                    val time = measureTimedValue {
                        (1..count).forEach { _ ->

                                notesDao.addNewNotes(note)


                        }
                    }.duration.inWholeSeconds


                    call.respond(hashMapOf("time" to time))

                } catch (e: Exception) {
                    println(e)
                    call.respond(
                        status = HttpStatusCode.ExpectationFailed,
                        message = e.message ?: "There have some problem"
                    )
                }
            }
            post("/upload") {
                try {
                    var fileDescription = ""
                    var fileName = ""
                    val multipartDart = call.receiveMultipart()
                    var result = ""

                    multipartDart.forEachPart { part ->

                        when (part) {
                            is PartData.FormItem -> {
                                fileDescription = part.value
                                println(fileDescription)
                            }

                            is PartData.FileItem -> {
                                fileName = part.originalFileName as String
                                val inputStream = part.streamProvider()
                                result = uploadAnObjectToGoogleCloudStorageAsInputStream(
                                    projectId = "latheef-node-project",
                                    bucketName = "ktor",
                                    objectName = fileName,
                                    inputStream = inputStream
                                )

                                /*  val fileBytes = part.streamProvider().readBytes()
                                  File("src/main/resources/static/$fileName").writeBytes(fileBytes)
                                  uploadAnObjectToGoogleCloudStorage(
                                      projectId = "latheef-node-project",
                                      bucketName = "ktor",
                                      objectName = fileName,
                                      filePath = "src/main/resources/static/$fileName"
                                  )*/
                            }

                            else -> {}
                        }
                        part.dispose()
                    }
                    call.respondText(result)
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.ExpectationFailed, e.message ?: "There have some error")
                }

            }

            /* get("/download-file/{fileName}") {
                 try {
                     val fileName = call.parameters["fileName"]

                     if (fileName==null){
                         call.respondText("No file name")
                     }else{
                         downloadObjectAsFile(
                             projectId = "latheef-node-project",
                             bucketName = "ktor",
                             objectName = fileName,
                             destFilePath = "src/main/resources/static/$fileName"
                         )
                         call.respond(HttpStatusCode.OK,fileName)
                     }
                 }catch (e:Exception){
                     call.respond(HttpStatusCode.ExpectationFailed,e.message?:"There have some problem")
                 }

             }*/

            get("/download-row/{fileName}") {
                try {
                    val fileName = call.parameters["fileName"]

                    if (fileName == null) {
                        call.respondText("No file name")
                    } else {
                        /*downloadObjectAsFile(
                            projectId = "latheef-node-project",
                            bucketName = "ktor",
                            objectName = fileName,
                            destFilePath = "src/main/resources/static/$fileName"
                        )
                        call.respond(HttpStatusCode.OK,fileName)*/

                        val bytes = downloadObjectAsBytes(
                            projectId = "latheef-node-project",
                            bucketName = "ktor",
                            objectName = fileName,
                        )
                        call.respondBytes(
                            bytes = bytes,
                            status = HttpStatusCode.OK,
                            contentType = ContentType.Image.JPEG
                        )
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.ExpectationFailed, e.message ?: "There have some problem")
                }
            }

            get("/generate-pdf") {
                val title = Date().time
                GeneratePdf.generatePdf(text = "Assalamualikum-walykumussalam", fileName = "$title.pdf")
                call.response.header(
                    HttpHeaders.ContentDisposition,
                    ContentDisposition.Attachment.withParameter(ContentDisposition.Parameters.FileName, "$title.pdf")
                        .toString()
                )
                call.respondFile(file = File("src/main/resources/static/pdf/$title.pdf"))
            }

            post("/upload-pdf/{text}") {
                try {
                    val text = call.parameters["text"] ?: throw Exception("No text")

                    val byteArray = GeneratePdf.generatePdfAndGetStream(text = text)
                        ?: throw Exception("failed to create pdf")
                    val result = uploadAnObjectToGoogleCloudStorageAsByteArray(
                        projectId = "latheef-node-project",
                        bucketName = "ktor",
                        objectName = "sample-${Date()}.pdf",
                        byteArray = byteArray
                    )
                    call.respondText(result)

                } catch (e: Exception) {
                    call.respond(HttpStatusCode.ExpectationFailed, e.message ?: "There have some problem")
                }


            }
        }
        // Static plugin. Try to access `/static/index.html`
        static("/static") {
            resources("static")
        }
    }
}

@Throws(IOException::class)
suspend fun uploadAnObjectToGoogleCloudStorage(
    projectId: String,
    bucketName: String,
    objectName: String,
    filePath: String
): String {

    val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
    val blobId: BlobId = BlobId.of(bucketName, objectName)

    val blobInfo: BlobInfo = BlobInfo.newBuilder(blobId).build()

    val preCondition: Storage.BlobWriteOption = if (storage.get(bucketName, objectName) == null) {
        Storage.BlobWriteOption.doesNotExist()
    } else {
        Storage.BlobWriteOption.generationMatch(
            storage.get(bucketName, objectName).generation
        )
    }

    val blob = storage.createFrom(blobInfo, Paths.get(filePath), preCondition)

    return blob.blobId.toGsUtilUri()


}

@Throws(IOException::class)
suspend fun uploadAnObjectToGoogleCloudStorageAsInputStream(
    projectId: String,
    bucketName: String,
    objectName: String,
    inputStream: InputStream
): String {

    val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
    val blobId: BlobId = BlobId.of(bucketName, objectName)

    val blobInfo: BlobInfo = BlobInfo.newBuilder(blobId).build()

    val preCondition: Storage.BlobWriteOption = if (storage.get(bucketName, objectName) == null) {
        Storage.BlobWriteOption.doesNotExist()
    } else {
        Storage.BlobWriteOption.generationMatch(
            storage.get(bucketName, objectName).generation
        )
    }

    val blob = storage.createFrom(blobInfo, inputStream, preCondition)

    return blob.blobId.toGsUtilUri()


}

@Throws(IOException::class)
suspend fun uploadAnObjectToGoogleCloudStorageAsByteArray(
    projectId: String,
    bucketName: String,
    objectName: String,
    byteArray: ByteArray
): String {

    val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
    val blobId: BlobId = BlobId.of(bucketName, objectName)

    val blobInfo: BlobInfo = BlobInfo.newBuilder(blobId).build()

    val preCondition: Storage.BlobWriteOption = if (storage.get(bucketName, objectName) == null) {
        Storage.BlobWriteOption.doesNotExist()
    } else {
        Storage.BlobWriteOption.generationMatch(
            storage.get(bucketName, objectName).generation
        )
    }

    val blob = storage.create(blobInfo, byteArray)

    return blob.blobId.toGsUtilUri()


}


fun downloadObjectAsFile(
    projectId: String,
    bucketName: String,
    objectName: String,
    destFilePath: String
) {
    val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
    val blobId: BlobId = BlobId.of(bucketName, objectName)

    val blob = storage.get(blobId)

    blob.downloadTo(Paths.get(destFilePath))
}

fun downloadObjectAsBytes(
    projectId: String,
    bucketName: String,
    objectName: String,

    ): ByteArray {
    val storage: Storage = StorageOptions.newBuilder().setProjectId(projectId).build().service
    /*val blobId: BlobId = BlobId.of(bucketName,objectName)

    val blob = storage.get(blobId)*/
    return storage.readAllBytes(bucketName, objectName);


}


