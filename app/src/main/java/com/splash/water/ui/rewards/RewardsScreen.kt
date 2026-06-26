package com.splash.water.ui.rewards

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.vector.ImageVector
import com.splash.water.domain.MilestoneIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splash.water.ui.theme.LocalAppGradient

@Composable
fun RewardsScreen(viewModel: RewardsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val gradient = LocalAppGradient.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(gradient.top, gradient.bottom))),
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            header("Rewards", "${state.unlockedCount} of ${state.totalCount} unlocked")
            state.level?.let { full { LevelBanner(it) } }
            section(if (state.themeName.isNotEmpty()) "Today · ${state.themeName}" else "Today")
            items(state.daily, key = { it.step.name }) { DailyCardView(it) }
            section("This Week")
            items(state.weekly, key = { it.def.id }) { RewardCardView(it) }
            section("Milestones")
            items(state.streak, key = { it.def.id }) { RewardCardView(it) }
        }
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.full(
    content: @Composable () -> Unit,
) = item(span = { GridItemSpan(maxLineSpan) }) { content() }

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.header(title: String, subtitle: String) {
    full {
        Column {
            Text(title, style = MaterialTheme.typography.headlineLarge)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
            Spacer(Modifier.height(8.dp))
        }
    }
}

private fun androidx.compose.foundation.lazy.grid.LazyGridScope.section(name: String) {
    full {
        Text(
            name,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun LevelBanner(level: LevelInfo) {
    val tint = Color(level.def.color)
    val onTint = Color.White
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    ) {
        Box(
            modifier = Modifier.background(
                Brush.horizontalGradient(listOf(lerp(tint, Color.Black, 0.18f), lerp(tint, Color.White, 0.12f))),
            ),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(52.dp).clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(iconFor(level.def.icon), contentDescription = null, tint = onTint,
                            modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.size(14.dp))
                    Column {
                        Text("Level ${level.def.level}", style = MaterialTheme.typography.labelLarge,
                            color = onTint.copy(alpha = 0.85f), fontWeight = FontWeight.SemiBold)
                        Text(level.def.title, style = MaterialTheme.typography.titleLarge,
                            color = onTint, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { level.progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = onTint,
                    trackColor = Color.White.copy(alpha = 0.25f),
                )
                Spacer(Modifier.height(8.dp))
                val caption = level.next?.let {
                    "${level.daysToNext} more goal-day${if (level.daysToNext == 1) "" else "s"} to ${it.title}"
                } ?: "Max level, you're a hydration legend!"
                Text(caption, style = MaterialTheme.typography.bodySmall, color = onTint.copy(alpha = 0.9f))
            }
        }
    }
}

@Composable
private fun DailyCardView(card: DailyRewardCard) {
    val tint = Color(card.accent)
    val surface = MaterialTheme.colorScheme.surface
    val container = if (card.unlocked) lerp(surface, tint, 0.14f) else MaterialTheme.colorScheme.surfaceVariant
    val titleColor = if (card.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val badgeColor = if (card.unlocked) tint.copy(alpha = 0.20f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
    val iconColor = if (card.unlocked) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Card(
        modifier = Modifier.fillMaxWidth().aspectRatio(0.92f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = if (card.unlocked) 2.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier.size(56.dp).clip(CircleShape).background(badgeColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (card.unlocked) iconFor(card.step.icon) else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(30.dp),
                )
            }
            Spacer(Modifier.height(8.dp))
            // % pill, makes the goal-relative steps obvious.
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(badgeColor)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
            ) {
                Text("${card.step.percent}%", style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold, color = iconColor)
            }
            Spacer(Modifier.height(6.dp))
            Text(card.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center, color = titleColor)
            Spacer(Modifier.height(4.dp))
            Text(
                if (card.thresholdMl > 0) "${card.thresholdMl} ml" else card.tip,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun RewardCardView(card: RewardCard) {
    val tint = Color(card.def.color)
    val surface = MaterialTheme.colorScheme.surface
    // Opaque tinted surface (no translucency) so the drop shadow can't bleed through.
    val container = if (card.unlocked) lerp(surface, tint, 0.12f) else MaterialTheme.colorScheme.surfaceVariant
    val titleColor = if (card.unlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
    val badgeColor = if (card.unlocked) tint.copy(alpha = 0.18f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
    val iconColor = if (card.unlocked) tint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.92f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = if (card.unlocked) 2.dp else 0.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(badgeColor),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (card.unlocked) iconFor(card.def.icon) else Icons.Filled.Lock,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(32.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                card.def.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = titleColor,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                card.def.tip,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                maxLines = 3,
            )
            if (card.count > 1) {
                Spacer(Modifier.height(6.dp))
                Text("×${card.count}", fontWeight = FontWeight.Bold, color = tint)
            }
        }
    }
}

private fun iconFor(icon: MilestoneIcon): ImageVector = when (icon) {
    MilestoneIcon.DROP -> Icons.Filled.WaterDrop
    MilestoneIcon.WAVE -> Icons.Filled.Waves
    MilestoneIcon.SPARKLE -> Icons.Filled.AutoAwesome
    MilestoneIcon.TROPHY -> Icons.Filled.EmojiEvents
    MilestoneIcon.CALENDAR -> Icons.Filled.CalendarMonth
    MilestoneIcon.FIRE -> Icons.Filled.LocalFireDepartment
    MilestoneIcon.CROWN -> Icons.Filled.WorkspacePremium
    MilestoneIcon.TARGET -> Icons.Filled.TrackChanges
    MilestoneIcon.BOLT -> Icons.Filled.Bolt
    MilestoneIcon.MEDAL -> Icons.Filled.MilitaryTech
    MilestoneIcon.STAR -> Icons.Filled.Star
    MilestoneIcon.SHIELD -> Icons.Filled.Shield
    MilestoneIcon.DIAMOND -> Icons.Filled.Diamond
    MilestoneIcon.PREMIUM -> Icons.Filled.WorkspacePremium
}
