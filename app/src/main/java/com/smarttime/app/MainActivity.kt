package com.smarttime.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smarttime.app.ui.theme.Gold
import com.smarttime.app.ui.theme.Navy
import com.smarttime.app.ui.theme.NavyRaised
import com.smarttime.app.ui.theme.NavySurface
import com.smarttime.app.ui.theme.SmartTimeTheme
import com.smarttime.app.ui.theme.TextPrimary
import com.smarttime.app.ui.theme.TextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { SmartTimeTheme { SmartTimeShell() } }
    }
}

private data class MainTab(val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

@Composable
private fun SmartTimeShell() {
    val tabs = listOf(
        MainTab("الرئيسية", Icons.Filled.Home),
        MainTab("المصروفات", Icons.Filled.AccountBalanceWallet),
        MainTab("الدردشة", Icons.Filled.Chat),
        MainTab("الإعدادات", Icons.Filled.Settings)
    )
    var selected by remember { mutableIntStateOf(0) }
    androidx.compose.runtime.CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Navy,
            bottomBar = {
                NavigationBar(containerColor = NavySurface, contentColor = Gold) {
                    tabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selected == index,
                            onClick = { selected = index },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = { Text(tab.title, fontSize = 11.sp) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Gold,
                                selectedTextColor = Gold,
                                indicatorColor = NavyRaised,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp, vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Smart Time", color = Gold, fontSize = 23.sp, fontWeight = FontWeight.Bold)
                        Text("وقتك من ذهب", color = TextSecondary, fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape)
                            .background(NavySurface).border(1.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) { Text("ST", color = Gold, fontWeight = FontWeight.Bold) }
                }
                Spacer(Modifier.height(28.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp))
                        .background(NavySurface).border(1.dp, Gold.copy(alpha = 0.35f), RoundedCornerShape(22.dp))
                        .padding(22.dp)
                ) {
                    Column {
                        Text(tabs[selected].title, color = Gold, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (selected == 0) "مرحبًا بك في Smart Time" else "سيتم تجهيز هذا القسم في مرحلته المخصصة.",
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("النسخة الأصلية من Android قيد التأسيس", color = TextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.weight(1f))
                Text("SMART TIME  •  ANDROID", color = TextSecondary, fontSize = 10.sp, letterSpacing = 2.sp)
            }
        }
    }
}
