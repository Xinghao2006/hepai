package com.example.hepai.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object Hepai {
    val Paper = Color(0xFFF2EEE3)
    val Card = Color(0xFFFBF9F3)
    val CardDeep = Color(0xFFF6F2E8)
    val Ink = Color(0xFF17251F)
    val InkSoft = Color(0xFF33443A)
    val Muted = Color(0xFF7C8075)
    val MutedLight = Color(0xFFA6A79B)
    val Line = Color(0xFFE4DFD2)
    val Green = Color(0xFF2F513D)
    val GreenMid = Color(0xFF7E9B68)
    val Leaf = Color(0xFFB9C977)
    val LeafWash = Color(0xFFEDF1D3)
    val Rose = Color(0xFFD97F95)
    val RoseDeep = Color(0xFFB85E75)
    val RoseWash = Color(0xFFFBE9ED)
    val Butter = Color(0xFFF1E4B8)
    val White = Color(0xFFFFFDF8)
    val Error = Color(0xFFB34848)
}

private val colors =
    lightColorScheme(
        primary = Hepai.Green,
        onPrimary = Hepai.White,
        secondary = Hepai.Rose,
        onSecondary = Hepai.Ink,
        background = Hepai.Paper,
        onBackground = Hepai.Ink,
        surface = Hepai.Card,
        onSurface = Hepai.Ink,
        surfaceVariant = Hepai.CardDeep,
        onSurfaceVariant = Hepai.InkSoft,
        outline = Hepai.Line,
        error = Hepai.Error,
    )

private val typography =
    Typography(
        displayLarge =
            TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 42.sp,
                lineHeight = 48.sp,
                letterSpacing = (-1.5).sp,
                color = Hepai.Ink,
            ),
        headlineLarge =
            TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 38.sp,
                letterSpacing = (-0.8).sp,
                color = Hepai.Ink,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                fontSize = 24.sp,
                lineHeight = 30.sp,
                color = Hepai.Ink,
            ),
        titleLarge =
            TextStyle(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                color = Hepai.Ink,
            ),
        titleMedium =
            TextStyle(
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Hepai.Ink,
            ),
        bodyLarge = TextStyle(fontSize = 14.sp, lineHeight = 21.sp, color = Hepai.Ink),
        bodyMedium = TextStyle(fontSize = 12.sp, lineHeight = 18.sp, color = Hepai.InkSoft),
        bodySmall = TextStyle(fontSize = 10.sp, lineHeight = 15.sp, color = Hepai.Muted),
        labelLarge = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Hepai.Ink),
        labelSmall = TextStyle(fontSize = 8.sp, letterSpacing = 0.8.sp, color = Hepai.Muted),
    )

@Composable
fun HepaiTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = colors,
        typography = typography,
        shapes =
            Shapes(
                extraSmall = RoundedCornerShape(8.dp),
                small = RoundedCornerShape(12.dp),
                medium = RoundedCornerShape(16.dp),
                large = RoundedCornerShape(22.dp),
                extraLarge = RoundedCornerShape(28.dp),
            ),
        content = content,
    )
}

@Composable
fun LogoMark(modifier: Modifier = Modifier) {
    Canvas(modifier.size(40.dp)) {
        val stroke = 1.5.dp.toPx()
        val r = size.minDimension * 0.37f
        drawCircle(
            color = Hepai.Rose,
            radius = r,
            center = Offset(size.width * 0.40f, size.height * 0.50f),
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = Hepai.Green,
            radius = r,
            center = Offset(size.width * 0.60f, size.height * 0.50f),
            style = Stroke(width = stroke),
        )
        drawCircle(
            color = Hepai.Ink,
            radius = r * 0.28f,
            center = Offset(size.width * 0.50f, size.height * 0.50f),
        )
    }
}

@Composable
fun LogoLockup(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LogoMark()
        Column {
            Text("合拍", style = MaterialTheme.typography.titleLarge, color = Hepai.Ink)
            Text(
                "IN SYNC",
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 2.sp),
                color = Hepai.Muted,
            )
        }
    }
}

@Composable
fun Text(
    text: String,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = style.color,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    overflow: androidx.compose.ui.text.style.TextOverflow = androidx.compose.ui.text.style.TextOverflow.Clip,
    textAlign: androidx.compose.ui.text.style.TextAlign? = null,
) {
    androidx.compose.material3.Text(
        text = text,
        style = style,
        color = color,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow,
        textAlign = textAlign,
    )
}

@Composable
fun Pill(
    text: String,
    color: Color = Hepai.Muted,
    background: Color = Hepai.CardDeep,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .background(background, RoundedCornerShape(999.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.2.sp), color = color)
    }
}

@Composable
fun Hairline(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Hepai.Line),
    )
}

@Composable
fun InitialAvatar(
    initial: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .size(30.dp)
                .background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(initial, color = Hepai.White, style = MaterialTheme.typography.bodySmall)
    }
}
