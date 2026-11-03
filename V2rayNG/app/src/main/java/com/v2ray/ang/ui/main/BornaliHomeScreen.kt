package com.v2ray.ang.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.v2ray.ang.R

private val BornaliGreen = Color(0xFF00A651)
private val BornaliLightGreen = Color(0xFFBDE8CD)
private val BornaliPink = Color(0xFFE8AEB0)
private val BornaliRed = Color(0xFFE53935)

/**
 * Bornali-style simple home screen, on top of the v2rayNG engine.
 *
 * What is real here: START/STOP uses the existing v2ray service,
 * LOCATION lists the profiles already added to the app (via + /
 * Subscriptions), + opens Subscriptions, edit/delete act on the
 * selected profile, UPDATES opens the update check, EXIT closes.
 *
 * What is display-only for now (no panel backend yet, no VPS yet):
 * the username/password fields, the NETWORK choice and the Custom
 * Tweaks values. They are shown so the layout matches Bornali, but
 * they do not change the connection until a server panel exists.
 */
@Composable
fun BornaliHomeScreen(
    mainViewModel: MainViewModel,
    onAction: (MainAction) -> Unit,
    onNavigate: (MainDestination) -> Unit,
    onExit: () -> Unit,
) {
    val uiState by mainViewModel.uiState.collectAsStateWithLifecycle()
    val groupState by mainViewModel.serverGroupState(uiState.selectedGroupId)
        .collectAsStateWithLifecycle()
    val selectedProfile = groupState.rows.firstOrNull { it.guid == uiState.selectedGuid }
    val isRunning = uiState.isRunning

    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var networkChoice by rememberSaveable { mutableStateOf("WiFi BDIX Bypass") }
    var customMode by rememberSaveable { mutableStateOf(false) }
    var showServerDialog by remember { mutableStateOf(false) }
    var showNetworkDialog by remember { mutableStateOf(false) }
    var showTweaksDialog by remember { mutableStateOf(false) }

    // Custom Tweaks values (kept on screen, not yet sent to any panel).
    var tweakMode by rememberSaveable { mutableStateOf("OVPN") }
    var tweakConnection by rememberSaveable { mutableStateOf("Direct Connection") }
    var tweakType by rememberSaveable { mutableStateOf("CF") }
    var tweakName by rememberSaveable { mutableStateOf("") }
    var tweakInfo by rememberSaveable { mutableStateOf("") }
    var frontQuery by rememberSaveable { mutableStateOf("") }
    var backQuery by rememberSaveable { mutableStateOf("") }
    var payload by rememberSaveable { mutableStateOf("") }
    var useDefaultProxy by rememberSaveable { mutableStateOf(true) }
    var proxyHost by rememberSaveable { mutableStateOf("") }
    var proxyPort by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(16.dp))
        // Logo: green circle with S, then the app name, like Bornali's
        // circular badge + name at the top.
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(BornaliGreen)
                .border(3.dp, BornaliLightGreen, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text("S", color = Color.White, fontSize = 52.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Text("System VPN", color = BornaliGreen, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .height(1.dp)
                .background(BornaliLightGreen)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(if (isRunning) "↓ —" else "↓ 0 KB", fontSize = 13.sp)
            Text("V2RAY", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(if (isRunning) "↑ —" else "↑ 0 KB", fontSize = 13.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            if (isRunning) "Connected" else "Disconnected",
            color = if (isRunning) BornaliGreen else BornaliRed,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
        )
        Text("Config Version : 2.3.15", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        // LOCATION card
        BornaliCard(label = "LOCATION", onClick = { showServerDialog = true }) {
            Text("⌖", color = BornaliGreen, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    selectedProfile?.remarks ?: "Auto Select Servers",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                )
                Text("System VPN", color = BornaliGreen, fontSize = 12.sp)
            }
            Icon(
                painterResource(R.drawable.ic_expand_more_24dp),
                contentDescription = null,
                tint = Color.Gray,
            )
        }
        Spacer(Modifier.height(12.dp))

        // NETWORK card
        BornaliCard(label = "NETWORK", onClick = { showNetworkDialog = true }) {
            Text("◉", color = BornaliGreen, fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(networkChoice, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    if (customMode) "Custom 1" else "Good Working",
                    color = BornaliGreen,
                    fontSize = 12.sp,
                )
            }
            Icon(
                painterResource(R.drawable.ic_expand_more_24dp),
                contentDescription = null,
                tint = Color.Gray,
            )
        }
        Spacer(Modifier.height(12.dp))

        // Username + expiry row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, BornaliGreen, RoundedCornerShape(24.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(48.dp)
                    .background(BornaliGreen),
                contentAlignment = Alignment.Center,
            ) {
                Text("ID", color = Color.White, fontWeight = FontWeight.Bold)
            }
            BasicTextField(
                value = username,
                onValueChange = { username = it },
                singleLine = true,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                decorationBox = { inner ->
                    if (username.isEmpty()) Text("Username", color = Color.Gray)
                    inner()
                },
            )
            Text(
                "Expiry: --/--/--",
                color = BornaliGreen,
                fontSize = 12.sp,
                modifier = Modifier.padding(end = 12.dp),
            )
        }
        Spacer(Modifier.height(12.dp))

        // Password row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, BornaliGreen, RoundedCornerShape(24.dp)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(48.dp)
                    .background(BornaliGreen),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painterResource(R.drawable.ic_lock_24dp),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp),
                )
            }
            BasicTextField(
                value = password,
                onValueChange = { password = it },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp),
                decorationBox = { inner ->
                    if (password.isEmpty()) Text("Password", color = Color.Gray)
                    inner()
                },
            )
            Text(
                if (passwordVisible) "Hide" else "Show",
                color = BornaliGreen,
                fontSize = 12.sp,
                modifier = Modifier
                    .padding(end = 12.dp)
                    .clickable { passwordVisible = !passwordVisible },
            )
        }
        Spacer(Modifier.height(24.dp))

        // Big START / STOP circle
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(if (isRunning) BornaliGreen else BornaliPink)
                .clickable { onAction(MainAction.ToggleService) },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(168.dp)
                    .clip(CircleShape)
                    .background(if (isRunning) BornaliGreen else Color.White)
                    .border(2.dp, if (isRunning) Color.White else BornaliPink, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (isRunning) "STOP" else "START",
                    color = if (isRunning) Color.White else BornaliGreen,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        // + | edit | delete | Custom toggle
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(R.drawable.ic_add_24dp),
                contentDescription = "Add subscription",
                tint = BornaliGreen,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onNavigate(MainDestination.Subscriptions) },
            )
            Text("  |  ", color = Color.Gray)
            Icon(
                painterResource(R.drawable.ic_edit_24dp),
                contentDescription = "Edit server",
                tint = BornaliGreen,
                modifier = Modifier
                    .size(26.dp)
                    .clickable {
                        selectedProfile?.let {
                            onAction(MainAction.EditServer(it.guid, it.profile))
                        }
                    },
            )
            Text("  |  ", color = Color.Gray)
            Icon(
                painterResource(R.drawable.ic_delete_24dp),
                contentDescription = "Delete server",
                tint = BornaliGreen,
                modifier = Modifier
                    .size(26.dp)
                    .clickable {
                        selectedProfile?.let { onAction(MainAction.RemoveServer(it.guid)) }
                    },
            )
            Text("  |  ", color = Color.Gray)
            Text("Custom", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.width(6.dp))
            Switch(
                checked = customMode,
                onCheckedChange = {
                    customMode = it
                    networkChoice = if (it) "Custom Payload" else "WiFi BDIX Bypass"
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BornaliGreen,
                    checkedTrackColor = BornaliLightGreen,
                ),
            )
        }
        Spacer(Modifier.height(32.dp))

        // Bottom buttons: UPDATES | TWEAKS | EXIT
        Row(modifier = Modifier.fillMaxWidth()) {
            BornaliBottomButton(
                label = "UPDATES",
                iconRes = R.drawable.ic_check_update_24dp,
                modifier = Modifier.weight(1f),
                onClick = { onNavigate(MainDestination.CheckUpdate) },
            )
            BornaliBottomButton(
                label = "TWEAKS",
                iconRes = R.drawable.ic_settings_24dp,
                modifier = Modifier.weight(1f),
                onClick = { showTweaksDialog = true },
            )
            BornaliBottomButton(
                label = "EXIT",
                iconRes = R.drawable.ic_arrow_back_24dp,
                modifier = Modifier.weight(1f),
                onClick = onExit,
            )
        }
        Spacer(Modifier.height(12.dp))
    }

    if (showServerDialog) {
        ServerPickDialog(
            rows = groupState.rows,
            onClose = { showServerDialog = false },
            onPick = { guid ->
                showServerDialog = false
                onAction(MainAction.SelectServer(guid))
            },
            onAddSubscription = {
                showServerDialog = false
                onNavigate(MainDestination.Subscriptions)
            },
        )
    }
    if (showNetworkDialog) {
        NetworkPickDialog(
            current = networkChoice,
            onClose = { showNetworkDialog = false },
            onPick = { picked ->
                networkChoice = picked
                showNetworkDialog = false
            },
        )
    }
    if (showTweaksDialog) {
        CustomTweaksDialog(
            mode = tweakMode,
            onModeChange = { tweakMode = it },
            connection = tweakConnection,
            onConnectionChange = { tweakConnection = it },
            type = tweakType,
            onTypeChange = { tweakType = it },
            name = tweakName,
            onNameChange = { tweakName = it },
            info = tweakInfo,
            onInfoChange = { tweakInfo = it },
            frontQuery = frontQuery,
            onFrontQueryChange = { frontQuery = it },
            backQuery = backQuery,
            onBackQueryChange = { backQuery = it },
            payload = payload,
            onPayloadChange = { payload = it },
            useDefaultProxy = useDefaultProxy,
            onUseDefaultProxyChange = { useDefaultProxy = it },
            proxyHost = proxyHost,
            onProxyHostChange = { proxyHost = it },
            proxyPort = proxyPort,
            onProxyPortChange = { proxyPort = it },
            onClose = { showTweaksDialog = false },
        )
    }
}

@Composable
private fun BornaliCard(
    label: String,
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BornaliGreen, RoundedCornerShape(12.dp))
            .padding(4.dp)
            .clickable { onClick() },
    ) {
        Column {
            Text(
                label,
                color = BornaliGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, top = 2.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .border(1.dp, BornaliLightGreen, RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = content,
            )
        }
    }
}

@Composable
private fun RowScope.BornaliBottomButton(
    label: String,
    iconRes: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .border(1.dp, BornaliGreen, RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painterResource(iconRes),
            contentDescription = null,
            tint = BornaliGreen,
            modifier = Modifier.size(24.dp),
        )
        Spacer(Modifier.height(4.dp))
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun ServerPickDialog(
    rows: List<ServerRowUiModel>,
    onClose: () -> Unit,
    onPick: (String) -> Unit,
    onAddSubscription: () -> Unit,
) {
    var query by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search Here") },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_search_24dp),
                            contentDescription = null,
                            tint = Color.Gray,
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                )
                Spacer(Modifier.height(12.dp))
                BornaliServerRow(
                    title = "Auto Select Servers",
                    subtitle = "System VPN",
                    badge = "Auto",
                    onClick = {
                        val first = rows.firstOrNull()
                        if (first != null) onPick(first.guid) else onClose()
                    },
                )
                val filtered = rows.filter {
                    it.remarks.contains(query, ignoreCase = true) || query.isBlank()
                }
                if (filtered.isEmpty()) {
                    Text(
                        "No servers yet. Add a subscription first.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                    TextButton(onClick = onAddSubscription) {
                        Text("Add subscription", color = BornaliGreen)
                    }
                }
                filtered.forEach { row ->
                    BornaliServerRow(
                        title = row.remarks,
                        subtitle = row.typeDescription,
                        badge = null,
                        onClick = { onPick(row.guid) },
                    )
                }
                Spacer(Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(BornaliGreen)
                            .clickable { onClose() }
                            .padding(horizontal = 32.dp, vertical = 12.dp),
                    ) {
                        Text("CLOSE", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun BornaliServerRow(
    title: String,
    subtitle: String,
    badge: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, BornaliGreen, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BornaliLightGreen),
            contentAlignment = Alignment.Center,
        ) {
            Text("🌐", fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, color = BornaliGreen, fontSize = 12.sp)
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(BornaliGreen)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
            ) {
                Text(badge, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NetworkPickDialog(
    current: String,
    onClose: () -> Unit,
    onPick: (String) -> Unit,
) {
    val options = listOf(
        "WiFi BDIX Bypass",
        "Custom Payload",
        "Direct Connection",
        "Auto",
    )
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("NETWORK", color = BornaliGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                options.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPick(option) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(
                            selected = option == current,
                            onClick = { onPick(option) },
                            colors = RadioButtonDefaults.colors(selectedColor = BornaliGreen),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(option, fontSize = 15.sp)
                    }
                }
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onClose) {
                        Text("CLOSE", color = BornaliGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomTweaksDialog(
    mode: String,
    onModeChange: (String) -> Unit,
    connection: String,
    onConnectionChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    info: String,
    onInfoChange: (String) -> Unit,
    frontQuery: String,
    onFrontQueryChange: (String) -> Unit,
    backQuery: String,
    onBackQueryChange: (String) -> Unit,
    payload: String,
    onPayloadChange: (String) -> Unit,
    useDefaultProxy: Boolean,
    onUseDefaultProxyChange: (Boolean) -> Unit,
    proxyHost: String,
    onProxyHostChange: (String) -> Unit,
    proxyPort: String,
    onProxyPortChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    var showConnectionMenu by remember { mutableStateOf(false) }
    val connectionOptions = listOf(
        "Direct Connection",
        "Direct SSL/TLS",
        "HTTP Proxy",
        "SSL/TLS Payload",
        "SSL/TLS Proxy + Payload",
    )
    Dialog(onDismissRequest = onClose) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text("Custom Tweaks", color = BornaliRed, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(12.dp))
                Row {
                    listOf("OVPN", "UDP", "V2ray").forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onModeChange(option) },
                        ) {
                            RadioButton(
                                selected = mode == option,
                                onClick = { onModeChange(option) },
                                colors = RadioButtonDefaults.colors(selectedColor = BornaliGreen),
                            )
                            Text(option, fontSize = 13.sp)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showConnectionMenu = true }
                            .padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(connection, fontSize = 15.sp)
                        Text("▾", color = Color.Gray)
                    }
                    DropdownMenu(
                        expanded = showConnectionMenu,
                        onDismissRequest = { showConnectionMenu = false },
                    ) {
                        connectionOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onConnectionChange(option)
                                    showConnectionMenu = false
                                },
                            )
                        }
                    }
                }
                Row {
                    listOf("CF", "WS", "HTTP").forEach { option ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onTypeChange(option) },
                        ) {
                            RadioButton(
                                selected = type == option,
                                onClick = { onTypeChange(option) },
                                colors = RadioButtonDefaults.colors(selectedColor = BornaliGreen),
                            )
                            Text(option, fontSize = 13.sp)
                        }
                    }
                }
                OutlinedTextField(value = name, onValueChange = onNameChange, label = { Text("Tweak Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = info, onValueChange = onInfoChange, label = { Text("Tweak Info") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = frontQuery, onValueChange = onFrontQueryChange, label = { Text("Front Query") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = backQuery, onValueChange = onBackQueryChange, label = { Text("Back Query") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = payload,
                        onValueChange = onPayloadChange,
                        label = { Text("Payload") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "+",
                        color = BornaliGreen,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Use default server and squid port", fontSize = 13.sp, modifier = Modifier.weight(1f))
                    Switch(
                        checked = useDefaultProxy,
                        onCheckedChange = onUseDefaultProxyChange,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = BornaliGreen,
                            checkedTrackColor = BornaliLightGreen,
                        ),
                    )
                }
                Row {
                    OutlinedTextField(
                        value = proxyHost,
                        onValueChange = onProxyHostChange,
                        label = { Text("Proxy") },
                        placeholder = { Text("[Default]") },
                        singleLine = true,
                        modifier = Modifier.weight(2f),
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = proxyPort,
                        onValueChange = onProxyPortChange,
                        label = { Text("Port") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onClose) {
                        Text("CANCEL", color = BornaliGreen, fontWeight = FontWeight.Bold)
                    }
                    TextButton(onClick = onClose) {
                        Text("SAVE", color = BornaliGreen, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
