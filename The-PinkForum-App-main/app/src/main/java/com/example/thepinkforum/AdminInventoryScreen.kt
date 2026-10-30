package com.example.thepinkforum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thepinkforum.network.InventoryData
import com.example.thepinkforum.network.UsersViewModel
import com.example.thepinkforum.ui.EmptyState
import com.example.thepinkforum.ui.LoadingState
import com.example.thepinkforum.ui.theme.PinkBackground
import com.example.thepinkforum.ui.theme.PinkPrimary
import com.example.thepinkforum.ui.theme.PinkSoftCard



@Composable
fun AdminInventoryScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: UsersViewModel = viewModel()
) {
    val context = LocalContext.current
    val items by viewModel.inventoryItems
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage

    LaunchedEffect(Unit) {
        viewModel.fetchAllInventory(context)
    }
    // Standard Admin Navigation Bottom Bar Tabs
    val adminScreens = listOf(
        NavigationItems.AdminHome,
        NavigationItems.AdminFiles,
        NavigationItems.AdminUpload, // Active tab
        NavigationItems.AdminCourses,
        NavigationItems.AdminInventory
    )

    // Dummy Data: To be replaced by your GET request to handleInventoryReadItemAPI

    Scaffold(
        bottomBar = {
            PinkBottomBar(navController = navController, items = adminScreens)
        },
        containerColor = PinkBackground
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Header
            Text(
                text = "Inventory Overview",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Text(
                text = "Read-only view. Manage stock on the web dashboard.",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Summary Cards Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                SummaryCard(
                    title = "Total Items",
                    count = items.sumOf { it.quantity }.toString(),
                    icon = Icons.Default.Inventory,
                    modifier = Modifier.width(160.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "All Items",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (isLoading) {
                LoadingState()
            } else if (errorMessage != null) {
                Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(16.dp))
            } else {
                if (items.isEmpty()) {
                    EmptyState(
                        message = "No Items Found",
                        subMessage = "There are no items in the inventory."
                    )
                } else {
                    // Inventory List
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(items) { item ->
                            InventoryItemCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryCard(
    modifier: Modifier = Modifier,
    title: String,
    count: String,
    icon: ImageVector,
    iconTint: Color = PinkPrimary,
) {
    Card(
        modifier = modifier.height(100.dp).width(120.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = title, fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

@Composable
fun InventoryItemCard(item: InventoryData) {
    val (statusText, statusColor) = when {
        item.quantity <= 0 -> "Out of Stock" to Color(0xFFE53935) // Red
        item.quantity < 10 -> "Low Stock" to Color(0xFFFF9800) // Orange
        else -> "In Stock" to Color(0xFF4CAF50) // Green
    }

    // Pick an icon based on the category
    val categoryIcon = when (item.category) {
        "Electronics" -> Icons.Default.Computer
        "Books" -> Icons.Default.MenuBook
        else -> Icons.Default.Category
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PinkSoftCard,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = PinkPrimary,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = "ID: ${item.id} • ${item.category}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${item.quantity}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusColor.copy(alpha = 0.1f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.3f)),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AdminInventoryPreview() {
    AdminInventoryScreen(navController = NavController(LocalContext.current))
}