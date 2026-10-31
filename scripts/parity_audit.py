#!/usr/bin/env python3
"""Build docs/ios_android_parity_map.json from the iOS working tree and this repo.

Re-running refreshes localization, proto field diffs, and endpoint presence.
Feature statuses already saved in the JSON are kept, unless --mark-done is passed.

Usage:
  python3 scripts/parity_audit.py
  python3 scripts/parity_audit.py --mark-done 1_network
"""

from __future__ import annotations

import argparse
import json
import re
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
IOS = Path("/Users/dante/Documents/dante/Documents/eater/eater")
OUT = ROOT / "docs" / "ios_android_parity_map.json"

FIELD_RE = re.compile(
    r"^\s*(repeated\s+|optional\s+)?([A-Za-z_][\w.]*)\s+([A-Za-z_]\w*)\s*=\s*(\d+)\s*;"
)


def feat(
    chapter: str,
    feature: str,
    ios_files: list[str],
    ios_symbols: list[str],
    android_files: list[str],
    android_symbols: list[str],
    status: str,
    change: str,
    endpoint: dict | None = None,
    loc_prefixes: list[str] | None = None,
    ios_commit: str = "",
    notes: str = "",
) -> dict:
    return {
        "chapter": chapter,
        "feature": feature,
        "ios": {"files": ios_files, "symbols": ios_symbols},
        "android": {"files": android_files, "symbols": android_symbols},
        "endpoint": endpoint,
        "loc_prefixes": loc_prefixes or [],
        "status": status,
        "change": change,
        "ios_commit": ios_commit,
        "notes": notes,
    }


FEATURES: list[dict] = [
    feat(
        "1_network",
        "Typed meal proto and sendFoodText",
        ["Services/eater_text.proto", "Services/GRPCService.swift"],
        ["sendFoodText", "FoodTextMessage", "FoodTextResponse"],
        ["app/src/main/proto/eater_text.proto", "services/GRPCService.kt"],
        ["sendFoodText"],
        "missing",
        "Add eater_text.proto (package eater) and GRPCService.sendFoodText: POST eater_receive_text, protobuf, 70s timeout, error codes not_food|blocked|empty|too_long|timeout.",
        {"path": "eater_receive_text", "method": "POST", "content_type": "application/protobuf"},
        ["type_food", "error"],
        "bb93fee",
    ),
    feat(
        "1_network",
        "Day score proto and getFoodScore",
        ["Services/get_food_score.proto", "Services/GRPCService.swift"],
        ["getFoodScore", "FoodScoreRequest", "FoodScoreResponse"],
        ["app/src/main/proto/get_food_score.proto", "services/GRPCService.kt"],
        ["getFoodScore"],
        "missing",
        "Add get_food_score.proto and GRPCService.getFoodScore. Request user_name + day yyyy-MM-dd. Response has_score, score, dish_time.",
        {"path": "get_food_score", "method": "POST", "content_type": "application/protobuf"},
        ["health.score"],
        "921884d",
    ),
    feat(
        "1_network",
        "Explain score proto and explainFoodScore",
        ["Services/explain_food_score.proto", "Services/GRPCService.swift"],
        ["explainFoodScore"],
        ["app/src/main/proto/explain_food_score.proto", "services/GRPCService.kt"],
        ["explainFoodScore"],
        "missing",
        "Add explain_food_score.proto and GRPCService.explainFoodScore. dish_time 0 explains the day; otherwise that dish.",
        {"path": "explain_food_score", "method": "POST", "content_type": "application/protobuf"},
        ["health", "disc"],
        "921884d",
    ),
    feat(
        "1_network",
        "Meal suggest proto and getMealSuggest",
        ["Services/meal_suggest.proto", "Services/GRPCService.swift"],
        ["getMealSuggest"],
        ["app/src/main/proto/meal_suggest.proto", "services/GRPCService.kt"],
        ["getMealSuggest"],
        "missing",
        "Add meal_suggest.proto and GRPCService.getMealSuggest: POST meal_suggest, protobuf, 90s, no retry, quota-aware. days=7, variant 0-based, remaining macros, meals_today.",
        {"path": "meal_suggest", "method": "POST", "content_type": "application/protobuf"},
        ["meal_planner", "meal"],
        "b92d6e6",
    ),
    feat(
        "1_network",
        "Statistics range proto and fetchStatisticsRange",
        ["Services/statistics_range.proto", "Services/GRPCService.swift"],
        ["fetchStatisticsRange"],
        ["app/src/main/proto/statistics_range.proto", "services/GRPCService.kt"],
        ["fetchStatisticsRange"],
        "missing",
        "Add statistics_range.proto and GRPCService.fetchStatisticsRange. Raw protobuf body, Content-Type and Accept application/grpc+proto, not gRPC-framed. Dates dd-MM-yyyy. 401 is unauthorized, not a retry.",
        {"path": "get_statistics_range", "method": "POST", "content_type": "application/grpc+proto"},
        ["stats"],
        "db07278",
    ),
    feat(
        "1_network",
        "Dish contains and alternative_names",
        ["Services/today_food.proto", "Services/custom_date_food.proto"],
        ["Dish.contains", "Dish.alternative_names"],
        ["app/src/main/proto/today_food.proto", "app/src/main/proto/custom_date_food.proto", "models/Product.kt"],
        ["Product.alternativeNames", "Product.contains"],
        "missing",
        "Add Dish.contains = 8 and repeated alternative_names = 9 to both today_food and custom_date_food. Map them onto Product when parsing.",
        None,
        [],
        "ce0db0e",
        "Android today_food has no package; custom_date_food uses package eater. Keep that split.",
    ),
    feat(
        "1_network",
        "Share response nickname_used",
        ["Services/share_food.proto"],
        ["ShareFoodResponse.nickname_used"],
        ["app/src/main/proto/share_food.proto", "services/GRPCService.kt"],
        ["shareFood"],
        "missing",
        "Add nickname_used = 2 on ShareFoodResponse. Keep package eater.",
        {"path": "autocomplete/sharefood", "method": "POST", "content_type": "application/protobuf"},
        ["share"],
        "",
    ),
    feat(
        "1_network",
        "Profile JSON endpoints",
        ["Services/GRPCService.swift"],
        ["fetchProfile", "updateProfile", "uploadProfilePhoto"],
        ["services/GRPCService.kt"],
        ["profileGet", "profileUpdate", "profilePhoto"],
        "missing",
        "Add profile_get GET JSON, profile_update POST JSON (first_name, last_name, optional profile_picture_id), profile_photo multipart JPEG field photo.",
        {"path": "profile_get", "method": "GET", "content_type": "application/json"},
        ["profile"],
        "",
        "Also profile_update and profile_photo.",
    ),
    feat(
        "1_network",
        "Daily limit JSON parser",
        ["Services/GRPCService.swift"],
        ["presentDailyLimitIfNeeded"],
        ["services/GRPCService.kt", "ui/views/AlertHelper.kt"],
        ["dailyLimitOrNull"],
        "missing",
        "Parse {error_code:daily_limit_reached, is_guest, limit} on photo, text, recommendation and meal_suggest. Do not retry those 4xx bodies.",
        None,
        ["quota"],
        "db07278",
    ),
    feat(
        "1_network",
        "Remove multi-photo proto",
        [],
        [],
        ["app/src/main/proto/multiple_photos.proto", "services/GRPCService.kt"],
        ["sendMultiplePhotos"],
        "remove",
        "Delete multiple_photos.proto and the sendMultiplePhotos loop. iOS removed the batch pipeline in the 5.3 working tree.",
        None,
        [],
        "",
        "iOS deleted multiple_photos.proto, MultiplePhotoPickerView, MealDetailSheet.",
    ),
    feat(
        "1_network",
        "Remove getChessStats",
        ["Services/GRPCService.swift"],
        [],
        ["services/GRPCService.kt"],
        ["getChessStats"],
        "remove",
        "Delete GRPCService.getChessStats (POST autocomplete/get_chess_stats). iOS working tree removed it. Keep get_chess_history and get_all_chess_data.",
        {"path": "autocomplete/get_chess_stats", "method": "POST", "content_type": "application/json"},
        ["chess"],
        "",
    ),
    feat(
        "2_localization",
        "Import missing and changed strings",
        ["Localization/en.json", "Localization/*.json"],
        ["loc"],
        ["app/src/main/assets/Localization/en.json", "scripts/sync-localization.py"],
        ["Localization.tr"],
        "missing",
        "Import iOS keys missing on Android and overwrite the 27 changed English (and locale) values. Map pt-BR.json to pt-br.json.",
        None,
        [],
        "c30ac9c",
    ),
    feat(
        "2_localization",
        "Clean Android-only keys",
        ["Localization/en.json"],
        [],
        ["app/src/main/assets/Localization/en.json"],
        [],
        "partial",
        "Drop Android-only keys that no Kotlin file references. Keep keys still used, or rename them onto the iOS key when one exists.",
        None,
        [],
        "",
    ),
    feat(
        "2_localization",
        "Quote files",
        ["Localization/quotes"],
        ["FoodQuotesLocalized"],
        ["app/src/main/assets/Localization/quotes", "services/QuotesService.kt"],
        ["QuotesService"],
        "partial",
        "Copy quote txt files from iOS when the Android copy differs.",
        None,
        ["notif"],
        "",
    ),
    feat(
        "3_design",
        "Design tokens",
        ["DesignSystem.swift"],
        ["AppTheme"],
        ["ui/theme/DesignSystem.kt", "ui/theme/Color.kt", "ui/theme/Dimensions.kt"],
        ["AppTheme"],
        "partial",
        "Match 5.3 colors (accent light 0,0.42,0.82 / dark 0.1,0.58,1), success, warning, danger, macro colors, radii 16/12/8, card shadow, background gradient, mascot callout blue-to-violet. Drop tokens iOS removed.",
        None,
        [],
        "0e89e57",
    ),
    feat(
        "3_design",
        "Liquid glass surfaces",
        ["DesignSystem.swift"],
        ["appSurface", "appCircleSurface", "appTintedGlass", "appSheetBackground"],
        ["ui/theme/DesignSystem.kt"],
        ["appSurface", "appCircleSurface", "appTintedGlass", "appSheetBackground"],
        "missing",
        "Compose modifiers: translucent tint, stroke, blur on API 31+, solid fallback below.",
        None,
        [],
        "0e89e57",
    ),
    feat(
        "3_design",
        "Unified sheets and notices",
        ["Views/ScaledNotice.swift", "DesignSystem.swift"],
        ["ScaledNotice", "appSheetBackground"],
        ["ui/views/AppSheet.kt", "ui/views/AlertHelper.kt"],
        ["AppSheet", "AppNotice"],
        "missing",
        "One ModalBottomSheet helper (medium/large, drag handle, sheet background) and an AppNotice. Replace AlertDialog/Dialog flows so sheets open the same way.",
        None,
        ["common"],
        "0e89e57",
    ),
    feat(
        "3_design",
        "App font scale",
        ["DesignSystem.swift", "Services/AppSettingsService.swift"],
        ["appInterfaceScale", "fontScale"],
        ["services/AppSettingsService.kt", "MainActivity.kt", "ui/views/UserProfileView.kt"],
        ["fontScale"],
        "missing",
        "Store fontScale 0.80-1.60 and apply it with CompositionLocalProvider(LocalDensity) in MainActivity. Profile control uses profile.font_size* keys.",
        None,
        ["profile.font_size", "a11y"],
        "0c38ce5",
    ),
    feat(
        "4_shell",
        "Bottom tab bar",
        ["Views/RootTabView.swift"],
        ["AppTab", "AppBottomBar"],
        ["ui/views/RootTabView.kt", "ui/views/ContentView.kt"],
        ["RootTabView", "AppBottomBar"],
        "missing",
        "Replace the 3-page HorizontalPager with Today, Ideas, raised Camera, Upload, Stats.",
        None,
        ["tab", "camera"],
        "db07278",
    ),
    feat(
        "4_shell",
        "App navigation state",
        ["Services/AppNavigation.swift"],
        ["AppNavigation", "PendingMeal"],
        ["navigation/AppNavigation.kt"],
        ["AppNavigation", "PendingMeal"],
        "missing",
        "State holder for selected tab, sheet requests (camera, type food, weight, activities, alcohol, guest login, backdating), toasts and PendingMeal.",
        None,
        [],
        "0e89e57",
    ),
    feat(
        "4_shell",
        "Swipe down to Today",
        ["Views/SwipeDownToTodayModifier.swift"],
        ["swipeDownToToday"],
        ["ui/views/RootTabView.kt"],
        ["swipeDownToToday"],
        "missing",
        "Swipe down on Ideas and Stats returns to Today. Keep food-row swipe-right camera.",
        None,
        ["common.close"],
        "d7abb40",
    ),
    feat(
        "5_home",
        "Today header",
        ["ContentView.swift"],
        ["macrosLineView"],
        ["ui/views/ComponentViews.kt", "ui/views/ContentView.kt"],
        ["TopBarView", "StatsButtonsView"],
        "partial",
        "Header: profile, alcohol, date chip with return-to-today, server day-score ring, sport. Second row: weight, calories left, Type.",
        None,
        ["type_food", "calories", "backdating", "macro"],
        "0e89e57",
    ),
    feat(
        "5_home",
        "Calories left sheet",
        ["Views/CaloriesLeftSheet.swift"],
        ["CaloriesLeftSheet"],
        ["ui/views/CaloriesLeftSheet.kt"],
        ["CaloriesLeftSheet"],
        "missing",
        "Port the calories-left sheet.",
        None,
        ["calories"],
        "",
    ),
    feat(
        "5_home",
        "Weight prompt sheet",
        ["Views/WeightPromptSheet.swift", "Views/WeightCameraView.swift"],
        ["WeightPromptSheet"],
        ["ui/views/WeightPromptSheet.kt", "ui/views/CameraViews.kt"],
        ["WeightPromptSheet"],
        "partial",
        "Port the typed-weight sheet. Weight camera already exists.",
        {"path": "manual_weight", "method": "POST", "content_type": "application/protobuf"},
        ["weight"],
        "",
    ),
    feat(
        "5_home",
        "Dish health sheet",
        ["Views/DishHealthSheet.swift", "Views/FoodHealthPopup.swift"],
        ["DishHealthSheet", "FoodHealthPopup"],
        ["ui/views/DishHealthSheet.kt", "ui/views/ProductRowView.kt"],
        ["DishHealthSheet"],
        "missing",
        "Open DishHealthSheet from the existing health popup. Card popup stays on food_health_level; the sheet explains via explain_food_score.",
        {"path": "food_health_level", "method": "POST", "content_type": "application/protobuf"},
        ["health", "disc"],
        "b71b144",
    ),
    feat(
        "5_home",
        "Fix food name",
        ["Views/FixFoodNameView.swift", "Services/DishNameNormalizer.swift"],
        ["FixFoodNameView", "DishNameNormalizer"],
        ["ui/views/FixFoodNameView.kt", "services/DishNameNormalizer.kt"],
        ["FixFoodNameView", "DishNameNormalizer"],
        "partial",
        "Show alternative_names first, then suggest_dish_names. Run typed input through DishNameNormalizer. Save still calls modify_food_manual.",
        {"path": "suggest_dish_names", "method": "POST", "content_type": "application/json"},
        ["manual_food"],
        "0e89e57",
    ),
    feat(
        "5_home",
        "Pending meal row",
        ["Services/AppNavigation.swift", "Views/ProductListView.swift"],
        ["PendingMeal"],
        ["ui/views/ProductListView.kt", "navigation/AppNavigation.kt"],
        ["PendingMeal"],
        "missing",
        "Show a placeholder row while a photo or typed meal is being analyzed.",
        None,
        [],
        "",
    ),
    feat(
        "6_camera",
        "Camera and upload from the tab bar",
        ["Views/CameraView.swift"],
        ["CameraView", "PhotoLibraryView"],
        ["ui/views/CameraViews.kt", "ui/views/CameraButtonView.kt"],
        ["FoodCameraView", "CameraButtonView"],
        "partial",
        "Rename CameraButtonView usage to CameraView. Camera and Upload are tab-bar actions, photoType default_prompt.",
        {"path": "eater_receive_photo", "method": "POST", "content_type": "application/protobuf"},
        ["camera"],
        "",
        "iOS renamed CameraButtonView.swift to CameraView.swift in the working tree.",
    ),
    feat(
        "6_camera",
        "Type food sheet",
        ["Views/TypeFoodSheet.swift"],
        ["TypeFoodSheet"],
        ["ui/views/TypeFoodSheet.kt"],
        ["TypeFoodSheet"],
        "missing",
        "Sheet calls sendFoodText. Banner when the selected date is in the past.",
        {"path": "eater_receive_text", "method": "POST", "content_type": "application/protobuf"},
        ["type_food"],
        "bb93fee",
    ),
    feat(
        "6_camera",
        "Backdating notice",
        ["Services/AppNavigation.swift", "Views/ScaledNotice.swift"],
        ["presentBackdatingPrompt"],
        ["navigation/AppNavigation.kt", "ui/views/AppSheet.kt"],
        ["presentBackdatingPrompt"],
        "partial",
        "Confirm / Log today / Cancel as AppNotice. Backdated meals use a noon-UTC timestamp.",
        None,
        ["backdating"],
        "6a4d9da",
    ),
    feat(
        "6_camera",
        "Remove multi-photo upload button",
        [],
        [],
        ["ui/views/CameraButtonView.kt"],
        ["MultiplePhotoUploadButton"],
        "remove",
        "Delete MultiplePhotoUploadButton. Uploads stay one photo at a time.",
        None,
        [],
        "",
    ),
    feat(
        "7_day_score",
        "Day score fetch and poll",
        ["ContentView.swift"],
        ["refreshDayFoodScore", "requestDayFoodScore"],
        ["viewmodels/MainViewModel.kt", "ui/views/ContentView.kt"],
        ["refreshDayFoodScore"],
        "missing",
        "Replace the local health-rating average with getFoodScore. Fetch after load, date change, photo/text success and account merge. Poll at 2s, 4s, 8s while newest dish time is newer than dish_time. Ring shows spinner, dash, or 0-100.",
        {"path": "get_food_score", "method": "POST", "content_type": "application/protobuf"},
        ["health.score"],
        "921884d",
    ),
    feat(
        "7_day_score",
        "Score explanation cache",
        ["Services/FoodScoreExplainStore.swift", "Views/HealthDisclaimerView.swift", "Views/DishHealthSheet.swift"],
        ["FoodScoreExplainStore"],
        ["services/FoodScoreExplainStore.kt", "ui/views/HealthDisclaimerView.kt", "ui/views/DishHealthSheet.kt"],
        ["FoodScoreExplainStore"],
        "missing",
        "Cache day|email and dish|email|time. Invalidate when basisDishTime changes. Disclaimer explains the day (dish_time 0). Dish sheet explains that dish.",
        {"path": "explain_food_score", "method": "POST", "content_type": "application/protobuf"},
        ["disc", "health"],
        "98b2d11",
    ),
    feat(
        "7_day_score",
        "Health plan calculator",
        ["Services/HealthPlanCalculator.swift"],
        ["HealthPlanCalculator", "HealthUnits"],
        ["services/HealthPlanCalculator.kt", "services/HealthDataService.kt"],
        ["HealthPlanCalculator"],
        "partial",
        "Port BMR, TDEE, BMI and calorie caps. HealthDataService already stores the same preference keys.",
        None,
        ["health"],
        "921884d",
    ),
    feat(
        "7_day_score",
        "Health settings layout",
        ["Views/HealthSettingsView.swift", "Views/HealthFormComponents.swift"],
        ["HealthSettingsView"],
        ["ui/views/HealthSettingsView.kt"],
        ["HealthSettingsView"],
        "partial",
        "Save at the bottom, Back at the top right, onSaved callback. Remove the BMI scale bar.",
        {"path": "goal_update", "method": "POST", "content_type": "application/json"},
        ["health"],
        "0095c5a",
    ),
    feat(
        "7_day_score",
        "Health Connect",
        ["Services/AppleHealthService.swift"],
        ["AppleHealthService"],
        ["services/HealthConnectService.kt", "ui/views/HealthSettingsView.kt"],
        ["HealthConnectService"],
        "missing",
        "Read profile (height, weight, age, sex) and optional active-energy share 50/75/100 percent into the local calorie limit. Never upload.",
        None,
        ["health.apple"],
        "921884d",
        "Android equivalent of Apple Health. Reads only.",
    ),
    feat(
        "8_stats",
        "Statistics range service",
        ["Services/StatisticsService.swift", "Services/StatisticsCacheService.swift"],
        ["fetchStatisticsForPeriod"],
        ["services/StatisticsService.kt", "services/StatisticsCacheService.kt"],
        ["fetchStatisticsForPeriod"],
        "partial",
        "Stop per-day eater_get_today / get_food_custom_date loops. Use get_statistics_range and fill empty days locally. Trim the cache to the iOS shape.",
        {"path": "get_statistics_range", "method": "POST", "content_type": "application/grpc+proto"},
        ["stats"],
        "db07278",
    ),
    feat(
        "8_stats",
        "Statistics cards and graphs",
        ["Views/StatisticsView.swift", "Views/StatisticsGraphsView.swift"],
        ["StatisticsView", "StatisticsGraphsView"],
        ["ui/views/StatisticsView.kt", "ui/views/StatisticsGraphsView.kt"],
        ["StatisticsView", "StatisticsGraphsView"],
        "partial",
        "Week/month cards: hero, energy, body, plate, protein, consistency, plus a day detail sheet. Move the current charts into StatisticsGraphsView, one tap away.",
        None,
        ["stats"],
        "facece2",
    ),
    feat(
        "8_stats",
        "Statistics guest 401",
        ["Services/GRPCService.swift", "Views/StatisticsView.swift"],
        ["fetchStatisticsRange"],
        ["services/StatisticsService.kt", "ui/views/StatisticsView.kt"],
        ["fetchStatisticsRange"],
        "missing",
        "A 401 on the range call opens the sign-in sheet for guests.",
        None,
        ["login"],
        "",
    ),
    feat(
        "9_ideas",
        "Ideas tab",
        ["Views/IdeasTabView.swift"],
        ["IdeasTabView"],
        ["ui/views/IdeasTabView.kt"],
        ["IdeasTabView"],
        "missing",
        "Ideas tab hosts the meal planner and weekly advice. Swipe down returns to Today.",
        None,
        ["tab.ideas", "ideas", "rec"],
        "db07278",
    ),
    feat(
        "9_ideas",
        "Meal planner",
        ["Views/MealPlannerView.swift"],
        ["MealPlannerView", "MealPlannerRemaining", "MealPlanResult"],
        ["ui/views/MealPlannerView.kt"],
        ["MealPlannerView"],
        "missing",
        "Call meal_suggest and cycle 3 cached variants from remaining macros and meals_today.",
        {"path": "meal_suggest", "method": "POST", "content_type": "application/protobuf"},
        ["meal_planner", "meal"],
        "7f30c72",
    ),
    feat(
        "9_ideas",
        "Weekly advice inside Ideas",
        ["Views/RecommendationView.swift", "Views/IdeasTabView.swift"],
        ["RecommendationView"],
        ["ui/views/RecommendationView.kt", "ui/views/IdeasTabView.kt"],
        ["RecommendationView"],
        "partial",
        "Move the existing get_recommendation (7 days) view into Ideas. Honor the daily limit.",
        {"path": "get_recommendation", "method": "POST", "content_type": "application/protobuf"},
        ["rec", "quota"],
        "",
    ),
    feat(
        "10_activities",
        "Slim chess entry",
        ["Views/ActivitiesView.swift"],
        ["ActivitiesView"],
        ["ui/views/ActivitiesView.kt", "services/GRPCService.kt"],
        ["ActivitiesView"],
        "partial",
        "Match the simplified iOS chess entry. Do not call getChessStats. Keep history and record_chess_game.",
        {"path": "record_chess_game", "method": "POST", "content_type": "application/json"},
        ["activities", "chess"],
        "",
    ),
    feat(
        "10_activities",
        "Remove unused ActivitiesViewModel",
        [],
        [],
        ["viewmodels/ActivitiesViewModel.kt"],
        ["ActivitiesViewModel"],
        "remove",
        "Delete ActivitiesViewModel.kt. The screen already talks to GRPCService.",
        None,
        [],
        "",
    ),
    feat(
        "10_activities",
        "Activity calorie budget",
        ["Views/ActivitiesView.swift", "ContentView.swift"],
        [],
        ["ui/views/ActivitiesView.kt", "viewmodels/MainViewModel.kt"],
        [],
        "partial",
        "Logged activities adjust the Home calorie budget the same way iOS does.",
        {"path": "activity_log", "method": "POST", "content_type": "application/json"},
        ["activities"],
        "",
    ),
    feat(
        "11_profile",
        "Local profile stores",
        ["Services/ProfileLocalStore.swift", "Services/ProfilePhotoStore.swift"],
        ["ProfileLocalStore", "ProfilePhotoStore"],
        ["services/ProfileLocalStore.kt", "services/ProfilePhotoStore.kt"],
        ["ProfileLocalStore", "ProfilePhotoStore"],
        "missing",
        "Store profile under filesDir/EateriaProfile/<email>/ so it survives logout. Sync with profile_get, profile_update and profile_photo.",
        {"path": "profile_update", "method": "POST", "content_type": "application/json"},
        ["profile"],
        "",
    ),
    feat(
        "11_profile",
        "My friends",
        ["Views/MyFriendsView.swift", "Views/AddFriendsView.swift"],
        ["MyFriendsView", "AddFriendsView"],
        ["ui/views/MyFriendsView.kt", "ui/views/AddFriendsView.kt"],
        ["MyFriendsView"],
        "partial",
        "Add MyFriendsView. AddFriendsView and the websocket search already exist. Hide anonymous emails.",
        {"path": "autocomplete/getfriend", "method": "GET", "content_type": "application/protobuf"},
        ["friends"],
        "b11cf37",
    ),
    feat(
        "11_profile",
        "Language sheet and app info",
        ["Views/LanguageSelectionSheet.swift", "Views/AppInfoView.swift"],
        ["LanguageSelectionSheet", "AppInfoView"],
        ["ui/views/LanguageSelectionSheet.kt", "ui/views/AppInfoView.kt", "ui/views/LoginView.kt"],
        ["LanguageSelectionSheet", "AppInfoView"],
        "missing",
        "Language picker sheet. App info is reachable before login.",
        None,
        ["profile.language", "info"],
        "",
    ),
    feat(
        "11_profile",
        "Profile section order",
        ["Views/UserProfileView.swift"],
        ["UserProfileView"],
        ["ui/views/UserProfileView.kt"],
        ["UserProfileView"],
        "partial",
        "Order: avatar, nickname, health, appearance, font size, language, notifications, mascot, friends, feedback, delete.",
        None,
        ["profile", "appearance", "nickname"],
        "",
    ),
    feat(
        "12_onboarding",
        "Onboarding rewrite",
        ["Views/OnboardingView.swift", "Assets.xcassets"],
        ["OnboardingView"],
        ["ui/views/OnboardingView.kt", "ui/views/OnboardingStepViews.kt"],
        ["OnboardingView"],
        "partial",
        "Port the 5.2 onboarding screens and typing animation. Copy onboarding_* and meal_planner images.",
        None,
        ["onboarding"],
        "b71b144",
    ),
    feat(
        "12_onboarding",
        "Tutorial steps",
        ["Views/MainAppTutorialView.swift"],
        ["TutorialStep"],
        ["ui/views/MainAppTutorialView.kt", "ui/views/ContentView.kt"],
        ["MainAppTutorialView"],
        "partial",
        "Steps: Camera, Calories, HealthScore, Sport, Weight, TypeGuide, Calendar, Alcohol, Stats. Wire the camera tutorial (onRequestTutorial is currently null).",
        None,
        ["tutorial"],
        "",
    ),
    feat(
        "12_onboarding",
        "Notification permission in progressive onboarding",
        ["Views/ProgressiveOnboardingView.swift"],
        ["ProgressiveOnboardingView"],
        ["ui/views/ProgressiveOnboardingView.kt"],
        ["ProgressiveOnboardingView"],
        "partial",
        "Request POST_NOTIFICATIONS on the notifications step. The TODO is already in the file.",
        None,
        ["onboarding"],
        "",
    ),
    feat(
        "13_notifications",
        "Cancel remaining reminders after a snap",
        ["Services/NotificationService.swift"],
        ["recordFoodSnap"],
        ["services/ReminderService.kt", "services/ReminderScheduler.kt", "work/FoodReminderWorker.kt"],
        ["recordFoodSnap", "scheduleAll"],
        "partial",
        "Times 12:00 / 17:00 / 21:00 and the 14-day horizon already match. A snap today must cancel every remaining reminder today, not only the next one.",
        None,
        ["notif"],
        "921884d",
    ),
    feat(
        "13_notifications",
        "Reschedule triggers",
        ["Services/NotificationService.swift"],
        ["rescheduleIfEnabled"],
        ["EateriaApplication.kt", "services/LanguageService.kt", "services/ReminderScheduler.kt"],
        ["scheduleAll"],
        "partial",
        "Reschedule on app start, day change and language change.",
        None,
        ["notif"],
        "921884d",
    ),
    feat(
        "14_auth",
        "In-place sign-in sheet",
        ["Views/SignInSheet.swift", "Services/AppNavigation.swift"],
        ["SignInSheet", "queueAnonymousLoginPrompt"],
        ["ui/views/SignInSheet.kt", "navigation/AppNavigation.kt"],
        ["SignInSheet", "queueAnonymousLoginPrompt"],
        "missing",
        "Guest upgrades in place. The sheet waits until the camera has closed.",
        {"path": "eater_auth", "method": "POST", "content_type": "application/json"},
        ["login"],
        "db07278",
    ),
    feat(
        "14_auth",
        "Guest scan prompt cadence",
        ["Services/AuthenticationService.swift"],
        ["recordAnonymousFoodScanIfNeeded"],
        ["services/AuthenticationService.kt", "viewmodels/MainViewModel.kt"],
        ["recordAnonymousFoodScanIfNeeded"],
        "partial",
        "Prompt after scan 5, then every 3 scans. anonymous_auth already exists.",
        {"path": "anonymous_auth", "method": "POST", "content_type": "application/json"},
        ["login.scan_prompt"],
        "",
    ),
    feat(
        "14_auth",
        "Quota dialog and guest gates",
        ["Views/AlertHelper.swift", "ContentView.swift"],
        ["showDailyLimitReached"],
        ["ui/views/AlertHelper.kt", "ui/views/ContentView.kt", "ui/views/ShareFoodView.kt"],
        ["showDailyLimitReached"],
        "partial",
        "Shared daily-limit sheet (quota.*). Guest invite banner on Home. Share and friends stay gated for guests.",
        None,
        ["quota", "login"],
        "",
    ),
]

REMOVED_ON_IOS = [
    {
        "ios": "Services/multiple_photos.proto",
        "android": "app/src/main/proto/multiple_photos.proto",
        "action": "delete on Android",
    },
    {
        "ios": "Services/GRPCService+MultiplePhotos.swift",
        "android": "services/GRPCService.kt sendMultiplePhotos",
        "action": "delete on Android",
    },
    {
        "ios": "Views/MultiplePhotoPickerView.swift",
        "android": "ui/views/CameraButtonView.kt MultiplePhotoUploadButton",
        "action": "delete on Android",
    },
    {
        "ios": "Views/MealDetailSheet.swift",
        "android": None,
        "action": "do not port",
    },
    {
        "ios": "GRPCService.getChessStats",
        "android": "services/GRPCService.kt getChessStats",
        "action": "delete on Android",
    },
    {
        "ios": "Views/HealthFormComponents.swift BMIScaleBar",
        "android": "ui/views/HealthSettingsView.kt BMI bar if present",
        "action": "remove if present",
    },
    {
        "ios": "Views/CameraButtonView.swift renamed to Views/CameraView.swift",
        "android": "ui/views/CameraButtonView.kt",
        "action": "rename call sites to CameraView; file may keep the old name if the composable is renamed",
    },
]

ENDPOINTS = [
    ("eater_get_today", "GET", "application/protobuf"),
    ("get_food_custom_date", "POST", "application/protobuf"),
    ("eater_receive_photo", "POST", "application/protobuf"),
    ("eater_receive_text", "POST", "application/protobuf"),
    ("delete_food", "POST", "application/protobuf"),
    ("modify_food_record", "POST", "application/protobuf"),
    ("modify_food_manual", "POST", "application/json"),
    ("suggest_dish_names", "POST", "application/json"),
    ("get_food_score", "POST", "application/protobuf"),
    ("explain_food_score", "POST", "application/protobuf"),
    ("food_health_level", "POST", "application/protobuf"),
    ("get_recommendation", "POST", "application/protobuf"),
    ("meal_suggest", "POST", "application/protobuf"),
    ("get_statistics_range", "POST", "application/grpc+proto"),
    ("manual_weight", "POST", "application/protobuf"),
    ("alcohol_latest", "GET", "application/grpc+proto"),
    ("alcohol_range", "POST", "application/grpc+proto"),
    ("activity_log", "POST", "application/json"),
    ("activity_summary", "GET", "application/json"),
    ("record_chess_game", "POST", "application/json"),
    ("autocomplete/get_chess_history", "GET", "application/json"),
    ("autocomplete/get_all_chess_data", "GET", "application/json"),
    ("autocomplete/get_chess_stats", "POST", "application/json"),
    ("autocomplete/addfriend", "POST", "application/protobuf"),
    ("autocomplete/getfriend", "GET", "application/protobuf"),
    ("autocomplete/sharefood", "POST", "application/protobuf"),
    ("set_language", "POST", "application/protobuf"),
    ("feedback", "POST", "application/protobuf"),
    ("delete_user", "POST", "application/protobuf"),
    ("nickname_update", "POST", "application/json"),
    ("goal_update", "POST", "application/json"),
    ("profile_get", "GET", "application/json"),
    ("profile_update", "POST", "application/json"),
    ("profile_photo", "POST", "multipart/form-data"),
    ("eater_auth", "POST", "application/json"),
    ("anonymous_auth", "POST", "application/json"),
    ("get_photo", "GET", "image"),
]


def flatten(data: dict, prefix: str = "") -> dict[str, str]:
    out: dict[str, str] = {}
    for key, value in data.items():
        name = f"{prefix}{key}" if not prefix else f"{prefix}.{key}"
        if isinstance(value, dict):
            out.update(flatten(value, name))
        else:
            out[name] = "" if value is None else str(value)
    return out


def load_json(path: Path) -> dict:
    return json.loads(path.read_text(encoding="utf-8"))


def locale_name(path: Path, ios: bool) -> str:
    name = path.name
    if ios and name == "pt-BR.json":
        return "pt-br.json"
    return name.lower()


def localization() -> dict:
    ios_dir = IOS / "Localization"
    and_dir = ROOT / "app" / "src" / "main" / "assets" / "Localization"
    ios_en = flatten(load_json(ios_dir / "en.json"))
    and_en = flatten(load_json(and_dir / "en.json"))
    missing = sorted(set(ios_en) - set(and_en))
    android_only = sorted(set(and_en) - set(ios_en))
    changed = sorted(k for k in set(ios_en) & set(and_en) if ios_en[k] != and_en[k])

    ios_files = {locale_name(p, True): p for p in ios_dir.glob("*.json")}
    and_files = {locale_name(p, False): p for p in and_dir.glob("*.json")}

    ios_quotes = sorted(p.name for p in (ios_dir / "quotes").glob("*") if p.is_file())
    and_quotes = sorted(p.name for p in (and_dir / "quotes").glob("*") if p.is_file())
    quote_diff = []
    for name in sorted(set(ios_quotes) & set(and_quotes)):
        a = (ios_dir / "quotes" / name).read_bytes()
        b = (and_dir / "quotes" / name).read_bytes()
        if a != b:
            quote_diff.append(name)

    return {
        "ios_en_count": len(ios_en),
        "android_en_count": len(and_en),
        "missing_in_android_count": len(missing),
        "changed_count": len(changed),
        "android_only_count": len(android_only),
        "locale_file_map": "iOS pt-BR.json maps to Android pt-br.json. iOS en-US.json has no Android file.",
        "ios_only_locale_files": sorted(set(ios_files) - set(and_files)),
        "android_only_locale_files": sorted(set(and_files) - set(ios_files)),
        "missing_in_android": [{"key": k, "en": ios_en[k]} for k in missing],
        "changed": [
            {"key": k, "ios": ios_en[k], "android": and_en[k]} for k in changed
        ],
        "android_only": android_only,
        "quotes_ios_only": sorted(set(ios_quotes) - set(and_quotes)),
        "quotes_android_only": sorted(set(and_quotes) - set(ios_quotes)),
        "quotes_different": quote_diff,
    }


def parse_messages(text: str) -> dict[str, dict[int, str]]:
    messages: dict[str, dict[int, str]] = {}
    name: str | None = None
    depth = 0
    buf: dict[int, str] = {}
    for raw in text.splitlines():
        line = raw.split("//", 1)[0].strip()
        if not line:
            continue
        m = re.match(r"message\s+(\w+)\s*\{", line)
        if m and depth == 0:
            name = m.group(1)
            buf = {}
            depth = line.count("{") - line.count("}")
            if depth == 0:
                messages[name] = buf
                name = None
            continue
        if name is None:
            continue
        depth += line.count("{") - line.count("}")
        fm = FIELD_RE.match(line)
        if fm:
            repeated = "repeated " if fm.group(1) and "repeated" in fm.group(1) else ""
            buf[int(fm.group(4))] = f"{repeated}{fm.group(2)} {fm.group(3)}"
        if depth <= 0:
            messages[name] = buf
            name = None
            depth = 0
    return messages


def proto_diff() -> list[dict]:
    ios_dir = IOS / "Services"
    and_dir = ROOT / "app" / "src" / "main" / "proto"
    ios_files = {p.name: p for p in ios_dir.glob("*.proto")}
    and_files = {p.name: p for p in and_dir.glob("*.proto")}
    rows = []
    for name in sorted(set(ios_files) | set(and_files)):
        if name not in and_files:
            rows.append(
                {
                    "file": name,
                    "status": "missing_on_android",
                    "messages": [],
                }
            )
            continue
        if name not in ios_files:
            rows.append(
                {
                    "file": name,
                    "status": "android_only",
                    "messages": [],
                    "action": "remove" if name == "multiple_photos.proto" else "review",
                }
            )
            continue
        ios_msgs = parse_messages(ios_files[name].read_text(encoding="utf-8"))
        and_msgs = parse_messages(and_files[name].read_text(encoding="utf-8"))
        message_rows = []
        for msg in sorted(set(ios_msgs) | set(and_msgs)):
            ios_fields = ios_msgs.get(msg, {})
            and_fields = and_msgs.get(msg, {})
            missing = [
                {"number": n, "field": ios_fields[n]}
                for n in sorted(set(ios_fields) - set(and_fields))
            ]
            extra = [
                {"number": n, "field": and_fields[n]}
                for n in sorted(set(and_fields) - set(ios_fields))
            ]
            changed = []
            for n in sorted(set(ios_fields) & set(and_fields)):
                if ios_fields[n] != and_fields[n]:
                    changed.append(
                        {"number": n, "ios": ios_fields[n], "android": and_fields[n]}
                    )
            if missing or extra or changed or msg not in and_msgs or msg not in ios_msgs:
                message_rows.append(
                    {
                        "message": msg,
                        "missing_on_android": missing,
                        "android_only_fields": extra,
                        "type_mismatch": changed,
                        "missing_message": msg not in and_msgs,
                    }
                )
        rows.append(
            {
                "file": name,
                "status": "diff" if message_rows else "match",
                "messages": message_rows,
            }
        )
    return rows


def source_blob(root: Path, suffix: str) -> str:
    parts = []
    for path in root.rglob(f"*{suffix}"):
        if any(bit in path.parts for bit in (".build", "build", ".git")):
            continue
        try:
            parts.append(path.read_text(encoding="utf-8", errors="ignore"))
        except OSError:
            continue
    return "\n".join(parts)


def endpoints(ios_src: str, and_src: str) -> list[dict]:
    rows = []
    for path, method, content_type in ENDPOINTS:
        needle = path.split("?")[0]
        rows.append(
            {
                "path": path,
                "method": method,
                "content_type": content_type,
                "ios": needle in ios_src,
                "android": needle in and_src,
            }
        )
    return rows


def merge_status(features: list[dict], previous: dict | None, mark_done: str | None) -> list[dict]:
    saved = {}
    if previous:
        for row in previous.get("features", []):
            saved[(row.get("chapter"), row.get("feature"))] = row.get("status")
    out = []
    for row in features:
        copy = json.loads(json.dumps(row))
        key = (copy["chapter"], copy["feature"])
        if key in saved and saved[key]:
            copy["status"] = saved[key]
        if mark_done and copy["chapter"] == mark_done:
            copy["status"] = "done"
        out.append(copy)
    return out


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--mark-done",
        default="",
        help="Chapter id whose feature statuses should be set to done (example: 1_network).",
    )
    args = parser.parse_args()

    previous = None
    if OUT.exists():
        previous = json.loads(OUT.read_text(encoding="utf-8"))

    ios_src = source_blob(IOS, ".swift")
    and_src = source_blob(ROOT / "app" / "src" / "main" / "java", ".kt")

    payload = {
        "meta": {
            "generated_at": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ"),
            "ios_root": str(IOS),
            "ios_baseline": "working tree, marketing version 5.3",
            "android_root": str(ROOT),
            "android_version_at_audit_start": "5.1.1 (versionCode 28)",
            "target_release": "5.3 (versionCode 29)",
            "branch": "feature/ios-5.3-parity",
            "replaces": [
                "eater/.docs/ANDROID_PARITY_food_card_features.md (those 6 items are already done)",
                "README.md Functionality Comparison",
            ],
        },
        "features": merge_status(FEATURES, previous, args.mark_done or None),
        "endpoints": endpoints(ios_src, and_src),
        "protos": proto_diff(),
        "localization": localization(),
        "removed_on_ios": REMOVED_ON_IOS,
    }
    OUT.parent.mkdir(parents=True, exist_ok=True)
    OUT.write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    features = payload["features"]
    counts: dict[str, int] = {}
    for row in features:
        counts[row["status"]] = counts.get(row["status"], 0) + 1
    loc = payload["localization"]
    print(
        f"wrote {OUT} features={len(features)} {counts} "
        f"missing_keys={loc['missing_in_android_count']} changed={loc['changed_count']} "
        f"android_only={loc['android_only_count']}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
