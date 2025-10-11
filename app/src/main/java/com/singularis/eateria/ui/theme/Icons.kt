package com.singularis.eateria.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Material Design 3 Icon System
 * Provides consistent, dynamic icons throughout the app
 * Uses filled icons for selected/active states, outlined for default states
 */
object AppIcons {
    
    // Navigation Icons
    object Navigation {
        val home = Icons.Filled.Home
        val homeOutlined = Icons.Outlined.Home
        val profile = Icons.Filled.AccountCircle
        val profileOutlined = Icons.Outlined.AccountCircle
        val back = Icons.AutoMirrored.Filled.ArrowBack
        val forward = Icons.AutoMirrored.Filled.ArrowForward
        val close = Icons.Filled.Close
        val menu = Icons.Filled.Menu
        val more = Icons.Filled.MoreVert
    }
    
    // Action Icons
    object Actions {
        val add = Icons.Filled.Add
        val addCircle = Icons.Filled.AddCircle
        val addCircleOutlined = Icons.Outlined.AddCircle
        val delete = Icons.Filled.Delete
        val deleteOutlined = Icons.Outlined.Delete
        val edit = Icons.Filled.Edit
        val editOutlined = Icons.Outlined.Edit
        val save = Icons.Filled.Save
        val saveOutlined = Icons.Outlined.Save
        val share = Icons.Filled.Share
        val shareOutlined = Icons.Outlined.Share
        val search = Icons.Filled.Search
        val searchOutlined = Icons.Outlined.Search
        val refresh = Icons.Filled.Refresh
        val download = Icons.Filled.Download
        val upload = Icons.Filled.Upload
    }
    
    // Food & Health Icons
    object FoodHealth {
        val restaurant = Icons.Filled.Restaurant
        val restaurantOutlined = Icons.Outlined.Restaurant
        val food = Icons.Filled.Fastfood
        val foodOutlined = Icons.Outlined.Fastfood
        val fitnessCentercompany = Icons.Filled.FitnessCenter
        val fitnessCenterOutlined = Icons.Outlined.FitnessCenter
        val favorite = Icons.Filled.Favorite
        val favoriteOutlined = Icons.Outlined.Favorite
        val favoriteBorder = Icons.Filled.FavoriteBorder
        val local_drink = Icons.Filled.LocalDrink
        val scale = Icons.Filled.MonitorWeight
        val scaleOutlined = Icons.Outlined.MonitorWeight
        val wineBar = Icons.Filled.WineBar
    }
    
    // Camera & Media Icons
    object Media {
        val camera = Icons.Filled.Camera
        val cameraAlt = Icons.Filled.CameraAlt
        val photoCamera = Icons.Filled.PhotoCamera
        val photoLibrary = Icons.Filled.PhotoLibrary
        val photoLibraryOutlined = Icons.Outlined.PhotoLibrary
        val image = Icons.Filled.Image
        val imageOutlined = Icons.Outlined.Image
        val brokenImage = Icons.Filled.BrokenImage
    }
    
    // Status & Feedback Icons
    object Status {
        val check = Icons.Filled.Check
        val checkCircle = Icons.Filled.CheckCircle
        val checkCircleOutlined = Icons.Outlined.CheckCircle
        val error = Icons.Filled.Error
        val errorOutlined = Icons.Outlined.Error
        val warning = Icons.Filled.Warning
        val warningOutlined = Icons.Outlined.Warning
        val info = Icons.Filled.Info
        val infoOutlined = Icons.Outlined.Info
        val notifications = Icons.Filled.Notifications
        val notificationsOutlined = Icons.Outlined.Notifications
        val notificationsActive = Icons.Filled.NotificationsActive
        val notificationsOff = Icons.Filled.NotificationsOff
    }
    
    // Date & Time Icons
    object DateTime {
        val calendar = Icons.Filled.CalendarToday
        val calendarOutlined = Icons.Outlined.CalendarToday
        val calendarMonth = Icons.Filled.CalendarMonth
        val schedule = Icons.Filled.Schedule
        val scheduleOutlined = Icons.Outlined.Schedule
        val today = Icons.Filled.Today
        val todayOutlined = Icons.Outlined.Today
    }
    
    // Settings & Configuration Icons
    object Settings {
        val settings = Icons.Filled.Settings
        val settingsOutlined = Icons.Outlined.Settings
        val language = Icons.Filled.Language
        val darkMode = Icons.Filled.DarkMode
        val darkModeOutlined = Icons.Outlined.DarkMode
        val lightMode = Icons.Filled.LightMode
        val lightModeOutlined = Icons.Outlined.LightMode
        val palette = Icons.Filled.Palette
        val paletteOutlined = Icons.Outlined.Palette
        val accessibility = Icons.Filled.Accessibility
        val accessibilityNew = Icons.Filled.AccessibilityNew
    }
    
    // Social & Communication Icons
    object Social {
        val person = Icons.Filled.Person
        val personOutlined = Icons.Outlined.Person
        val personAdd = Icons.Filled.PersonAdd
        val personAddOutlined = Icons.Outlined.PersonAdd
        val people = Icons.Filled.People
        val peopleOutlined = Icons.Outlined.People
        val feedback = Icons.Filled.Feedback
        val feedbackOutlined = Icons.Outlined.Feedback
        val email = Icons.Filled.Email
        val emailOutlined = Icons.Outlined.Email
    }
    
    // Statistics & Analytics Icons
    object Statistics {
        val trendingUp = Icons.AutoMirrored.Filled.TrendingUp
        val trendingDown = Icons.AutoMirrored.Filled.TrendingDown
        val showChart = Icons.Filled.ShowChart
        val barChart = Icons.Filled.BarChart
        val pieChart = Icons.Filled.PieChart
        val analytics = Icons.Filled.Analytics
        val analyticsOutlined = Icons.Outlined.Analytics
        val insights = Icons.Filled.Insights
        val psychology = Icons.Filled.Psychology
    }
    
    // System & Other Icons
    object System {
        val logout = Icons.AutoMirrored.Filled.ExitToApp
        val login = Icons.AutoMirrored.Filled.Login
        val help = Icons.Filled.Help
        val helpOutlined = Icons.Outlined.Help
        val arrowRight = Icons.AutoMirrored.Filled.KeyboardArrowRight
        val arrowLeft = Icons.AutoMirrored.Filled.KeyboardArrowLeft
        val arrowUp = Icons.Filled.KeyboardArrowUp
        val arrowDown = Icons.Filled.KeyboardArrowDown
        val expandMore = Icons.Filled.ExpandMore
        val expandLess = Icons.Filled.ExpandLess
        val visibility = Icons.Filled.Visibility
        val visibilityOff = Icons.Filled.VisibilityOff
    }
    
    /**
     * Get icon based on state (selected/default)
     */
    @Composable
    fun getIconForState(
        selectedIcon: ImageVector,
        defaultIcon: ImageVector,
        isSelected: Boolean
    ): ImageVector {
        return if (isSelected) selectedIcon else defaultIcon
    }
}

/**
 * Extension functions for common icon state patterns
 */
object IconModifiers {
    /**
     * Get the appropriate icon variant based on selection state
     */
    fun forSelection(isSelected: Boolean, filled: ImageVector, outlined: ImageVector): ImageVector {
        return if (isSelected) filled else outlined
    }
    
    /**
     * Get the appropriate icon for boolean state (on/off)
     */
    fun forToggle(isOn: Boolean, onIcon: ImageVector, offIcon: ImageVector): ImageVector {
        return if (isOn) onIcon else offIcon
    }
}

