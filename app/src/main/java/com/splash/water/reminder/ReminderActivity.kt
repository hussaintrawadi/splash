package com.splash.water.reminder

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.splash.water.domain.DateUtils

/**
 * Full-screen, incoming-call-style reminder shown over the lockscreen (or on top of whatever's
 * open) when a reminder fires. Buttons reuse [ReminderActionReceiver] so behaviour matches the
 * notification actions. The ringing stops only when the user picks an action.
 */
class ReminderActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showWhenLockedAndTurnScreenOn()
        renderFrom(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        renderFrom(intent)
    }

    private fun renderFrom(intent: Intent) {
        val amount = intent.getIntExtra(ReminderConstants.EXTRA_AMOUNT, 250)
        val skipCount = intent.getIntExtra(ReminderConstants.EXTRA_SKIP_COUNT, 0)
        val lastDrink = intent.getLongExtra(ReminderConstants.EXTRA_LAST_DRINK, 0L)

        setContent {
            ReminderScreen(
                amountMl = amount,
                skipCount = skipCount,
                lastDrink = lastDrink,
                onDrank = { sendAction(ReminderConstants.ACTION_DRANK, amount) },
                onLog = { ml -> sendAction(ReminderConstants.ACTION_DRANK, ml) },
                onSnooze = { sendAction(ReminderConstants.ACTION_SNOOZE, 0) },
                onLater = { sendAction(ReminderConstants.ACTION_DISMISS, 0) },
            )
        }
    }

    private fun sendAction(action: String, amount: Int) {
        val intent = Intent(this, ReminderActionReceiver::class.java).apply {
            this.action = action
            if (amount > 0) putExtra(ReminderConstants.EXTRA_AMOUNT, amount)
        }
        sendBroadcast(intent)
        finish()
    }

    private fun showWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
            )
        }
    }
}

// Branded deep-water palette, independent of app light/dark theme so the call screen always pops.
private val CallTop = Color(0xFF0E63B8)
private val CallBottom = Color(0xFF063C73)
private val OnCall = Color.White

@Composable
private fun ReminderScreen(
    amountMl: Int,
    skipCount: Int,
    lastDrink: Long,
    onDrank: () -> Unit,
    onLog: (Int) -> Unit,
    onSnooze: () -> Unit,
    onLater: () -> Unit,
) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "scale",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(CallTop, CallBottom))),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Text("SPLASH REMINDER", color = OnCall.copy(alpha = 0.7f),
                fontWeight = FontWeight.Bold, fontSize = 13.sp, letterSpacing = 2.sp)

            Spacer(Modifier.height(40.dp))
            Box(
                modifier = Modifier.size(140.dp).scale(scale).clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center,
            ) {
                Text("💧", fontSize = 76.sp)
            }
            Spacer(Modifier.height(28.dp))
            Text("Time to drink water!", color = OnCall, fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp)
            Spacer(Modifier.height(10.dp))

            if (skipCount >= ReminderConstants.SKIP_WARNING_THRESHOLD) {
                SkipWarning(skipCount = skipCount, lastDrink = lastDrink)
            } else {
                Text("Take a sip and log it", color = OnCall.copy(alpha = 0.9f), fontSize = 16.sp)
            }

            Spacer(Modifier.weight(1f))

            // Primary action, log the default quick-add amount.
            Button(
                onClick = onDrank,
                modifier = Modifier.fillMaxWidth().height(62.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OnCall, contentColor = CallBottom),
            ) {
                Icon(Icons.Filled.WaterDrop, null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.size(8.dp))
                Text("I drank $amountMl ml", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(12.dp))
            // Quick alternative amounts.
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                QuickLogButton("+250 ml", Modifier.weight(1f)) { onLog(250) }
                QuickLogButton("+500 ml", Modifier.weight(1f)) { onLog(500) }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(27.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OnCall),
                ) {
                    Icon(Icons.Filled.Snooze, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.size(6.dp))
                    Text("Snooze", fontSize = 16.sp)
                }
            }
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = onLater, modifier = Modifier.fillMaxWidth()) {
                Text("I'll drink later", color = OnCall.copy(alpha = 0.8f), fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun QuickLogButton(label: String, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(27.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OnCall),
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SkipWarning(skipCount: Int, lastDrink: Long) {
    val since = if (lastDrink > 0) "since ${DateUtils.formatClock(lastDrink)}" else "in a while"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFFFB300).copy(alpha = 0.22f))
            .padding(14.dp),
    ) {
        Text(
            "Heads up, you haven't logged water $since. That's $skipCount reminders skipped. " +
                "Your body will thank you for a glass now.",
            color = OnCall,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
