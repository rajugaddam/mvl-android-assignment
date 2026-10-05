package com.mvl.assignment.data.repository

import com.mvl.assignment.data.mapper.toDomain
import com.mvl.assignment.data.mapper.toDto
import com.mvl.assignment.data.remote.api.BookApi
import com.mvl.assignment.data.remote.apiCall
import com.mvl.assignment.data.remote.dto.BookRequest
import com.mvl.assignment.di.IoDispatcher
import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.BookNotFoundException
import com.mvl.assignment.domain.model.BookingMonth
import com.mvl.assignment.domain.model.Place
import com.mvl.assignment.domain.repository.BookRepository
import com.mvl.assignment.domain.repository.CurrentMonthProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Talks to the books API. Bookings returned by the server are also cached by id so the result
 * screen shows the response data (not the request) without a second round trip; when the cache
 * is empty (e.g. after process death) the booking is fetched again from `GET /books`.
 */
@Singleton
class BookRepositoryImpl @Inject constructor(
    private val api: BookApi,
    private val currentMonthProvider: CurrentMonthProvider,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : BookRepository {

    private val booksById = ConcurrentHashMap<String, Book>()

    override suspend fun book(a: Place, b: Place): Result<Book> = withContext(ioDispatcher) {
        apiCall {
            api.createBook(BookRequest(a = a.toDto(), b = b.toDto())).toDomain()
        }.onSuccess { booksById[it.id] = it }
    }

    /** Cache first; otherwise the server's list for the current month (a booking is made "now"). */
    override suspend fun getBook(id: String): Result<Book> {
        booksById[id]?.let { return Result.success(it) }
        return getMonthlyBooks(currentMonthProvider.currentMonth()).mapCatching { books ->
            books.firstOrNull { it.id == id } ?: throw BookNotFoundException(id)
        }
    }

    override suspend fun getMonthlyBooks(month: BookingMonth): Result<List<Book>> =
        withContext(ioDispatcher) {
            apiCall {
                api.getBooks(year = month.year, month = month.month).map { it.toDomain() }
            }.onSuccess { books -> books.forEach { booksById[it.id] = it } }
        }
}
