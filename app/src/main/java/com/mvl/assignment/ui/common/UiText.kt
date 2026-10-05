package com.mvl.assignment.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.BookNotFoundException
import com.mvl.assignment.domain.model.IncompleteSelectionException
import com.mvl.assignment.domain.model.LocationUnavailableException
import com.mvl.assignment.domain.model.MissingApiKeyException
import com.mvl.assignment.domain.model.NicknameTooLongException
import com.mvl.assignment.domain.model.RemoteServiceException
import java.io.IOException

/** A string resource plus its format args, so ViewModels expose text without holding a Context. */
@Immutable
data class UiText(
    @param:StringRes val resId: Int,
    val args: List<Any> = emptyList(),
) {
    @Composable
    fun asString(): String = stringResource(resId, *args.toTypedArray())
}

/** Maps domain / network failures to user-facing messages. */
fun Throwable.toUiText(): UiText = when (this) {
    is MissingApiKeyException -> UiText(R.string.error_missing_key, listOf(keyName))
    is RemoteServiceException -> UiText(R.string.error_server)
    is IOException -> UiText(R.string.error_network)
    is NicknameTooLongException -> UiText(R.string.error_nickname_too_long, listOf(maxLength))
    is BookNotFoundException -> UiText(R.string.error_book_not_found)
    is IncompleteSelectionException -> UiText(R.string.error_selection_incomplete)
    is LocationUnavailableException -> UiText(R.string.error_location_unavailable)
    else -> UiText(R.string.error_unknown)
}
