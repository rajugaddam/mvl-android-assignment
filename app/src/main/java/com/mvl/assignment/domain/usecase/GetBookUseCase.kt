package com.mvl.assignment.domain.usecase

import com.mvl.assignment.domain.model.Book
import com.mvl.assignment.domain.repository.BookRepository
import javax.inject.Inject

class GetBookUseCase @Inject constructor(
    private val bookRepository: BookRepository,
) {
    suspend operator fun invoke(id: String): Result<Book> = bookRepository.getBook(id)
}
