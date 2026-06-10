package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.FrostedWhite
import com.example.ui.theme.GlassGrey

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderAccent: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(24.dp)
    val baseModifier = modifier
        .clip(cardShape)
        .background(
            Brush.verticalGradient(
                listOf(
                    Color(0x13FFFFFF), // Transparent white/7%
                    Color(0x04FFFFFF)  // Transparent white/1.5%
                )
            )
        )
        .border(
            width = 1.dp,
            brush = Brush.linearGradient(
                if (borderAccent) listOf(Color.White.copy(alpha = 0.20f), Color.White.copy(alpha = 0.05f))
                else listOf(Color.White.copy(alpha = 0.09f), Color.White.copy(alpha = 0.02f))
            ),
            shape = cardShape
        )
        .padding(18.dp)

    if (onClick != null) {
        Column(
            modifier = Modifier
                .clickable { onClick() }
                .then(baseModifier),
            content = content
        )
    } else {
        Column(
            modifier = baseModifier,
            content = content
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorAccent: Color = Color.White,
    enabled: Boolean = true
) {
    val buttonShape = RoundedCornerShape(24.dp) // Fully rounded sleek button!
    Box(
        modifier = modifier
            .clip(buttonShape)
            .background(
                if (enabled) {
                    if (colorAccent == Color.White) {
                        // High-contrast filled button for primary action!
                        Brush.horizontalGradient(
                            listOf(
                                Color.White,
                                Color.White.copy(alpha = 0.9f)
                            )
                        )
                    } else {
                        Brush.horizontalGradient(
                            listOf(
                                colorAccent.copy(alpha = 0.2f),
                                colorAccent.copy(alpha = 0.04f)
                            )
                        )
                    }
                } else {
                    BoxBackgroundMuted()
                }
            )
            .border(
                width = 1.dp,
                color = if (enabled) {
                    if (colorAccent == Color.White) Color.Transparent else colorAccent.copy(alpha = 0.3f)
                } else {
                    Color(0x1AFFFFFF)
                },
                shape = buttonShape
            )
            .clickable(enabled = enabled) { onClick() }
            .padding(vertical = 12.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            color = if (enabled) {
                if (colorAccent == Color.White) Color.Black else Color.White
            } else {
                Color.Gray
            },
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            letterSpacing = 1.5.sp
        )
    }
}

@Composable
private fun BoxBackgroundMuted(): Brush {
    return Brush.linearGradient(listOf(Color(0x06FFFFFF), Color(0x02FFFFFF)))
}

@Composable
fun GlassBadge(
    text: String,
    colorAccent: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(colorAccent.copy(alpha = 0.08f))
            .border(0.5.dp, colorAccent.copy(alpha = 0.20f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = text.uppercase(),
            color = colorAccent,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SubHeader(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text.uppercase(),
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
        modifier = modifier.padding(bottom = 8.dp)
    )
}
