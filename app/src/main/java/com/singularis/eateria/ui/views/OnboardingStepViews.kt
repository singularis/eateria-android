package com.singularis.eateria.ui.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.singularis.eateria.R
import com.singularis.eateria.services.Localization
import com.singularis.eateria.ui.theme.AppTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BalancedPlateStepView(page: OnboardingPage) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = page.title,
            color = AppTheme.textPrimary(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
        )
        Text(
            text = page.description,
            color = AppTheme.textSecondary(),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        val pagerState = rememberPagerState(pageCount = { 3 })
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxWidth()) { pageIndex ->
            val imageRes = when(pageIndex) {
                0 -> R.drawable.onboarding_plate1
                1 -> R.drawable.onboarding_plate2
                else -> R.drawable.onboarding_plate3
            }
            val title = when(pageIndex) {
                0 -> Localization.tr(context, "onboarding.plates.slide1.title", "Avocado salad")
                1 -> Localization.tr(context, "onboarding.plates.slide2.title", "Chicken breast bowl")
                else -> Localization.tr(context, "onboarding.plates.slide3.title", "Chicken with broccoli and egg")
            }
            val score = when(pageIndex) { 0 -> "95 / 100"; 1 -> "94 / 100"; else -> "93 / 100" }
            val desc = when(pageIndex) {
                0 -> Localization.tr(context, "onboarding.plates.slide1.desc", "Avocado, egg, tomato, cheese, olive. A real life example that often scores in the 90s.")
                1 -> Localization.tr(context, "onboarding.plates.slide2.desc", "Chicken breast, rice, avocado, cucumber. Filling and balanced, typical high score.")
                else -> Localization.tr(context, "onboarding.plates.slide3.desc", "Grilled chicken, broccoli, egg. Simple, high protein, high score.")
            }
            
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(20.dp)) {
                    Image(painter = painterResource(id = imageRes), contentDescription = null, modifier = Modifier.size(200.dp))
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = AppTheme.textPrimary(), textAlign = TextAlign.Center)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        Localization.tr(context, "onboarding.plates.score", "Score: %d").replace("%d", "$score"),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(desc, fontSize = 16.sp, color = AppTheme.textSecondary(), textAlign = TextAlign.Center)
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            repeat(3) { i ->
                Box(modifier = Modifier.padding(4.dp).size(8.dp).clip(CircleShape).background(if (pagerState.currentPage == i) AppTheme.accent() else AppTheme.textSecondary().copy(alpha = 0.5f)))
            }
        }
    }
}

@Composable
fun SmartTipsStepView(page: OnboardingPage) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = page.title,
            color = AppTheme.textPrimary(),
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
        )
        Text(
            text = page.description,
            fontSize = 19.sp,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(brush = Brush.linearGradient(colors = listOf(Color(0xFF4CAF50), Color(0xFF9C27B0)))),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chess & Activity Tracking
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Grid3x3, contentDescription = null, tint = Color(0xFF9C27B0), modifier = Modifier.size(44.dp).background(Color(0xFF9C27B0).copy(alpha=0.12f), CircleShape).padding(10.dp))
                        Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(44.dp).background(Color(0xFFFF9800).copy(alpha=0.12f), CircleShape).padding(10.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(Localization.tr(context, "onboarding.tips.chess_activities.title", "Chess & Activity tracking"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.textPrimary())
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(Localization.tr(context, "onboarding.tips.chess_activities.desc", "• Track your chess score with real time updates during games\n• Log activities: gym, steps, treadmill, elliptical, yoga\n• Calories burned are calculated automatically"), fontSize = 14.sp, color = AppTheme.textSecondary(), lineHeight = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Try Manually
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(44.dp).background(Color(0xFFFF9800).copy(alpha=0.12f), CircleShape).padding(10.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(Localization.tr(context, "onboarding.tips.try_manual.title", "Try Manually"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.textPrimary())
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(Localization.tr(context, "onboarding.tips.try_manual.desc", "If you want to enter a dish manually, tap \"Try manually\". The app will then:\n\n• automatically recalculate calories\n• automatically update the health score"), fontSize = 14.sp, color = AppTheme.textSecondary(), lineHeight = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        
        // Additives
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            shape = RoundedCornerShape(18.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocalCafe, contentDescription = null, tint = Color(0xFFFFEB3B), modifier = Modifier.size(44.dp).background(Color(0xFFFFEB3B).copy(alpha=0.12f), CircleShape).padding(10.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(Localization.tr(context, "onboarding.tips.addons.title", "Additives"), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = AppTheme.textPrimary())
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(Localization.tr(context, "onboarding.tips.addons.desc2", "Tap any dish → Additives to add milk, wasabi, etc."), fontSize = 14.sp, color = AppTheme.textSecondary(), lineHeight = 20.sp)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun TeamStepView(page: OnboardingPage) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = page.title,
            color = AppTheme.textPrimary(),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
        )
        Text(
            text = Localization.tr(context, "onboarding.team.message", "Dear customers,\nThank you for using Eateria.\n\nWe're building the app actively and would love to grow it with you. If you'd like to join the team, contribute ideas, or simply share feedback, we'd be happy to hear from you.\n\nWith \uD83D\uDC9C,\nThe Eateria team"),
            fontSize = 17.sp,
            textAlign = TextAlign.Start,
            style = androidx.compose.ui.text.TextStyle(brush = Brush.linearGradient(colors = listOf(Color(0xFF4CAF50), Color(0xFF9C27B0)))),
            modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
            lineHeight = 24.sp
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            // Eugen
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Image(painter = painterResource(id = R.drawable.team_eugen), contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Eugen", fontWeight = FontWeight.Bold, color = AppTheme.textPrimary(), fontSize = 18.sp)
                Text(Localization.tr(context, "onboarding.team.role.founder", "Cat"), color = AppTheme.textSecondary(), fontSize = 14.sp)
            }
            // Olha
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Image(painter = painterResource(id = R.drawable.team_olha), contentDescription = null, modifier = Modifier.size(100.dp).clip(CircleShape))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Olha", fontWeight = FontWeight.Bold, color = AppTheme.textPrimary(), fontSize = 18.sp)
                Text(Localization.tr(context, "onboarding.team.role.co_founder", "Dog"), color = AppTheme.textSecondary(), fontSize = 14.sp)
            }
        }
        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
fun PlanStepView(page: OnboardingPage) {
    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Icon
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(bottom = 14.dp)) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(Color(0xFF4CAF50).copy(alpha = 0.12f), CircleShape)
            )
            Icon(imageVector = Icons.Default.AdsClick, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(50.dp))
        }
        
        Text(
            text = page.title,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = AppTheme.textPrimary(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(14.dp))
        
        Text(
            text = Localization.tr(context, "onboarding.plan.subtitle", "Set a safe goal and let the app build a realistic path for you."),
            fontSize = 16.sp,
            color = AppTheme.textSecondary(),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 28.dp),
            lineHeight = 22.sp
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(containerColor = AppTheme.surface()),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Localization.tr(context, "onboarding.plan.point.weight", "Choose your target weight. BMI never goes below 18.5, so the goal stays safe."), fontSize = 14.sp, color = AppTheme.textSecondary(), lineHeight = 20.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.DirectionsRun, contentDescription = null, tint = Color(0xFF2196F3), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Localization.tr(context, "onboarding.plan.point.activity", "Pick your activity level or use the activity only mode to focus on movement instead of diet."), fontSize = 14.sp, color = AppTheme.textSecondary(), lineHeight = 20.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccessTime, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(Localization.tr(context, "onboarding.plan.point.timeline", "We calculate a daily calorie range and a timeline that fits your lifestyle."), fontSize = 14.sp, color = AppTheme.textSecondary(), lineHeight = 20.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = Localization.tr(context, "onboarding.plan.hint", "After this tutorial you can open\nProfile → Health\nto calculate your plan right away."),
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            style = androidx.compose.ui.text.TextStyle(brush = Brush.linearGradient(colors = listOf(Color(0xFF4CAF50), Color(0xFF9C27B0)))),
            modifier = Modifier.padding(horizontal = 28.dp).padding(bottom = 24.dp),
            lineHeight = 28.sp
        )
    }
}
