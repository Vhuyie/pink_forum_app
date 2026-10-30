package com.example.thepinkforum

data class FileItems(
    val id: Int,
    val title: String,
    val category: String,
    val fileSize: String,
    val fileType: String,
    val uploaded: String = "Admin",
    val date: String = "Aug 2026"
)

val sampleFiles = listOf(
    FileItems(1, "Digital Safety Guide.pdf", "Web Safety", "2.4 MB", "PDF", "Educator Kinglee"),
    FileItems(2, "CV Template 2026.docx", "Career Readiness", "1.1 MB", "DOCX", "Admin Team"),
    FileItems(3, "Intro to HTML & CSS Notes.pdf", "Web Dev", "5.8 MB", "PDF", "Educator Kinglee"),
    FileItems(4, "Budgeting Spreadsheet.xlsx", "Financial Literacy", "850 KB", "XLSX", "Admin Team")
)