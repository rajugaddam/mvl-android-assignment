package com.mvl.assignment.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mvl.assignment.domain.model.Slot
import com.mvl.assignment.ui.common.labelRes
import com.mvl.assignment.ui.theme.Dimens
import com.mvl.assignment.ui.theme.MvlTheme

/**
 * Grey A/B box on the map screen: bold slot letter followed by the place label
 * (nickname or address). [text] is `null` while the slot is empty; [onClick] `null` disables taps.
 */
@Composable
fun SlotLabel(
    slot: Slot,
    text: String?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(Dimens.CornerRadius)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.SlotLabelHeight)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant, shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Dimens.SpacingMedium, vertical = Dimens.SpacingSmall),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpacingMedium),
    ) {
        Text(
            text = stringResource(slot.labelRes()),
            style = MaterialTheme.typography.labelLarge,
        )
        Text(
            text = text.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
    }
}

@Preview
@Composable
private fun SlotLabelPreview() {
    MvlTheme {
        SlotLabel(slot = Slot.A, text = "Seocho District, Yangjae 2(i)-dong", onClick = {})
    }
}
