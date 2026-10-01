package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.DefaultRoadmaps
import com.example.ui.CareerViewModel
import com.example.ui.components.ApiKeyConfigDialog
import com.example.ui.components.CareerTopAppBar
import com.example.ui.screens.GenerateScreen
import com.example.ui.screens.PresetsScreen
import com.example.ui.screens.RoadmapDetailScreen
import com.example.ui.screens.SavedRoadmapsScreen
import com.example.ui.theme.CareerPathTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CareerPathTheme {
                CareerApp()
            }
        }
    }
}

@Composable
fun CareerApp(
    viewModel: CareerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedRoadmaps by viewModel.savedRoadmaps.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        val msg = uiState.userMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle system back navigation cleanly
    BackHandler(enabled = uiState.selectedTab != 0) {
        viewModel.setSelectedTab(0)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            val title = when (uiState.selectedTab) {
                0 -> "Career Architect"
                1 -> uiState.activeRoadmap?.targetRole ?: "Career Roadmap"
                2 -> "Saved Roadmaps"
                3 -> "Career Blueprints"
                else -> "AI Career Path"
            }
            val subtitle = when (uiState.selectedTab) {
                0 -> "Personalized Learning & Skill Roadmaps"
                1 -> "Phases, Skills, Resources & Projects"
                2 -> "${savedRoadmaps.size} Saved Plans"
                3 -> "High-Demand Transformation Paths"
                else -> null
            }
            CareerTopAppBar(
                title = title,
                subtitle = subtitle,
                onApiKeyClick = { viewModel.setShowApiKeyDialog(true) }
            )
        },
        bottomBar = {
            NavigationBar(modifier = Modifier.testTag("bottom_nav_bar")) {
                NavigationBarItem(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Architect") },
                    label = { Text("Architect") },
                    modifier = Modifier.testTag("nav_tab_architect")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 1,
                    onClick = {
                        if (uiState.activeRoadmap == null && savedRoadmaps.isNotEmpty()) {
                            viewModel.selectRoadmap(savedRoadmaps.first())
                        } else if (uiState.activeRoadmap == null) {
                            viewModel.selectRoadmap(DefaultRoadmaps.createAiEngineerRoadmap())
                        } else {
                            viewModel.setSelectedTab(1)
                        }
                    },
                    icon = { Icon(Icons.Default.Timeline, contentDescription = "Roadmap") },
                    label = { Text("Roadmap") },
                    modifier = Modifier.testTag("nav_tab_roadmap")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = "Saved") },
                    label = { Text("Saved") },
                    modifier = Modifier.testTag("nav_tab_saved")
                )
                NavigationBarItem(
                    selected = uiState.selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = { Icon(Icons.Default.Explore, contentDescription = "Blueprints") },
                    label = { Text("Blueprints") },
                    modifier = Modifier.testTag("nav_tab_blueprints")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> GenerateScreen(
                    uiState = uiState,
                    viewModel = viewModel
                )
                1 -> {
                    val currentRoadmap = uiState.activeRoadmap ?: savedRoadmaps.firstOrNull() ?: DefaultRoadmaps.createAiEngineerRoadmap()
                    RoadmapDetailScreen(
                        roadmap = currentRoadmap,
                        viewModel = viewModel,
                        onBack = { viewModel.setSelectedTab(0) }
                    )
                }
                2 -> SavedRoadmapsScreen(
                    roadmaps = savedRoadmaps,
                    viewModel = viewModel,
                    onSelectRoadmap = { roadmap -> viewModel.selectRoadmap(roadmap) },
                    onCreateNew = { viewModel.setSelectedTab(0) }
                )
                3 -> PresetsScreen(
                    viewModel = viewModel,
                    onViewRoadmap = { roadmap -> viewModel.selectRoadmap(roadmap) }
                )
            }
        }
    }

    ApiKeyConfigDialog(
        isOpen = uiState.showApiKeyDialog,
        currentKey = uiState.customApiKey,
        onDismiss = { viewModel.setShowApiKeyDialog(false) },
        onSave = { key -> viewModel.updateCustomApiKey(key) }
    )
}
