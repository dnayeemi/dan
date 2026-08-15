package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QrCenterLogo
import com.example.data.QrDotStyle
import com.example.ui.QrUiState
import com.example.ui.theme.QrBgPresets
import com.example.ui.theme.QrColorPresets

@Composable
fun QrCustomizationPanel(
    uiState: QrUiState,
    onSetFgColor: (Long) -> Unit,
    onSetBgColor: (Long) -> Unit,
    onSetDotStyle: (QrDotStyle) -> Unit,
    onSetCenterLogo: (QrCenterLogo) -> Unit,
    onSetErrorCorrection: (String) -> Unit,
    onSetResolution: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(true) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qr_customization_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header with toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Styling & Customization",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand or collapse styling"
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Foreground Color
                    ColorPaletteRow(
                        title = "QR Code Color (Foreground)",
                        selectedColorHex = uiState.fgColorHex,
                        colors = QrColorPresets,
                        onColorSelected = onSetFgColor,
                        tagPrefix = "fg_color"
                    )

                    // 2. Background Color
                    ColorPaletteRow(
                        title = "Background Color",
                        selectedColorHex = uiState.bgColorHex,
                        colors = QrBgPresets,
                        onColorSelected = onSetBgColor,
                        tagPrefix = "bg_color"
                    )

                    // 3. Dot / Module Pattern Style
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Pattern Style",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val styles = QrDotStyle.values()
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            styles.forEachIndexed { index, style ->
                                SegmentedButton(
                                    selected = uiState.dotStyle == style,
                                    onClick = { onSetDotStyle(style) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = styles.size),
                                    modifier = Modifier.testTag("style_${style.name.lowercase()}")
                                ) {
                                    Text(
                                        text = when (style) {
                                            QrDotStyle.SQUARE -> "Square"
                                            QrDotStyle.ROUNDED -> "Rounded"
                                            QrDotStyle.DOTS -> "Dots"
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }

                    // 4. Center Logo / Icon Badge
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Center Badge / Logo",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (uiState.centerLogo != QrCenterLogo.NONE) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.tertiaryContainer
                                ) {
                                    Text(
                                        text = "High EC Active",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        val logoScrollState = rememberScrollState()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(logoScrollState),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            QrCenterLogo.values().forEach { logo ->
                                val isSelected = uiState.centerLogo == logo
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSetCenterLogo(logo) },
                                    label = { Text(logo.displayName, style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = {
                                        getLogoIcon(logo)?.let { icon ->
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                                    ),
                                    modifier = Modifier.testTag("logo_${logo.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // 5. Error Correction & Export Quality
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Error Correction
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Error Correction",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val ecOptions = listOf("L", "M", "Q", "H")
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                ecOptions.forEachIndexed { index, ec ->
                                    SegmentedButton(
                                        selected = uiState.errorCorrection == ec,
                                        onClick = { onSetErrorCorrection(ec) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ecOptions.size)
                                    ) {
                                        Text(ec, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }

                        // Resolution Size
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Quality / Size",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val resolutions = listOf(512 to "512", 1024 to "1024", 2048 to "2K")
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                resolutions.forEachIndexed { index, (size, label) ->
                                    SegmentedButton(
                                        selected = uiState.resolutionPx == size,
                                        onClick = { onSetResolution(size) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = resolutions.size)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorPaletteRow(
    title: String,
    selectedColorHex: Long,
    colors: List<com.example.ui.theme.ColorPreset>,
    onColorSelected: (Long) -> Unit,
    tagPrefix: String
) {
    val scrollState = rememberScrollState()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            colors.forEach { preset ->
                val isSelected = selectedColorHex == preset.hex
                val color = preset.composeColor

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(preset.hex) }
                        .testTag("${tagPrefix}_${preset.name.lowercase().replace(' ', '_')}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected ${preset.name}",
                            tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun Color.luminance(): Float {
    return (0.299f * red + 0.587f * green + 0.114f * blue)
}

private fun getLogoIcon(logo: QrCenterLogo): ImageVector? {
    return when (logo) {
        QrCenterLogo.NONE -> null
        QrCenterLogo.LINK -> Icons.Default.Link
        QrCenterLogo.WIFI -> Icons.Default.Wifi
        QrCenterLogo.PHONE -> Icons.Default.Phone
        QrCenterLogo.EMAIL -> Icons.Default.Email
        QrCenterLogo.STAR -> Icons.Default.Star
        QrCenterLogo.HEART -> Icons.Default.Favorite
        QrCenterLogo.SHIELD -> Icons.Default.Security
        QrCenterLogo.INFO -> Icons.Default.Info
    }
}
