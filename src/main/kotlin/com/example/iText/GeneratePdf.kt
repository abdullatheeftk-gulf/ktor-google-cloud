package com.example.iText




import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.action.PdfTarget
import com.itextpdf.kernel.pdf.canvas.PdfCanvasConstants
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell

import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.css.Color
import java.io.ByteArrayOutputStream


object GeneratePdf {
    private const val FILE_PATH = "src/main/resources/static/pdf"

    suspend fun generatePdf(text: String, fileName: String) =
        withContext<String>(Dispatchers.IO) {
            val pdfDocument = PdfDocument(PdfWriter("$FILE_PATH/$fileName"))
            val document = Document(pdfDocument, PageSize.A4)


            val heading = Paragraph("Customer Payment Report")
                .setBold()
                .setFontSize(16f)
                .setTextAlignment(TextAlignment.CENTER)
            document.add(heading)



            val table = Table(UnitValue.createPercentArray(floatArrayOf(0.2f,0.8f,1f))).apply {
                setTextAlignment(TextAlignment.CENTER)
                setHorizontalAlignment(HorizontalAlignment.CENTER)
                useAllAvailableWidth()
            }
            val idHeader = Cell().add(Paragraph("Id")
                .setBold()
                .setFontSize(10f)
                .setFontColor(DeviceRgb(128,128,128))

            ).setBackgroundColor(
                DeviceRgb(150,150,0)
            )
            val titleHeader = Cell().add(Paragraph("Title")
                .setBold()
                .setFontSize(10f)
                .setFontColor(DeviceRgb(128,128,128))

            ).setBackgroundColor(
                DeviceRgb(150,150,0)
            )
            val descriptionHeader = Cell().add(Paragraph("Description")
                .setBold()
                .setFontSize(10f)
                .setFontColor(DeviceRgb(128,128,128))

            ).setBackgroundColor(
                DeviceRgb(150,150,0)
            )
            //table.addCell("Column 1")
            table.addHeaderCell(idHeader)
            table.addHeaderCell(titleHeader)
            table.addHeaderCell(descriptionHeader)



            // Add data rows to the table
            for (i in 1..100) {
                table.addCell("$i")
                table.addCell("Row $i, Col 2")
                table.addCell("Row $i, Column 3 is wider")
            }

            document.add(table)


            document.close()
            "ddf"
        }

    suspend fun generatePdfAndGetStream(text: String): ByteArray? =

        withContext(Dispatchers.IO) {
            try {
                val stream = ByteArrayOutputStream()
                val document = Document(PdfDocument(PdfWriter(stream)))
                document.add(Paragraph(text))
                document.close()
                stream.toByteArray()
            } catch (e: Exception) {
                println(e.message)
                null
            }
        }


    /*fun createHeaderCell(text: String): Cell {
        val cell = Cell()
        cell.add()
        cell.setTextAlignment(TextAlignment.CENTER)
       // cell.setBackgroundColor(com.itextpdf.kernel.colors.Color.LIGHT_GRAY)
        return cell
    }

    fun createCell(text: String): Cell {
        val cell = Cell()
        cell.add(text)
        cell.setTextAlignment(TextAlignment.CENTER)
        return cell
    }*/


}