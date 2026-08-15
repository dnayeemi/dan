package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Subject
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.QrType
import com.example.ui.QrUiState

@Composable
fun QrTypeSelector(
    selectedType: QrType,
    onSelectType: (QrType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QrType.values().forEach { type ->
            val isSelected = selectedType == type
            val icon = getIconForType(type)

            FilterChip(
                selected = isSelected,
                onClick = { onSelectType(type) },
                label = {
                    Text(
                        text = type.displayName,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(100.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                    selectedBorderColor = MaterialTheme.colorScheme.primary,
                    borderWidth = 1.dp
                ),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                modifier = Modifier.testTag("type_chip_${type.name.lowercase()}")
            )
        }
    }
}

private fun getIconForType(type: QrType): ImageVector {
    return when (type) {
        QrType.URL -> Icons.Default.Link
        QrType.TEXT -> Icons.Default.TextFields
        QrType.WIFI -> Icons.Default.Wifi
        QrType.PHONE -> Icons.Default.Phone
        QrType.EMAIL -> Icons.Default.Email
        QrType.SMS -> Icons.Default.Message
        QrType.VCARD -> Icons.Default.Person
    }
}

@Composable
fun QrTypeInputForms(
    uiState: QrUiState,
    onUpdateUrl: (String) -> Unit,
    onUpdateText: (String) -> Unit,
    onUpdateWifi: (ssid: String?, pass: String?, type: String?, hidden: Boolean?) -> Unit,
    onUpdatePhone: (String) -> Unit,
    onUpdateEmail: (to: String?, sub: String?, body: String?) -> Unit,
    onUpdateSms: (phone: String?, msg: String?) -> Unit,
    onUpdateVCard: (first: String?, last: String?, phone: String?, email: String?, org: String?) -> Unit,
    modifier: Modifier = Modifier
) {
    androidx.compose.material3.Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("qr_input_card"),
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
            Text(
                text = "Content Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            AnimatedContent(
                targetState = uiState.qrType,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "qr_input_form_animation"
            ) { targetType ->
                when (targetType) {
                    QrType.URL -> {
                        UrlForm(
                            url = uiState.urlInput,
                            onUrlChange = onUpdateUrl
                        )
                    }
                    QrType.TEXT -> {
                        TextForm(
                            text = uiState.textInput,
                            onTextChange = onUpdateText
                        )
                    }
                    QrType.WIFI -> {
                        WifiForm(
                            ssid = uiState.wifiSsid,
                            password = uiState.wifiPassword,
                            securityType = uiState.wifiType,
                            hidden = uiState.wifiHidden,
                            onUpdate = onUpdateWifi
                        )
                    }
                    QrType.PHONE -> {
                        PhoneForm(
                            phone = uiState.phoneInput,
                            onPhoneChange = onUpdatePhone
                        )
                    }
                    QrType.EMAIL -> {
                        EmailForm(
                            to = uiState.emailTo,
                            subject = uiState.emailSubject,
                            body = uiState.emailBody,
                            onUpdate = onUpdateEmail
                        )
                    }
                    QrType.SMS -> {
                        SmsForm(
                            phone = uiState.smsPhone,
                            message = uiState.smsMessage,
                            onUpdate = onUpdateSms
                        )
                    }
                    QrType.VCARD -> {
                        VCardForm(
                            firstName = uiState.vcardFirstName,
                            lastName = uiState.vcardLastName,
                            phone = uiState.vcardPhone,
                            email = uiState.vcardEmail,
                            org = uiState.vcardOrg,
                            onUpdate = onUpdateVCard
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun UrlForm(
    url: String,
    onUrlChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text("Website URL") },
            placeholder = { Text("https://example.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Uri,
                imeAction = ImeAction.Done
            ),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("url_input_field")
        )
    }
}

@Composable
private fun TextForm(
    text: String,
    onTextChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Plain Text") },
            placeholder = { Text("Type any message or note...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.TextFields,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            minLines = 3,
            maxLines = 6,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("text_input_field")
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${text.length} characters",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

@Composable
private fun WifiForm(
    ssid: String,
    password: String,
    securityType: String,
    hidden: Boolean,
    onUpdate: (ssid: String?, pass: String?, type: String?, hidden: Boolean?) -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = ssid,
            onValueChange = { onUpdate(it, null, null, null) },
            label = { Text("Network Name (SSID)") },
            placeholder = { Text("MyHomeWiFi") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wifi_ssid_input")
        )

        OutlinedTextField(
            value = password,
            onValueChange = { onUpdate(null, it, null, null) },
            label = { Text("Password") },
            placeholder = { Text("Wi-Fi network password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Password Visibility"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("wifi_password_input")
        )

        // Security Options
        Text(
            text = "Security Type",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        val securityOptions = listOf("WPA", "WEP", "nopass")
        val securityLabels = listOf("WPA/WPA2", "WEP", "None (Open)")
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            securityOptions.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = securityType == option,
                    onClick = { onUpdate(null, null, option, null) },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = securityOptions.size)
                ) {
                    Text(securityLabels[index], style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Hidden Network Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Hidden Network",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
                checked = hidden,
                onCheckedChange = { onUpdate(null, null, null, it) },
                modifier = Modifier.testTag("wifi_hidden_switch")
            )
        }
    }
}

@Composable
private fun PhoneForm(
    phone: String,
    onPhoneChange: (String) -> Unit
) {
    Column {
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number") },
            placeholder = { Text("+1 (555) 000-0000") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("phone_input_field")
        )
    }
}

@Composable
private fun EmailForm(
    to: String,
    subject: String,
    body: String,
    onUpdate: (to: String?, sub: String?, body: String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = to,
            onValueChange = { onUpdate(it, null, null) },
            label = { Text("Recipient Email") },
            placeholder = { Text("user@example.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_to_input")
        )

        OutlinedTextField(
            value = subject,
            onValueChange = { onUpdate(null, it, null) },
            label = { Text("Subject (Optional)") },
            placeholder = { Text("Inquiry / Hello") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Subject,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_subject_input")
        )

        OutlinedTextField(
            value = body,
            onValueChange = { onUpdate(null, null, it) },
            label = { Text("Message Body (Optional)") },
            placeholder = { Text("Write email content here...") },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("email_body_input")
        )
    }
}

@Composable
private fun SmsForm(
    phone: String,
    message: String,
    onUpdate: (phone: String?, msg: String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = phone,
            onValueChange = { onUpdate(it, null) },
            label = { Text("Phone Number") },
            placeholder = { Text("+1 (555) 123-4567") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sms_phone_input")
        )

        OutlinedTextField(
            value = message,
            onValueChange = { onUpdate(null, it) },
            label = { Text("Pre-filled Message") },
            placeholder = { Text("Hello from QR Maker...") },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sms_message_input")
        )
    }
}

@Composable
private fun VCardForm(
    firstName: String,
    lastName: String,
    phone: String,
    email: String,
    org: String,
    onUpdate: (first: String?, last: String?, phone: String?, email: String?, org: String?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { onUpdate(it, null, null, null, null) },
                label = { Text("First Name") },
                placeholder = { Text("John") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("vcard_first_name")
            )
            OutlinedTextField(
                value = lastName,
                onValueChange = { onUpdate(null, it, null, null, null) },
                label = { Text("Last Name") },
                placeholder = { Text("Doe") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("vcard_last_name")
            )
        }

        OutlinedTextField(
            value = phone,
            onValueChange = { onUpdate(null, null, it, null, null) },
            label = { Text("Phone") },
            placeholder = { Text("+1 (555) 000-0000") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vcard_phone")
        )

        OutlinedTextField(
            value = email,
            onValueChange = { onUpdate(null, null, null, it, null) },
            label = { Text("Email") },
            placeholder = { Text("john.doe@example.com") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vcard_email")
        )

        OutlinedTextField(
            value = org,
            onValueChange = { onUpdate(null, null, null, null, it) },
            label = { Text("Organization / Company") },
            placeholder = { Text("Tech Corp Inc.") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("vcard_org")
        )
    }
}
