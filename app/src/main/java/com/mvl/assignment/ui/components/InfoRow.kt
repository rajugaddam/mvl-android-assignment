package com.mvl.assignment.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.mvl.assignment.R
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.ui.common.labelRes
import com.mvl.assignment.ui.theme.Dimens

/** "A   location name" header used on the detail and result screens. */
@Composable
fun PlaceHeader(
    slot: Slot,
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingLarge),
        verticalAlignment = Alignment.Top,
    ) {
        Text(text = stringResource(slot.labelRes()), style = MaterialTheme.typography.titleMedium)
        Text(
            text = name,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
        )
    }
}

/** Indented "aqi   0" / "nickname   home" row: grey label column, then the value. */
@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = Dimens.InfoIndent, top = Dimens.SpacingXSmall, bottom = Dimens.SpacingXSmall),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(Dimens.InfoLabelWidth),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

/** AQI value, or "-" when the station reports none. */
@Composable
fun aqiText(aqi: Int?): String = aqi?.toString() ?: stringResource(R.string.aqi_unknown)
