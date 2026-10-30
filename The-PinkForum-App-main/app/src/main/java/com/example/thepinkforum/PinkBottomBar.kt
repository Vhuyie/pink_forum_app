package com.example.thepinkforum

import android.app.Activity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.TextMuted

@Composable
fun PinkBottomBar(
    navController: NavController,
    items: List<NavigationItems>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val context = LocalContext.current
    val window = (context as? Activity)?.window
    window?.navigationBarColor = Color.White.toArgb()

    if(currentRoute == "get_started" || currentRoute == "register_as" || currentRoute == "login_as" || currentRoute?.startsWith("verify_email") == true){
        window?.navigationBarColor = PinkBackground.toArgb()
    }

    // 1. The crisp separator line
    HorizontalDivider(
        thickness = 1.dp,
        color = Color.LightGray.copy(alpha = 0.4f)
    )

    // Use NavigationBar directly without the Surface wrapper
    NavigationBar(
        modifier = Modifier.fillMaxWidth().height(100.dp).navigationBarsPadding(), // Anchors to the edges
        containerColor = Color.White,
        tonalElevation = 8.dp, // Gives it a slight shadow
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val isUploadAction = item.route == "educator_upload" || item.route == "admin_upload"

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    if (isUploadAction) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PinkPrimary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = item.title,
                                    tint = Color.White
                                )
                            }
                        }
                    } else {
                        Icon(
                            imageVector = if (isSelected) item.SelectedIcon else item.UnselectedIcon,
                            contentDescription = item.title
                        )
                    }
                },
                label = {
                    if (!isUploadAction) {
                        Text(
                            text = item.title,
                            fontSize = 10.sp,
                            maxLines = 1,
                            softWrap = false,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PinkPrimary,
                    selectedTextColor = PinkPrimary,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = Color.Transparent
                )
            )
        }
    }
}
