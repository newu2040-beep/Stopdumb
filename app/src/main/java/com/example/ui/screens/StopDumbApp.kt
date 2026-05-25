package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.StopDumbTheme
import com.example.ui.viewmodel.StopDumbViewModel
import coil.compose.rememberAsyncImagePainter
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.PI
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StopDumbApp(viewModel: StopDumbViewModel) {
    val settings by viewModel.globalSettings.collectAsStateWithLifecycle()
    val appBlocks by viewModel.appBlocks.collectAsStateWithLifecycle()
    val appUsages by viewModel.appUsages.collectAsStateWithLifecycle()
    val focusSessions by viewModel.focusSessions.collectAsStateWithLifecycle()
    val habits by viewModel.habits.collectAsStateWithLifecycle()
    val wellnessLogs by viewModel.wellnessLogs.collectAsStateWithLifecycle()
    val blockedAttempts by viewModel.blockedAttempts.collectAsStateWithLifecycle()

    StopDumbTheme(
        selectedTheme = settings.currentTheme,
        isDark = true, // Force eye-pleasing deep look by default
        isAmoled = settings.isAmoledBlack
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primary,
                                                    MaterialTheme.colorScheme.tertiary
                                                )
                                            ),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Block,
                                        contentDescription = "StopDumb Logo icon",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Text(
                                    text = "StopDumb",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            titleContentColor = MaterialTheme.colorScheme.onBackground
                        ),
                        actions = {
                            Row(
                                modifier = Modifier
                                    .padding(end = 12.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.activeTab = "Themes" }
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Eco,
                                    contentDescription = "Streak Icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${settings.currentTheme}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    )
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                        tonalElevation = 8.dp,
                        modifier = Modifier
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .testTag("app_navigation")
                    ) {
                        val tabs = listOf(
                            Triple("Dashboard", Icons.Default.Analytics, "Dashboard"),
                            Triple("Distraction", Icons.Default.Block, "Blocks"),
                            Triple("Productivity", Icons.Default.Timer, "Focus"),
                            Triple("Wellness", Icons.Default.SelfImprovement, "Wellness"),
                            Triple("Themes", Icons.Default.Palette, "Themes")
                        )
                        tabs.forEach { (tabName, icon, label) ->
                            val selected = viewModel.activeTab == tabName
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.activeTab = tabName },
                                icon = {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = tabName,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                    unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (viewModel.activeTab) {
                        "Dashboard" -> DashboardTab(viewModel, appUsages, focusSessions, settings)
                        "Distraction" -> DistractionTab(viewModel, appBlocks, blockedAttempts, settings)
                        "Productivity" -> ProductivityTab(viewModel, habits, focusSessions)
                        "Wellness" -> WellnessTab(viewModel, wellnessLogs, habits)
                        "Themes" -> ThemesTab(viewModel, settings)
                    }

                    // Render Sub-Screens
                    AnimatedVisibility(
                        visible = viewModel.activeSubScreen == "DailyAnalysis",
                        enter = slideInVertically { it } + fadeIn(),
                        exit = slideOutVertically { it } + fadeOut()
                    ) {
                        DailyAnalysisScreen(viewModel)
                    }

                    // Render Simulation Dialogs if triggered
                    SimulationDialogsRenderer(viewModel)
                }
            }
        }
    }
}

// ==================== DASHBOARD TAB ====================
@Composable
fun DashboardTab(
    viewModel: StopDumbViewModel,
    usages: List<AppUsageRecord>,
    focusSessions: List<FocusSessionRecord>,
    settings: GlobalWellnessSettings
) {
    val analysis by viewModel.dailyAnalysis.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Hero Score Card (Bento Style)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable { viewModel.activeSubScreen = "DailyAnalysis" },
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Decorative Canvas elements for waves
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val path = Path().apply {
                            moveTo(0f, size.height * 0.7f)
                            quadraticBezierTo(size.width * 0.25f, size.height * 0.5f, size.width * 0.5f, size.height * 0.7f)
                            quadraticBezierTo(size.width * 0.75f, size.height * 0.9f, size.width, size.height * 0.7f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(path, Color.White.copy(alpha = 0.1f))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    "DAILY FOCUS SCORE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White.copy(alpha = 0.8f),
                                    letterSpacing = 1.sp
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        "${analysis.focusScore}",
                                        fontSize = 56.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Text(
                                        "/100",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(bottom = 10.dp, start = 4.dp)
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF49E191), CircleShape)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        "DEEP FLOW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        // Mini Wave Indicator mock
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            val heights = listOf(0.4f, 0.6f, 0.85f, 0.7f, 0.45f, 0.55f, 0.95f, 0.3f)
                            heights.forEach { h ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(h)
                                        .background(
                                            Color.White.copy(alpha = 0.4f + (h * 0.5f)),
                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bento Grid Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Large Screen Time Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "TOTAL SCREEN TIME",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f)
                            )
                            val hours = analysis.totalTimeMs / (1000 * 3600)
                            val mins = (analysis.totalTimeMs % (1000 * 3600)) / (1000 * 60)
                            Text(
                                "${hours}h ${mins}m",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Box(
                                modifier = Modifier
                                    .padding(top = 4.dp)
                                    .background(
                                        MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                                        CircleShape
                                    )
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "-14% from yesterday",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .border(
                                    width = 6.dp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.1f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = 0.82f,
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 6.dp,
                                strokeCap = StrokeCap.Round,
                                trackColor = Color.Transparent
                            )
                            Text(
                                "82%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // Two Small Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Pickups
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp)
                            .clickable { viewModel.triggerPhysicalInteraction("pickup") },
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFDDE2EA))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "PICKUPS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF43474E)
                            )
                            Text(
                                "${settings.totalPickupsToday}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF1A1C1E)
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.TrendingUp,
                                    null,
                                    tint = Color(0xFFBA1A1A),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "High Frequency",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBA1A1A)
                                )
                            }
                        }
                    }

                    // Top Sink
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(130.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFDDE2EA))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "TOP SINK",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF43474E)
                            )
                            Column {
                                Text(
                                    analysis.topSinkName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1A1C1E),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val sinkMins = analysis.topSinkTimeMs / (1000 * 60)
                                Text(
                                    "${sinkMins}m today",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Box(
                                Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(Color(0xFFE1E2E9), CircleShape)
                            ) {
                                Box(
                                    Modifier
                                        .fillMaxWidth(0.7f)
                                        .fillMaxHeight()
                                        .background(Color(0xFFBA1A1A), CircleShape)
                                )
                            }
                        }
                    }
                }

                // Large Action button
                Button(
                    onClick = { viewModel.activeTab = "Productivity"; viewModel.activeSubScreen = "PomodoroTimer" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1C1E))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color(0xFF49E191), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.PlayArrow,
                                null,
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            "Start Deep Work Session",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                // Digital Health Analysis Section (New)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, Color(0xFFE1E2E9))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Devices, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Text(
                                "REAL-TIME DEVICE ENGINE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("DEVICE STATUS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}", fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("OS VERSION", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("Android ${android.os.Build.VERSION.RELEASE} (API ${android.os.Build.VERSION.SDK_INT})", fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("APP UPTIME", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("${(System.currentTimeMillis() / 1000 / 60) % 60}m 14s active", fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("SECURITY LEVEL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                                Text("Strict / Protected", fontSize = 13.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }

                // Recent Activity Feed
                Text(
                    "LIVE ACTIVITY FEED",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                usages.take(4).forEach { usage ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, Color(0xFFF1F2F9))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (usage.category == "Productivity") Icons.Default.CheckCircle else Icons.Default.Launch,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(usage.appName, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                Text("${usage.category} • Just now", fontSize = 10.sp, color = Color.Gray)
                            }
                            Text(
                                text = "+${(usage.durationMs / 1000 / 60)}m",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (usage.category == "Productivity") Color(0xFF49E191) else Color(0xFFBA1A1A)
                            )
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val icon = remember(packageName) {
        try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: Exception) {
            null
        }
    }
    
    if (icon != null) {
        androidx.compose.foundation.Image(
            painter = rememberAsyncImagePainter(icon),
            contentDescription = null,
            modifier = modifier.clip(RoundedCornerShape(8.dp))
        )
    } else {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Apps, null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        }
    }
}

// ==================== DISTRACTION TAB ====================
@Composable
fun DistractionTab(
    viewModel: StopDumbViewModel,
    blocks: List<AppBlockConfig>,
    recentAttempts: List<BlockedAttemptRecord>,
    settings: GlobalWellnessSettings
) {
    val installedApps by viewModel.installedApps.collectAsStateWithLifecycle()
    var showLimitsSheet by remember { mutableStateOf<AppBlockConfig?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("distraction_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Control Switches Board
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Global Intervention Shields",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Hardcore Mode Config
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, "Hardcore", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Hardcore Absolute Lock", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text("Removes delete/override buttons during active timers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                        Switch(
                            checked = settings.isHardcoreModeActive,
                            onCheckedChange = { viewModel.toggleHardcoreMode(it) },
                            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.onPrimary, checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

                    // Temporary apps freeze mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AcUnit, "Freeze Mode", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            Column {
                                Text("Instant App Freeze Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text("Shuts down all game/social launch capabilities", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                        Switch(
                            checked = settings.isFreezeActive,
                            onCheckedChange = { viewModel.toggleFreezeMode(it) }
                        )
                    }
                }
            }
        }

        // Addictive app launch block options
        item {
            Text(
                text = "Addictive Apps Strict Config",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        if (blocks.isEmpty()) {
            item {
                Text(
                    text = "Loading configurations...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        } else {
            items(blocks) { appConfig ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppIcon(packageName = appConfig.packageName, modifier = Modifier.size(40.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = appConfig.appName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Limit: ${if (appConfig.dailyLimitMinutes > 0) "${appConfig.dailyLimitMinutes}m" else "Keep Focus"} | CD: ${appConfig.launchCooldownSeconds}s",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                            )
                        }

                        Switch(
                            checked = appConfig.isBlocked,
                            onCheckedChange = { viewModel.toggleAppBlock(appConfig) }
                        )
                    }
                }
            }
        }

        // System Applications Engine (Real-time detection)
        item {
            Text(
                text = "System Applications Engine (Live)",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Detected ${installedApps.size} external apps on device. Tap to configure blocking rules for addictive games and tools.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    installedApps.take(10).forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.simulateAppLaunch(app.packageName, app.appName, app.category) }
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AppIcon(packageName = app.packageName, modifier = Modifier.size(32.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(app.appName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(app.packageName, fontSize = 10.sp, color = Color.Gray)
                            }
                            Icon(Icons.Default.Launch, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 48.dp), thickness = 0.5.dp, color = Color.Gray.copy(alpha = 0.1f))
                    }
                    
                    if (installedApps.size > 10) {
                        TextButton(
                            onClick = { /* Could show full list */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("And ${installedApps.size - 10} more applications...", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Recent Block Action Tracking Logs
        item {
            Text(
                text = "Anti-Distraction Log (Real-time)",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (recentAttempts.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Shield, contentDescription = "No Blocks", tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("No recent block activities.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                            }
                        }
                    } else {
                        recentAttempts.take(6).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(MaterialTheme.colorScheme.error, CircleShape)
                                    )
                                    Column {
                                        Text("${log.appName} Intercepted", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                        Text("Reason: ${log.blockedReason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                                    }
                                }

                                val timeStr = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(log.timestamp))
                                Text(
                                    text = timeStr,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f))
                        }
                    }
                }
            }
        }
    }

    // Modal Sheet Limits Modifier Dialog
    if (showLimitsSheet != null) {
        val app = showLimitsSheet!!
        var limitValStr by remember { mutableStateOf(if (app.dailyLimitMinutes > 0) app.dailyLimitMinutes.toString() else "30") }
        var cooldownStr by remember { mutableStateOf(app.launchCooldownSeconds.toString()) }

        Dialog(onDismissRequest = { showLimitsSheet = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Edit Rules for ${app.appName}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = limitValStr,
                        onValueChange = { limitValStr = it },
                        label = { Text("Daily limit threshold (minutes)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = cooldownStr,
                        onValueChange = { cooldownStr = it },
                        label = { Text("Launch cooldown gap (seconds)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showLimitsSheet = null }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                val limitM = limitValStr.toIntOrNull() ?: 30
                                val coolS = cooldownStr.toIntOrNull() ?: 0
                                viewModel.updateAppLimits(app, limitM, coolS)
                                showLimitsSheet = null
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Save Rules")
                        }
                    }
                }
            }
        }
    }
}


// ==================== PRODUCTIVITY TAB ====================
@Composable
fun ProductivityTab(
    viewModel: StopDumbViewModel,
    habits: List<HabitRecord>,
    focusHistory: List<FocusSessionRecord>
) {
    var showAddHabitDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("productivity_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Pomodoro Timer Circular Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aesthetic Deep Work Clock",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, "Category tag", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = viewModel.timerCategory, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Circular clock area
                    Box(
                        modifier = Modifier.size(190.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val strokeWidth = 10.dp
                        val colorPrimary = MaterialTheme.colorScheme.primary
                        val colorTrack = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.08f)
                        val totalSeconds = viewModel.pomodoroTimeMinutes * 60
                        val progressAngle = if (totalSeconds > 0) {
                            (viewModel.pomodoroSecondsRemaining.toFloat() / totalSeconds.toFloat()) * 360f
                        } else 360f

                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawArc(
                                color = colorTrack,
                                startAngle = 0f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx())
                            )
                            drawArc(
                                color = colorPrimary,
                                startAngle = -90f,
                                sweepAngle = progressAngle,
                                useCenter = false,
                                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Inside time string
                        val mSeconds = viewModel.pomodoroSecondsRemaining
                        val minutesStr = (mSeconds / 60).toString().padStart(2, '0')
                        val secondsStr = (mSeconds % 60).toString().padStart(2, '0')

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$minutesStr:$secondsStr",
                                fontSize = 38.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (viewModel.isTimerRunning) "MINDFUL FOCUSING" else "PAUSED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Selection Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp) // Simple helper value for tab choice
                    ) {
                        val configs = listOf(
                            Triple(25, "Pomodoro", "25m"),
                            Triple(45, "Deep Work", "45m"),
                            Triple(60, "Study Planner", "1h"),
                            Triple(10, "Digital Detox", "10m")
                        )
                        configs.forEach { (minutes, label, display) ->
                            val active = viewModel.pomodoroTimeMinutes == minutes && viewModel.timerCategory == label
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { viewModel.selectTimerConfig(minutes, label) }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = display,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!viewModel.isTimerRunning) {
                            Button(
                                onClick = { viewModel.startTimer() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play Timer")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Engage Focus", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Button(
                                onClick = { viewModel.pauseTimer() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = "Pause Timer")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Hold Space", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { viewModel.resetTimer() },
                            modifier = Modifier.weight(0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Habit builder header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Daily Discipline & Habits",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { showAddHabitDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Habit", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Build", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Habits Loop Renderer
        if (habits.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No habits set yet. Let's make some discipline rules!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        } else {
            items(habits) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = when (item.category) {
                                        "Hydration" -> Icons.Default.LocalDrink
                                        "Eye Rest" -> Icons.Default.Visibility
                                        "Mindfulness" -> Icons.Default.SelfImprovement
                                        else -> Icons.Default.FitnessCenter
                                    },
                                    contentDescription = "Habit Category icon",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(text = item.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                                Text(
                                    text = "Streak: ${item.currentStreak} days 🔥 | Max: ${item.maxStreak} | Completions: ${item.totalCompletions}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { viewModel.completeHabit(item) }) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Complete Habit button",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.removeHabit(item.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Habit button",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddHabitDialog) {
        var hName by remember { mutableStateOf("") }
        var hCategory by remember { mutableStateOf("Mindfulness") }
        val cats = listOf("Mindfulness", "Hydration", "Eye Rest", "Physical")

        Dialog(onDismissRequest = { showAddHabitDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Build Discipline Rule",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    OutlinedTextField(
                        value = hName,
                        onValueChange = { hName = it },
                        label = { Text("What habit do you want to form?") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(text = "App category type", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        cats.forEach { c ->
                            val active = hCategory == c
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { hCategory = c }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = c,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showAddHabitDialog = false }) {
                            Text("Dismiss")
                        }
                        Button(
                            onClick = {
                                if (hName.isNotBlank()) {
                                    viewModel.addHabit(hName, hCategory, "Daily")
                                    showAddHabitDialog = false
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enforce")
                        }
                    }
                }
            }
        }
    }
}


// ==================== WELLNESS TAB ====================
@Composable
fun WellnessTab(
    viewModel: StopDumbViewModel,
    wellnessLogs: List<WellnessLogRecord>,
    habits: List<HabitRecord>
) {
    var showMoodDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wellness_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Deep Mindful Breathing Box Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Calming Box-Breathing Detox",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Reduce cortisol instantly using ancient paced breathing",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Animated Breathing circle
                    val scale by animateFloatAsState(
                        targetValue = when (viewModel.breathingPhase) {
                            "Inhale" -> 1.7f
                            "Hold Breath" -> 1.7f
                            "Exhale" -> 1.0f
                            "Rest" -> 1.0f
                            else -> 1.1f
                        },
                        animationSpec = tween(
                            durationMillis = 4000,
                            easing = LinearEasing
                        ),
                        label = ""
                    )

                    Box(
                        modifier = Modifier.size(180.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Shadow breathing waves
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale(scale)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                            Color.Transparent
                                        )
                                    ),
                                    shape = CircleShape
                                )
                        )

                        // Central core circle
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .scale((scale * 0.9f).coerceAtLeast(0.8f))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = viewModel.breathingPhase,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                if (viewModel.isBreathingActive) {
                                    Text(
                                        text = "${viewModel.breathingTicksRemaining}s",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!viewModel.isBreathingActive) {
                        Button(
                            onClick = { viewModel.startBreathingExercise() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Icon(Icons.Default.Spa, contentDescription = "Breathe icon")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Begin Breathe Session", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { viewModel.stopBreathingExercise() },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            Text("End Session", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Mood Check-in History Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mood & Burnout Logs",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Button(
                    onClick = { showMoodDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Mood, contentDescription = "Log Mood icon", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Mood lists
        if (wellnessLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No mood checks logged today. Let we check your mental flow!", fontSize = 12.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                    }
                }
            }
        } else {
            items(wellnessLogs) { log ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (log.moodValue) {
                                        5 -> "😄"
                                        4 -> "🙂"
                                        3 -> "😐"
                                        2 -> "🥱"
                                        else -> "😩"
                                    },
                                    fontSize = 20.sp
                                )
                            }
                            Column {
                                Text(
                                    text = if (log.logNote.isNotBlank()) log.logNote else "No notes logged",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Energy level: ${log.energyLevel}/5 | Sleep: ${log.sleepHours} hrs",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                                )
                            }
                        }

                        val dateStr = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(log.timestamp))
                        Text(
                            text = dateStr,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }

    if (showMoodDialog) {
        var moodVal by remember { mutableIntStateOf(4) }
        var energyVal by remember { mutableIntStateOf(4) }
        var sleepHrs by remember { mutableFloatStateOf(7.5f) }
        var notesStr by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showMoodDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Mood & Burnout Assessment",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    // Mood Radio Picker
                    Text("Select your current emotional rating", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val emojiList = listOf("😩", "🥱", "😐", "🙂", "😄")
                        emojiList.forEachIndexed { idx, emoji ->
                            val score = idx + 1
                            val active = moodVal == score
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(
                                        color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f), CircleShape)
                                    .clickable { moodVal = score },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = emoji, fontSize = 20.sp)
                            }
                        }
                    }

                    // Energy selector
                    Text("Productivity energy (1 = Exhausted, 5 = Peak)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        (1..5).forEach { stars ->
                            val active = energyVal == stars
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .border(1.dp, MaterialTheme.colorScheme.onBackground.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .clickable { energyVal = stars },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$stars",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }

                    // Sleep slider
                    Column {
                        Text("Sleep hours today (${sleepHrs.toInt()}h)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Slider(
                            value = sleepHrs,
                            onValueChange = { sleepHrs = it },
                            valueRange = 3f..12f,
                            steps = 18
                        )
                    }

                    OutlinedTextField(
                        value = notesStr,
                        onValueChange = { notesStr = it },
                        label = { Text("Log any burnout notes / distractions...") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showMoodDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                viewModel.registerWellnessLog(moodVal, energyVal, sleepHrs, notesStr)
                                showMoodDialog = false
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Enforce Log")
                        }
                    }
                }
            }
        }
    }
}


// ==================== THEMES & SETTINGS TAB ====================
@Composable
fun ThemesTab(
    viewModel: StopDumbViewModel,
    settings: GlobalWellnessSettings
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("themes_tab"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Theme selection card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Calming Pastel Themes Engine",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Customize gradient accents & calming display colors",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val themesList = listOf("Bento Grid", "Cyberpunk", "Nordic Ice", "Solarized Light", "Mint", "Lavender", "Peach", "Ocean Blue", "Rose Pink", "Sunset Orange", "Arctic White")
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(themesList.size) { index ->
                            val t = themesList[index]
                            val active = settings.currentTheme == t
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        width = if (active) 2.dp else 0.dp,
                                        color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.changeTheme(t) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = t,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }
            }
        }

        // Amoled and Popup controls
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Ecosystem System Controls",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // Amoled black Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("AMOLED Pure Black", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text("Reduces blue emission, maximizes battery safety.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = settings.isAmoledBlack,
                            onCheckedChange = { viewModel.toggleAmoledMode(it) }
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))

                    // Interruption Dialog Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Mindless Unlock Prompts", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                            Text("Force focus intention prompt before launching addictive portals.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f))
                        }
                        Switch(
                            checked = settings.isInterruptionQuestionsActive,
                            onCheckedChange = { viewModel.toggleInterruptionInterventions(it) }
                        )
                    }
                }
            }
        }

        // Privacy and Architectural Details
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.VerifiedUser, "Offline security lock", tint = MaterialTheme.colorScheme.primary)
                        Text("Zero Cloud Leak Architecture", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "StopDumb operates on an exclusive offline-first setup. All usage histories, habits, timer clocks, mental checklists, and logs are kept encrypted locally in your Room system. Algorithms do not read or share your digital behavior patterns.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}


// ==================== SIMULATION INTERVENTION POPUPS ====================
@Composable
fun SimulationDialogsRenderer(viewModel: StopDumbViewModel) {
    if (viewModel.activeSimulationDialog == null) return

    val keyboardController = LocalSoftwareKeyboardController.current

    when (viewModel.activeSimulationDialog) {
        "IntentionCheck" -> {
            Dialog(onDismissRequest = { viewModel.closeSimulationDialogs() }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Psychology, "Reality check query logo", tint = MaterialTheme.colorScheme.primary)
                        }

                        Text(
                            text = "Smart Intention Double-Check",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "You requested to open ${viewModel.simAppName}. You have already launched this app ${viewModel.appOpeningCountSimulated} times today. Why are you opening it now?",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        OutlinedTextField(
                            value = viewModel.intentionInputText,
                            onValueChange = { viewModel.intentionInputText = it },
                            placeholder = { Text("E.g., I need to message a team-member") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.closeSimulationDialogs()
                                    keyboardController?.hide()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Close Path")
                            }

                            Button(
                                onClick = {
                                    val logReason = if (viewModel.intentionInputText.isNotBlank()) {
                                        viewModel.intentionInputText
                                    } else "Mindless launch"
                                    viewModel.bypassSimulatedBlock(logReason)
                                    keyboardController?.hide()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                enabled = viewModel.intentionInputText.length >= 4
                            ) {
                                Text("Proceed Anyway")
                            }
                        }
                    }
                }
            }
        }

        "LimitsAlert" -> {
            Dialog(onDismissRequest = { viewModel.closeSimulationDialogs() }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Security, "Limits Enforced Lock icon", tint = MaterialTheme.colorScheme.error)
                        }

                        Text(
                            text = "Access Lock Intercepted",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "Access to ${viewModel.simAppName} is strictly forbidden. Your daily limits have been fully exhausted, or StopDumb's Hardcore Freeze mode is actively holding digital shields.",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.08f)),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Mindful Prompt Override Challenge", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                                Text("Spend 1 minute inside the calms box-breathing widget or complete a pending fitness task in the habits tracker to bypass emergency lockups.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f))
                            }
                        }

                        Button(
                            onClick = { viewModel.closeSimulationDialogs() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Enforce Focus Shields")
                        }
                    }
                }
            }
        }

        "RealityCheck" -> {
            Dialog(onDismissRequest = { viewModel.closeSimulationDialogs() }) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Verified, "Reality Check Logo Icon", tint = MaterialTheme.colorScheme.primary)
                        }

                        Text(
                            text = "Doomscroll Reality Check",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        Text(
                            text = "TikTok/Instagram is highly addictive. Continuous scrolling reduces attention span. Let's do a 1-minute breathing detox instead?",
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.closeSimulationDialogs() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Dismiss Check")
                            }

                            Button(
                                onClick = {
                                    viewModel.closeSimulationDialogs()
                                    viewModel.activeTab = "Wellness"
                                    viewModel.startBreathingExercise()
                                },
                                modifier = Modifier.weight(1.2f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Spa, contentDescription = "breathe", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Do Breathe Detox")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyAnalysisScreen(viewModel: StopDumbViewModel) {
    val analysis by viewModel.dailyAnalysis.collectAsStateWithLifecycle()
    
    // Bento Grid Theming colors
    val bentoBg = Color(0xFFF7F9FF)
    val bentoPrimary = Color(0xFF0061A4)
    val bentoSecondary = Color(0xFFD1E4FF)
    val bentoText = Color(0xFF001D34)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = bentoBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { viewModel.activeSubScreen = null }) {
                    Icon(Icons.Default.Close, "Close analysis", tint = bentoPrimary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Daily Focus Report", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = bentoPrimary.copy(alpha = 0.7f))
                    Text(SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = bentoText)
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Hero Score Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(32.dp),
                        colors = CardDefaults.cardColors(containerColor = bentoPrimary)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column {
                                    Text("DAILY FOCUS SCORE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                                    Row(verticalAlignment = Alignment.Bottom) {
                                        Text("${analysis.focusScore}", fontSize = 52.sp, fontWeight = FontWeight.Black, color = Color.White)
                                        Text("/100", fontSize = 18.sp, fontWeight = FontWeight.Normal, color = Color.White.copy(alpha = 0.6f), modifier = Modifier.padding(bottom = 8.dp, start = 4.dp))
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(Modifier.size(6.dp).background(Color(0xFF49E191), CircleShape))
                                        Spacer(Modifier.width(6.dp))
                                        Text("FLOW MODE", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Color.White)
                                    }
                                }
                            }
                            
                            Text(
                                text = analysis.recommendation,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }

                // Grid stats
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).height(120.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = bentoSecondary)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    Text("SCREEN TIME", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = bentoText.copy(alpha = 0.6f))
                                    val hours = analysis.totalTimeMs / (1000 * 3600)
                                    val mins = (analysis.totalTimeMs % (1000 * 3600)) / (1000 * 60)
                                    Text("${hours}h ${mins}m", fontSize = 24.sp, fontWeight = FontWeight.Black, color = bentoText)
                                    Text("Real-time usage", fontSize = 9.sp, fontWeight = FontWeight.Medium, color = bentoText.copy(alpha = 0.5f))
                                }
                            }
                            
                            Card(
                                modifier = Modifier.weight(1f).height(120.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFDDE2EA))
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    Text("PICKUPS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43474E))
                                    Text("${analysis.pickups}", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1C1E))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.TrendingUp, null, tint = Color(0xFFBA1A1A), modifier = Modifier.size(12.dp))
                                        Text("Frequent", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFBA1A1A))
                                    }
                                }
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Card(
                                modifier = Modifier.weight(1f).height(120.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = BorderStroke(1.dp, Color(0xFFDDE2EA))
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
                                    Text("TOP SINK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF43474E))
                                    val sinkMins = analysis.topSinkTimeMs / (1000 * 60)
                                    Column {
                                        Text(analysis.topSinkName, fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF1A1C1E), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text("${sinkMins}m today", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = bentoPrimary)
                                    }
                                    Box(Modifier.fillMaxWidth().height(4.dp).background(Color(0xFFE1E2E9), CircleShape)) {
                                        Box(Modifier.fillMaxWidth(0.7f).fillMaxHeight().background(Color(0xFFBA1A1A), CircleShape))
                                    }
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f).height(120.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1C1E))
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize().clickable { viewModel.activeTab = "Productivity"; viewModel.activeSubScreen = "PomodoroTimer" },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PlayArrow, null, tint = Color(0xFF49E191), modifier = Modifier.size(32.dp))
                                        Text("DEEP WORK", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Composition of Digital Life",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = bentoText,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFFDDE2EA))
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CompositionRow("Productive Focus", analysis.productiveTimeMs, analysis.totalTimeMs, bentoPrimary)
                            CompositionRow("Mindless Consumption", analysis.distractingTimeMs, analysis.totalTimeMs, Color(0xFFBA1A1A))
                            val otherMs = analysis.totalTimeMs - analysis.productiveTimeMs - analysis.distractingTimeMs
                            CompositionRow("Utility & Misc", otherMs.coerceAtLeast(0), analysis.totalTimeMs, Color(0xFF8E9199))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompositionRow(label: String, valueMs: Long, totalMs: Long, color: Color) {
    val progress = if (totalMs > 0) valueMs.toFloat() / totalMs.toFloat() else 0f
    val hours = valueMs / (1000 * 3600)
    val mins = (valueMs % (1000 * 3600)) / (1000 * 60)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
            Text("${hours}h ${mins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1A1C1E))
        }
        LinearProgressIndicator(
            progress = progress,
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = color,
            trackColor = color.copy(alpha = 0.1f)
        )
    }
}

val bentoSubtitleColor = Color(0xFF43474E)
