package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class QrType(val displayName: String, val iconRes: String) {
    URL("Website URL", "link"),
    TEXT("Plain Text", "text_fields"),
    WIFI("Wi-Fi Network", "wifi"),
    PHONE("Phone Number", "call"),
    EMAIL("Email Address", "email"),
    SMS("SMS Message", "sms"),
    VCARD("Contact Card", "person")
}

enum class QrDotStyle(val displayName: String) {
    SQUARE("Classic Square"),
    ROUNDED("Smooth Rounded"),
    DOTS("Circular Dots")
}

enum class QrCenterLogo(val displayName: String, val id: String) {
    NONE("None", "none"),
    LINK("Web Link", "link"),
    WIFI("Wi-Fi", "wifi"),
    PHONE("Phone", "phone"),
    EMAIL("Email", "email"),
    STAR("Star Badge", "star"),
    HEART("Heart", "heart"),
    SHIELD("Security", "shield"),
    INFO("Info", "info")
}

@Entity(tableName = "qr_history")
data class QrItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val content: String,
    val rawInput: String = "",
    val qrType: String = QrType.URL.name,
    val fgColorHex: Long = 0xFF000000,
    val bgColorHex: Long = 0xFFFFFFFF,
    val dotStyle: String = QrDotStyle.SQUARE.name,
    val centerLogo: String = QrCenterLogo.NONE.name,
    val errorCorrection: String = "M",
    val timestamp: Long = System.currentTimeMillis()
)
