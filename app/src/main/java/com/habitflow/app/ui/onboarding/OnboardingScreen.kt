package com.habitflow.app.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.habitflow.app.data.PreferencesManager
import com.habitflow.app.ui.components.OnboardingHabitPreview
import com.habitflow.app.ui.components.OnboardingHeatmapPreview
import com.habitflow.app.ui.components.OnboardingTaskPreview
import com.habitflow.app.ui.theme.*
import kotlinx.coroutines.launch

private data class OnboardingPage(
    val title: String,
    val titleHighlight: String,
    val subtitle: String,
)

private val pages = listOf(
    OnboardingPage(
        title = "Small Steps,",
        titleHighlight = "Big Changes",
        subtitle = "Habi Wabi helps you plant tiny habits and watch them grow — no pressure, just gentle consistency."
    ),
    OnboardingPage(
        title = "See Every",
        titleHighlight = "Day You Showed Up",
        subtitle = "Like rings in a tree — your progress map reveals the beautiful pattern of your consistency."
    ),
    OnboardingPage(
        title = "One Day",
        titleHighlight = "at a Time",
        subtitle = "Wabi-sabi: beauty in imperfection. Miss a day? That's okay. Just begin again."
    ),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefsManager = remember { PreferencesManager(context) }
    var showNotifRationale by remember { mutableStateOf(false) }
    var completeAfterPermission by remember { mutableStateOf(false) }

    // Permission launcher — fires system dialog, then completes onboarding
    val notifPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Proceed regardless of grant outcome
        scope.launch { prefsManager.setOnboardingComplete(true); onComplete() }
    }

    fun finishOnboarding() {
        scope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val granted = ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
                if (!granted) {
                    showNotifRationale = true
                    return@launch
                }
            }
            prefsManager.setOnboardingComplete(true)
            onComplete()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .navigationBarsPadding()
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // --- Pill dot indicator at the top ---
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(pages.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    val width by animateDpAsState(
                        targetValue = if (isSelected) 28.dp else 8.dp,
                        label = "dot_width_$index"
                    )
                    val color by animateColorAsState(
                        targetValue = if (isSelected) GoldAccent else TextTertiary,
                        label = "dot_color_$index"
                    )
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .width(width)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                }
            }

            // --- Pager content ---
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                OnboardingPage(page = pages[page], pageIndex = page)
            }

            // --- CTA Button ---
            val isLastPage = pagerState.currentPage == pages.size - 1
            Button(
                onClick = {
                    if (isLastPage) {
                        finishOnboarding()
                    } else {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 32.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldAccent,
                    contentColor = Background
                )
            ) {
                Text(
                    text = if (isLastPage) "Get Started" else "Next",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
            }
        }
    }

    // ── Notification rationale bottom sheet ──────────────────────────────────
    if (showNotifRationale) {
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = {
                showNotifRationale = false
                // Still complete without permission
                scope.launch { prefsManager.setOnboardingComplete(true); onComplete() }
            },
            containerColor = Surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("🔔", fontSize = 40.sp)
                Text(
                    "Enable Habit Reminders?",
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Habi Wabi can gently remind you when it's time for your habits — one small nudge at the right moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Text(
                    "No spam. Only habits you choose to be reminded about. You can always change this in Settings.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(
                        onClick = {
                            showNotifRationale = false
                            notifPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Background)
                    ) { Text("Allow Reminders", fontWeight = FontWeight.SemiBold, fontSize = 16.sp) }
                }
                TextButton(
                    onClick = {
                        showNotifRationale = false
                        scope.launch { prefsManager.setOnboardingComplete(true); onComplete() }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip for now", color = TextTertiary)
                }
            }
        }
    }
}

@Composable
private fun OnboardingPage(page: OnboardingPage, pageIndex: Int) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        // --- Preview illustration ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Surface),
            contentAlignment = Alignment.Center
        ) {
            when (pageIndex) {
                0 -> OnboardingHabitPreview()
                1 -> OnboardingHeatmapPreview()
                2 -> OnboardingTaskPreview()
            }
        }

        Spacer(Modifier.height(40.dp))

        // --- Title ---
        Text(
            text = buildAnnotatedString {
                append(page.title)
                append(" ")
                withStyle(SpanStyle(color = GoldAccent)) {
                    append(page.titleHighlight)
                }
            },
            style = MaterialTheme.typography.displayLarge.copy(
                fontSize = 32.sp,
                lineHeight = 40.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        // --- Subtitle ---
        Text(
            text = page.subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}
