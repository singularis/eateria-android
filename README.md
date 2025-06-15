# Eateria - Android Kotlin

A nutrition tracking app that uses AI to analyze food photos and provide dietary insights. This is the Android Kotlin equivalent of the iOS Swift Eater app.

## Features

- **AI-Powered Food Recognition**: Take photos of food to automatically log nutrition information
- **Weight Tracking**: Photo-based weight scale reading or manual entry
- **Calorie Management**: Set and track daily calorie limits with visual indicators
- **Health Insights**: Get personalized dietary recommendations based on eating patterns
- **Date-based Viewing**: Browse historical nutrition data
- **Google Authentication**: Secure sign-in with Google accounts
- **Dark Theme**: Modern dark UI matching the iOS app design

## Architecture

### Tech Stack
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with ViewModels and StateFlow
- **Networking**: Retrofit + OkHttp
- **Protocol Buffers**: For API communication
- **Authentication**: Google Sign-In
- **Storage**: DataStore for preferences, local file storage for images
- **Camera**: CameraX for photo capture

### Project Structure

```
app/src/main/java/com/singularis/eateria/
├── models/                 # Data classes
│   ├── Product.kt         # Food item model
│   └── DailyStatistics.kt # Daily nutrition statistics
├── services/              # Business logic and API services
│   ├── AuthenticationService.kt    # Google authentication
│   ├── GRPCService.kt             # API communication
│   ├── ImageStorageService.kt     # Local image management
│   └── ProductStorageService.kt   # Food data management
├── ui/
│   ├── theme/             # Material Design theme
│   └── views/             # Compose UI components
│       ├── LoginView.kt   # Authentication screen
│       ├── ContentView.kt # Main app interface
│       └── ComponentViews.kt # Reusable UI components
├── viewmodels/            # MVVM ViewModels
│   ├── AuthViewModel.kt   # Authentication state
│   └── MainViewModel.kt   # Main app state
└── MainActivity.kt        # Entry point
```

### API Endpoints

The app communicates with the same backend as the iOS version:

- `GET /eater_get_today` - Fetch today's food data
- `POST /eater_receive_photo` - Upload food/weight photos
- `POST /delete_food` - Delete food entries
- `POST /get_recommendation` - Get health recommendations
- `POST /modify_food_record` - Modify food portions
- `POST /manual_weight` - Submit manual weight entries
- `POST /get_food_custom_date` - Fetch historical data
- `POST /eater_auth` - Authentication endpoint

### Protocol Buffers

Proto files define the data structures for API communication:

- `today_food.proto` - Today's nutrition data
- `photo_message.proto` - Photo upload format
- `custom_date_food.proto` - Historical data requests
- `delete_food.proto` - Food deletion requests
- `get_recomendation.proto` - Health recommendation requests
- `manual_weight.proto` - Manual weight entry
- `modify_food_record.proto` - Portion modification
- `delete_user.proto` - Account deletion

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 30+
- Google OAuth 2.0 client ID

### Configuration

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Eateria
   ```

2. **Configure Google Sign-In**
   - Create a project in [Google Cloud Console](https://console.cloud.google.com/)
   - Enable Google Sign-In API
   - Create OAuth 2.0 credentials for Android
   - Replace `"your-google-oauth-client-id"` in `AuthenticationService.kt` with your actual client ID

3. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```

### Dependencies

Key dependencies include:
- Jetpack Compose BOM for UI
- Retrofit for networking
- Protocol Buffers for data serialization
- Google Sign-In for authentication
- CameraX for camera functionality
- DataStore for preferences
- Coil for image loading

## Functionality Comparison with iOS App

### Core Features ✅
- [x] Google Authentication
- [x] Food photo upload and analysis
- [x] Weight tracking (photo and manual)
- [x] Calorie limit management
- [x] Product list with nutrition info
- [x] Historical date viewing
- [x] Health recommendations
- [x] Dark theme UI
- [x] Loading states and error handling

### UI Components ✅
- [x] Login screen
- [x] Top bar with profile, date, and info buttons
- [x] Stats buttons (weight, calories, trends)
- [x] Product cards with delete functionality
- [x] Camera button
- [x] Custom date picker navigation
- [x] Loading overlays

### Backend Integration ✅
- [x] Same API endpoints as iOS app
- [x] Protocol buffer communication
- [x] JWT authentication
- [x] Retry logic with exponential backoff
- [x] Image storage and retrieval

## Development Notes

### State Management
- Uses Jetpack Compose with StateFlow for reactive UI
- ViewModels handle business logic and state
- Repository pattern for data access

### Error Handling
- Comprehensive error handling for network requests
- User-friendly error messages
- Graceful degradation for offline scenarios

### Performance
- Lazy loading for product lists
- Image compression for photo uploads
- Efficient caching strategies

### Testing
- Unit tests for ViewModels and services
- Integration tests for API communication
- UI tests for critical user flows

## Future Enhancements

- [ ] Offline mode with local database
- [ ] Push notifications for meal reminders
- [ ] Nutrition goal tracking
- [ ] Export functionality for nutrition data
- [ ] Widget for quick food logging
- [ ] Integration with health apps (Google Fit)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project follows the same license as the original iOS application.

## Support

For issues and questions:
- Check existing GitHub issues
- Create a new issue with detailed description
- Include device info and Android version for bugs 