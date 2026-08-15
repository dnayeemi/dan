package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.QrViewModel
import com.example.ui.components.DownloadDialog
import com.example.ui.components.QrCodePreview
import com.example.ui.components.QrCustomizationPanel
import com.example.ui.components.QrHistoryView
import com.example.ui.components.QrTypeInputForms
import com.example.ui.components.QrTypeSelector
import com.example.ui.theme.QrMakerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: QrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val isDark = uiState.isDarkMode ?: systemDark

            QrMakerTheme(darkTheme = isDark) {
                QrMakerApp(
                    viewModel = viewModel,
                    isDarkTheme = isDark
                )
            }
        }
    }
}

@Composable
fun QrMakerApp(
    viewModel: QrViewModel,
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyItems by viewModel.historyItems.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header Bar
            HeaderBar(
                isDarkTheme = isDarkTheme,
                onToggleDarkTheme = { viewModel.toggleDarkMode() }
            )

            // Top Navigation Tabs
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface
            ) {
                TabRow(
                    selectedTabIndex = uiState.selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    divider = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("main_tabs")
                ) {
                    Tab(
                        selected = uiState.selectedTab == 0,
                        onClick = { viewModel.setTab(0) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = if (uiState.selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Generate",
                                    fontWeight = if (uiState.selectedTab == 0) FontWeight.Bold else FontWeight.Medium,
                                    color = if (uiState.selectedTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_generator")
                    )
                    Tab(
                        selected = uiState.selectedTab == 1,
                        onClick = { viewModel.setTab(1) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                BadgedBox(
                                    badge = {
                                        if (historyItems.isNotEmpty()) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.primary,
                                                contentColor = MaterialTheme.colorScheme.onPrimary
                                            ) {
                                                Text("${historyItems.size}")
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = if (uiState.selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "History",
                                    fontWeight = if (uiState.selectedTab == 1) FontWeight.Bold else FontWeight.Medium,
                                    color = if (uiState.selectedTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        modifier = Modifier.testTag("tab_history")
                    )
                }
            }

            // Main Content Area
            AnimatedContent(
                targetState = uiState.selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "tab_switch_animation",
                modifier = Modifier.weight(1f)
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        GeneratorScreen(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                    1 -> {
                        QrHistoryView(
                            historyItems = historyItems,
                            onLoadItem = { viewModel.loadFromHistory(it) },
                            onDeleteItem = { viewModel.deleteHistoryItem(it) },
                            onClearAll = { viewModel.clearAllHistory() },
                            onNavigateToGenerator = { viewModel.setTab(0) }
                        )
                    }
                }
            }
        }
    }

    // Download / Export Dialog
    if (uiState.showDownloadDialog) {
        DownloadDialog(
            onDismiss = { viewModel.setDownloadDialogVisible(false) },
            onExportPng = { viewModel.exportPng(context) },
            onExportJpg = { viewModel.exportJpg(context) },
            onExportSvg = { viewModel.exportSvg(context) },
            onShare = { viewModel.shareQrCode(context) }
        )
    }
}

@Composable
private fun HeaderBar(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "QR Maker Logo",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "QR Maker",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = onToggleDarkTheme,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("theme_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Dark Mode",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    }
}

@Composable
private fun GeneratorScreen(
    uiState: com.example.ui.QrUiState,
    viewModel: QrViewModel
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. QR Code Preview Card
        QrCodePreview(
            bitmap = uiState.qrBitmap,
            isGenerating = uiState.isGenerating,
            qrType = uiState.qrType,
            formattedContent = uiState.formattedContent,
            errorCorrection = uiState.errorCorrection,
            onCopyContent = { viewModel.copyContentToClipboard(context) },
            onShare = { viewModel.shareQrCode(context) }
        )

        // 2. Main Primary Utility Actions Bar
        UtilityActionBar(
            onGenerate = { viewModel.generateCurrentQr(saveToHistory = true) },
            onDownload = { viewModel.setDownloadDialogVisible(true) },
            onShare = { viewModel.shareQrCode(context) },
            onCopy = { viewModel.copyContentToClipboard(context) },
            onClear = { viewModel.clearAllInputs() }
        )

        // 3. QR Type Chips Selector
        QrTypeSelector(
            selectedType = uiState.qrType,
            onSelectType = { viewModel.setQrType(it) }
        )

        // 4. QR Content Inputs Form
        QrTypeInputForms(
            uiState = uiState,
            onUpdateUrl = { viewModel.updateUrl(it) },
            onUpdateText = { viewModel.updateText(it) },
            onUpdateWifi = { ssid, pass, type, hidden -> viewModel.updateWifi(ssid, pass, type, hidden) },
            onUpdatePhone = { viewModel.updatePhone(it) },
            onUpdateEmail = { to, sub, body -> viewModel.updateEmail(to, sub, body) },
            onUpdateSms = { phone, msg -> viewModel.updateSms(phone, msg) },
            onUpdateVCard = { first, last, phone, email, org -> viewModel.updateVCard(first, last, phone, email, org) }
        )

        // 5. Customization Panel (Colors, Styles, Logos, EC, Quality)
        QrCustomizationPanel(
            uiState = uiState,
            onSetFgColor = { viewModel.setFgColor(it) },
            onSetBgColor = { viewModel.setBgColor(it) },
            onSetDotStyle = { viewModel.setDotStyle(it) },
            onSetCenterLogo = { viewModel.setCenterLogo(it) },
            onSetErrorCorrection = { viewModel.setErrorCorrection(it) },
            onSetResolution = { viewModel.setResolution(it) }
        )

        // 6. App Footer
        FooterSection()

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun UtilityActionBar(
    onGenerate: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    onClear: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Main Prominent Generate Button
        Button(
            onClick = onGenerate,
            shape = RoundedCornerShape(100.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("generate_qr_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Generate QR",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Secondary Action Row (Download, Share, Copy, Clear)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onDownload,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("download_qr_button"),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onShare,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .testTag("share_qr_button"),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(0.9f)
                    .height(44.dp)
                    .testTag("copy_text_button"),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Copy", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onClear,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier
                    .weight(0.85f)
                    .height(44.dp)
                    .testTag("clear_inputs_button"),
                contentPadding = PaddingValues(horizontal = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(3.dp))
                Text("Clear", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FooterSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "QR Maker",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "100% Client-Side Generation • Private & Secure\n© 2026 QR Maker. All rights reserved.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )
    }
}
