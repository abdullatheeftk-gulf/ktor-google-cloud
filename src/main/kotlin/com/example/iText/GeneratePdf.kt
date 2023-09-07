package com.example.iText

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GeneratePdf {
    private const val FILE_PATH = "src/main/resources/static/pdf"

    suspend fun generatePdf(text:String,fileName:String){
        withContext(Dispatchers.IO){
            val pdfDocument = PdfDocument(PdfWriter("$FILE_PATH/$fileName"))
            val document = Document(pdfDocument)
            document.add(Paragraph(text))
            document.close()
        }

    }

}