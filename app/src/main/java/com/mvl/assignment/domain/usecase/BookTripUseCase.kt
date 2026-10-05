package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.model.IncompleteSelectionException
import com.mvl.assignment.domain.repository.BookRepository
import com.mvl.assignment.domain.repository.SelectionRepository
import javax.inject.Inject

/** Books the current A → B selection (`POST /books`). */
class BookTripUseCase @Inject constructor(
    private val selectionRepository: SelectionRepository,
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke(): Result<Book> {
        val selection = selectionRepository.selection.value
        val a = selection.a
        val b = selection.b
        if (a == null || b == null) return Result.failure(IncompleteSelectionException())
        return bookRepository.book(a, b)
    }
}
