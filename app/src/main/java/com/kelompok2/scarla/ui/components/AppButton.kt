package com.kelompok2.scarla.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kelompok2.scarla.ui.theme.*

enum class ButtonType {
    PRIMARY,
    SECONDARY,
    TERTIARY
}

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    buttonType: ButtonType = ButtonType.PRIMARY,
    enabled: Boolean = true
) {

    val buttonColors = when (buttonType) {

        ButtonType.PRIMARY -> ButtonDefaults.buttonColors(
            containerColor = Primary500,
            contentColor = Neutral900,
            disabledContainerColor = Neutral200,
            disabledContentColor = Neutral500
        )

        ButtonType.SECONDARY -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Secondary500,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Neutral400
        )

        ButtonType.TERTIARY -> ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Primary500,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = Neutral400
        )
    }

    val border = when (buttonType) {
        ButtonType.SECONDARY -> BorderStroke(
            1.5.dp,
            if (enabled) Secondary500 else Neutral400
        )
        else -> null
    }

    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = buttonColors,
        border = border,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}