package com.mvl.assignment.mock

import com.mvl.assignment.data.remote.dto.BookDto
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/** Storage of the mock backend: the bookings it received through `POST /books`. */
interface MockBookingStore {
    fun all(): List<BookDto>
    fun add(book: BookDto)
}

/**
 * Persists the mock backend's bookings as JSON in a file of its own (not the app's Room database),
 * so history survives app restarts like a real server's would. The app only reaches this data
 * through `POST /books` / `GET /books`.
 */
class FileMockBookingStore(
    private val file: File,
    private val json: Json,
) : MockBookingStore {

    private val serializer = ListSerializer(BookDto.serializer())
    private var cache: MutableList<BookDto>? = null

    @Synchronized
    override fun all(): List<BookDto> = loaded().toList()

    @Synchronized
    override fun add(book: BookDto) {
        val books = loaded()
        books += book
        write(books)
    }

    private fun loaded(): MutableList<BookDto> = cache ?: read().toMutableList().also { cache = it }

    /** A missing or unreadable file means "no bookings yet" — the mock backend must never crash the app. */
    private fun read(): List<BookDto> =
        runCatching { if (file.exists()) json.decodeFromString(serializer, file.readText()) else emptyList() }
            .getOrDefault(emptyList())

    /** Write to a temp file, then swap it in, so a crash mid-write never leaves a half-written file. */
    private fun write(books: List<BookDto>) {
        file.parentFile?.mkdirs()
        val temp = File(file.parentFile, "${file.name}.tmp")
        temp.writeText(json.encodeToString(serializer, books))
        if (!temp.renameTo(file)) {
            file.delete()
            check(temp.renameTo(file)) { "Could not save mock bookings" }
        }
    }
}
