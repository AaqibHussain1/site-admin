package com.aaqibhussain.admin

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.launch

private val CyberBg = Color(0xFF07090E)
private val CyberCard = Color(0xFF0B101D)
private val CyberCyan = Color(0xFF22D3EE)
private val CyberEmerald = Color(0xFF34D399)
private val CyberRose = Color(0xFFFB7185)
private val CyberText = Color(0xFFF1F5F9)
private val CyberMuted = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = CyberCyan,
                    background = CyberBg,
                    surface = CyberCard,
                    onPrimary = Color(0xFF020617),
                    onBackground = CyberText,
                    onSurface = CyberText
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = CyberBg
                ) {
                    SohnaThadluAdminApp()
                }
            }
        }
    }
}

enum class AdminModule(val label: String) {
    HERO("1. Hero & Metrics"),
    ABOUT("2. About & Bio"),
    JOURNEY("3. Journey Timeline"),
    SKILLS("4. Skills & Arsenal"),
    CERTS("5. Certifications"),
    PLATFORMS("6. Platforms & Logos"),
    SOCIALS("7. Social Links"),
    WRITEUPS("8. Write-up Studio"),
    INBOX_SECURITY("9. Inbox & Security")
}

@Composable
fun SohnaThadluAdminApp() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val apiClient = remember { AdminApiClient() }

    var serverUrl by remember { mutableStateOf(apiClient.baseUrl) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var operatorName by remember { mutableStateOf("") }
    var siteData by remember { mutableStateOf<SiteData?>(null) }
    var statusBanner by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    // Login state
    var loginUsername by remember { mutableStateOf("") }
    var loginPassword by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    // Active Module & Dropdown
    var activeModule by remember { mutableStateOf(AdminModule.WRITEUPS) }
    var moduleDropdownExpanded by remember { mutableStateOf(false) }

    // Write-up Popup Editor State
    var showWriteupDialog by remember { mutableStateOf(false) }
    var editingWriteupIndex by remember { mutableStateOf<Int?>(null) }
    var wuId by remember { mutableStateOf("") }
    var wuTitle by remember { mutableStateOf("") }
    var wuTarget by remember { mutableStateOf("") }
    var wuSeverity by remember { mutableStateOf("high") }
    var wuStatus by remember { mutableStateOf("Resolved") }
    var wuDate by remember { mutableStateOf("Disclosed & Patched") }
    var wuTags by remember { mutableStateOf("IDOR, API Security") }
    var wuSummary by remember { mutableStateOf("") }
    var wuMarkdown by remember { mutableStateOf("") }

    // File Picker for Server-Side Attachment Upload ({I24239})
    val attachmentPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isBusy = true
                statusBanner = "Uploading attachment to server..."
                try {
                    val mime = context.contentResolver.getType(uri) ?: "image/png"
                    val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    if (bytes != null) {
                        val b64 = Base64.encodeToString(bytes, Base64.NO_WRAP)
                        val result = apiClient.uploadAttachment("upload_${System.currentTimeMillis()}", mime, b64)
                        result.onSuccess { (att, updatedSite) ->
                            siteData = updatedSite
                            val tag = "{${att.id}}"
                            wuMarkdown = "$wuMarkdown\n\n$tag\n"
                            statusBanner = "Uploaded $tag to live server!"
                        }.onFailure { err ->
                            statusBanner = "Upload error: ${err.message}"
                        }
                    }
                } catch (e: Exception) {
                    statusBanner = "Upload failed: ${e.message}"
                } finally {
                    isBusy = false
                }
            }
        }
    }

    if (!isAuthenticated || siteData == null) {
        // Cyber Admin Login Screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard, RoundedCornerShape(16.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Sohna Thadlu Admin App",
                    color = CyberText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Live Website & Cloud Firestore Command Node",
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                OutlinedTextField(
                    value = serverUrl,
                    onValueChange = {
                        serverUrl = it
                        apiClient.baseUrl = it
                    },
                    label = { Text("Live Website URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = loginUsername,
                    onValueChange = { loginUsername = it },
                    label = { Text("Operator Username") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = loginPassword,
                    onValueChange = { loginPassword = it },
                    label = { Text("Passphrase") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (loginError != null) {
                    Text(
                        text = loginError!!,
                        color = CyberRose,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Button(
                    onClick = {
                        scope.launch {
                            isBusy = true
                            loginError = null
                            apiClient.baseUrl = serverUrl
                            val res = apiClient.login(loginUsername.trim(), loginPassword)
                            isBusy = false
                            res.onSuccess { (user, data) ->
                                operatorName = user
                                siteData = data
                                isAuthenticated = true
                                statusBanner = "Connected to live website & Firestore"
                            }.onFailure { err ->
                                loginError = err.message ?: "Authentication failed"
                            }
                        }
                    },
                    enabled = !isBusy,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                ) {
                    Text(
                        text = if (isBusy) "Authenticating..." else "Authenticate Session",
                        color = Color(0xFF020617),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        return
    }

    val currentData = siteData!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBg)
    ) {
        // Top Command Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF080B12))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CMS Command Center",
                    color = CyberText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Operator: $operatorName · Live Sync",
                    color = CyberEmerald,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        scope.launch {
                            isBusy = true
                            statusBanner = "Publishing to live website..."
                            val res = apiClient.publishSiteData(currentData)
                            isBusy = false
                            res.onSuccess {
                                siteData = it
                                statusBanner = "Published live to website & Firestore!"
                            }.onFailure {
                                statusBanner = "Sync error: ${it.message}"
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text("Publish", color = Color(0xFF020617), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        apiClient.sessionToken = null
                        isAuthenticated = false
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("Logout", color = CyberRose, fontSize = 12.sp)
                }
            }
        }

        if (statusBanner != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D1829))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = statusBanner!!,
                    color = CyberCyan,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Single Mobile Dropdown Selector
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard, RoundedCornerShape(12.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .clickable { moduleDropdownExpanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Module: ${activeModule.label}",
                    color = CyberText,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text("▼", color = CyberCyan, fontSize = 12.sp)
            }

            DropdownMenu(
                expanded = moduleDropdownExpanded,
                onDismissRequest = { moduleDropdownExpanded = false },
                modifier = Modifier.background(CyberCard)
            ) {
                AdminModule.entries.forEach { mod ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = mod.label,
                                color = if (mod == activeModule) CyberCyan else CyberText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            )
                        },
                        onClick = {
                            activeModule = mod
                            moduleDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Main Module Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            when (activeModule) {
                AdminModule.WRITEUPS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Write-ups (${currentData.writeups.size})",
                                color = CyberText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Button(
                                onClick = {
                                    editingWriteupIndex = null
                                    wuId = "wu-${System.currentTimeMillis()}"
                                    wuTitle = ""
                                    wuTarget = ""
                                    wuSeverity = "high"
                                    wuStatus = "Resolved"
                                    wuDate = "Disclosed & Patched"
                                    wuTags = "IDOR, API Security"
                                    wuSummary = ""
                                    wuMarkdown = "## Executive Summary\n\n## Proof of Concept\n"
                                    showWriteupDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                            ) {
                                Text("+ New Write-up", color = Color(0xFF020617), fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    itemsIndexed(currentData.writeups) { idx, wu ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCard, RoundedCornerShape(14.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${wu.severity.uppercase()} · ${wu.targetApp}",
                                    color = CyberCyan,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = wu.status,
                                    color = CyberEmerald,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(
                                text = wu.title,
                                color = CyberText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = wu.summary,
                                color = CyberMuted,
                                fontSize = 13.sp
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(onClick = {
                                    editingWriteupIndex = idx
                                    wuId = wu.id
                                    wuTitle = wu.title
                                    wuTarget = wu.targetApp
                                    wuSeverity = wu.severity
                                    wuStatus = wu.status
                                    wuDate = wu.dateDisclosed
                                    wuTags = wu.tags.joinToString(", ")
                                    wuSummary = wu.summary
                                    wuMarkdown = wu.markdownContent
                                    showWriteupDialog = true
                                }) {
                                    Text("Edit", color = CyberCyan)
                                }
                                TextButton(onClick = {
                                    scope.launch {
                                        val updatedList = currentData.writeups.filterIndexed { i, _ -> i != idx }
                                        val next = currentData.copy(writeups = updatedList)
                                        apiClient.publishSiteData(next).onSuccess {
                                            siteData = it
                                            statusBanner = "Deleted write-up from live website"
                                        }
                                    }
                                }) {
                                    Text("Delete", color = CyberRose)
                                }
                            }
                        }
                    }
                }

                AdminModule.INBOX_SECURITY -> {
                    item {
                        Text(
                            text = "Encrypted Dispatch Inbox (${currentData.inbox.size})",
                            color = CyberText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    itemsIndexed(currentData.inbox) { _, msg ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCard, RoundedCornerShape(14.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("${msg.name} <${msg.email}>", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Subject: ${msg.subject}", color = CyberEmerald, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                            Text(msg.message, color = CyberText, fontSize = 13.sp)
                        }
                    }
                }

                else -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(CyberCard, RoundedCornerShape(14.dp))
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = activeModule.label,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Connected to live website (${currentData.updatedAt}). Tap Publish in the top bar after editing to broadcast changes immediately.",
                                color = CyberMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Popup Modal for New / Edit Write-up + Attachment Vault ({I24239})
    if (showWriteupDialog) {
        Dialog(onDismissRequest = { showWriteupDialog = false }) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberCard, RoundedCornerShape(16.dp))
                    .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = if (editingWriteupIndex == null) "New Write-up" else "Edit Write-up",
                        color = CyberText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                item {
                    OutlinedTextField(
                        value = wuTitle,
                        onValueChange = { wuTitle = it },
                        label = { Text("Write-up Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = wuId,
                        onValueChange = { wuId = it },
                        label = { Text("Slug URL ID (e.g. idor-bounty-01)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = wuTarget,
                        onValueChange = { wuTarget = it },
                        label = { Text("Target App / Scope") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = wuSummary,
                        onValueChange = { wuSummary = it },
                        label = { Text("Executive Summary") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    // Server-Side Attachment Vault Box (Only inside New/Edit Write-up Modal)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberBg, RoundedCornerShape(12.dp))
                            .border(1.dp, CyberCyan.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Server-Side Attachment Vault ({I24239})",
                            color = CyberCyan,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { attachmentPicker.launch("*/*") },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Upload Attachment from Phone", color = Color(0xFF020617), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = wuMarkdown,
                        onValueChange = { wuMarkdown = it },
                        label = { Text("Markdown Content (Use {I24239} for attachments)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showWriteupDialog = false }) {
                            Text("Cancel", color = CyberMuted)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                scope.launch {
                                    val newItem = WriteupItem(
                                        id = wuId.trim().ifEmpty { "wu-${System.currentTimeMillis()}" },
                                        title = wuTitle.trim(),
                                        targetApp = wuTarget.trim().ifEmpty { "Web Application" },
                                        severity = wuSeverity,
                                        status = wuStatus,
                                        summary = wuSummary.trim(),
                                        fullDescription = "",
                                        stepsToReproduce = emptyList(),
                                        requestPayloads = emptyList(),
                                        impact = "",
                                        recommendation = "",
                                        tags = wuTags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                        featured = true,
                                        order = currentData.writeups.size + 1,
                                        dateDisclosed = wuDate,
                                        markdownContent = wuMarkdown
                                    )
                                    val updatedList = currentData.writeups.toMutableList()
                                    if (editingWriteupIndex == null) {
                                        updatedList.add(0, newItem)
                                    } else {
                                        updatedList[editingWriteupIndex!!] = newItem
                                    }
                                    val nextData = currentData.copy(writeups = updatedList)
                                    val res = apiClient.publishSiteData(nextData)
                                    res.onSuccess {
                                        siteData = it
                                        showWriteupDialog = false
                                        statusBanner = "Published Write-up to live website!"
                                    }.onFailure {
                                        statusBanner = "Error: ${it.message}"
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                        ) {
                            Text("Publish Write-up", color = Color(0xFF020617), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
