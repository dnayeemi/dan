package com.example.ui

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.QrCenterLogo
import com.example.data.QrDotStyle
import com.example.data.QrItem
import com.example.data.QrRepository
import com.example.data.QrType
import com.example.generator.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class QrUiState(
    val selectedTab: Int = 0, // 0 = Generator, 1 = History
    val qrType: QrType = QrType.URL,
    
    // Type inputs
    val urlInput: String = "https://",
    val textInput: String = "Hello, QR Maker!",
    
    val wifiSsid: String = "",
    val wifiPassword: String = "",
    val wifiType: String = "WPA", // WPA, WEP, nopass
    val wifiHidden: Boolean = false,
    
    val phoneInput: String = "",
    
    val emailTo: String = "",
    val emailSubject: String = "",
    val emailBody: String = "",
    
    val smsPhone: String = "",
    val smsMessage: String = "",
    
    val vcardFirstName: String = "",
    val vcardLastName: String = "",
    val vcardPhone: String = "",
    val vcardEmail: String = "",
    val vcardOrg: String = "",

    // Customization
    val fgColorHex: Long = 0xFF000000,
    val bgColorHex: Long = 0xFFFFFFFF,
    val dotStyle: QrDotStyle = QrDotStyle.SQUARE,
    val centerLogo: QrCenterLogo = QrCenterLogo.NONE,
    val errorCorrection: String = "M",
    val resolutionPx: Int = 1024,
    
    // Rendered Output
    val formattedContent: String = "",
    val qrBitmap: Bitmap? = null,
    val isGenerating: Boolean = false,
    
    // UI Feedback
    val snackbarMessage: String? = null,
    val showDownloadDialog: Boolean = false,
    val isDarkMode: Boolean? = null // null = system default, true = dark, false = light
)

class QrViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: QrRepository

    init {
        val database = AppDatabase.getDatabase(application)
        repository = QrRepository(database.qrDao())
    }

    private val _uiState = MutableStateFlow(QrUiState())
    val uiState: StateFlow<QrUiState> = _uiState.asStateFlow()

    val historyItems: StateFlow<List<QrItem>> = repository.allHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Initial preview generation with default URL
        generateCurrentQr(saveToHistory = false)
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun toggleDarkMode() {
        _uiState.update {
            val current = it.isDarkMode
            val next = when (current) {
                null -> true
                true -> false
                false -> true
            }
            it.copy(isDarkMode = next)
        }
    }

    fun setQrType(type: QrType) {
        _uiState.update { it.copy(qrType = type) }
        generateCurrentQr(saveToHistory = false)
    }

    fun updateUrl(value: String) {
        _uiState.update { it.copy(urlInput = value) }
        generateCurrentQr(saveToHistory = false)
    }

    fun updateText(value: String) {
        _uiState.update { it.copy(textInput = value) }
        generateCurrentQr(saveToHistory = false)
    }

    fun updateWifi(ssid: String? = null, pass: String? = null, type: String? = null, hidden: Boolean? = null) {
        _uiState.update {
            it.copy(
                wifiSsid = ssid ?: it.wifiSsid,
                wifiPassword = pass ?: it.wifiPassword,
                wifiType = type ?: it.wifiType,
                wifiHidden = hidden ?: it.wifiHidden
            )
        }
        generateCurrentQr(saveToHistory = false)
    }

    fun updatePhone(number: String) {
        _uiState.update { it.copy(phoneInput = number) }
        generateCurrentQr(saveToHistory = false)
    }

    fun updateEmail(to: String? = null, subject: String? = null, body: String? = null) {
        _uiState.update {
            it.copy(
                emailTo = to ?: it.emailTo,
                emailSubject = subject ?: it.emailSubject,
                emailBody = body ?: it.emailBody
            )
        }
        generateCurrentQr(saveToHistory = false)
    }

    fun updateSms(phone: String? = null, message: String? = null) {
        _uiState.update {
            it.copy(
                smsPhone = phone ?: it.smsPhone,
                smsMessage = message ?: it.smsMessage
            )
        }
        generateCurrentQr(saveToHistory = false)
    }

    fun updateVCard(
        first: String? = null,
        last: String? = null,
        phone: String? = null,
        email: String? = null,
        org: String? = null
    ) {
        _uiState.update {
            it.copy(
                vcardFirstName = first ?: it.vcardFirstName,
                vcardLastName = last ?: it.vcardLastName,
                vcardPhone = phone ?: it.vcardPhone,
                vcardEmail = email ?: it.vcardEmail,
                vcardOrg = org ?: it.vcardOrg
            )
        }
        generateCurrentQr(saveToHistory = false)
    }

    fun setFgColor(colorHex: Long) {
        _uiState.update { it.copy(fgColorHex = colorHex) }
        generateCurrentQr(saveToHistory = false)
    }

    fun setBgColor(colorHex: Long) {
        _uiState.update { it.copy(bgColorHex = colorHex) }
        generateCurrentQr(saveToHistory = false)
    }

    fun setDotStyle(style: QrDotStyle) {
        _uiState.update { it.copy(dotStyle = style) }
        generateCurrentQr(saveToHistory = false)
    }

    fun setCenterLogo(logo: QrCenterLogo) {
        // If logo is enabled, bump error correction to Q or H if currently L/M for better scan reliability
        val nextEc = if (logo != QrCenterLogo.NONE && (_uiState.value.errorCorrection == "L" || _uiState.value.errorCorrection == "M")) {
            "Q"
        } else {
            _uiState.value.errorCorrection
        }
        _uiState.update { it.copy(centerLogo = logo, errorCorrection = nextEc) }
        generateCurrentQr(saveToHistory = false)
    }

    fun setErrorCorrection(ec: String) {
        _uiState.update { it.copy(errorCorrection = ec) }
        generateCurrentQr(saveToHistory = false)
    }

    fun setResolution(px: Int) {
        _uiState.update { it.copy(resolutionPx = px) }
        generateCurrentQr(saveToHistory = false)
    }

    fun setDownloadDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showDownloadDialog = visible) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun computeCurrentFormattedContent(): Pair<String, String> {
        val state = _uiState.value
        return when (state.qrType) {
            QrType.URL -> {
                val url = state.urlInput.trim()
                val finalUrl = if (url.isNotEmpty() && !url.startsWith("http://") && !url.startsWith("https://")) {
                    "https://$url"
                } else url
                (if (finalUrl.isNotEmpty()) finalUrl else "https://google.com") to (if (url.isNotEmpty()) url else "Website URL")
            }
            QrType.TEXT -> {
                val text = state.textInput
                text to (if (text.isNotEmpty()) text.take(30) else "Plain Text")
            }
            QrType.WIFI -> {
                val ssid = state.wifiSsid.trim()
                val pass = state.wifiPassword
                val type = state.wifiType
                val hidden = if (state.wifiHidden) "H:true;" else ""
                val formatted = "WIFI:S:$ssid;T:$type;P:$pass;$hidden;"
                formatted to (if (ssid.isNotEmpty()) "Wi-Fi: $ssid" else "Wi-Fi Network")
            }
            QrType.PHONE -> {
                val phone = state.phoneInput.trim()
                val formatted = "tel:$phone"
                formatted to (if (phone.isNotEmpty()) "Phone: $phone" else "Phone Number")
            }
            QrType.EMAIL -> {
                val to = state.emailTo.trim()
                val sub = Uri.encode(state.emailSubject.trim())
                val body = Uri.encode(state.emailBody)
                val params = buildList {
                    if (sub.isNotEmpty()) add("subject=$sub")
                    if (body.isNotEmpty()) add("body=$body")
                }.joinToString("&")
                val formatted = if (params.isNotEmpty()) "mailto:$to?$params" else "mailto:$to"
                formatted to (if (to.isNotEmpty()) "Email: $to" else "Email Address")
            }
            QrType.SMS -> {
                val phone = state.smsPhone.trim()
                val msg = state.smsMessage
                val formatted = "smsto:$phone:$msg"
                formatted to (if (phone.isNotEmpty()) "SMS: $phone" else "SMS Message")
            }
            QrType.VCARD -> {
                val first = state.vcardFirstName.trim()
                val last = state.vcardLastName.trim()
                val fullName = "$first $last".trim()
                val formatted = """
                    BEGIN:VCARD
                    VERSION:3.0
                    N:$last;$first;;;
                    FN:$fullName
                    ORG:${state.vcardOrg.trim()}
                    TEL;TYPE=CELL:${state.vcardPhone.trim()}
                    EMAIL:${state.vcardEmail.trim()}
                    END:VCARD
                """.trimIndent()
                formatted to (if (fullName.isNotEmpty()) "Contact: $fullName" else "Contact Card")
            }
        }
    }

    /**
     * Renders QR Code bitmap in background and optionally commits it to History.
     */
    fun generateCurrentQr(saveToHistory: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true) }
            val (content, title) = computeCurrentFormattedContent()
            val state = _uiState.value

            val bitmap = withContext(Dispatchers.Default) {
                val matrix = QrCodeGenerator.generateBitMatrix(
                    content = content,
                    errorCorrection = state.errorCorrection
                )
                if (matrix != null) {
                    QrCodeGenerator.renderBitmap(
                        matrix = matrix,
                        sizePx = state.resolutionPx,
                        fgColor = state.fgColorHex.toInt(),
                        bgColor = state.bgColorHex.toInt(),
                        dotStyle = state.dotStyle,
                        centerLogo = state.centerLogo
                    )
                } else null
            }

            _uiState.update {
                it.copy(
                    formattedContent = content,
                    qrBitmap = bitmap,
                    isGenerating = false
                )
            }

            if (saveToHistory && bitmap != null && content.isNotBlank()) {
                val historyItem = QrItem(
                    title = title,
                    content = content,
                    rawInput = when (state.qrType) {
                        QrType.URL -> state.urlInput
                        QrType.TEXT -> state.textInput
                        QrType.WIFI -> state.wifiSsid
                        QrType.PHONE -> state.phoneInput
                        QrType.EMAIL -> state.emailTo
                        QrType.SMS -> state.smsPhone
                        QrType.VCARD -> "${state.vcardFirstName} ${state.vcardLastName}"
                    },
                    qrType = state.qrType.name,
                    fgColorHex = state.fgColorHex,
                    bgColorHex = state.bgColorHex,
                    dotStyle = state.dotStyle.name,
                    centerLogo = state.centerLogo.name,
                    errorCorrection = state.errorCorrection
                )
                repository.saveQr(historyItem)
                showSnackbar("QR Code generated & saved to History!")
            }
        }
    }

    fun clearAllInputs() {
        _uiState.update {
            it.copy(
                urlInput = "",
                textInput = "",
                wifiSsid = "",
                wifiPassword = "",
                wifiHidden = false,
                phoneInput = "",
                emailTo = "",
                emailSubject = "",
                emailBody = "",
                smsPhone = "",
                smsMessage = "",
                vcardFirstName = "",
                vcardLastName = "",
                vcardPhone = "",
                vcardEmail = "",
                vcardOrg = ""
            )
        }
        generateCurrentQr(saveToHistory = false)
        showSnackbar("Fields cleared")
    }

    fun loadFromHistory(item: QrItem) {
        val type = try {
            QrType.valueOf(item.qrType)
        } catch (e: Exception) {
            QrType.URL
        }
        val dotStyle = try {
            QrDotStyle.valueOf(item.dotStyle)
        } catch (e: Exception) {
            QrDotStyle.SQUARE
        }
        val centerLogo = try {
            QrCenterLogo.valueOf(item.centerLogo)
        } catch (e: Exception) {
            QrCenterLogo.NONE
        }

        _uiState.update {
            it.copy(
                selectedTab = 0, // Switch to Generator
                qrType = type,
                fgColorHex = item.fgColorHex,
                bgColorHex = item.bgColorHex,
                dotStyle = dotStyle,
                centerLogo = centerLogo,
                errorCorrection = item.errorCorrection,
                // Assign raw inputs
                urlInput = if (type == QrType.URL) item.content else it.urlInput,
                textInput = if (type == QrType.TEXT) item.content else it.textInput,
                phoneInput = if (type == QrType.PHONE) item.content.removePrefix("tel:") else it.phoneInput
            )
        }
        generateCurrentQr(saveToHistory = false)
        showSnackbar("Loaded QR Code from history")
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch {
            repository.deleteQr(id)
            showSnackbar("Item removed from history")
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearHistory()
            showSnackbar("History cleared")
        }
    }

    fun copyContentToClipboard(context: Context) {
        val (content, _) = computeCurrentFormattedContent()
        if (content.isBlank()) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("QR Maker Content", content)
        clipboard.setPrimaryClip(clip)
        showSnackbar("Copied content to clipboard!")
    }

    fun shareQrCode(context: Context) {
        val bitmap = _uiState.value.qrBitmap ?: return
        val (content, title) = computeCurrentFormattedContent()
        QrCodeGenerator.shareQrCode(context, bitmap, "QR Code: $title\n$content")
    }

    fun exportPng(context: Context) {
        val bitmap = _uiState.value.qrBitmap ?: return
        val (_, title) = computeCurrentFormattedContent()
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20)
        val result = QrCodeGenerator.saveBitmapToDevice(
            context = context,
            bitmap = bitmap,
            format = Bitmap.CompressFormat.PNG,
            title = "QR_${safeTitle}"
        )
        if (result.isSuccess) {
            showSnackbar("Saved PNG to Pictures/QRMaker!")
        } else {
            showSnackbar("Failed to save PNG: ${result.exceptionOrNull()?.message}")
        }
    }

    fun exportJpg(context: Context) {
        val bitmap = _uiState.value.qrBitmap ?: return
        val (_, title) = computeCurrentFormattedContent()
        val safeTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20)
        val result = QrCodeGenerator.saveBitmapToDevice(
            context = context,
            bitmap = bitmap,
            format = Bitmap.CompressFormat.JPEG,
            title = "QR_${safeTitle}"
        )
        if (result.isSuccess) {
            showSnackbar("Saved JPG to Pictures/QRMaker!")
        } else {
            showSnackbar("Failed to save JPG: ${result.exceptionOrNull()?.message}")
        }
    }

    fun exportSvg(context: Context) {
        val (content, title) = computeCurrentFormattedContent()
        val state = _uiState.value
        val matrix = QrCodeGenerator.generateBitMatrix(content, state.errorCorrection) ?: return
        
        fun colorToHex(c: Long): String {
            val r = ((c shr 16) and 0xFF).toString(16).padStart(2, '0')
            val g = ((c shr 8) and 0xFF).toString(16).padStart(2, '0')
            val b = (c and 0xFF).toString(16).padStart(2, '0')
            return "#$r$g$b"
        }

        val svgString = QrCodeGenerator.generateSvg(
            matrix = matrix,
            fgColorHex = colorToHex(state.fgColorHex),
            bgColorHex = colorToHex(state.bgColorHex),
            dotStyle = state.dotStyle
        )

        val safeTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20)
        val result = QrCodeGenerator.saveSvgToDevice(
            context = context,
            svgString = svgString,
            title = "QR_${safeTitle}"
        )
        if (result.isSuccess) {
            showSnackbar("Saved Vector SVG to Downloads/QRMaker!")
        } else {
            showSnackbar("Failed to save SVG: ${result.exceptionOrNull()?.message}")
        }
    }
}
