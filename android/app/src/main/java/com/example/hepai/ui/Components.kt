package com.example.hepai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hepai.data.HepaiTab

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = RoundedCornerShape(14.dp),
        colors =
            ButtonDefaults.buttonColors(
                containerColor = Hepai.Green,
                contentColor = Hepai.White,
                disabledContainerColor = Hepai.Green.copy(alpha = 0.35f),
                disabledContentColor = Hepai.White.copy(alpha = 0.7f),
            ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
    ) {
        Text(
            text,
            style = MaterialTheme.typography.bodyMedium.copy(color = Hepai.White, fontWeight = FontWeight.Medium),
        )
    }
}

@Composable
fun SoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Hepai.InkSoft),
    ) {
        Text(text, style = MaterialTheme.typography.bodySmall.copy(color = Hepai.InkSoft))
    }
}

@Composable
fun SectionHead(
    kicker: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(kicker, style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.2.sp), color = Hepai.Muted)
            Text(title, style = MaterialTheme.typography.titleLarge)
        }
        trailing?.invoke()
    }
}

@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    color: Color = Hepai.Card,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = color,
        border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
        content = content,
    )
}

@Composable
fun EmptyDashedCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, Hepai.Line),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Hepai.Muted)
        }
    }
}

private data class NavSpec(val tab: HepaiTab, val label: String, val icon: ImageVector)

@Composable
fun BottomNav(
    selected: HepaiTab,
    onSelect: (HepaiTab) -> Unit,
) {
    val specs =
        listOf(
            NavSpec(HepaiTab.WEEK, "周历", Icons.Outlined.CalendarMonth),
            NavSpec(HepaiTab.FREE, "空档", Icons.Outlined.WbSunny),
            NavSpec(HepaiTab.IMPORT, "课表", Icons.Outlined.UploadFile),
        )
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(Hepai.Card)
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        specs.forEach { spec ->
            val active = selected == spec.tab
            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .clickable { onSelect(spec.tab) }
                        .padding(vertical = 2.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(34.dp)
                            .background(if (active) Hepai.LeafWash else Color.Transparent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        spec.icon,
                        contentDescription = spec.label,
                        tint = if (active) Hepai.Green else Hepai.MutedLight,
                        modifier = Modifier.size(19.dp),
                    )
                }
                Text(
                    spec.label,
                    style =
                        MaterialTheme.typography.labelSmall.copy(
                            fontFamily = FontFamily.SansSerif,
                            letterSpacing = 0.4.sp,
                        ),
                    color = if (active) Hepai.Ink else Hepai.Muted,
                )
            }
        }
    }
}

@Composable
fun AvatarBadge(
    initial: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(34.dp).background(color, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            initial,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Hepai.White,
        )
    }
}

@Composable
fun SmallDot(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Spacer(modifier = modifier.size(5.dp).background(color, CircleShape))
}

@Composable
fun SpacerH(width: Int) {
    Spacer(Modifier.width(width.dp))
}
