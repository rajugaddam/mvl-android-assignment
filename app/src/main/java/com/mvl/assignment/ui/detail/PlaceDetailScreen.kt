package com.mvl.assignment.ui.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mvl.assignment.R
import com.mvl.assignment.ui.common.ObserveAsEvents
import com.mvl.assignment.ui.components.BottomPrimaryButton
import com.mvl.assignment.ui.components.InfoRow
import com.mvl.assignment.ui.components.LoadingContent
import com.mvl.assignment.ui.components.MessageContent
import com.mvl.assignment.ui.components.PlaceHeader
import com.mvl.assignment.ui.components.aqiText
import com.mvl.assignment.ui.theme.Dimens

/** Screen 2: shows the place in a slot and edits its nickname. */
@Composable
fun PlaceDetailScreen(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            PlaceDetailEvent.Done -> onDone()
        }
    }

    PlaceDetailBody(
        state = state,
        nicknameInput = viewModel.nicknameInput,
        onNicknameChange = viewModel::onNicknameChange,
        onConfirm = viewModel::onConfirm,
        onMissingConfirm = onDone,
        modifier = modifier,
    )
}

@Composable
private fun PlaceDetailBody(
    state: PlaceDetailUiState,
    nicknameInput: String,
    onNicknameChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onMissingConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .padding(Dimens.ScreenPadding),
    ) {
        when (val content = state.content) {
            PlaceDetailContent.Loading -> LoadingContent(modifier = Modifier.weight(1f))

            PlaceDetailContent.Missing -> {
                MessageContent(
                    message = stringResource(R.string.error_place_missing),
                    modifier = Modifier.weight(1f),
                )
                BottomPrimaryButton(text = stringResource(R.string.action_confirm), onClick = onMissingConfirm)
            }

            is PlaceDetailContent.Loaded -> {
                PlaceHeader(slot = state.slot, name = content.place.name)
                InfoRow(
                    label = stringResource(R.string.label_aqi),
                    value = aqiText(content.place.aqi),
                    modifier = Modifier.padding(top = Dimens.SpacingSmall),
                )

                Spacer(modifier = Modifier.weight(1f))

                NicknameField(
                    value = nicknameInput,
                    onValueChange = onNicknameChange,
                    onDone = onConfirm,
                    errorText = state.error?.asString(),
                )
                Spacer(modifier = Modifier.height(Dimens.SpacingLarge))
                BottomPrimaryButton(text = stringResource(R.string.action_confirm), onClick = onConfirm)
            }
        }
    }
}

/** Rounded "nickname" field; the ViewModel enforces the length limit on every change. */
@Composable
private fun NicknameField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    errorText: String?,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                text = stringResource(R.string.nickname_hint),
                fontWeight = FontWeight.SemiBold,
            )
        },
        singleLine = true,
        isError = errorText != null,
        supportingText = errorText?.let { { Text(it) } },
        shape = RoundedCornerShape(Dimens.CornerRadius),
        textStyle = MaterialTheme.typography.bodyMedium,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
            focusedBorderColor = MaterialTheme.colorScheme.secondary,
            cursorColor = MaterialTheme.colorScheme.secondary,
        ),
    )
}
