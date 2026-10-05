package com.mvl.assignment.data.repository

import com.mvl.assignment.domain.model.Selection
import com.mvl.assignment.domain.repository.SelectionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/** Process-wide A/B selection shared by the map, detail and result screens (each with its own ViewModel). */
@Singleton
class SelectionRepositoryImpl @Inject constructor() : SelectionRepository {

    private val state = MutableStateFlow(Selection())

    override val selection: StateFlow<Selection> = state.asStateFlow()

    override fun update(transform: (Selection) -> Selection) = state.update(transform)
}
