package com.mvl.assignment.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.mvl.assignment.R
import com.mvl.assignment.ui.theme.Dimens
import com.mvl.assignment.ui.theme.MvlTheme

/**
 * Yellow action button (the "V" button in Figma). Size comes from [modifier];
 * while [loading] it shows a spinner and ignores taps.
 */
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    loading: Boolean = false,
) {
    val loadingDescription = stringResource(R.string.cd_loading)
    Button(
        onClick = { if (!loading) onClick() },
        modifier = modifier.defaultMinSize(minHeight = Dimens.ButtonHeight),
        shape = RoundedCornerShape(Dimens.CornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(Dimens.ProgressSmall)
                    .semantics { contentDescription = loadingDescription },
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = Dimens.ProgressStroke,
            )
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge, textAlign = TextAlign.Center)
        }
    }
}

/** Full-width variant used at the bottom of screens 2 and 3. */
@Composable
fun BottomPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryButton(
        text = text,
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.ButtonHeight),
    )
}

@Preview
@Composable
private fun PrimaryButtonPreview() {
    MvlTheme {
        BottomPrimaryButton(text = stringResource(R.string.action_confirm), onClick = {})
    }
}
